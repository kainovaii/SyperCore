package com.azalealibrary.azaleacore.api;

import com.azalealibrary.azaleacore.foundation.AzaleaException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public abstract class AzaleaApi<T> {

    private final Map<String, T> objects = new HashMap<>();

    public ImmutableList<String> getKeys() {
        return ImmutableList.copyOf(objects.keySet());
    }

    public ImmutableList<T> getObjects() {
        return ImmutableList.copyOf(objects.values());
    }

    public ImmutableMap<String, T> getEntries() {
        return ImmutableMap.copyOf(objects);
    }

    public T get(String key) {
        return objects.get(key);
    }

    public @Nullable String getKey(T object) {
        return objects.entrySet().stream()
                .filter(entry -> object.equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .findFirst().orElse(null);
    }

    public boolean hasKey(String key) {
        return get(key) != null;
    }

    public void add(String key, T object) {
        if (objects.containsKey(key)) {
            throw new AzaleaException("Object with key '" + key + "' already exists.");
        }
        update(key, object);
    }

    protected void update(String key, T object) {
        objects.put(key, object);
    }

    public void remove(String key) {
        if (!objects.containsKey(key)) {
            throw new AzaleaException("Object with key '" + key + "' does not exists.");
        }
        objects.remove(key);
    }

    public void remove(T object) {
        while (objects.values().remove(object));
    }
}
