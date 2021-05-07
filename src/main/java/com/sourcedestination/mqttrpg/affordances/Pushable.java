package com.sourcedestination.mqttrpg.affordances;

import com.sourcedestination.mqttrpg.*;

public interface Pushable extends Affordance {
    public default Action push(Entity pusher) {
        return game -> {
            if(game.getEntityLocation(pusher) instanceof  Tile pusherTile) {
                if(game.getEntityLocation(getSelfReference()) instanceof Tile myTile) {
                    var board = pusherTile.getBoard();
                    if(myTile.getBoard() == board) {
                        var dir = board.getAdjacentTileDirection(pusherTile, myTile);
                        if(dir.isPresent()) {
                            var newTile = board.getAdjacentTile(myTile, dir.get());
                            if(newTile.isPresent()) {
                                game.moveEntity(getSelfReference(), newTile.get());
                            }
                        }
                    }
                }
            }
        };
    }
}
