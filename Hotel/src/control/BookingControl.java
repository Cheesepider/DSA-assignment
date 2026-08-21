/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package control;

/**
 * @author Tham Cle Ment
 */

import entity.Member;
import entity.Booking;
import entity.Room;
import entity.Room.RoomType;
import main.App;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import adt.DoublyLinkedList;
import adt.ListInterface;
import utility.VirtualClock;

public class BookingControl {

    public static Member loginMember() {
        if (App.memberList == null || App.memberList.isEmpty()) {
            System.out.println("No members registered in the system.");
            return null;
        }

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Enter member name or phone number to search (or enter empty to show all, or 'exit' to cancel): ");
            String query = scanner.nextLine().trim();

            if (query.equalsIgnoreCase("exit")) {
                System.out.println("Login cancelled.");
                return null;
            }

            ListInterface<Member> matches = new DoublyLinkedList<>();
            for (int i = 1; i <= App.memberList.getNumberOfEntries(); i++) {
                Member m = App.memberList.getEntry(i);
                if (query.isEmpty() || 
                    (m.getMemberName() != null && m.getMemberName().toLowerCase().contains(query.toLowerCase())) || 
                    (m.getPhoneNumber() != null && m.getPhoneNumber().contains(query))) {
                    matches.add(m);
                }
            }

            if (matches.isEmpty()) {
                System.out.println("No matching members found. Please try again.");
                continue;
            }

            if (matches.getNumberOfEntries() == 1) {
                Member selected = matches.getEntry(1);
                System.out.println("Found 1 matching member: " + selected.getMemberName() + " (ID: " + selected.getMemberID() + ")");
                System.out.print("Confirm login as this member? (Y/N): ");
                String confirm = scanner.nextLine().trim();
                if (confirm.equalsIgnoreCase("y")) {
                    System.out.println("Successfully logged in as: " + selected.getMemberName());
                    return selected;
                } else {
                    continue;
                }
            }

            // Multiple matches
            System.out.println("Matches found:");
            for (int i = 1; i <= matches.getNumberOfEntries(); i++) {
                Member m = matches.getEntry(i);
                System.out.println(i + ". " + m.getMemberName() + " (ID: " + m.getMemberID() + ", Phone: " + m.getPhoneNumber() + ")");
            }
            System.out.print("Select a member (1-" + matches.getNumberOfEntries() + ") or 0 to search again: ");
            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                if (choice == 0) {
                    continue;
                }
                if (choice >= 1 && choice <= matches.getNumberOfEntries()) {
                    Member selected = matches.getEntry(choice);
                    System.out.println("Successfully logged in as: " + selected.getMemberName());
                    return selected;
                } else {
                    System.out.println("Invalid selection.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input.");
            }
        }
    }

    public static void makeBooking(Member member) {
        if (member == null) {
            System.out.println("Please log in first before making a booking.");
            return;
        }

        Scanner scanner = new Scanner(System.in);
        LocalDate checkInDate = null;
        LocalDate checkOutDate = null;

        // 1. Get dates
        while (checkInDate == null) {
            System.out.print("Enter Check-In Date (YYYY-MM-DD) or type 'cancel' to abort: ");
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("cancel")) {
                System.out.println("Booking aborted. Returning to menu.");
                return;
            }
            try {
                checkInDate = LocalDate.parse(input);
                if (checkInDate.isBefore(VirtualClock.getInstance().today())) {
                    System.out.println("Check-in date cannot be in the past. (Today is " + VirtualClock.getInstance().today() + ")");
                    checkInDate = null;
                }
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please use YYYY-MM-DD.");
            }
        }

        while (checkOutDate == null) {
            System.out.print("Enter Check-Out Date (YYYY-MM-DD) or type 'cancel' to abort: ");
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("cancel")) {
                System.out.println("Booking aborted. Returning to menu.");
                return;
            }
            try {
                checkOutDate = LocalDate.parse(input);
                if (!checkOutDate.isAfter(checkInDate)) {
                    System.out.println("Check-out date must be after check-in date.");
                    checkOutDate = null;
                }
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please use YYYY-MM-DD.");
            }
        }

        // 2. Select Room Type
        RoomType selectedType = null;
        while (selectedType == null) {
            System.out.println("Select Room Type:");
            System.out.println("1. Single (Rate: $100.0/night)");
            System.out.println("2. Double (Rate: $150.0/night)");
            System.out.println("3. Suite (Rate: $300.0/night)");
            System.out.print("Choice: ");
            System.out.print("Choice (or type 'cancel' to abort): ");
            String choice = scanner.nextLine().trim();
            if (choice.equalsIgnoreCase("cancel")) {
                System.out.println("Booking aborted. Returning to menu.");
                return;
            }
            if (choice.equals("1")) selectedType = RoomType.SINGLE;
            else if (choice.equals("2")) selectedType = RoomType.DOUBLE;
            else if (choice.equals("3")) selectedType = RoomType.SUITE;
            else System.out.println("Invalid choice. Please select 1, 2, or 3.");
        }

        // 3. Find available rooms of selected type
        ListInterface<Room> availableRooms = new DoublyLinkedList<>();
        for (int i = 1; i <= App.roomList.getNumberOfEntries(); i++) {
            Room r = App.roomList.getEntry(i);
            if (r.getRoomType() == selectedType) {
                // Check if this room has overlapping bookings
                boolean hasOverlap = false;
                for (int j = 1; j <= App.bookingList.getNumberOfEntries(); j++) {
                    Booking b = App.bookingList.getEntry(j);
                    if (b.getRoom() != null && b.getRoom().getRoomID() == r.getRoomID() && 
                        b.getBookingStatus() != Booking.BookingStatus.CANCELLED) {
                        // Check overlap
                        if (checkInDate.isBefore(b.getCheckOutDate()) && checkOutDate.isAfter(b.getCheckInDate())) {
                            hasOverlap = true;
                            break;
                        }
                    }
                }
                if (!hasOverlap) {
                    availableRooms.add(r);
                }
            }
        }

        // 4. Handle results
        if (availableRooms.isEmpty()) {
            System.out.println("No " + selectedType + " rooms available for the selected dates.");
            System.out.print("Would you like to join the priority waiting list for a " + selectedType + " room? (Y/N): ");
            String waitlistChoice = scanner.nextLine().trim();
            if (waitlistChoice.equalsIgnoreCase("y")) {
                Booking req = new Booking(
                    VirtualClock.getInstance().today(),
                    checkInDate,
                    checkOutDate,
                    VirtualClock.getInstance().now(),
                    Booking.BookingStatus.PENDING,
                    member,
                    new Room("WAITLIST", selectedType) // dummy room to store requested room type
                );
                
                // Enqueue by loyalty tier priority
                int newMemberTier = member.getLoyaltyTier().ordinal();
                int position = 1;
                int totalEntries = App.bookingRequestsQueue.getNumberOfEntries();
                while (position <= totalEntries) {
                    Booking existing = App.bookingRequestsQueue.getEntry(position);
                    int existingMemberTier = existing.getMember().getLoyaltyTier().ordinal();
                    if (newMemberTier > existingMemberTier) {
                        break;
                    }
                    position++;
                }
                App.bookingRequestsQueue.add(position, req);
                System.out.println("Booking request successfully added to the priority queue at position: " + position);
            } else {
                System.out.println("Booking cancelled.");
            }
        } else {
            Room selectedRoom = availableRooms.getEntry(1); // Auto-assign the first available
            Booking booking = new Booking(
                VirtualClock.getInstance().today(),
                checkInDate,
                checkOutDate,
                VirtualClock.getInstance().now(),
                Booking.BookingStatus.CONFIRMED,
                member,
                selectedRoom
            );
            App.bookingList.add(booking);
            System.out.println("Booking successfully created! Booking ID: " + booking.getBookingID() + ", Auto-assigned Room: " + selectedRoom.getRoomNumber() + " (" + selectedType + ")");
        }
    }

    public static void viewBookings(Member member) {
        if (member == null) {
            System.out.println("Please log in first.");
            return;
        }

        System.out.println("\n--- Active Bookings for " + member.getMemberName() + " ---");
        ListInterface<Booking> activeBookings = new DoublyLinkedList<>();
        for (int i = 1; i <= App.bookingList.getNumberOfEntries(); i++) {
            Booking b = App.bookingList.getEntry(i);
            if (b.getMember().getMemberID() == member.getMemberID()) {
                activeBookings.add(b);
            }
        }
        if (activeBookings.isEmpty()) {
            System.out.println("No confirmed bookings found.");
        } else {
            System.out.printf("%-10s | %-10s | %-12s | %-14s | %-14s | %-15s\n", "Booking ID", "Room No.", "Room Type", "Check-In Date", "Check-Out Date", "Status");
            System.out.println("------------------------------------------------------------------------------------------");
            for (int i = 1; i <= activeBookings.getNumberOfEntries(); i++) {
                Booking b = activeBookings.getEntry(i);
                System.out.printf("%-10d | %-10s | %-12s | %-14s | %-14s | %-15s\n",
                        b.getBookingID(),
                        (b.getRoom() != null ? b.getRoom().getRoomNumber() : "N/A"),
                        (b.getRoom() != null && b.getRoom().getRoomType() != null ? b.getRoom().getRoomType().toString() : "N/A"),
                        b.getCheckInDate(),
                        b.getCheckOutDate(),
                        b.getBookingStatus());
            }
            System.out.println("------------------------------------------------------------------------------------------");
        }

        System.out.println("\n--- Past Bookings for " + member.getMemberName() + " ---");
        ListInterface<Booking> pastBookings = new DoublyLinkedList<>();
        for (int i = 1; i <= App.bookingHistoryList.getNumberOfEntries(); i++) {
            Booking b = App.bookingHistoryList.getEntry(i);
            if (b.getMember().getMemberID() == member.getMemberID()) {
                pastBookings.add(b);
            }
        }
        if (pastBookings.isEmpty()) {
            System.out.println("No past bookings found.");
        } else {
            System.out.printf("%-10s | %-10s | %-12s | %-14s | %-14s | %-15s\n", "Booking ID", "Room No.", "Room Type", "Check-In Date", "Check-Out Date", "Status");
            System.out.println("------------------------------------------------------------------------------------------");
            for (int i = 1; i <= pastBookings.getNumberOfEntries(); i++) {
                Booking b = pastBookings.getEntry(i);
                System.out.printf("%-10d | %-10s | %-12s | %-14s | %-14s | %-15s\n",
                        b.getBookingID(),
                        (b.getRoom() != null ? b.getRoom().getRoomNumber() : "N/A"),
                        (b.getRoom() != null && b.getRoom().getRoomType() != null ? b.getRoom().getRoomType().toString() : "N/A"),
                        b.getCheckInDate(),
                        b.getCheckOutDate(),
                        b.getBookingStatus());
            }
            System.out.println("------------------------------------------------------------------------------------------");
        }

        System.out.println("\n--- Booking Requests in Waiting List ---");
        boolean foundRequest = false;
        for (int i = 1; i <= App.bookingRequestsQueue.getNumberOfEntries(); i++) {
            Booking b = App.bookingRequestsQueue.getEntry(i);
            if (b.getMember().getMemberID() == member.getMemberID()) {
                if (!foundRequest) {
                    System.out.printf("%-10s | %-12s | %-14s | %-14s | %-15s\n", "Position", "Room Type", "Check-In Date", "Check-Out Date", "Status");
                    System.out.println("-----------------------------------------------------------------------------");
                    foundRequest = true;
                }
                System.out.printf("%-10d | %-12s | %-14s | %-14s | %-15s\n",
                        i,
                        (b.getRoom() != null && b.getRoom().getRoomType() != null ? b.getRoom().getRoomType().toString() : "N/A"),
                        b.getCheckInDate(),
                        b.getCheckOutDate(),
                        b.getBookingStatus());
            }
        }
        if (!foundRequest) {
            System.out.println("No pending booking requests found.");
        } else {
            System.out.println("-----------------------------------------------------------------------------");
        }
    }

    public static void cancelBooking(Member member) {
        if (member == null) {
            System.out.println("Please log in first.");
            return;
        }

        Scanner scanner = new Scanner(System.in);
        System.out.println("\n--- Your Active Bookings ---");
        ListInterface<Booking> activeBookings = new DoublyLinkedList<>();
        for (int i = 1; i <= App.bookingList.getNumberOfEntries(); i++) {
            Booking b = App.bookingList.getEntry(i);
            if (b.getMember().getMemberID() == member.getMemberID()) {
                activeBookings.add(b);
            }
        }
        if (activeBookings.isEmpty()) {
            System.out.println("No active bookings found.");
        } else {
            System.out.printf("%-10s | %-10s | %-12s | %-14s | %-14s | %-15s\n", "Booking ID", "Room No.", "Room Type", "Check-In Date", "Check-Out Date", "Status");
            System.out.println("------------------------------------------------------------------------------------------");
            for (int i = 1; i <= activeBookings.getNumberOfEntries(); i++) {
                Booking b = activeBookings.getEntry(i);
                System.out.printf("%-10d | %-10s | %-12s | %-14s | %-14s | %-15s\n",
                        b.getBookingID(),
                        (b.getRoom() != null ? b.getRoom().getRoomNumber() : "N/A"),
                        (b.getRoom() != null && b.getRoom().getRoomType() != null ? b.getRoom().getRoomType().toString() : "N/A"),
                        b.getCheckInDate(),
                        b.getCheckOutDate(),
                        b.getBookingStatus());
            }
            System.out.println("------------------------------------------------------------------------------------------");
        }

        System.out.println("\n--- Booking Requests in Waiting List ---");
        boolean foundRequest = false;
        for (int i = 1; i <= App.bookingRequestsQueue.getNumberOfEntries(); i++) {
            Booking b = App.bookingRequestsQueue.getEntry(i);
            if (b.getMember().getMemberID() == member.getMemberID()) {
                if (!foundRequest) {
                    System.out.printf("%-10s | %-12s | %-14s | %-14s | %-15s\n", "Position", "Room Type", "Check-In Date", "Check-Out Date", "Status");
                    System.out.println("-----------------------------------------------------------------------------");
                    foundRequest = true;
                }
                System.out.printf("%-10d | %-12s | %-14s | %-14s | %-15s\n",
                        i,
                        (b.getRoom() != null && b.getRoom().getRoomType() != null ? b.getRoom().getRoomType().toString() : "N/A"),
                        b.getCheckInDate(),
                        b.getCheckOutDate(),
                        b.getBookingStatus());
            }
        }
        if (!foundRequest) {
            System.out.println("No pending booking requests found.");
        } else {
            System.out.println("-----------------------------------------------------------------------------");
        }

        System.out.print("\nEnter the Booking ID or Waitlist Position to cancel (or 0 to exit): ");
        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());
            if (choice == 0) return;

            // First check if it's a confirmed booking
            Booking toCancel = null;
            for (int i = 1; i <= App.bookingList.getNumberOfEntries(); i++) {
                Booking b = App.bookingList.getEntry(i);
                if (b.getMember().getMemberID() == member.getMemberID() && b.getBookingID() == choice) {
                    toCancel = b;
                    break;
                }
            }

            if (toCancel != null) {
                if (toCancel.getBookingStatus() == Booking.BookingStatus.CANCELLED) {
                    System.out.println("Booking is already cancelled.");
                    return;
                }
                if (toCancel.getBookingStatus() == Booking.BookingStatus.CHECKED_IN) {
                    System.out.println("You cannot cancel a booking that is currently active. Please proceed to check-out instead.");
                    return;
                }
                
                // Remove from active list
                for (int i = 1; i <= App.bookingList.getNumberOfEntries(); i++) {
                    if (App.bookingList.getEntry(i).getBookingID() == toCancel.getBookingID()) {
                        App.bookingList.remove(i);
                        break;
                    }
                }
                
                toCancel.setBookingStatus(Booking.BookingStatus.CANCELLED);
                App.bookingHistoryList.add(toCancel);
                System.out.println("Booking ID " + toCancel.getBookingID() + " has been successfully cancelled and moved to history.");

                // Waitlist resolution
                Room freedRoom = toCancel.getRoom();
                if (freedRoom != null) {
                    RegistrationControl.resolveWaitlistForRoom(freedRoom);
                }
                return;
            }

            // Check if it's a waitlist position
            if (choice >= 1 && choice <= App.bookingRequestsQueue.getNumberOfEntries()) {
                Booking req = App.bookingRequestsQueue.getEntry(choice);
                if (req.getMember().getMemberID() == member.getMemberID()) {
                    App.bookingRequestsQueue.remove(choice);
                    System.out.println("Waitlist request at position " + choice + " has been cancelled.");
                    return;
                }
            }

            System.out.println("Invalid Booking ID or Waitlist Position.");

        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
        }
    }

    public static void updateBooking(Member member) {
        if (member == null) {
            System.out.println("Please log in first.");
            return;
        }

        Scanner scanner = new Scanner(System.in);
        ListInterface<Booking> activeBookings = new DoublyLinkedList<>();
        for (int i = 1; i <= App.bookingList.getNumberOfEntries(); i++) {
            Booking b = App.bookingList.getEntry(i);
            if (b.getMember().getMemberID() == member.getMemberID() && b.getBookingStatus() != Booking.BookingStatus.CANCELLED) {
                activeBookings.add(b);
            }
        }

        if (activeBookings.isEmpty()) {
            System.out.println("\n--- Your Active Bookings ---");
            System.out.println("You have no active bookings to update.");
            return;
        }

        System.out.println("\n--- Your Active Bookings ---");
        System.out.printf("%-10s | %-10s | %-12s | %-14s | %-14s | %-15s\n", "Booking ID", "Room No.", "Room Type", "Check-In Date", "Check-Out Date", "Status");
        System.out.println("------------------------------------------------------------------------------------------");
        for (int i = 1; i <= activeBookings.getNumberOfEntries(); i++) {
            Booking b = activeBookings.getEntry(i);
            System.out.printf("%-10d | %-10s | %-12s | %-14s | %-14s | %-15s\n",
                    b.getBookingID(),
                    (b.getRoom() != null ? b.getRoom().getRoomNumber() : "N/A"),
                    (b.getRoom() != null && b.getRoom().getRoomType() != null ? b.getRoom().getRoomType().toString() : "N/A"),
                    b.getCheckInDate(),
                    b.getCheckOutDate(),
                    b.getBookingStatus());
        }
        System.out.println("------------------------------------------------------------------------------------------");

        System.out.print("\nEnter the Booking ID to update (or 0 to exit): ");
        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());
            if (choice == 0) return;

            Booking toUpdate = null;
            for (int i = 1; i <= App.bookingList.getNumberOfEntries(); i++) {
                Booking b = App.bookingList.getEntry(i);
                if (b.getMember().getMemberID() == member.getMemberID() && b.getBookingID() == choice && b.getBookingStatus() != Booking.BookingStatus.CANCELLED) {
                    toUpdate = b;
                    break;
                }
            }

            if (toUpdate == null) {
                System.out.println("Invalid Booking ID.");
                return;
            }

            LocalDate newCheckIn = toUpdate.getCheckInDate();
            LocalDate newCheckOut = toUpdate.getCheckOutDate();
            RoomType newType = toUpdate.getRoom() != null ? toUpdate.getRoom().getRoomType() : null;

            if (toUpdate.getBookingStatus() == Booking.BookingStatus.CHECKED_IN) {
                System.out.println("Check-in date cannot be changed for checked-in bookings.");
            } else {
                System.out.print("Enter new Check-In Date (YYYY-MM-DD) or press Enter to keep [" + newCheckIn + "]: ");
                String inStr = scanner.nextLine().trim();
                if (!inStr.isEmpty()) {
                    try {
                        LocalDate d = LocalDate.parse(inStr);
                        if (d.isBefore(VirtualClock.getInstance().today())) {
                            System.out.println("Date cannot be in the past. Update cancelled.");
                            return;
                        }
                        newCheckIn = d;
                    } catch (Exception e) {
                        System.out.println("Invalid date. Update cancelled.");
                        return;
                    }
                }
            }

            System.out.print("Enter new Check-Out Date (YYYY-MM-DD) or press Enter to keep [" + newCheckOut + "]: ");
            String outStr = scanner.nextLine().trim();
            if (!outStr.isEmpty()) {
                try {
                    LocalDate d = LocalDate.parse(outStr);
                    if (!d.isAfter(newCheckIn)) {
                        System.out.println("Check-out must be after check-in. Update cancelled.");
                        return;
                    }
                    newCheckOut = d;
                } catch (Exception e) {
                    System.out.println("Invalid date. Update cancelled.");
                    return;
                }
            }

            if (toUpdate.getBookingStatus() == Booking.BookingStatus.CHECKED_IN) {
                System.out.println("Room type cannot be changed for checked-in bookings.");
            } else {
                System.out.println("Current Room Type: " + newType);
                System.out.println("1. Single\n2. Double\n3. Suite\nPress Enter to keep current type.");
                System.out.print("Choice: ");
                String typeStr = scanner.nextLine().trim();
                if (!typeStr.isEmpty()) {
                    if (typeStr.equals("1")) newType = RoomType.SINGLE;
                    else if (typeStr.equals("2")) newType = RoomType.DOUBLE;
                    else if (typeStr.equals("3")) newType = RoomType.SUITE;
                    else {
                        System.out.println("Invalid choice. Update cancelled.");
                        return;
                    }
                }
            }

            Room newRoom = null;
            if (toUpdate.getBookingStatus() == Booking.BookingStatus.CHECKED_IN) {
                Room currentRoom = toUpdate.getRoom();
                boolean hasOverlap = false;
                for (int j = 1; j <= App.bookingList.getNumberOfEntries(); j++) {
                    Booking b = App.bookingList.getEntry(j);
                    if (b.getBookingID() != toUpdate.getBookingID() && b.getRoom() != null && b.getRoom().getRoomID() == currentRoom.getRoomID() && b.getBookingStatus() != Booking.BookingStatus.CANCELLED) {
                        if (newCheckIn.isBefore(b.getCheckOutDate()) && newCheckOut.isAfter(b.getCheckInDate())) {
                            hasOverlap = true;
                            break;
                        }
                    }
                }
                if (!hasOverlap) {
                    newRoom = currentRoom;
                }
            } else {
                ListInterface<Room> availableRooms = new DoublyLinkedList<>();
                for (int i = 1; i <= App.roomList.getNumberOfEntries(); i++) {
                    Room r = App.roomList.getEntry(i);
                    if (r.getRoomType() == newType) {
                        boolean hasOverlap = false;
                        for (int j = 1; j <= App.bookingList.getNumberOfEntries(); j++) {
                            Booking b = App.bookingList.getEntry(j);
                            if (b.getBookingID() != toUpdate.getBookingID() && b.getRoom() != null && b.getRoom().getRoomID() == r.getRoomID() && b.getBookingStatus() != Booking.BookingStatus.CANCELLED) {
                                if (newCheckIn.isBefore(b.getCheckOutDate()) && newCheckOut.isAfter(b.getCheckInDate())) {
                                    hasOverlap = true;
                                    break;
                                }
                            }
                        }
                        if (!hasOverlap) availableRooms.add(r);
                    }
                }
                if (!availableRooms.isEmpty()) {
                    newRoom = availableRooms.getEntry(1);
                }
            }

            if (newRoom == null) {
                System.out.println("No rooms available for the new dates. Update failed, original booking retained.");
            } else {
                Room oldRoom = toUpdate.getRoom();
                toUpdate.setCheckInDate(newCheckIn);
                toUpdate.setCheckOutDate(newCheckOut);
                toUpdate.setRoom(newRoom);
                System.out.println("Booking updated successfully! Room: " + newRoom.getRoomNumber() + " (" + newType + ")");
                
                // If the update freed up the old room, attempt to resolve the waitlist
                if (oldRoom != null && oldRoom.getRoomID() != newRoom.getRoomID()) {
                    RegistrationControl.resolveWaitlistForRoom(oldRoom);
                }
            }

        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
        }
    }

    public static void checkRoomAvailability() {
        Scanner scanner = new Scanner(System.in);
        LocalDate checkInDate = null;
        LocalDate checkOutDate = null;

        while (checkInDate == null) {
            System.out.print("Enter Check-In Date (YYYY-MM-DD): ");
            String input = scanner.nextLine().trim();
            try {
                checkInDate = LocalDate.parse(input);
                if (checkInDate.isBefore(VirtualClock.getInstance().today())) {
                    System.out.println("Check-in date cannot be in the past.");
                    checkInDate = null;
                }
            } catch (Exception e) {
                System.out.println("Invalid date format.");
            }
        }

        while (checkOutDate == null) {
            System.out.print("Enter Check-Out Date (YYYY-MM-DD): ");
            String input = scanner.nextLine().trim();
            try {
                checkOutDate = LocalDate.parse(input);
                if (!checkOutDate.isAfter(checkInDate)) {
                    System.out.println("Check-out date must be after check-in date.");
                    checkOutDate = null;
                }
            } catch (Exception e) {
                System.out.println("Invalid date format.");
            }
        }

        System.out.println("\n--- Room Availability for " + checkInDate + " to " + checkOutDate + " ---");
        int singleCount = 0, doubleCount = 0, suiteCount = 0;

        for (int i = 1; i <= App.roomList.getNumberOfEntries(); i++) {
            Room r = App.roomList.getEntry(i);
            boolean hasOverlap = false;
            for (int j = 1; j <= App.bookingList.getNumberOfEntries(); j++) {
                Booking b = App.bookingList.getEntry(j);
                if (b.getRoom() != null && b.getRoom().getRoomID() == r.getRoomID() && b.getBookingStatus() != Booking.BookingStatus.CANCELLED) {
                    if (checkInDate.isBefore(b.getCheckOutDate()) && checkOutDate.isAfter(b.getCheckInDate())) {
                        hasOverlap = true;
                        break;
                    }
                }
            }
            if (!hasOverlap) {
                if (r.getRoomType() == RoomType.SINGLE) singleCount++;
                else if (r.getRoomType() == RoomType.DOUBLE) doubleCount++;
                else if (r.getRoomType() == RoomType.SUITE) suiteCount++;
            }
        }

        System.out.println("Single Rooms Available: " + singleCount + " (Rate: $100.0/night)");
        System.out.println("Double Rooms Available: " + doubleCount + " (Rate: $150.0/night)");
        System.out.println("Suite Rooms Available:  " + suiteCount + " (Rate: $300.0/night)");
    }
}
