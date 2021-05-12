package com.sourcedestination.sokoban;

import com.sourcedestination.mqttrpg.*;

public interface PushableToPassable extends Pushable {
    @Override
    public default boolean canPush(Entity pusher, Container container) {
        if(container instanceof Tile tile) {
            return 
                !((tile.hasProperty("impassable") && 
                 tile.getProperty("impassable").equals("true")) ||
                 tile.getEntities().anyMatch(e -> 
                    e.hasProperty("impassable") && 
                    e.getProperty("impassable").equals("true")));
        } else return false;
    }
}