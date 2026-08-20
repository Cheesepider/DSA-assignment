/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package adt;

/**
 *
 * @author jlohz， Kao Yong Feng, Lee Shen Fung
 */
public interface ListInterface<T> {

    boolean add(T newEntry);

    boolean add(int position, T newEntry);

    T remove(int position);

    T getEntry(int position);

    boolean replace(int position, T newEntry);

    boolean contains(T anEntry);

    boolean isEmpty();

    int getNumberOfEntries();
    
    void swap(int position1, int position2);

    void clear();

    void printList();

    // returns a new list containing the same entries, in the same order,
    // as this list (a shallow copy - the entries themselves are not cloned)
    ListInterface<T> copy();

    // returns the position (1-based) of the first occurrence of anEntry
    // found while scanning simultaneously from both ends of the list,
    // or -1 if anEntry is not found
    int indexOf(T anEntry);
}