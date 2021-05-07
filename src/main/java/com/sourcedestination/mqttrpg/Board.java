package com.sourcedestination.mqttrpg;

import com.google.gson.GsonBuilder;
import net.sourcedestination.funcles.function.Function2;
import net.sourcedestination.funcles.tuple.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.sourcedestination.funcles.tuple.Pair.makePair;

/** represents a 2d grid of tiles used as a playing surface for a game
 *   */
public class Board {
	private static Logger logger = Logger.getLogger(Board.class.getCanonicalName());

	private final Map<Pair<Integer>, Tile> tiles;
	private final Map<EventListener,Object> listeners = new ConcurrentHashMap<>();
			// no concurrent set, so only keys used to mimic set
	private final String name;
	private Game game;

	/** outfits board according to layout of characters in multi-line string charMap.
	 * Characters that are not keys in {@param tileTypeChars} can be used to
	 * represent blank space in the map (no tile will be generated). Blank spaces are always treated
	 * as empty cells and cannot be used.
	 *
	 * @param name
	 * @param charMap
	 */
	public Board(String name,
				 String charMap,
				 Map<Character, Function2<Integer,Integer,Tile>> tileGenerators,
				 Tile ... initialTiles) {
		var tiles = new HashMap<Pair<Integer>,Tile>();

		for(Tile t : initialTiles) {
			t.setBoard(this);
			tiles.put(makePair(t.getColumn(), t.getRow()), t);
		}

		int col=0, row=0;
		for(char c : charMap.toCharArray()) {
			if(c == '\n') { // reset to next row
				row++; // increment row
				col = 0; // start at first column
			} else  {  // create a tile in this column
				if(!tiles.containsKey(makePair(col, row))) {
					var generator = tileGenerators.get(c);

					// generate generic tiles if no generator defined
					if(generator == null) generator =  (fcol, frow) -> new Tile(fcol, frow, "generic", c, Map.of());
                    var properties = new HashMap<String,Object>();

                    // spring XML makes pairs of strings instead of pairs of integers, so strings are used below
                    if(!properties.containsKey("character"))
                    	properties.put("character", ""+c);
                    var tile = generator.apply(col, row);
                    tile.setProperties(properties);
                    tile.setBoard(this);
					tiles.put(Pair.makePair(col, row), tile);
				}
				col++; // increment column
			}
		}
		this.name = name;
		this.tiles = Collections.unmodifiableMap(tiles);
	}

	/**
	 * Returns the {@link Game} associated with this board
	 * @return associated Game
	 */
	public Game getGame() { return game; }

	public void setGame(Game game) {
		this.game = game;
		getTileStream().forEach(t -> {
			t.setBoard(this);
		});
	}
	
	/**
	 * Checks if Location exists on board.
	 * @return
	 */
	private boolean locationExists(int column, int row){
		return tiles.containsKey(Pair.makePair(column, row));
	}

	/**
	 * Find an adjacent {@link Tile} given a Tile and {@link Direction}
	 * @param tile original Tile
	 * @param direction direction of adjacent tile
	 * @return adjacent Tile
	 */
	public Optional<Tile> getAdjacentTile(Tile tile, Direction direction) {
		var row = tile.getRow();
		var column = tile.getColumn();
		switch (direction) {
			case NORTH: row--; break;
			case SOUTH: row++; break;
			case WEST: column--; break;
			case EAST: column++; break;
		}
		if (locationExists(column, row))
			return Optional.of(getTile(column, row));
		return Optional.empty();
	}

	/**
	 * Returns the {@link Direction} between two {@link Tile}s
	 * <p>
	 * Returns the direction tile 'to' is relative to tile 'from'.
	 * If the tiles are not adjacent, this method returns one direction
	 * 'to' is relative to 'from', but not necessarily all of them. 
	 * 
	 * @param from starting Tile
	 * @param to   ending Tile
	 * @return direction from starting Tile to ending Tile
	 */
	public Optional<Direction> getAdjacentTileDirection(Tile from, Tile to) {
		if(Math.abs(from.getColumn() - to.getColumn()) + Math.abs(from.getRow() - to.getRow()) != 1) {
			// tiles are not adjecent
			return Optional.empty();
		}

		if(from.getColumn() > to.getColumn())
			return Optional.of(Direction.WEST);
		if(from.getColumn() < to.getColumn())
			return Optional.of(Direction.EAST);
		if(from.getRow() > to.getRow())
			return Optional.of(Direction.NORTH);
		return Optional.of(Direction.SOUTH);
	}
	
	/**
	 * Returns HashMap of {@link Tile}s associated with this Board
	 * @return HashMap of tiles
	 */
	public Map<Pair<Integer>, Tile> getTiles() { return tiles; }
	
	/**
	 * Returns stream of {@link Tile}s associated with this Board
	 * @return stream of all tiles associated with the board
	 */
	public Stream<Tile> getTileStream() { return tiles.values().stream(); }

	/**
	 * Returns a {@link Tile} at the given coordinates
	 * @return tile at given location
	 */
	public Tile getTile(int column, int row) {
		if(!locationExists(column, row))
			return null;
		return tiles.get(Pair.makePair(column, row));
	}

	/**
	 * Returns a {@link Tile} with the given {@link Entity}
	 * @param ent - entity that exists on tile
	 * @return tile that contains given entity
	 */
	public Optional<Tile> getTile(Entity ent){
		var container = getGame().getEntityLocation(ent);
		if(container instanceof Tile) {
			return Optional.of((Tile)container);
		}
		return Optional.empty();
	}

	public String getName() {
		return name;
	}

	public int getWidth() {
		return tiles.keySet().stream()
				.mapToInt(Pair::_1)
				.max().getAsInt() + 1;
	}

	public int getHeight() {
		return tiles.keySet().stream()
				.mapToInt(Pair::_2)
				.max().getAsInt() + 1;
	}


	/** returns a multi-line string representing the layout of tile types on this board.
	 * The names of classes represented by different characters in this string are held in tileTypeChars.
	 * @return
	 */
	public String getTileMap() {

		StringBuffer sb = new StringBuffer();
		for(int r = 0; r < getHeight(); r++) {
			for(int c = 0; c < getWidth(); c++) {
				var location = Pair.makePair(c, r);
				if(tiles.containsKey(location))
					sb.append(tiles.get(location).getCharacter());
				else sb.append('?');
			}
			sb.append('\n');
		}
		return sb.toString();
	}

	/** returns a JSON representation of this board and its properties
	 */
	@Override
	public String toString() {
		var gsonBuilder = new GsonBuilder();
		var gson = gsonBuilder.create();
		var m = new HashMap<String,Object>();
		m.put("height", getHeight());
		m.put("width", getWidth());
		m.put("tilemap", getTileMap());
		m.put("tileTypes", getTileStream().collect(Collectors.toMap(
				t -> t.getCharacter(),
				t -> t.getType(),
				(key1, key2) -> key1 // ignore duplicates
		)));
		return gson.toJson(m);
	}

	/** loads a text file resource as a string.
	 *  used to load maps from text files in spring config
	 * @param mapFileName text file name for the map
	 * @return the text file data
	 */
	public static String loadMap(String mapFileName) throws IOException {
		var mapString = new BufferedReader(new InputStreamReader(
				Board.class.getClassLoader()
						.getResourceAsStream(mapFileName)))
				.lines().collect(Collectors.joining("\n"));
		return mapString;
	}
}

