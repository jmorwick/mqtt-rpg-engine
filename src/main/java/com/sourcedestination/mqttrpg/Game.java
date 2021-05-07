package com.sourcedestination.mqttrpg;

import com.google.common.collect.*;
import com.google.gson.GsonBuilder;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

/** Class for managing the state of games using the 2D API
 */
public abstract class Game implements Container {

	private final String id;
	private final Map<String,Board> boards = new ConcurrentHashMap<>();
	private final long startTime;    // time when game was started or restarted
	private final long elapsedTime;  // time elapsed in game since start or last restart
	private final AtomicInteger nextEntityID;
	private final AtomicInteger nextEventID = new AtomicInteger(1);
	private final Map<EventListener,Object> listeners = new ConcurrentHashMap<>();
	private final DataStore dataStore;
	private final EventListener eventPropagator;

	// no concurrent set, so only keys used to mimic set
	final BiMap<Integer, Entity> registeredEntities;
	private final BiMap<String, Agent> allAgents;

	// access must be protected by monitor
	private final Multimap<Container, Entity> containerContents;
	private final Map<Entity, Container> entityLocations;

	public Game(String id,
				DataStore dataStore,
				EventListener eventPropagator,
				Consumer<EventListener> incomingEventCallback,
				Board ... boards) {
		this.id = id;
		this.dataStore = dataStore;
		this.startTime = System.currentTimeMillis();
		this.elapsedTime = 0;
		registeredEntities = Maps.synchronizedBiMap(HashBiMap.create());
		allAgents = Maps.synchronizedBiMap(HashBiMap.create());
		// access must be protected by monitor
		containerContents = HashMultimap.create();
		entityLocations = new HashMap<>();
		  // set next entity ID to be one more than the biggest one in the database
		this.nextEntityID = new AtomicInteger(dataStore.getMaxEntityId() + 1);
		this.eventPropagator = eventPropagator;
		incomingEventCallback.accept(new EventListener() {
			@Override
			public synchronized void acceptEvent(Event event) {
				getListeners().forEach(listener -> listener.accept(event));
			}
		});

		for(var board : boards) addBoard(board);
	}

	public String getId() { return this.id; }

	@Override
	public final Game getGame() { return this; }

	/** UNIX time of the last start or restart */
	public long getStartTime() {
		return startTime;
	}

	/** returns the number of milliseconds elapsed since the start of the game */
	public long getGameTime() {
		return System.currentTimeMillis() - startTime + elapsedTime;
	}

	public DataStore getDataStore(){
		return (DataStore) dataStore;
	}

	/** begins forwarding all game events to specified listener */
	public void registerListener(EventListener listener) {
		listeners.put(listener, "thing");
	}

	/** stops forwarding all game events to specified listener */
	public void deregisterListener(EventListener listener) {
		listeners.put(listener, "thing");
	}

	/** returns currently registered event listeners
	 * All listeners registered via registerListener
	 * @return
	 */
	public Stream<EventListener> getListeners() {
		return listeners.keySet().stream();
	}

	/** add an agent to the game
	 * @param agent agent to be added to the game
	 */
	public void addAgent(Agent agent) {
		allAgents.put(agent.getAgentID(), agent);
		getDataStore().load(agent);
	}

	/** remove agent from the game
	 *
	 * @param agent agent to be removed from the game
	 */
	public void removeAgent(Agent agent) {
		allAgents.remove(agent.getAgentID());
	}
	public void removePlayer(String playerId) {
		allAgents.remove(playerId);
	}

	/** find player with associated ID that has joined this game
	 * If the player has not joined this game, null will be returned
	 * @param id ID of player to be found
	 * @return player object with associated ID
	 */
	public Agent getAgent(String id) {
		return allAgents.get(id);
	}

	/**
	 * Returns {@link Entity}s associated with this game
	 * @return Stream of associated Entities
	 */
	public Stream<Entity> getEntities() {
		return registeredEntities.values().stream();
	}

	/**
	 * Returns an {@link Entity} with the specified id
	 * @param id entity id
	 * @return entity 
	 */
	public Entity getEntity(int id) {
		return registeredEntities.get(id);
	}

	/**
	 * returns the id of the Entity ent
	 * If ent is not registered with this game, a runtime exception will be thrown.
	 * @param ent the entity whose ID is to be found
	 * @return the id of the supplied entity
	 */
	public int getEntityId(Entity ent) {
		return registeredEntities.entrySet().stream()
				.filter(e -> e.getValue() == ent)
				.mapToInt(e -> e.getKey())
				.findFirst().getAsInt();
	}

	/** determine the number of players currently in the game
	 *
	 * @return the number of players in the game
	 */
	public int getNumPlayers() {
		return allAgents.size();
	}
	/**
	 * Returns the set of all {@link Agent}s
	 * @return connected Players
	 */
	public Stream<Agent> getAllAgents() {
		return allAgents.values().stream();
	}

	/**
	 * Returns the {@link Board} associated with this Game
	 * @return associated Board
	 */
	public Board getBoard(String name) {
		return boards.get(name);
	}

	public void addBoard(Board board) {
		board.setGame(this);
		boards.put(board.getName(), board);
	}

	/**
	 * Registers given {@link Entity} with the Game
	 * If the entity is an event listener, it is also registered as such.
	 * @param ent registering Entity
	 */
	public void addEntity(Entity ent) {
		assert ent != null;
		var id = -1; // temporary id, -1 means entity was not found in db
		if(dataStore != null && ent.getClass().isAnnotationPresent(Permanent.class)) {
			// this entity should be loaded from the database if possible
			var ids = dataStore.search(ent.getProperties());
			if(ids.size() == 1) {  // found a unique entity in the db
				id = ids.get(0);   // assign its id
				ent.setProperty("id", ""+id);  // set the id as a property to look up other properties
				dataStore.load(ent);  // load other properties from the database
			}
		}
		if(id == -1) {
			// could not load id from database
			id = nextEntityID.getAndIncrement();
		}
		registeredEntities.put(id, ent);
		synchronized (this) { // add entity to the game's contents as default
			entityLocations.put(ent, this);
			containerContents.put(this, ent);
		}
		if(ent instanceof EventListener) {
			registerListener((EventListener)ent);
		}
		propagateEvent(new Event(this, "entity-creation",
				Map.of(
						"entity-id", ent.getID()+""
				)));
	}

	/**
	 * Removes a registered {@link Entity} and every reference to it.
	 * @param ent Entity to be removed
	 */
	public void removeEntity(Entity ent) {
		synchronized(this) {
			if(ent instanceof EventListener) {
				deregisterListener((EventListener)ent);
			}
			moveEntity(ent, this); // generate an entity moved event

			var currentContainer = entityLocations.get(ent);
			entityLocations.remove(ent);
			if(currentContainer != null) {
				containerContents.remove(currentContainer, ent);
			}
			// remove entity from game
			registeredEntities.remove(ent);
		}

		// alert other game components to entity removal
		propagateEvent(new Event(this, "entity-deletion",
				Map.of(
						"entity-id", ent.getID()+""
				)));
	}

	/** moves the entity to a new Container.
	 * Container may be a Player, a Tile, or another Entity
	 * @param ent
	 * @param container
	 */
	public void moveEntity(Entity ent, Container container) {
		assert ent != null;
		assert container != null;
		assert registeredEntities.containsKey(ent.getID());

		Container prev = getGame().getEntityLocation(ent);

			// move entity to new location
			var currentLocation = getEntityLocation(ent);
			if(currentLocation != null)
				entityLocations.remove(ent);
				containerContents.remove(currentLocation, ent);
			entityLocations.put(ent, container);
			containerContents.put(container, ent);
		propagateEvent(entityMovedEvent(ent, prev));
	}

	public Event entityMovedEvent(Entity ent, Container prev) {
		var properties = new HashMap<String,Object>();
		properties.put("entity", ent.getID());
		if(prev instanceof Tile) {
			properties.put("previous-board", ((Tile)prev).getBoard().getName());
			properties.put("previous-row", ((Tile)prev).getRow()+"");
			properties.put("previous-column", ((Tile)prev).getColumn()+"");
		} else if(prev instanceof Entity) {
			properties.put("previous-entity-container", ((Entity)prev).getID()+"");
		}
		var current = getEntityLocation(ent);
		if(current instanceof Tile) {
			properties.put("board", ((Tile)current).getBoard().getName());
			properties.put("row", ((Tile)current).getRow()+"");
			properties.put("column", ((Tile)current).getColumn()+"");
		} else if(current instanceof Entity) {
			properties.put("entity-container", ((Entity)current).getID()+"");
		}
		return new Event(this, "entity-moved", properties);
	}

	/** Determines whether or not a specified Container holds the specified entity */
	public synchronized boolean containsEntity(Container container, Entity ent) {
		assert ent != null;
		assert container != null;
		assert registeredEntities.containsKey(ent.getID());

		return containerContents.containsEntry(container, ent);
	}

	/** locates the Container holding an Entity.
	 * Every Entity is held by exactly one container (possibly the Game itself if no other) */
	public synchronized Container getEntityLocation(Entity ent) {
		assert ent != null;
		assert registeredEntities.containsKey(ent.getID());

		return entityLocations.get(ent);
	}

	/** returns all entities contained by the specified container */
	public synchronized Stream<Entity> getContainerContents(Container container) {
		assert container != null;

		return new HashSet<Entity>(containerContents.get(container)).stream();
	}

	/** determine what non-entity contains an entity.
	 * For instance, if an entity is held by a treasure chest and the treasure chest appears on a tile,
	 * the tile holding the treasure chest is returned.
	 * Returns null if no tile contains this entity */
	public synchronized Container getTopLevelEntityLocation(Entity ent) {
		assert ent != null;

		var location = getEntityLocation(ent);
		if(location == null) return null;
		if(location instanceof Entity)
			return getEntityLocation((Entity)location);
		return location instanceof Tile ? (Tile)location : null;
	}

	/** determines next ID to be used for an event, then increments the count of events */
	protected int getNextEventId() {
		return nextEventID.getAndIncrement();
	}

	/** returns a JSON representation of this game
	 */
	@Override
	public String toString() {
		var gsonBuilder = new GsonBuilder();
		var gson = gsonBuilder.create();
		var m = new HashMap<String,Object>();
		m.put("type", getClass().getSimpleName());
		m.put("elapsed-time", getGameTime());
		return gson.toJson(m);
	}

	public void propagateEvent(Event event) {
		eventPropagator.acceptEvent(event);
		for(var listener : listeners.keySet())
			listener.accept(event);
	}
	public void propagateEvent(Event event, long delayTime) {
		eventPropagator.acceptEvent(event, delayTime);
	}

	/** create an agent for the specified id/role (or retrieve existing agent)
	 * called when a client connects to control the agent
	 * @param id
	 * @param role
	 * @return
	 */
	public abstract Agent getAgent(String id, String role);
}
