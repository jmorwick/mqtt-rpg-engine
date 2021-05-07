package com.sourcedestination.mqttrpg;

/** Interface for any class that listens for and acts on game events
 *
 */
@FunctionalInterface
public interface EventListener {
  public void acceptEvent(Event event);
}
