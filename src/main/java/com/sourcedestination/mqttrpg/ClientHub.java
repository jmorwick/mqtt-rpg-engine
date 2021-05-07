package com.sourcedestination.mqttrpg;

import java.util.function.Consumer;

public interface ClientHub {
    /** relays event to client communication hub */
    public void publishEvent(Event e);

    /** relays update to board state to client communication hub */
    public void publishBoardState(Board b);

    /** relays update to entity state to client communication hub */
    public void publishEntityState(Entity e);

    /** relays update to agent state to client communication hub */
    public void publishAgent(Agent a);

    /** registers callback for arrival of commands from client to communication hub */
    public void registerCommandCallback(String agent, Consumer<String> jsonCallback);
}
