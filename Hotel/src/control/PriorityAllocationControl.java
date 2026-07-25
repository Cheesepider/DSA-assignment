/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package control;

import entity.Booking;
import adt.DoublyLinkedList;
import entity.Room;

/**
 *
 * @author jlohz
 */
public class PriorityAllocationControl {

    private DoublyLinkedList<Booking> bookingList;
    private DoublyLinkedList<Room> roomList;

    public PriorityAllocationControl() {
        bookingList = new DoublyLinkedList<>();
        roomList = new DoublyLinkedList<>();
    }

    public void addBooking(Booking newBooking) {
        bookingList.add(newBooking);
        reorganizePriority();
    }

    public void addRoom(Room newRoom) {
        roomList.add(newRoom);
    }

    private int getTierPriority(String loyaltyTier) {
        if (loyaltyTier.equals("Elite")) {
            return 4;
        } else if (loyaltyTier.equals("Diamond")) {
            return 3;
        } else if (loyaltyTier.equals("Platinum")) {
            return 2;
        } else if (loyaltyTier.equals("Standard")) {
            return 1;
        } else {
            throw new IllegalArgumentException("Invalid loyalty tier");
        }
    }

    private int comparePriority(Booking bookingA, Booking bookingB) {

        int tierA = getTierPriority(
                bookingA.getMember().getLoyaltyTier()
        );

        int tierB = getTierPriority(
                bookingB.getMember().getLoyaltyTier()
        );

        // First priority: Loyalty Tier
        if (tierA > tierB) {
            return 1;
        } else if (tierA < tierB) {
            return -1;
        }

        // Second priority: Earlier Booking Date
        if (bookingA.getBookingDate().isBefore(
                bookingB.getBookingDate())) {
            return 1;
        } else if (bookingA.getBookingDate().isAfter(
                bookingB.getBookingDate())) {
            return -1;
        }

        // Same Tier and same Booking Date
        return 0;
    }

    public void reorganizePriority() {

        for (int i = 1; i <= bookingList.getNumberOfEntries(); i++) {

            for (int j = i + 1; j <= bookingList.getNumberOfEntries(); j++) {

                Booking bookingA = bookingList.getEntry(i);
                Booking bookingB = bookingList.getEntry(j);

                if (comparePriority(bookingA, bookingB) < 0) {
                    bookingList.swap(i, j);
                }
            }
        }
    }

    private Room findVacantRoom() {

        for (int i = 1; i <= roomList.getNumberOfEntries(); i++) {

            Room room = roomList.getEntry(i);

            if (room.getRoomStatus().equals("Vacant")) {
                return room;
            }
        }

        return null;
    }

    public boolean allocateRoom() {
        Booking booking = bookingList.getEntry(1);

        if (booking.getRoom() == null) {
            Room vacantRoom = findVacantRoom();

            if (vacantRoom != null) {
                booking.setRoom(vacantRoom);
                vacantRoom.setRoomStatus("Occupied");
                return true;
            }
        }
        return false;
    }

    //to make sure non stop room assignment for other tiers.
    //note: Higher loyalty tier guests receive priority access to vacant rooms.
    public void allocateRooms() {

        for (int i = 1; i <= bookingList.getNumberOfEntries(); i++) {

            Booking booking = bookingList.getEntry(i);

            if (booking.getRoom() == null) {

                Room vacantRoom = findVacantRoom();

                if (vacantRoom != null) {
                    booking.setRoom(vacantRoom);
                    vacantRoom.setRoomStatus("Occupied");
                } else {
                    break;
                }
            }
        }
    }
}
//remider: allocation structure reorganizes itself automatically upon new insertions