package utils;

import lexer.Token;

public class BinarySearchTree<T extends Comparable<T>> {
    private static class Node<T> {
        T key;
        int tableNumber; // Номер группы: 1 — ключевые слова, 2 — идентификаторы
        int id;          // Порядковый номер лексемы
        Node<T> left, right;

        Node(T key, int tableNumber, int id) {
            this.key = key;
            this.tableNumber = tableNumber;
            this.id = id;
        }
    }

    private Node<T> root;
    private int nodeCount = 0; // Порядковый номер для всех узлов

    public void insert(T key, int tableNumber) {
        nodeCount++;
        root = insertRec(root, key, tableNumber, nodeCount);
    }

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
        System.out.println("In-order traversal:");
        inOrderRec(root);
        System.out.println();
    }

    private void inOrderRec(Node<T> node) {
        if (node != null) {
            inOrderRec(node.left);
            System.out.print(node.key + " ");
            inOrderRec(node.right);
        }
    }

    // Красивый вывод дерева
    public void printTree() {
        System.out.println("Binary Search Tree (beautiful view):");
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
