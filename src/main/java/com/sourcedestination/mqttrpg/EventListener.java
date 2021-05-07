package com.sourcedestination.mqttrpg;

import java.util.function.Consumer;

/** Interface for any class that listens for and acts on game events
 *
 */
public interface EventListener extends Consumer<Event> {
  public default void accept(Event event) {
      acceptEvent(event);
  }

  public void acceptEvent(Event event);

  public default void acceptEvent(Event event, long delayTime) {
      // delay not implemented by default, must be overridden
      // (should be overridden by server provisioning Game)
      acceptEvent(event);
  }
}
