/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package adt;


/**
 *
 * @author jlohz, Kao Yong Feng
 * @param <T>
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
        if (isEmpty()|| firstNode == null) {
            firstNode = newNode;
            lastNode = newNode;
        } else {
            lastNode.next = newNode;
            newNode.previous = lastNode;
        }
        lastNode = newNode;
        numberOfEntries++;
        return true;
    }

    @Override
    public boolean add(int position, T newEntry) { // adds entry at specified position in the DoublyLinkedList
        // returns true if successful, false if position is invalid
        boolean added = false;

        // check if position is valid: >=1 and <= numberOfEntries + 1
        if (checkPositionValid(position, numberOfEntries + 1)) {
            // here on all cases are valid position
            // position can be 1(beginning), numberOfEntries + 1(end), or in between
            // list can be empty or not empty
            if (isEmpty()) {
                // if list is empty, add newEntry as firstNode and lastNode
                firstNode = new Node(newEntry);
                lastNode = firstNode;
                added = true;
                numberOfEntries++;
            } else if (position == 1) {
                // if position is 1, add newEntry as firstNode
                Node newNode = new Node(newEntry, firstNode, null);
                firstNode.previous = newNode;
                firstNode = newNode;
                added = true;
                numberOfEntries++;
            } else if (position == numberOfEntries + 1) {
                // if position is numberOfEntries + 1, add newEntry as lastNode
                Node newNode = new Node(newEntry, null, lastNode);
                lastNode.next = newNode;
                lastNode = newNode;
                added = true;
                numberOfEntries++;
            } else {
                // if position is in between, add newEntry at specified position
                // to increase efficiency, we can check if position is closer to firstNode or lastNode
                if (position <= numberOfEntries / 2) {
                    // position is closer to firstNode, traverse from front
                    Node currentNode = firstNode;
                    for (int i = 1; i < position - 1; i++) {
                        currentNode = currentNode.next; // traverse to the node before the specified position
                    }
                    Node newNode = new Node(newEntry, currentNode.next, currentNode);
                    currentNode.next.previous = newNode;
                    currentNode.next = newNode;
                    added = true;
                    numberOfEntries++;
                } else {
                    // position is closer to lastNode, traverse from behind
                    Node currentNode = lastNode;
                    for (int i = numberOfEntries; i > position - 1; i--) {
                        currentNode = currentNode.previous; // traverse to the node before the specified position
                    }
                    Node newNode = new Node(newEntry, currentNode.next, currentNode);
                    currentNode.next.previous = newNode;
                    currentNode.next = newNode;
                    added = true;
                    numberOfEntries++;
                }
            }
        }
        return added;
    }

    @Override
    public T remove(int position) {
    // v1, traverse from front to back
        // removes entry at specified position in the DoublyLinkedList
        // returns the removed entry if successful, null if position is invalid

        T removedData = null;
        // check if position is valid: >=1 and <= numberOfEntries
        if (checkPositionValid(position, numberOfEntries)){
            //position is valid, proceed to remove entry at specified position
            if (numberOfEntries == 1) { // if only one node exist
                removedData = firstNode.data;
                clear();
            } else if (position == 1) { // if node is firstnode
                removedData = firstNode.data;
                firstNode = firstNode.next;
                firstNode.previous = null;
                numberOfEntries --;
            } else if (position == numberOfEntries) { // if node is last node
                removedData = lastNode.data;
                lastNode = lastNode.previous;
                lastNode.next = null;
                numberOfEntries --;
            } else {
                //position is in between, remove entry at specified position
                // to increase efficiency, we can check if position is closer to firstNode or lastNode
                if (position <= numberOfEntries / 2) {
                    // position is closer to firstNode, traverse from firstNode
                    Node currentNode = firstNode;
                    for (int i = 1; i < position; i++) {
                        currentNode = currentNode.next; // traverse to the node at the specified position
                    }
                    removedData = currentNode.data;
                    currentNode.previous.next = currentNode.next;
                    currentNode.next.previous = currentNode.previous;
                    numberOfEntries --;

                } else {
                    // position is closer to lastNode, traverse from lastNode
                    Node currentNode = lastNode;
                    for (int i = numberOfEntries; i > position; i--) {
                        currentNode = currentNode.previous; // traverse to the node at the specified position
                    }
                    removedData = currentNode.data;
                    currentNode.previous.next = currentNode.next;
                    currentNode.next.previous = currentNode.previous;
                    numberOfEntries --;
                }
            }
        }
        return removedData;
    }

    @Override
    public T getEntry(int position) {
        // retrieves entry at specified position in the DoublyLinkedList
        // returns the entry if successful, null if position is invalid

        T result = null;
        // check if position is valid: >=1 and <= numberOfEntries
        if (checkPositionValid(position, numberOfEntries)) {
            // position is valid, proceed to retrieve entry at specified position
            // to increase efficiency, we can check if position is closer to firstNode or lastNode
            if (position <= numberOfEntries / 2) {
                // position is closer to firstNode, traverse from firstNode
                Node currentNode = firstNode;
                for (int i = 1; i < position; i++) {
                    currentNode = currentNode.next; // traverse to the node at the specified position
                }
                result = currentNode.data;
            } else {
                // position is closer to lastNode, traverse from lastNode
                Node currentNode = lastNode;
                for (int i = numberOfEntries; i > position; i--) {
                    currentNode = currentNode.previous; // traverse to the node at the specified position
                }
                result = currentNode.data;
            }
        }
        return result;
    }

    @Override
    public boolean replace(int position, T newEntry) {
        // replaces entry at specified position in the DoublyLinkedList
        // returns true if successful, false if position is invalid

        boolean replaced = false;
        // check if position is valid: >=1 and <= numberOfEntries
        if (checkPositionValid(position, numberOfEntries)) {
            // position is valid, proceed to interpret position
            // to increase efficiency, we can check if position is closer to firstNode or lastNode
            if (position <= numberOfEntries / 2) {
                // position is closer to firstNode, traverse from firstNode
                Node currentNode = firstNode;
                for (int i = 1; i < position; i++) {
                    currentNode = currentNode.next; // traverse to the node at the specified position
                }
                currentNode.data = newEntry; // replace entry at specified position
                replaced = true;
            } else {
                // position is closer to lastNode, traverse from lastNode
                Node currentNode = lastNode;
                for (int i = numberOfEntries; i > position; i--) {
                    currentNode = currentNode.previous; // traverse to the node at the specified position
                }
                currentNode.data = newEntry; // replace entry at specified position
                replaced = true;
            }
        }
        return replaced;
    }

    @Override
    public boolean contains(T anEntry) {
        // checks if the DoublyLinkedList contains the specified entry
        // returns true if the entry is found, false otherwise
        // can implement by traversing both end at same time

        // if list is empty, return false
        if (isEmpty()) {
            return false;
        }

        // list is not empty, traverse from both ends of the list
        Node frontNode = firstNode;
        Node backNode = lastNode;
        while (frontNode != null && backNode != null) {
            if (frontNode.data.equals(anEntry)) {
                return true;
            }
            if (backNode.data.equals(anEntry)) {
                return true;
            }
            frontNode = frontNode.next;
            backNode = backNode.previous;
        }
        return false;
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
        // clears the DoublyLinkedList
        firstNode = null;
        lastNode = null;
        numberOfEntries = 0;
    }
    

    //node swapping based on priority, but just swap the data(.data) 
    @Override
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
    
    @Override
    public void printList() {
        // prints the entries in the DoublyLinkedList
        Node currentNode = firstNode;
        while (currentNode != null) {
            System.out.print(currentNode.data + " ");
            currentNode = currentNode.next;
        }
        System.out.println();
    }
    
    @Override
    public String toString() {
        // returns a string representation of the DoublyLinkedList
        StringBuilder sb = new StringBuilder();
        Node currentNode = firstNode;
        while (currentNode != null) {
            sb.append(currentNode.data).append(" ");
            currentNode = currentNode.next;
        }
        return sb.toString();
    }

    // returns a new DoublyLinkedList containing the same entries, in the
    // same order, as this list (a shallow copy - entries are not cloned).
    // Useful whenever a caller needs to sort/rearrange a list (e.g. via
    // swap()) without disturbing the original list's order.
    @Override
    public ListInterface<T> copy() {
        ListInterface<T> newList = new DoublyLinkedList<>();
        Node currentNode = firstNode;
        while (currentNode != null) {
            newList.add(currentNode.data);
            currentNode = currentNode.next;
        }
        return newList;
    }

    // returns the 1-based position of the first occurrence of anEntry,
    // or -1 if not found. Reuses the same front/back simultaneous-traversal
    // technique as contains(), but returns the matching position instead
    // of just a boolean.
    @Override
    public int indexOf(T anEntry) {
        if (isEmpty()) {
            return -1;
        }

        Node frontNode = firstNode;
        Node backNode = lastNode;
        int frontPosition = 1;
        int backPosition = numberOfEntries;

        while (frontPosition <= backPosition) {
            if (frontNode.data.equals(anEntry)) {
                return frontPosition;
            }
            if (backNode.data.equals(anEntry)) {
                return backPosition;
            }
            frontNode = frontNode.next;
            backNode = backNode.previous;
            frontPosition++;
            backPosition--;
        }
        return -1;
    }

    private boolean checkPositionValid(int position, int positionLimit) {
        if (position < 1 || position > positionLimit) {
            return false; // checks for invalid position, if true ends by returning false
        }
        return true;
    }
}