package com.sourcedestination.mqttrpg;

import com.sourcedestination.mqttrpg.*;

public interface Moveable extends Affordance {

    public default boolean canMove(Agent mover, Container container) {
        return true;
    }

    public default Action move(Agent mover, Container container) {
        return game -> {
          if(canMove(mover, container)) {
              game.moveEntity((Entity)this, container);
          }
        };
    }
}
