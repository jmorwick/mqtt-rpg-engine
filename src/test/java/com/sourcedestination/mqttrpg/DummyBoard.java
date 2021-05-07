package com.sourcedestination.mqttrpg;

import java.util.Map;

public class DummyBoard extends Board {

    public DummyBoard(String name, String charMap, Tile ... tile){
        super(name,charMap, Map.of(), tile);

    }
}
