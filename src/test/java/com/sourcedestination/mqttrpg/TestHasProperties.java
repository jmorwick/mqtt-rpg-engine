package com.sourcedestination.mqttrpg;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;

public class TestHasProperties {
    DummyTile tile1 = new DummyTile(0,0);
    DummyBoard board = new DummyBoard("unsmart-board", new Map1().toString(), tile1);
    DummyGame game = new DummyGame(board);
    DummyEntity entity = new DummyEntity(game);
    JsonParser parser = new JsonParser();


    public TestHasProperties() {
        tile1.addEntity(entity);
    }

    @Test
    public void testSetProperties(){
        tile1.setProperties(Map.of("sprite", "U"));
        JsonObject json = (JsonObject)parser.parse(tile1.getProperties().toString());
        assertEquals("\"U\"", json.get("sprite").toString());
    }

}
