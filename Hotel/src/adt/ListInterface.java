/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package adt;

/**
 *
 * @author Jerry,KaoYongFeng,LeeShenFung,ThamClement, Lee Shen Fung
 */
public interface ListInterface<T> {

    boolean add(T newEntry);

    boolean add(int position, T newEntry);

    T remove(int position);

    T   getEntry(int position);

    boolean replace(int position, T newEntry);

    boolean contains(T anEntry);

    boolean isEmpty();

    int getNumberOfEntries();

    void swap(int position1, int position2);

    void clear();

    void printList();

    ListInterface<T> copy();

    int indexOf(T anEntry);
}
