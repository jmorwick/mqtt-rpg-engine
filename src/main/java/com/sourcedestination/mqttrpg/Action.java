package com.sourcedestination.mqttrpg;

import java.util.function.Consumer;

@FunctionalInterface
public interface Action extends Consumer<Game> {
}
