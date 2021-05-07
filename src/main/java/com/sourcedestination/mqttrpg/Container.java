package com.sourcedestination.mqttrpg;
import java.util.stream.Stream;

/**
 * Interface for a container that can hold entities.
 */
public interface Container extends HasProperties {

	public Game getGame();

	/**
	 * Adds Entity to container.
	 * @param ent Entity to add.
	 */
	public default void addEntity(Entity ent) {
		getGame().moveEntity(ent, this);
	}

	/**
	 * Removes entity from container.
	 * By default simply removes entity from all contained container entities, recursively.
	 * Should be overridden and called from implementing class.
	 * @param ent Entity to remove.
	 */
	public default void removeEntity(Entity ent) {
		getGame().moveEntity(ent, getGame());
	}

	/**
	 * returns all entities in container.
	 * @return HashSet of all entities. 
	 */
	public default Stream<Entity> getEntities() {
		return getGame().getContainerContents(this);
	}

	public default boolean isEmpty() { return !getEntities().findAny().isPresent(); }

	public default boolean containsEntity(Entity ent) {
		return getGame().containsEntity(this, ent);
	}
}
