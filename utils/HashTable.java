package utils;

import java.util.LinkedList;

public class HashTable<K, V> {

    // Класс для хранения пары ключ-значение в таблице
    private static class Entry<K, V> {
        final K key;
        V value;

        Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    private final int SIZE = 128;
    private LinkedList<Entry<K, V>>[] table; // Массив списков для хранения элементов

    @SuppressWarnings("unchecked")
    public HashTable() {
        table = new LinkedList[SIZE];
        // Инициализируем каждый элемент массива пустым связным списком
        for (int i = 0; i < SIZE; i++) {
            table[i] = new LinkedList<>();
        }
    }

    // Хеш-функция
    private int hash(K key) {
        return Math.abs(key.hashCode()) % SIZE;
    }

    /**
     * Добавляет пару ключ-значение в хеш-таблицу.
     * Если ключ уже существует, то обновляет значение.
     */
    public void put(K key, V value) {
        int index = hash(key);
        for (Entry<K, V> entry : table[index]) {
            if (entry.key.equals(key)) {
                entry.value = value;
                return;
            }
        }
        table[index].add(new Entry<>(key, value));
    }

    // Выводит содержимое таблицы (только непустые списки)
    public void printTable() {
        for (int i = 0; i < SIZE; i++) {
            if (!table[i].isEmpty()) {
                for (Entry<K, V> entry : table[i]) {
                    System.out.println(entry.key + " => " + entry.value);
                }
            }
        }
    }
}
