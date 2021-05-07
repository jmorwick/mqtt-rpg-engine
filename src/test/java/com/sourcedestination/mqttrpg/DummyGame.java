package com.sourcedestination.mqttrpg;

import com.sourcedestination.mqttrpg.datastores.InMemoryDataStore;

import java.util.Map;

public class DummyGame extends Game {

    public static final Map<Character,String> TILE_TYPES =
            Map.of('#', "wall",
                    ' ', "floor");

    public Agent getAgent(String id, String role) {
        return null;
    }

    public DummyGame(Board board){
        super("game1", new InMemoryDataStore(),
                event -> {},
                event -> {},
                new Map1(),
                board);
    }

    public DummyGame() {
        super("game1", new InMemoryDataStore(),
                event -> {},
                event -> {},
                new Map1());
    }
}
