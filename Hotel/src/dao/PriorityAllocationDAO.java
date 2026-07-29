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

public class PriorityAllocationDAO {

    public Member[] getMembers() {

        return new Member[]{
            new Member("M001", "Alice", "0123456789",
            "alice@email.com", "Elite", 1000),
            new Member("M002", "Bob", "0123456788",
            "bob@email.com", "Diamond", 800),
            new Member("M003", "Charlie", "0123456787",
            "charlie@email.com", "Platinum", 500),
            new Member("M004", "David", "0123456786",
            "david@email.com", "Standard", 100),
            new Member("M005", "Emma", "0123456785",
            "emma@email.com", "Elite", 1200)
        };
    }

    public Room[] getRooms() {

        return new Room[]{
            new Room("R001", "Standard", "Vacant"),
            new Room("R002", "Deluxe", "Vacant"),
            new Room("R003", "Suite", "Vacant"),
            new Room("R004", "Standard", "Vacant"),
            new Room("R005", "Deluxe", "Vacant")
        };
    }

    public Booking[] getBookings(Member[] members) {

        return new Booking[]{
            new Booking(
            "B001",
            LocalDate.of(2026, 7, 20), // Booking Date
            LocalDate.of(2026, 8, 1), // Check-In
            LocalDate.of(2026, 8, 3), // Check-Out
            LocalDateTime.of(2026, 7, 20, 9, 0), // Registration Time
            members[3],
            null
            ),
            new Booking(
            "B002",
            LocalDate.of(2026, 7, 15), // Booking Date
            LocalDate.of(2026, 8, 1), // Check-In
            LocalDate.of(2026, 8, 3), // Check-Out
            LocalDateTime.of(2026, 7, 15, 9, 0), // Registration Time
            members[2],
            null
            ),
            new Booking(
            "B003",
            LocalDate.of(2026, 7, 21), // Booking Date
            LocalDate.of(2026, 8, 1), // Check-In
            LocalDate.of(2026, 8, 3), // Check-Out
            LocalDateTime.of(2026, 7, 21, 9, 0), // Registration Time
            members[1],
            null
            ),
            new Booking(
            "B004",
            LocalDate.of(2026, 7, 21), // Booking Date
            LocalDate.of(2026, 8, 1), // Check-In
            LocalDate.of(2026, 8, 3), // Check-Out
            LocalDateTime.of(2026, 7, 21, 9, 30), // Registration Time
            members[0],
            null
            ),
            new Booking(
            "B005",
            LocalDate.of(2026, 7, 14), // Booking Date
            LocalDate.of(2026, 8, 1), // Check-In
            LocalDate.of(2026, 8, 3), // Check-Out
            LocalDateTime.of(2026, 7, 14, 21, 15), // Registration Time
            members[4],
            null
            ),
            new Booking(
            "B006",
            LocalDate.of(2026, 7, 22), // Booking Date
            LocalDate.of(2026, 8, 1), // Check-In
            LocalDate.of(2026, 8, 3), // Check-Out
            LocalDateTime.of(2026, 7, 22, 19, 0), // Registration Time
            members[1],
            null
            ),
            new Booking(
            "B007",
            LocalDate.of(2026, 7, 15), // Booking Date
            LocalDate.of(2026, 8, 1), // Check-In
            LocalDate.of(2026, 8, 3), // Check-Out
            LocalDateTime.of(2026, 7, 15, 8, 0), // Registration Time
            members[2],
            null
            ),
            new Booking(
            "B008",
            LocalDate.of(2026, 7, 17), // Booking Date
            LocalDate.of(2026, 8, 1), // Check-In
            LocalDate.of(2026, 8, 3), // Check-Out
            LocalDateTime.of(2026, 7, 17, 23, 49), // Registration Time
            members[3],
            null
            ),
            new Booking(
            "B009",
            LocalDate.of(2026, 7, 19), // Booking Date
            LocalDate.of(2026, 8, 1), // Check-In
            LocalDate.of(2026, 8, 3), // Check-Out
            LocalDateTime.of(2026, 7, 19, 10, 4), // Registration Time
            members[0],
            null
            ),
            new Booking(
            "B010",
            LocalDate.of(2026, 7, 25), // Booking Date
            LocalDate.of(2026, 8, 1), // Check-In
            LocalDate.of(2026, 8, 3), // Check-Out
            LocalDateTime.of(2026, 7, 25, 17, 0), // Registration Time
            members[4],
            null
            )
        };
    }
}
