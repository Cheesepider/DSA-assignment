/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package control;

/**
 *
 * @author jlohz
 */
import entity.Booking;
import entity.Member;
import entity.Room;
import main.App;
import adt.ListInterface;

public class PriorityAllocationControl {

    public int comparePriority(Booking bookingA, Booking bookingB) {

        int tierA = bookingA.getMember().getLoyaltyTier().ordinal();
        int tierB = bookingB.getMember().getLoyaltyTier().ordinal();

        // First priority: Loyalty Tier
        if (tierA > tierB) {
            return 1;
        } else if (tierA < tierB) {
            return -1;
        }

        // Second priority: Earlier Registration Time
        if (bookingA.getRegistrationTime().isBefore(
                bookingB.getRegistrationTime())) {
            return 1;
        } else if (bookingA.getRegistrationTime().isAfter(
                bookingB.getRegistrationTime())) {
            return -1;
        }

        return 0;
    }

    public void reorganizePriority() {

        ListInterface<Booking> queue = App.bookingRequestsQueue;

        for (int i = 1; i <= queue.getNumberOfEntries(); i++) {

            for (int j = i + 1; j <= queue.getNumberOfEntries(); j++) {

                Booking bookingA = queue.getEntry(i);
                Booking bookingB = queue.getEntry(j);

                if (comparePriority(bookingA, bookingB) < 0) {

                    queue.replace(i, bookingB);
                    queue.replace(j, bookingA);
                }
            }
        }
    }

    private Room findAvailableRoom(Booking request) {

        Room.RoomType requestedType = request.getRoom().getRoomType();

        for (int i = 1; i <= App.roomList.getNumberOfEntries(); i++) {

            Room room = App.roomList.getEntry(i);

            if (room.getRoomType() != requestedType) {
                continue;
            }

            boolean hasOverlap = false;

            for (int j = 1; j <= App.bookingList.getNumberOfEntries(); j++) {

                Booking existingBooking = App.bookingList.getEntry(j);

                if (existingBooking.getRoom() != null
                        && existingBooking.getRoom().getRoomID() == room.getRoomID()
                        && existingBooking.getBookingStatus()
                        != Booking.BookingStatus.CANCELLED) {

                    boolean overlap
                            = request.getCheckInDate()
                                    .isBefore(existingBooking.getCheckOutDate())
                            && request.getCheckOutDate()
                                    .isAfter(existingBooking.getCheckInDate());

                    if (overlap) {
                        hasOverlap = true;
                        break;
                    }
                }
            }
            if (!hasOverlap) {
                return room;
            }
        }
        return null;
    }

    public Booking allocateNextRoom() {

        reorganizePriority();

        for (int i = 1;
                i <= App.bookingRequestsQueue.getNumberOfEntries();
                i++) {

            Booking request = App.bookingRequestsQueue.getEntry(i);

            Room availableRoom = findAvailableRoom(request);

            if (availableRoom != null) {

                request.setRoom(availableRoom);
                request.setBookingStatus(
                        Booking.BookingStatus.CONFIRMED);

                App.bookingList.add(request);
                App.bookingRequestsQueue.remove(i);

                return request;
            }
        }

        return null;
    }

    //Let UI see/refer Priority waiting list
    public ListInterface<Booking> getWaitingList() {
        return App.bookingRequestsQueue;
    }

    //to search 2 types, the confirmed bookings and the bookings that requested but still waiting.
    public Booking findBookingByID(int bookingID) {

        // Search confirmed bookings
        for (int i = 1; i <= App.bookingList.getNumberOfEntries(); i++) {

            Booking booking = App.bookingList.getEntry(i);

            if (booking.getBookingID() == bookingID) {
                return booking;
            }
        }

        // Search priority waiting list
        for (int i = 1; i <= App.bookingRequestsQueue.getNumberOfEntries(); i++) {

            Booking booking = App.bookingRequestsQueue.getEntry(i);

            if (booking.getBookingID() == bookingID) {
                return booking;
            }
        }

        return null;
    }

    //Below are counters for displaying in report as summary before displaying table
    ////////////////////////////////////////////////////////////////////////////
    /// @return 
    public int countWaitingBookings() {
        return App.bookingRequestsQueue.getNumberOfEntries();
    }

    public int countConfirmedBookings() {
        return App.bookingList.getNumberOfEntries();
    }

    public int countWaitingBookingsByTier(Member.LoyaltyTier tier) {

        int count = 0;

        for (int i = 1;
                i <= App.bookingRequestsQueue.getNumberOfEntries();
                i++) {

            Booking booking
                    = App.bookingRequestsQueue.getEntry(i);

            if (booking.getMember().getLoyaltyTier() == tier) {
                count++;
            }
        }

        return count;
    }

    public int countRoomsByType(Room.RoomType type) {

        int count = 0;

        for (int i = 1;
                i <= App.roomList.getNumberOfEntries();
                i++) {

            Room room = App.roomList.getEntry(i);

            if (room.getRoomType() == type) {
                count++;
            }
        }

        return count;
    }
}
