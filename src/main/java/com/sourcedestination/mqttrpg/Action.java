package com.sourcedestination.mqttrpg;

import java.util.function.Consumer;

@FunctionalInterface
public interface Action extends Consumer<Game> {

    /** returns a description for the game log */
    public default String getLogDescription() { return "a generic action was performed"; }

    /** modifies action to include specified description */
    public default Action describe(String description) {
        var self = this;
        return new Action() {
            @Override
            public void accept(Game game) {
                self.accept(game);
            }

            public String getDescription() { return description; }
        };
    }
}
