package com.sourcedestination.mqttrpg;

import java.util.Map;

public class DummyEntity extends Entity {

    public DummyEntity(Game game) {
        super(game, Map.of("DummyKey", "DummyValue"));
    }

    public String getType() { return "dummy"; }
}
