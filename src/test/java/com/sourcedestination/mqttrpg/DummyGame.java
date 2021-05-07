package com.sourcedestination.mqttrpg;

import com.sourcedestination.mqttrpg.datastores.InMemoryDataStore;
import org.checkerframework.checker.units.qual.C;

import java.util.Map;
import java.util.function.Consumer;

public class DummyGame extends Game {

    public static final Map<Character,String> TILE_TYPES =
            Map.of('#', "wall",
                    ' ', "floor");

    public Agent getAgent(String id, String role) {
        return null;
    }


    public static ClientHub getDummyHub() {
        return new ClientHub() {
            @Override
            public void publishEvent(Event e) {

            }

            @Override
            public void publishBoardState(Board b) {

            }

            @Override
            public void publishEntityState(Entity e) {

            }

            @Override
            public void publishAgent(Agent a) {

            }

            @Override
            public void registerCommandCallback(String agent, Consumer<String> jsonCallback) {

            }
        };
    }

    public DummyGame(Board board){
        super("game1", getDummyHub(), new InMemoryDataStore(), new Map1(), board);
    }

    public DummyGame() {
        super("game1", getDummyHub(), new InMemoryDataStore(), new Map1());
    }
}
