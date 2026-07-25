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
                    LocalDateTime.now().minusHours(5),
                    LocalDate.now().plusDays(3),
                    LocalDate.now().plusDays(5),
                    members[3],
                    null
            ),

            new Booking(
                    "B002",
                    LocalDateTime.now().minusHours(4),
                    LocalDate.now().plusDays(2),
                    LocalDate.now().plusDays(4),
                    members[2],
                    null
            ),

            new Booking(
                    "B003",
                    LocalDateTime.now().minusHours(3),
                    LocalDate.now().plusDays(5),
                    LocalDate.now().plusDays(7),
                    members[1],
                    null
            ),

            new Booking(
                    "B004",
                    LocalDateTime.now().minusHours(2),
                    LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(3),
                    members[0],
                    null
            ),

            new Booking(
                    "B005",
                    LocalDateTime.now().minusHours(1),
                    LocalDate.now().plusDays(4),
                    LocalDate.now().plusDays(6),
                    members[4],
                    null
            ),

            new Booking(
                    "B006",
                    LocalDateTime.now().minusMinutes(50),
                    LocalDate.now().plusDays(6),
                    LocalDate.now().plusDays(8),
                    members[1],
                    null
            ),

            new Booking(
                    "B007",
                    LocalDateTime.now().minusMinutes(40),
                    LocalDate.now().plusDays(7),
                    LocalDate.now().plusDays(9),
                    members[2],
                    null
            ),

            new Booking(
                    "B008",
                    LocalDateTime.now().minusMinutes(30),
                    LocalDate.now().plusDays(2),
                    LocalDate.now().plusDays(5),
                    members[3],
                    null
            ),

            new Booking(
                    "B009",
                    LocalDateTime.now().minusMinutes(20),
                    LocalDate.now().plusDays(3),
                    LocalDate.now().plusDays(6),
                    members[0],
                    null
            ),

            new Booking(
                    "B010",
                    LocalDateTime.now().minusMinutes(10),
                    LocalDate.now().plusDays(5),
                    LocalDate.now().plusDays(8),
                    members[4],
                    null
            )
        };
    }
}
