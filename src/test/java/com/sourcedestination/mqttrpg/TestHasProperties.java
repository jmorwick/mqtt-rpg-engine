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

    @Test
    public void testHasProperty(){
        assertFalse(tile1.hasProperty("sprite"));
        assertTrue(tile1.hasProperty("character"));
        assertFalse(tile1.hasProperty("Character"));
    }

    @Test
    public void testGetProperty(){
        System.out.println(tile1.getProperties());
        JsonObject json = (JsonObject)parser.parse(tile1.getProperties().toString());
        assertEquals("\"L\"", json.get("character").toString());
    }

    @Test
    public void testGetEntity(){
        assertEquals(1, tile1.getEntities().count());
        assertEquals(entity, tile1.getEntities().findFirst().get());
    }

    @Test
    public void testGetDirection(){
        assertEquals(Optional.empty(), tile1.getDirection());
        tile1.setProperties(Map.of("direction", "NORTH"));
        assertEquals("Optional[N]", tile1.getDirection().toString());
    }

    @Test
    public void testGetTile(){
        assertEquals(tile1, board.getTile(0,0));
    }

    @Test
    public void testSerializeProperties(){
        JsonObject json = (JsonObject)parser.parse(tile1.serializeProperties());
        assertEquals("\"L\"", json.get("character").toString());
    }

}
