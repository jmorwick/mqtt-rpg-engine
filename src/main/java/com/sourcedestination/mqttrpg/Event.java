package com.sourcedestination.mqttrpg;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


// TODO: create annotation indicating required and optional properties

/** Indicates when something happens during the game that other game components may react to.
 * Also used to record important events in the processing of the game.
 */
public class Event implements HasProperties {

    private final Map<String,Object> properties;
    private final int id;
    private final Game game;
    private final long eventTime;
    private final String type;

    public Event(Game game, String type) {
        this(game, type, game.getNextEventId(), new HashMap<>());
    }

    private Event(Game game, String type, int id) {
        this(game, type, id, new HashMap<>());
    }

    public Event(Game game, String type, Map<String,Object> properties) {
        this(game, type, game.getNextEventId(), properties);
    }

    private Event(Game game, String type, int id, Map<String,Object> properties) {
        this.id = id;
        this.game = game;
        this.type = type;
        this.eventTime = game.getGameTime();
        properties = new HashMap<>(properties); // add id to properties
        properties.put("id", ""+id);
        this.properties = Collections.unmodifiableMap(properties);
    }

    @Override
    public Map<String, Object> getProperties() {
        return properties;
    }

    @Override
    public void setProperty(String key, Object value) {
        throw new UnsupportedOperationException("Event properties are immutable");
    }

    public static Event fromJson(Game game, String json) {
        Gson gson = new Gson();
        var obj = gson.fromJson(json, JsonObject.class);
        int id = obj.get("id").getAsInt();
        String type = obj.get("type").getAsString();
        var map = new HashMap<String,Object>();
        if(obj.has("properties"))
            for(String key : obj.get("properties").getAsJsonObject().keySet())
                map.put(key, obj.get("properties").getAsJsonObject().get(key).toString());
        return new Event(game, type, id, map);
    }

    /** time elapsed since start of game when this event occurred */
    public long getEventTime() {
        return eventTime;
    }

    public Game getGame() {
        return game;
    }

    public String getType() { return type; }

    public String toString() {
            var gsonBuilder = new GsonBuilder();
            var gson = gsonBuilder.create();
            var m = new HashMap<String,Object>();
            m.put("id", id);
            m.put("time", getEventTime());
            m.put("type", type);
            m.put("properties", getProperties());
            return gson.toJson(m);
    }

}
