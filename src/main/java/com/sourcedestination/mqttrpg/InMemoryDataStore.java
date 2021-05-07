package com.sourcedestination.mqttrpg;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryDataStore implements DataStore {
    private Map<Integer,Map<String,Object>> entities = new ConcurrentHashMap<>();
    private Map<String,Map<String,Object>> agents = new ConcurrentHashMap<>();

    public void save(HasProperties obj) {
        // implement multiple dispatch
        if(obj instanceof Agent) {
            save((Agent)obj);
        } else if(obj instanceof Entity) {
            save((Entity)obj);
        }
    }

    private void save(Agent agent) {
        agents.put(agent.getAgentID(), agent.getProperties());
    }

    private void save(Entity entity) {
        entities.put(entity.getID(), entity.getProperties());
    }

    public void load(HasProperties obj) {
        // implement multiple dispatch
        if(obj instanceof Agent) {
            load((Agent)obj);
        } else if(obj instanceof Entity) {
            load((Entity)obj);
        }
    }

    private void load(Agent agent) {
        assert agents.containsKey(agent.getAgentID());

        var properties = agents.get(agent);
        if(properties != null) for(var key : properties.keySet())
            agent.setProperty(key, properties.get(key));
    }

    private void load(Entity entity) {
        assert entities.containsKey(entity.getID());

        var properties = entities.get(entity);
        if(properties != null) for(var key : properties.keySet())
            entity.setProperty(key, properties.get(key));
    }

    public ArrayList<Integer> search(Map<String, Object> query) {
        var setQuery = query.entrySet();
        var results = new ArrayList<Integer>();
        for(var id : entities.keySet()) {
            var entityProperties = entities.get(id).entrySet();
            if(entityProperties.containsAll(setQuery))
                results.add(id);
        }
        return results;
    }

    public int getMaxEntityId() {
        return entities.keySet().stream().mapToInt(x->x.intValue()).max().orElseGet(()->1);
    }
}
