package com.sourcedestination.mqttrpg;

import java.util.Map;

public class Map1 extends Board {

    public static final String MAP1 =
                    "#####\n" +
                    "#   #\n" +
                    "#   #\n" +
                    "#   #\n" +
                    "#####\n";


    public Map1() {
        super("map1", MAP1, Map.of());
    }
}
