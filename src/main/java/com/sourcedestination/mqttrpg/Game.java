package com.sourcedestination.mqttrpg;

import com.google.common.collect.*;
import com.google.gson.GsonBuilder;
import net.sourcedestination.funcles.tuple.Tuple2;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Class for managing the state of games using the 2D API
 */
public abstract class Game implements Container, Runnable {

	private final String id;
	private final Map<String,Board> boards = new ConcurrentHashMap<>();
	private final long startTime;    // time when game was started or restarted
	private final long elapsedTime;  // time elapsed in game since start or last restart
	private final AtomicInteger nextEntityID;
	private final AtomicInteger nextEventID = new AtomicInteger(1);
	private final List<Action> actionQueue;
	private final List<Tuple2<Agent,Command>> commandQueue;

	// no concurrent set, so only keys used to mimic set
	final BiMap<Integer, Entity> registeredEntities;
	private final BiMap<String, Agent> allAgents;

	// access must be protected by monitor
	private final Multimap<Container, Entity> containerContents;
	private final Map<Entity, Container> entityLocations;

	public Game(String id, Board ... boards) {
		this.id = id;
		this.startTime = System.currentTimeMillis();
		this.elapsedTime = 0;
		registeredEntities = Maps.synchronizedBiMap(HashBiMap.create());
		allAgents = Maps.synchronizedBiMap(HashBiMap.create());
		// access must be protected by monitor
		containerContents = HashMultimap.create();
		entityLocations = new HashMap<>();
		  // set next entity ID to be one more than the biggest one in the database
		this.nextEntityID = new AtomicInteger(1);
		this.actionQueue = new Vector<>();
		this.commandQueue = new Vector<>();
		for(var board : boards) addBoard(board);
	}

	public String getId() { return this.id; }

	@Override
	public final Game getGame() { return this; }

	/** UNIX time of the last start or restart */
	public long getStartTime() {
		return startTime;
	}

	public String getID() { return id; }

	/** returns the number of milliseconds elapsed since the start of the game */
	public long getGameTime() {
		return System.currentTimeMillis() - startTime + elapsedTime;
	}


	/** add an agent to the game
	 * @param agent agent to be added to the game
	 */
	public void addAgent(Agent agent) {
		allAgents.put(agent.getAgentID(), agent);
	}

	public void addAction(Action a) { actionQueue.add(a); }
	public void addCommand(Agent a, Command c) { commandQueue.add(Tuple2.makeTuple(a,c)); }

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
		boards.put(board.getID(), board);
		propagateEvent(new Event(this, "board-creation",
				Map.of(
						"board-id", board.getID()+""
				), board));
	}

	public void removeBoard(Board board) {
		// remove all entities on the board
		board.getTileStream().flatMap(t -> t.getEntities()).forEach( e-> {
			removeEntity(e);
		});
		boards.remove(board);
		propagateEvent(new Event(this, "board-creation",
				Map.of(
						"board-id", board.getID()+""
				), board));
	}

	/**
	 * Registers given {@link Entity} with the Game
	 * @param ent registering Entity
	 */
	public void addEntity(Entity ent) {
		assert ent != null;
		var id = nextEntityID.getAndIncrement();
		
		registeredEntities.put(id, ent);
		synchronized (this) { // add entity to the game's contents as default
			entityLocations.put(ent, this);
			containerContents.put(this, ent);
		}
		propagateEvent(new Event(this, "entity-creation",
				Map.of(
						"entity-id", ent.getID()+""
				), ent));
	}

	/**
	 * Removes a registered {@link Entity} and every reference to it.
	 * @param ent Entity to be removed
	 */
	public void removeEntity(Entity ent) {
		synchronized(this) {
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
				), ent));
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
		var properties = new HashMap<String,Object>();
		properties.put("entity", ent.getID());
		if(prev instanceof Tile) {
			properties.put("previous-board", ((Tile)prev).getBoard().getID());
			properties.put("previous-row", ((Tile)prev).getRow()+"");
			properties.put("previous-column", ((Tile)prev).getColumn()+"");
		} else if(prev instanceof Entity) {
			properties.put("previous-entity-container", ((Entity)prev).getID()+"");
		}
		var current = getEntityLocation(ent);
		if(current instanceof Tile) {
			properties.put("board", ((Tile)current).getBoard().getID());
			properties.put("row", ((Tile)current).getRow()+"");
			properties.put("column", ((Tile)current).getColumn()+"");
		} else if(current instanceof Entity) {
			properties.put("entity-container", ((Entity)current).getID()+"");
		}
		propagateEvent(new Event(this, "entity-moved", properties, ent, container));
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
	@Deprecated
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
		getAllAgents()
				.forEach(listener -> ((EventListener) listener).acceptEvent(event));
		getEntities()
				.filter(ent -> ent instanceof EventListener)
				.forEach(listener -> ((EventListener) listener).acceptEvent(event));
		boards.values().stream()
				.filter(board -> board instanceof EventListener)
				.forEach(listener -> ((EventListener) listener).acceptEvent(event));
		boards.values().stream()
			.flatMap(board -> board.getTileStream())
				.filter(tile -> tile instanceof EventListener)
				.forEach(listener -> ((EventListener) listener).acceptEvent(event));
	}
	
	public abstract boolean checkGameAlive();

	public void run() {
		while(checkGameAlive()) {
			commandQueue.stream()
					.collect(Collectors.toList()).stream() // copy to avoid modification errors
					.forEach(t2 -> {
				commandQueue.remove(t2);
				t2.unpack((agent, command) -> {
					// TODO: log command
					try {
						agent.receiveCommand(command);
					} catch (CommandException e) {
						// TODO: log/handle error
					}
				});
			});
			actionQueue.stream()
					.collect(Collectors.toList()).stream() // copy to avoid modification errors
					.forEach(action -> {
				actionQueue.remove(action);
				action.accept(this);
			});
		}
	}
}
