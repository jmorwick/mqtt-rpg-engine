package com.sourcedestination.mqttrpg;

import com.google.gson.GsonBuilder;
import net.sourcedestination.funcles.function.Function2;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/** Represents spaces on the {@link Board} that contain entities */
public class Tile implements Container, HasProperties {
	public final int row;
	public final int column;
	final char character;
	private Board board;
	private final String type;
	private final Map<String,Object> properties;

	public static Function2<Integer,Integer,Tile> getGenerator(char character,
															   String type,
															   Map<String,Object> properties) {
		return (col,row) -> new Tile(col, row, type, character, properties);
	}

	/**
	 * Constructs a tile from a given {@link Board} at a given location with the given character representation
	 * */
	protected Tile(int column, int row, String type, char character, Map<String,Object> properties) {
		this.row = row;
		this.column = column;
		this.type = type;
		this.character = character;
		this.properties = new ConcurrentHashMap<>(properties);
	}

	/** row placement of the tile on the board */
	public int getRow() {
		return row;
	}

	/** column placement of the tile on the board */
	public int getColumn() {
		return column;
	}

	@Override
	public Game getGame() { return board == null ? null : board.getGame(); }

	public String getType() { return type; }

	public char getCharacter() { return character; }

	/**
	 * Return the {@link Board} associated with this tile
	 * @return associated Board
	 */
	public Board getBoard() { return board;}

	@Override
	public Map<String,Object> getProperties() {
		return properties;
	}

	@Override
	public void setProperty(String key, Object value) {
		properties.put(key, value);
		if(getGame() != null)
			board.getGame().propagateEvent(tileStatusUpdateEvent());
	}

	// added to avoid circular references in spring config
	// TODO: consider a better workaround that allows board to stay final
	public void setBoard(Board board) {
		this.board = board;
		if(this instanceof EventListener && getGame() != null)
			getGame().registerListener((EventListener)this);
	}

	/** returns a JSON representation of this tile and its properties
	 */
	@Override
	public String toString() {
		var gsonBuilder = new GsonBuilder();
		var gson = gsonBuilder.create();
		var m = new HashMap<String,Object>();
		m.put("row", getRow());
		m.put("column", getColumn());
		m.put("board", getBoard().getName());
		m.put("type", type);
		m.put("properties", properties);
		return gson.toJson(m);
	}

	public Event tileStatusUpdateEvent() {
		return new Event(getGame(), "tile-status-update",
				Map.of(
						"board", getBoard().getName(),
						"row", row+"",
						"column", column+""
				));
	}

}
