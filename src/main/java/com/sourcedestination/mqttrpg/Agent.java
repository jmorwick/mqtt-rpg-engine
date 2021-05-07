package com.sourcedestination.mqttrpg;

import com.google.gson.JsonParser;

import java.util.Map;

/** represents an outside agent that acts within the game
 * not an {@link Entity} as an agent may potentially comprise multiple entities within the game.
 */
public interface Agent extends Container, HasProperties, EventListener {

	public default void issueCommand(String command, String parameter) {
		getGame().propagateEvent(new Event(getGame(), "command",
				Map.of(
						"agent", getAgentID(),
						"command", command,
						"parameter", parameter
				)));
	}

	public default void issueCommandFromJson(String json) {
		var element = new JsonParser().parse(json);
		var command = element.getAsJsonObject().get("command").getAsString();
		var parameter = element.getAsJsonObject().get("parameter").getAsString();
		getGame().propagateEvent(new Event(getGame(), "command",
				Map.of("agent", getAgentID(),
						"command", command,
						"parameter", parameter)));
	}

	public String getAgentID();
	public String getRole();
}
