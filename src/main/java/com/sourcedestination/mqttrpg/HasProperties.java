package com.sourcedestination.mqttrpg;

import java.util.Map;
import java.util.Optional;

/** interface for all game objects that can have properties associated with them */
public interface HasProperties {
    public Map<String,Object> getProperties();
    public void setProperty(String key, Object value);

    public default void setProperties(Map<String,Object> properties) {
        for(var entry : properties.entrySet())
            setProperty(entry.getKey(), entry.getValue());
    }

    public default boolean hasProperty(String key) {
        return getProperties().containsKey(key);
    }

    public default Object getProperty(String key) {
        var m = getProperties();
        if(!m.containsKey(key))
            throw new IllegalArgumentException("no such property " +
                    key + " in " +this);
        return m.get(key);
    }

    public default String getString(String key) {
        var m = getProperties();
        if(!m.containsKey(key))
            throw new IllegalArgumentException("no such property " +
                    key + " in " +this);
        if((m.get(key) instanceof String))
            return (String)m.get(key);
        return m.get(key)+"";
    }

    public default Integer getInteger(String key) {
        var m = getProperties();
        if(!m.containsKey(key))
            throw new IllegalArgumentException("no such property " +
                    key + " in " +this);
        if((m.get(key) instanceof Integer))
            return (Integer)m.get(key);

        try{ return Integer.parseInt(m.get(key)+"");}
        catch(NumberFormatException e) {
            throw new IllegalArgumentException("property " +
                    key + " does not have type Integer in " +this);
        }
    }

    public Game getGame();

    public default Optional<Entity> getEntity(String property) {
        var properties = getProperties();
        if(properties.containsKey(property)) {
            try {
                var entity = getGame().getEntity((Integer)getProperty(property));
                return Optional.of(entity);
            } catch(Exception e) {

            }
        }
        return Optional.empty();
    }
    public default Optional<Entity> getEntity() { return getEntity("entity"); }

    public default Optional<Direction> getDirection(String property) {
        var properties = getProperties();
        if(properties.containsKey(property)) {
            try {
                var direction = Direction.valueOf(properties.get(property).toString());
                return Optional.of(direction);
            } catch(Exception e) {

            }
        }
        return Optional.empty();
    }
    public default Optional<Direction> getDirection() { return getDirection("direction"); }

    public default Optional<Tile> getTile(String prefix) {
        var properties = getProperties();
        if(properties.containsKey(prefix+"board") &&
                properties.containsKey(prefix+"row") &&
                properties.containsKey(prefix + "column")) {
            var board = getGame().getBoard(properties.get(prefix+"board").toString());
            if(board != null) {
                try {
                    var row = (Integer)properties.get(prefix+"row");
                    var column = (Integer)(properties.get(prefix+"column"));
                    var tile = board.getTile(column, row);
                    if(tile != null) return Optional.of(tile);
                } catch(NumberFormatException e) {
                }
            }
        }
        return Optional.empty();
    }
    public default Optional<Tile> getTile() { return getTile(""); }

    /** Creates a JSON representation of the properties */
    // TODO: use GSON library
    public default String serializeProperties() {
        return "{" + getProperties().entrySet().stream()
                .map(e -> "\"" + e.getKey() + "\": " + e.getValue())
                .reduce((s1, s2) -> s1 + ","+ s2).orElse("") + "}";
    }
}
