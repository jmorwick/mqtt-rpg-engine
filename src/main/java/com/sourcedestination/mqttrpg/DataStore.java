package com.sourcedestination.mqttrpg;
import java.util.ArrayList;
import java.util.Map;

/**
 * Interface for a container that can hold entities.
 */
public interface DataStore {
	public void save(HasProperties object);

	public void load(HasProperties object);

	public ArrayList<Integer> search(Map<String, Object> map);

	/** returns the largest entity id in the database */
	public int getMaxEntityId();
}
