package com.gpsaviator.android.model;

import com.google.android.gms.maps.GoogleMap;
import lombok.Getter;

import java.util.*;

/**
 * Created by khaines on 31/05/14.
 */
public class MapElements<E,M> {

    private final Map<E, M> elements;

    public MapElements() {
        elements = new HashMap<E, M>();
    }

    public synchronized int size() {
        return elements.size();
    }

    public void clear() {
        elements.clear();
    }

    public static class Update<E> {
        @Getter
        private final Set<E> toDelete;

        @Getter
        private final Set<E> toAdd;

        Update(Set<E> add, Set<E> remove) {
            toAdd = add;
            toDelete = remove;
        }
    }

    public synchronized Update<E> getUpdateRecord(Collection<E> newElements) {
        Set<E> newSet = new HashSet<E>(newElements);
        Set<E> oldSet = elements.keySet();

        // Airspaces to remove are the set difference (oldSet - newSet)
        Set<E> remove = new HashSet<E>(oldSet);
        remove.removeAll(newSet);

        // Airspaces to add are the set difference (newSet - oldSet)
        Set<E> add = new HashSet<E>(newSet);
        add.removeAll(oldSet);
        return new Update(add, remove);
    }

    public synchronized M get(E element) {
        return elements.get(element);
    }

    public synchronized void remove(E a) {
        elements.remove(a);
    }

    public Collection<E> getAll() {
        return elements.keySet();
    }

    public synchronized boolean exists(E element) {
        return elements.containsKey(element);
    }

    public synchronized void add(E element, M mapObject) {
        elements.put(element, mapObject);
    }
}
