package me.eddie.inventoryguiapi.gui.session;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple implementation of the Attributable interface that uses a hashmap
 */
public abstract class AbstractAttributable implements Attributable {
    private Map<Object, Object> attributes = new HashMap<Object, Object>();

    @Override
    public Object getAttribute(Object key) {
        if(key == null){
            throw new IllegalArgumentException("Key cannot be null");
        }
        return attributes.get(key);
    }

    @Override
    public boolean hasAttribute(Object key){
        return attributes.containsKey(key);
    }

    @Override
    public void putAttribute(Object key, Object value) {
        if(key == null){
            throw new IllegalArgumentException("Key cannot be null");
        }

        attributes.put(key, value);
    }

    @Override
    public Object removeAttribute(Object key) {
        if(key == null){
            throw new IllegalArgumentException("Key cannot be null");
        }

        return attributes.remove(key);
    }
}
