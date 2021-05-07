package com.sourcedestination.mqttrpg;

/** represents an outside agent that acts within the game
 * not an {@link Entity} as an agent may potentially comprise multiple entities within the game.
 */
public abstract class Agent implements Container, HasProperties, EventListener {

	private final String id;
	private final String role;

	public Agent(String id, String role) {
		this.id = id;
		this.role = role;
	}

	public String getAgentID() { return id; }
	public String getRole() { return role; }

	public abstract void receiveCommand(Command command) throws CommandException;
}
