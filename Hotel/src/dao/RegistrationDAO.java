/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import entity.Member;
import entity.Member.LoyaltyTier;
import entity.Room;
import entity.Room.RoomStatus;
import entity.Room.RoomType;
import entity.Booking;
import java.time.LocalDate;
import java.time.LocalDateTime;
import main.App;

public class RegistrationDAO {

    // Data initialization happens here.
    // Populate App's global lists with seed data for demo/testing.
    public static void initializeData() {
        initializeMemberData();
        initializeRoomData();
        initializeBookingData();
    }

    // -------------------------
    // ROOMS
    // -------------------------
    public static void initializeRoomData() {
        // Floor 1 - Single rooms (base rate $100/night)
        App.roomList.add(new Room("101", RoomType.SINGLE, RoomStatus.Ready_for_Check_In));
        App.roomList.add(new Room("102", RoomType.SINGLE, RoomStatus.Ready_for_Check_In));
        App.roomList.add(new Room("103", RoomType.SINGLE, RoomStatus.Dirty));
        App.roomList.add(new Room("104", RoomType.SINGLE, RoomStatus.Dirty));

        // Floor 2 - Double rooms (base rate $150/night)
        App.roomList.add(new Room("201", RoomType.DOUBLE, RoomStatus.Ready_for_Check_In));
        App.roomList.add(new Room("202", RoomType.DOUBLE, RoomStatus.Ready_for_Check_In));
        App.roomList.add(new Room("203", RoomType.DOUBLE, RoomStatus.Dirty));

        // Floor 3 - Suites (base rate $300/night)
        App.roomList.add(new Room("301", RoomType.SUITE, RoomStatus.Ready_for_Check_In));
        App.roomList.add(new Room("302", RoomType.SUITE, RoomStatus.Dirty));

        System.out.println("[DAO] " + App.roomList.getNumberOfEntries() + " rooms loaded.");
    }

    // -------------------------
    // MEMBERS
    // -------------------------
    public static void initializeMemberData() {
        // Regular tier members
        App.memberList.add(new Member("Alice Tan", "91234567", "alice@mail.com", LoyaltyTier.Regular, 0));
        App.memberList.add(new Member("Bob Lim", "87654321", "bob@mail.com", LoyaltyTier.Regular, 500));

        // Platinum tier members
        App.memberList.add(new Member("Carol Wong", "93456789", "carol@mail.com", LoyaltyTier.Platinum, 1200));
        App.memberList.add(new Member("David Ng", "81234567", "david@mail.com", LoyaltyTier.Platinum, 2350));

        // Diamond tier members
        App.memberList.add(new Member("Eve Chua", "99887766", "eve@mail.com", LoyaltyTier.Diamond, 3800));

        // Elite tier member
        App.memberList.add(new Member("Frank Ho", "98765432", "frank@mail.com", LoyaltyTier.Elite, 6000));

        System.out.println("[DAO] " + App.memberList.getNumberOfEntries() + " members loaded.");
    }

    // -------------------------
    // BOOKINGS
    // -------------------------
    public static void initializeBookingData() {  //* @author Jerry

        // Existing shared members
        Member alice = App.memberList.getEntry(1);  // Regular
        Member bob = App.memberList.getEntry(2);    // Regular
        Member carol = App.memberList.getEntry(3);  // Platinum
        Member david = App.memberList.getEntry(4);  // Platinum
        Member eve = App.memberList.getEntry(5);    // Diamond
        Member frank = App.memberList.getEntry(6);  // Elite

        // Existing shared rooms
        Room room101 = App.roomList.getEntry(1);    // SINGLE
        Room room102 = App.roomList.getEntry(2);    // SINGLE
        Room room103 = App.roomList.getEntry(3);    // SINGLE
        Room room104 = App.roomList.getEntry(4);    // SINGLE

        Room room201 = App.roomList.getEntry(5);    // DOUBLE
        Room room202 = App.roomList.getEntry(6);    // DOUBLE
        Room room203 = App.roomList.getEntry(7);    // DOUBLE

        Room room301 = App.roomList.getEntry(8);    // SUITE
        Room room302 = App.roomList.getEntry(9);    // SUITE

        // =========================================================
        // 12 CONFIRMED BOOKINGS
        // =========================================================
        App.bookingList.add(new Booking(
                LocalDate.of(2026, 8, 18),
                LocalDate.of(2026, 8, 20),
                LocalDate.of(2026, 8, 22),
                LocalDateTime.of(2026, 8, 18, 8, 0),
                Booking.BookingStatus.CONFIRMED,
                alice,
                room101
        ));

        App.bookingList.add(new Booking(
                LocalDate.of(2026, 8, 18),
                LocalDate.of(2026, 8, 20),
                LocalDate.of(2026, 8, 22),
                LocalDateTime.of(2026, 8, 18, 8, 10),
                Booking.BookingStatus.CONFIRMED,
                bob,
                room102
        ));

        App.bookingList.add(new Booking(
                LocalDate.of(2026, 8, 18),
                LocalDate.of(2026, 8, 20),
                LocalDate.of(2026, 8, 22),
                LocalDateTime.of(2026, 8, 18, 8, 20),
                Booking.BookingStatus.CONFIRMED,
                carol,
                room103
        ));

        App.bookingList.add(new Booking(
                LocalDate.of(2026, 8, 18),
                LocalDate.of(2026, 8, 20),
                LocalDate.of(2026, 8, 22),
                LocalDateTime.of(2026, 8, 18, 8, 30),
                Booking.BookingStatus.CONFIRMED,
                david,
                room104
        ));

        App.bookingList.add(new Booking(
                LocalDate.of(2026, 8, 18),
                LocalDate.of(2026, 8, 20),
                LocalDate.of(2026, 8, 22),
                LocalDateTime.of(2026, 8, 18, 8, 40),
                Booking.BookingStatus.CONFIRMED,
                eve,
                room201
        ));

        App.bookingList.add(new Booking(
                LocalDate.of(2026, 8, 18),
                LocalDate.of(2026, 8, 20),
                LocalDate.of(2026, 8, 22),
                LocalDateTime.of(2026, 8, 18, 8, 50),
                Booking.BookingStatus.CONFIRMED,
                frank,
                room202
        ));

        App.bookingList.add(new Booking(
                LocalDate.of(2026, 8, 18),
                LocalDate.of(2026, 8, 20),
                LocalDate.of(2026, 8, 22),
                LocalDateTime.of(2026, 8, 18, 9, 0),
                Booking.BookingStatus.CONFIRMED,
                alice,
                room203
        ));

        App.bookingList.add(new Booking(
                LocalDate.of(2026, 8, 18),
                LocalDate.of(2026, 8, 20),
                LocalDate.of(2026, 8, 22),
                LocalDateTime.of(2026, 8, 18, 9, 10),
                Booking.BookingStatus.CONFIRMED,
                bob,
                room301
        ));

        App.bookingList.add(new Booking(
                LocalDate.of(2026, 8, 18),
                LocalDate.of(2026, 8, 20),
                LocalDate.of(2026, 8, 22),
                LocalDateTime.of(2026, 8, 18, 9, 20),
                Booking.BookingStatus.CONFIRMED,
                carol,
                room302
        ));

        // Later non-overlapping bookings
        App.bookingList.add(new Booking(
                LocalDate.of(2026, 8, 19),
                LocalDate.of(2026, 8, 23),
                LocalDate.of(2026, 8, 25),
                LocalDateTime.of(2026, 8, 19, 9, 30),
                Booking.BookingStatus.CONFIRMED,
                david,
                room101
        ));

        App.bookingList.add(new Booking(
                LocalDate.of(2026, 8, 19),
                LocalDate.of(2026, 8, 23),
                LocalDate.of(2026, 8, 25),
                LocalDateTime.of(2026, 8, 19, 9, 40),
                Booking.BookingStatus.CONFIRMED,
                eve,
                room201
        ));

        App.bookingList.add(new Booking(
                LocalDate.of(2026, 8, 19),
                LocalDate.of(2026, 8, 23),
                LocalDate.of(2026, 8, 25),
                LocalDateTime.of(2026, 8, 19, 9, 50),
                Booking.BookingStatus.CONFIRMED,
                frank,
                room301
        ));

        // =========================================================
        // 8 PRIORITY WAITING REQUESTS
        // =========================================================
        App.bookingRequestsQueue.add(new Booking(
                LocalDate.of(2026, 8, 19),
                LocalDate.of(2026, 8, 20),
                LocalDate.of(2026, 8, 22),
                LocalDateTime.of(2026, 8, 19, 10, 0),
                Booking.BookingStatus.PENDING,
                alice,
                new Room("WAITLIST", RoomType.SINGLE)
        ));

        App.bookingRequestsQueue.add(new Booking(
                LocalDate.of(2026, 8, 19),
                LocalDate.of(2026, 8, 20),
                LocalDate.of(2026, 8, 22),
                LocalDateTime.of(2026, 8, 19, 10, 5),
                Booking.BookingStatus.PENDING,
                bob,
                new Room("WAITLIST", RoomType.DOUBLE)
        ));

        App.bookingRequestsQueue.add(new Booking(
                LocalDate.of(2026, 8, 19),
                LocalDate.of(2026, 8, 20),
                LocalDate.of(2026, 8, 22),
                LocalDateTime.of(2026, 8, 19, 10, 10),
                Booking.BookingStatus.PENDING,
                carol,
                new Room("WAITLIST", RoomType.SUITE)
        ));

        App.bookingRequestsQueue.add(new Booking(
                LocalDate.of(2026, 8, 19),
                LocalDate.of(2026, 8, 20),
                LocalDate.of(2026, 8, 22),
                LocalDateTime.of(2026, 8, 19, 10, 15),
                Booking.BookingStatus.PENDING,
                david,
                new Room("WAITLIST", RoomType.SINGLE)
        ));

        App.bookingRequestsQueue.add(new Booking(
                LocalDate.of(2026, 8, 19),
                LocalDate.of(2026, 8, 20),
                LocalDate.of(2026, 8, 22),
                LocalDateTime.of(2026, 8, 19, 10, 20),
                Booking.BookingStatus.PENDING,
                eve,
                new Room("WAITLIST", RoomType.DOUBLE)
        ));

        App.bookingRequestsQueue.add(new Booking(
                LocalDate.of(2026, 8, 19),
                LocalDate.of(2026, 8, 20),
                LocalDate.of(2026, 8, 22),
                LocalDateTime.of(2026, 8, 19, 10, 25),
                Booking.BookingStatus.PENDING,
                frank,
                new Room("WAITLIST", RoomType.SUITE)
        ));

        App.bookingRequestsQueue.add(new Booking(
                LocalDate.of(2026, 8, 19),
                LocalDate.of(2026, 8, 20),
                LocalDate.of(2026, 8, 22),
                LocalDateTime.of(2026, 8, 19, 10, 30),
                Booking.BookingStatus.PENDING,
                carol,
                new Room("WAITLIST", RoomType.DOUBLE)
        ));

        App.bookingRequestsQueue.add(new Booking(
                LocalDate.of(2026, 8, 19),
                LocalDate.of(2026, 8, 20),
                LocalDate.of(2026, 8, 22),
                LocalDateTime.of(2026, 8, 19, 10, 35),
                Booking.BookingStatus.PENDING,
                frank,
                new Room("WAITLIST", RoomType.SINGLE)
        ));

        System.out.println(
                "[DAO] " + App.bookingList.getNumberOfEntries()
                + " confirmed bookings loaded.");

        System.out.println(
                "[DAO] " + App.bookingRequestsQueue.getNumberOfEntries()
                + " priority waiting requests loaded.");
    }
}
