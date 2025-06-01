package utils;

import java.util.*;

/**
 * Простая хеш-таблица (chaining) для хранения пар (ключ → значение).
 */
public class HashTable<K, V> {
    public static class Entry<K, V> {
        public final K key;
        public final V value;
        public Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    private static final int SIZE = 16;
    private LinkedList<Entry<K, V>>[] table;

    @SuppressWarnings("unchecked")
    public HashTable() {
        table = (LinkedList<Entry<K, V>>[]) new LinkedList[SIZE];
        for (int i = 0; i < SIZE; i++) {
            table[i] = new LinkedList<>();
        }
    }

    private int hash(K key) {
        return (key.hashCode() & 0x7FFFFFFF) % SIZE;
    }

    public void put(K key, V value) {
        int index = hash(key);
        for (Entry<K, V> entry : table[index]) {
            if (entry.key.equals(key)) {
                table[index].remove(entry);
                table[index].add(new Entry<>(key, value));
                return;
            }
        }
        table[index].add(new Entry<>(key, value));
    }

    public V get(K key) {
        int index = hash(key);
        for (Entry<K, V> entry : table[index]) {
            if (entry.key.equals(key)) {
                return entry.value;
            }
        }
        return null;
    }

    public boolean containsKey(K key) {
        return get(key) != null;
    }

    public List<Entry<K, V>> entries() {
        List<Entry<K, V>> allEntries = new ArrayList<>();
        for (int i = 0; i < SIZE; i++) {
            allEntries.addAll(table[i]);
        }
        return allEntries;
    }

    public static <K> void printSortedTable(HashTable<K, Integer> table) {
        List<HashTable.Entry<K, Integer>> entries = table.entries();
        entries.stream()
                .sorted(Comparator.comparingInt(e -> e.value))
                .forEach(e -> System.out.println(e.key + " → " + e.value));
    }
}
