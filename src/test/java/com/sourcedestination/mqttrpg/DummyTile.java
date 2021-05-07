package com.sourcedestination.mqttrpg;

import java.util.Map;

public class DummyTile extends Tile{

    public DummyTile(int column, int row){
        super(column, row, "dummy-tile", '~',
                Map.of("character", "L"));

    }
}
