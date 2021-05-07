package com.sourcedestination.mqttrpg;

import org.junit.Test;
import static org.junit.Assert.*;


public class TestBoardLocations {
    @Test public void testGetTile() {
        var board = new Map1();
        var location = board.getTile(2, 2);
        assertNotNull(location);
    }
}
