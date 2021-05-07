package com.sourcedestination.mqttrpg;

import org.junit.Test;

public class TestDatastores {

    DummyTile tile1 = new DummyTile(0,0);
    DummyBoard board = new DummyBoard("unsmart-board", new Map1().toString(), tile1);
    DummyGame game = new DummyGame(board);
    DummyEntity entity = new DummyEntity(game);

    @Test
    public void testSave(){

    }

    @Test
    public void testLoad(){

    }

    @Test
    public void testSearch(){

    }

    @Test
    public void testGetMaxEntityId(){

    }

}
