package com.sourcedestination.mqttrpg;

import com.google.gson.GsonBuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** a class representing tile-occupying entities in the game */
public abstract class Entity implements HasProperties {
	private final int id;
	private final Game game;
	private Direction heading;
	private final Map<String,Object> properties;

	/**
	 * Constructs Entity from a {@link Game}
	 * @param game associated Game
	 */
	public Entity(Game game, Map<String,String> properties) {

		this.game = game;
		this.properties = new ConcurrentHashMap<>(properties);
		game.addEntity(this);
		this.id = game.getEntityId(this);
	}

	public Entity(Game game, Map<String,String> properties, Container initialLocation) {
		this(game,properties);
		game.moveEntity(this, initialLocation);
	}

	@Override
	public Map<String,Object> getProperties() {
		var properties = new HashMap<>(this.properties); // add id to properties
		properties.put("id", id);
		return Collections.unmodifiableMap(properties);
	}

	@Override
	public void setProperty(String key, Object value) {
		properties.put(key, value);
	}

	/**
	 * Returns the facing {@link Direction} of the Entity
	 * @return facing Direction
	 */
	public Direction getHeading() { return heading; }

	/**
	 * Updates the facing {@link Direction} of the Entity
	 * @param heading facing Direction
	 */
	public void setHeading(Direction heading) {
		this.heading = heading;
	}

	/** 
	 * returns the unique identifier within this game for this entity
     * @return this entity's ID
	 */
	public int getID() { return id; }

	/**
	 * Returns {@link Game} associated with this Entity
	 * @return associated Game
	 */
	public Game getGame() { return game; }

	/**
	 * Returns the String representation of the Entity type
	 * @return type
	 */
	public abstract String getType();

	/** returns a JSON representation of this tile and its properties
	 */
	@Override
	public String toString() {
		var gsonBuilder = new GsonBuilder();
		var gson = gsonBuilder.create();
		var m = new HashMap<String,Object>();
		var location = game.getEntityLocation(this);
		var container = game.getEntityLocation(this);
		if(location instanceof Tile) {
			Tile tile = (Tile)container;
			m.put("board", tile.getBoard().getName());
			m.put("column", tile.getColumn());
			m.put("row", tile.getRow());
			m.put("heading", getHeading());
		} else if(container instanceof Entity) {
			m.put("container", ((Entity)container).getID());
		}
		m.put("id", getID());
		m.put("type", getType());
		m.put("properties", properties);
		return gson.toJson(m);
	}

	public Entity getSelfReference() { return this; }
	
}
