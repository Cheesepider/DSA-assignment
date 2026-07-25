/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package adt;

/**
 *
 * @author jlohz
 */
public class DoublyLinkedList<T> implements ListInterface<T> {

    private Node firstNode;
    private Node lastNode;
    private int numberOfEntries;

    public DoublyLinkedList() {
        firstNode = null;
        lastNode = null;
        numberOfEntries = 0;
    }

    private class Node {

        private T data;
        private Node next;
        private Node previous;

        public Node(T data) {
            this(data, null, null);
        }

        public Node(T data, Node next, Node previous) {
            this.data = data;
            this.next = next;
            this.previous = previous;
        }
    }

    @Override
    public boolean add(T newEntry) {
        Node newNode = new Node(newEntry);
        if (isEmpty()) {
            firstNode = newNode;
        } else {
            lastNode.next = newNode;
            newNode.previous = lastNode;
        }
        lastNode = newNode;
        numberOfEntries++;
        return true;
    }

    @Override
    public boolean add(int position, T newEntry) {
        Node newNode = new Node(newEntry);

        if (isEmpty()) {
            firstNode = newNode;
            lastNode = newNode;
        } else {
            lastNode.next = newNode;
            newNode.previous = lastNode;
            lastNode = newNode;
        }
        numberOfEntries++;
        return true;
    }

    @Override
    public T remove(int position) {
        if (position >= 1 && position <= numberOfEntries) {
            Node nodeToRemove = firstNode;
            for (int i = 1; i < position; i++) {
                nodeToRemove = nodeToRemove.next;
            }
            T result = nodeToRemove.data;
            if (position == 1) {
                firstNode = firstNode.next;
                if (firstNode != null) {
                    firstNode.previous = null;
                } else {
                    lastNode = null;
                }
            } else if (position == numberOfEntries) {
                lastNode = lastNode.previous;
                if (lastNode != null) {
                    lastNode.next = null;
                } else {
                    firstNode = null;
                }
            } else {
                Node nodeBefore = nodeToRemove.previous;
                Node nodeAfter = nodeToRemove.next;
                nodeBefore.next = nodeAfter;
                nodeAfter.previous = nodeBefore;
            }
            numberOfEntries--;
            return result;
        }
        return null;
    }

    @Override
    public T getEntry(int position) {
        if (position >= 1 && position <= numberOfEntries) {
            Node currentNode = firstNode;
            for (int i = 1; i < position; i++) {
                currentNode = currentNode.next;
            }
            return currentNode.data;
        }
        return null;
    }

    @Override
    public boolean replace(int position, T newEntry) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public boolean contains(T anEntry) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public boolean isEmpty() {
        return numberOfEntries == 0;
    }

    @Override
    public int getNumberOfEntries() {
        return numberOfEntries;
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
    //node swapping based on priority, but just swap the data(.data) 
    public void swap(int position1, int position2) {
        if (position1 >= 1 && position1 <= numberOfEntries
                && position2 >= 1 && position2 <= numberOfEntries) {

            Node node1 = firstNode;
            Node node2 = firstNode;

            for (int i = 1; i < position1; i++) {
                node1 = node1.next;
            }

            for (int i = 1; i < position2; i++) {
                node2 = node2.next;
            }

            T temp = node1.data;
            node1.data = node2.data;
            node2.data = temp;
        }
    }
}
