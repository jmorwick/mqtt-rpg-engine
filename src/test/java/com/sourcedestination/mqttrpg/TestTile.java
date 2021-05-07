package com.sourcedestination.mqttrpg;

import org.junit.Test;

import java.util.Map;

import static junit.framework.TestCase.*;

public class TestTile {
    DummyTile tile1 = new DummyTile(0,0);

    DummyBoard board1 = new DummyBoard("Dumb Board", new Map1().toString(), tile1);
    DummyGame game1 = new DummyGame(board1);




    @Test
    public void testGetRow(){
        assertEquals(0, tile1.getRow());
        assertEquals(1, new DummyTile(0,1).getRow());
    }

    @Test
    public void testGetColumn(){
        assertEquals(0, tile1.getColumn());
        assertEquals(1, new DummyTile(1,1).getColumn());
    }

    @Test
    public void testGetGame(){
        String getGame = tile1.getGame().toString();

        assertTrue(getGame.endsWith("DummyGame\"}"));
        assertEquals(null, new DummyTile(0,0).getGame());
        assertEquals(game1, tile1.getGame());
    }

    @Test
    public void testGetType(){
        assertEquals("dummy-tile", tile1.getType());
    }

    @Test
    public void testGetCharacter(){
        assertEquals('~', tile1.getCharacter());
    }

    @Test
    public void testGetBoard(){
        assertEquals(board1, tile1.getBoard());
        assertEquals(null, new DummyTile(0,0).getBoard());
    }

    @Test
    public void testGetProperties(){
        Map testMap = Map.of("character", "L");
        assertEquals("{character=L}", tile1.getProperties().toString());
        assertEquals(testMap, tile1.getProperties());
    }

    @Test
    public void testSetBoard(){
        DummyTile tile2 = new DummyTile(1,1);
        DummyBoard board2 = new DummyBoard("Temp Dumb Board",new Map2().toString(), tile2);
        tile1.setBoard(board2);

        assertEquals(board2, tile1.getBoard());
    }

    @Test
    public void testTileStatusUpdateEvent(){


        assertEquals("{column=0, row=0, id=1, board=Dumb Board}", tile1.tileStatusUpdateEvent().getProperties().toString());
    }

}
