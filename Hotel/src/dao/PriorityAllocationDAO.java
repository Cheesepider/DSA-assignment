/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

/**
 *
 * @author jlohz
 */
import entity.Booking;
import entity.Member;
import entity.Room;
import java.time.LocalDate;
import java.time.LocalDateTime;
import adt.ListInterface;
import adt.DoublyLinkedList;

public class PriorityAllocationDAO {

    public ListInterface<Member> initializeMemberDAO() {

        ListInterface<Member> memberList = new DoublyLinkedList<>();

        memberList.add(new Member("M001", "Alice", "0123456789", "alice@email.com", "Elite", 1000));
        memberList.add(new Member("M002", "Bob", "0123456788", "bob@email.com", "Diamond", 800));
        memberList.add(new Member("M003", "Charlie", "0123456787", "charlie@email.com", "Platinum", 500));
        memberList.add(new Member("M004", "David", "0123456786", "david@email.com", "Standard", 100));
        memberList.add(new Member("M005", "Emma", "0123456785", "emma@email.com", "Elite", 1200));

        return memberList;
    }

    public ListInterface<Room> initializeRoomDAO() {

        ListInterface<Room> roomList = new DoublyLinkedList<>();

        roomList.add(new Room("R001", "Standard", "Vacant"));
        roomList.add(new Room("R002", "Deluxe", "Vacant"));
        roomList.add(new Room("R003", "Suite", "Vacant"));
        roomList.add(new Room("R004", "Standard", "Vacant"));
        roomList.add(new Room("R005", "Deluxe", "Vacant"));

        return roomList;
    }

    public ListInterface<Booking> initializeBookingDAO(ListInterface<Member> members) {

        ListInterface<Booking> bookingList = new DoublyLinkedList<>();

        bookingList.add(new Booking(
                "B001",
                LocalDate.of(2026, 7, 20),
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 3),
                LocalDateTime.of(2026, 7, 20, 9, 0),
                members.getEntry(4), // David
                null));

        bookingList.add(new Booking(
                "B002",
                LocalDate.of(2026, 7, 15),
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 3),
                LocalDateTime.of(2026, 7, 15, 9, 0),
                members.getEntry(3), // Charlie
                null));

        bookingList.add(new Booking(
                "B003",
                LocalDate.of(2026, 7, 21),
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 3),
                LocalDateTime.of(2026, 7, 21, 9, 0),
                members.getEntry(2), // Bob
                null));

        bookingList.add(new Booking(
                "B004",
                LocalDate.of(2026, 7, 21),
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 3),
                LocalDateTime.of(2026, 7, 21, 9, 30),
                members.getEntry(1), // Alice
                null));

        bookingList.add(new Booking(
                "B005",
                LocalDate.of(2026, 7, 14),
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 3),
                LocalDateTime.of(2026, 7, 14, 21, 15),
                members.getEntry(5), // Emma
                null));

        bookingList.add(new Booking(
                "B006",
                LocalDate.of(2026, 7, 22),
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 3),
                LocalDateTime.of(2026, 7, 22, 19, 0),
                members.getEntry(2), // Bob
                null));

        bookingList.add(new Booking(
                "B007",
                LocalDate.of(2026, 7, 15),
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 3),
                LocalDateTime.of(2026, 7, 15, 8, 0),
                members.getEntry(3), // Charlie
                null));

        bookingList.add(new Booking(
                "B008",
                LocalDate.of(2026, 7, 17),
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 3),
                LocalDateTime.of(2026, 7, 17, 23, 49),
                members.getEntry(4), // David
                null));

        bookingList.add(new Booking(
                "B009",
                LocalDate.of(2026, 7, 19),
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 3),
                LocalDateTime.of(2026, 7, 19, 10, 4),
                members.getEntry(1), // Alice
                null));

        bookingList.add(new Booking(
                "B010",
                LocalDate.of(2026, 7, 25),
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 3),
                LocalDateTime.of(2026, 7, 25, 17, 0),
                members.getEntry(5), // Emma
                null));

        return bookingList;
    }
}
