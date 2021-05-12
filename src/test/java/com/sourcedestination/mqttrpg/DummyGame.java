package com.sourcedestination.mqttrpg;

import java.util.Map;
import java.util.function.Consumer;

public class DummyGame extends Game {

    public static final Map<Character,String> TILE_TYPES =
            Map.of('#', "wall",
                    ' ', "floor");

    public Agent getAgent(String id, String role) {
        return null;
    }

    public boolean checkGameAlive() { return true; }


    public DummyGame(Board board){
        super("game1", new Map1(), board);
    }

    public DummyGame() {
        super("game1", new Map1());
    }

    @Override
    public Map<String, Object> getProperties() {
        return null;
    }

    @Override
    public void setProperty(String key, Object value) {

    }
}
