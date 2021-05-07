package com.sourcedestination.mqttrpg;

import java.util.Map;

public class Command implements HasProperties {
    private final Map<String,Object> properties;
    private final Game game;

    public Command(Game game, String json) throws CommandException {
        properties = null; // TODO: parse from json
        this.game = game;
    }

    public Command(Game game, Map<String,Object> properties) {
        this.properties = properties;
        this.game = game;
    }

    public Map<String,Object> getProperties() { return properties; }

    public void setProperty(String key, Object value) {
        throw new UnsupportedOperationException();
    }

    public Game getGame() { return game; }
}
