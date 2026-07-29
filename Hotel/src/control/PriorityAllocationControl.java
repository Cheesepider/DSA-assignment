/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package control;

import adt.DoublyLinkedList;
import adt.ListInterface;
import entity.Booking;
import entity.Room;
import entity.Member;
import dao.PriorityAllocationDAO;

/**
 *
 * @author jlohz
 */
public class PriorityAllocationControl {

    private DoublyLinkedList<Booking> bookingList;
    private DoublyLinkedList<Room> roomList;
    private DoublyLinkedList<Member> memberList;

    public PriorityAllocationControl() {
        bookingList = new DoublyLinkedList<>();
        roomList = new DoublyLinkedList<>();
        memberList = new DoublyLinkedList<>();

        PriorityAllocationDAO dao = new PriorityAllocationDAO();

        // Load Members
        Member[] members = dao.getMembers();

        for (Member member : members) {
            addMember(member);
        }

        // Load Rooms
        Room[] rooms = dao.getRooms();

        for (Room room : rooms) {
            addRoom(room);
        }

        // Load Bookings
        Booking[] bookings = dao.getBookings(members);

        for (Booking booking : bookings) {
            addBooking(booking);
        }
    }

    public void addMember(Member newMember) {
        memberList.add(newMember);
    }

    public void addBooking(Booking newBooking) {
        bookingList.add(newBooking);
        reorganizePriority();
    }

    public void addRoom(Room newRoom) {
        roomList.add(newRoom);
    }

    public ListInterface<Booking> getBookingList() {
        return bookingList;
    }

    public ListInterface<Room> getRoomList() {
        return roomList;
    }

    public Member findMemberByID(String memberID) {

        for (int i = 1; i <= memberList.getNumberOfEntries(); i++) {

            Member member = memberList.getEntry(i);

            if (member.getMemberID().equals(memberID)) {
                return member;
            }
        }

        return null;
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

    public int comparePriority(Booking bookingA, Booking bookingB) {

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
    
    //allocation structure reorganizes itself automatically upon new insertions
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

    public Room allocateNextRoom() {

        for (int i = 1; i <= bookingList.getNumberOfEntries(); i++) {

            Booking booking = bookingList.getEntry(i);

            // Because the list is already sorted by priority,
            // the first unallocated booking has the highest priority.
            if (booking.getRoom() == null) {

                Room vacantRoom = findVacantRoom();

                if (vacantRoom != null) {

                    booking.setRoom(vacantRoom);
                    vacantRoom.setRoomStatus("Occupied");

                    return vacantRoom;
                }

                return null;
            }
        }

        return null;
    }

    public Booking findBookingByID(String bookingID) {

        for (int i = 1; i <= bookingList.getNumberOfEntries(); i++) {

            Booking booking = bookingList.getEntry(i);

            if (booking.getBookingID().equals(bookingID)) {
                return booking;
            }
        }

        return null;
    }
}

