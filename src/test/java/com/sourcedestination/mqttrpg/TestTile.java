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


}
