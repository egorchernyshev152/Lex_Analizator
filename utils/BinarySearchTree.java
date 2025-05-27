package utils;

public class BinarySearchTree<T extends Comparable<T>> {
    private static class Node<T> {
        T key;
        int tableNumber; // Номер группы: 1 — ключевые слова, 2 — идентификаторы
        int id;          // Номер лексемы
        Node<T> left, right;

        Node(T key, int tableNumber, int id) {
            this.key = key;
            this.tableNumber = tableNumber;
            this.id = id;
        }
    }

    // Корень дерева
    private Node<T> root;

    // Счётчик уникального номера каждому новому узлу
    private int nodeCount = 0;

    // Вставка нового ключа в дерево
    public void insert(T key, int tableNumber) {
        nodeCount++;
        root = insertRec(root, key, tableNumber, nodeCount);
    }

    // Рекурсивная функция вставки нового узла в бинарное дерево поиска
    private Node<T> insertRec(Node<T> node, T key, int tableNumber, int id) {
        if (node == null) {
            return new Node<>(key, tableNumber, id);
        }
        if (key.compareTo(node.key) < 0) {
            node.left = insertRec(node.left, key, tableNumber, id);
        } else if (key.compareTo(node.key) > 0) {
            node.right = insertRec(node.right, key, tableNumber, id);
        }
        return node;
    }

    public void inOrderTraversal() {
        System.out.println("Сначала по алфавиту:");
        inOrderRec(root);
        System.out.println();
    }

    // Рекурсивная функция обхода дерева
    private void inOrderRec(Node<T> node) {
        if (node != null) {
            inOrderRec(node.left);
            System.out.print(node.key + " ");
            inOrderRec(node.right);
        }
    }

    // Вывод дерева
    public void printTree() {
        System.out.println("\nА теперь - графический вариант:");
        printTreeRec(root, "", true);
    }

    private void printTreeRec(Node<T> node, String prefix, boolean isLeft) {
        if (node != null) {
            System.out.println(prefix + (isLeft ? "├── " : "└── ") + "[" + node.tableNumber + "." + node.id + "] " + node.key);
            printTreeRec(node.left, prefix + (isLeft ? "│   " : "    "), true);
            printTreeRec(node.right, prefix + (isLeft ? "│   " : "    "), false);
        }
    }
}
