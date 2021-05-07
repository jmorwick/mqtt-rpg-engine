package com.sourcedestination.mqttrpg;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.sourcedestination.funcles.function.Function2;
import org.junit.Test;


import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;

//TODO: getHeight,getWidth do not seem correct.


public class TestBoard {
    DummyTile tile1 = new DummyTile(0,0);
    DummyTile tile2 = new DummyTile(0,1);
    DummyTile tile3 = new DummyTile(1,0);
    DummyTile tile4 = new DummyTile(1,1);

    DummyBoard board = new DummyBoard("unsmart-board", new Map1().toString(), new DummyTile[]{tile1,tile2, tile3, tile4});

    DummyGame game = new DummyGame(board);
    DummyEntity entity = new DummyEntity(game);
    @Test
    public void testGetGame(){
        assertEquals(game, board.getGame());
    }

    @Test
    public void testSetGame(){
        DummyGame tempGame = new DummyGame();
        board.setGame(tempGame);
        assertEquals(tempGame, board.getGame());
    }

    @Test
    public void testGetAdjacentTile(){
        assertEquals(tile3,board.getAdjacentTile(tile1, Direction.EAST).get());
        assertFalse(board.getAdjacentTile(tile1, Direction.NORTH).isPresent());
        assertEquals(tile2, board.getAdjacentTile(tile1, Direction.SOUTH).get());
        assertFalse(board.getAdjacentTile(tile1, Direction.WEST).isPresent());

    }

    @Test
    public void testGetAdjacentTileDirection(){
        assertEquals(Direction.EAST, board.getAdjacentTileDirection(tile1, tile3).get());
        assertEquals(Direction.SOUTH, board.getAdjacentTileDirection(tile1, tile2).get());
        assertEquals(Direction.NORTH, board.getAdjacentTileDirection(tile4, tile3).get());

        //these tiles aren't adjacent
        assertFalse(board.getAdjacentTileDirection(tile3, tile2).isPresent());
    }

    @Test
    public void testGetTiles(){
        System.out.println(board.getTiles());
    }

    @Test
    public void testGetTileStream(){
        //parsing tilestream to json
        JsonParser parser = new JsonParser();
        JsonObject json = (JsonObject) parser.parse(board.getTileStream().toArray()[0].toString());

        int column = Integer.parseInt(json.get("column").toString());
        int row = Integer.parseInt(json.get("row").toString());
        String type = json.get("type").toString();
        String board = json.get("board").toString();
        String properties = json.get("properties").toString();

        assertEquals(107, column);
        assertEquals(0, row);
        assertEquals("\"generic\"", type);
        assertEquals("\"unsmart-board\"", board);
        assertEquals("{\"character\":\"#\"}", properties);
    }

    @Test
    public void TestGetTile(){
        assertEquals(tile1, board.getTile(0,0));
        assertEquals(Optional.empty(), board.getTile(entity));
    }

    @Test
    public void TestGetName(){
        assertEquals("unsmart-board", board.getName());
    }

    @Test
    public void testGetWidth(){
        DummyBoard widthBoard = new DummyBoard("get-width-board", "1");
        assertEquals(112, board.getWidth());
        assertEquals(1, widthBoard.getWidth());
    }

    @Test
    public void testGetHeight(){
        //height never exceeds with tile generator in constructor.
        //the only thing that effects height are the specific tiles added.
        assertEquals(2, board.getHeight());

        Map<Character, Function2<Integer,Integer,Tile>> forTileGen = new HashMap<>();

        DummyBoard heightBoard = new DummyBoard("get-height-board", "123455555555");
        assertEquals(1, heightBoard.getHeight());
    }
    @Test
    public void testGetTileMap(){
        assertEquals("5", board.getTileMap().substring(9,10));
    }


}
