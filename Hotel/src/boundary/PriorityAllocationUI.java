/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package boundary;

/**
 *
 * @author jlohz
 */
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Scanner;
import control.PriorityAllocationControl;
import adt.ListInterface;
import adt.DoublyLinkedList;
import entity.Booking;
import entity.Member;
import entity.Room;
import utility.ValidationUtility;

public class PriorityAllocationUI {

    private Scanner scanner = new Scanner(System.in);
    private PriorityAllocationControl control = new PriorityAllocationControl();

    public void startUI() {

        int choice = -1;
        while (choice != 0) {

            displayMenu();

            System.out.print("Enter your choice: ");
            choice = ValidationUtility.inputChoice(scanner);

            switch (choice) {

                case 1:
                    addBookingMenu();
                    break;
                case 2:
                    allocateNextRoom();
                    break;
                case 3:
                    displayWaitingList();
                    break;
                case 4:
                    searchBooking();
                    break;
                case 5:
                    generateAllocationReport();
                    break;
                case 6:
                    generateFilteredPriorityReport();
                    break;
                case 0:
                    System.out.println("Returning to Main Menu...");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    public void displayMenu() {

        System.out.println("\n===============================================");
        System.out.println("      VIP & Loyalty Tier Room Allocation");
        System.out.println("===============================================");
        System.out.println("1. Add Booking Request");
        System.out.println("2. Allocate Next Room");
        System.out.println("3. Display Waiting List");
        System.out.println("4. Search Booking");
        System.out.println("5. Generate Allocation Report");
        System.out.println("6. Generate Filtered Priority Report");
        System.out.println("0. Back to Main Menu");
        System.out.println("===============================================");
    }

    private void addBookingMenu() {

        System.out.println("\n--- Add Booking Request ---");

        String bookingID = ValidationUtility.inputBookingID(scanner);

        String memberID = ValidationUtility.inputMemberID(scanner);

        Member member = control.findMemberByID(memberID);

        if (member == null) {
            System.out.println("Member ID not found.");
            return;
        }

        // System automatically records the current registration date and time
        LocalDate bookingDate = ValidationUtility.inputDate(scanner,
                "Enter Booking Date (yyyy-MM-dd): ");

        LocalDate checkInDate = ValidationUtility.inputDate(scanner,
                "Enter Check-In Date (yyyy-MM-dd): ");

        LocalDate checkOutDate = ValidationUtility.inputDate(scanner,
                "Enter Check-Out Date (yyyy-MM-dd): ");

        LocalDateTime registrationTime = LocalDateTime.now();

        Booking newBooking = new Booking(
                bookingID,
                bookingDate,
                checkInDate,
                checkOutDate,
                registrationTime,
                member,
                null
        );

        control.addBooking(newBooking);

        System.out.println("\nBooking request added successfully!");
        System.out.println("Booking Date: " + bookingDate);
        System.out.println("Registration Time: " + registrationTime);
        System.out.println("Member: " + member.getMemberName());
        System.out.println("Loyalty Tier: " + member.getLoyaltyTier());
    }

    private void allocateNextRoom() {

        System.out.println("\n--- Allocate Next Room ---");

        ListInterface<Booking> bookings = control.getBookingList();

        Booking nextBooking = null;

        // Find the highest-priority booking that has no room yet
        for (int i = 1; i <= bookings.getNumberOfEntries(); i++) {

            Booking booking = bookings.getEntry(i);

            if (booking.getRoom() == null) {
                nextBooking = booking;
                break;
            }
        }

        if (nextBooking == null) {
            System.out.println("No booking is currently waiting for room allocation.");
            return;
        }

        Room allocatedRoom = control.allocateNextRoom();

        if (allocatedRoom != null) {

            System.out.println("\nRoom allocation successful!");
            System.out.println("Booking ID: " + nextBooking.getBookingID());
            System.out.println("Member: " + nextBooking.getMember().getMemberName());
            System.out.println("Loyalty Tier: "
                    + nextBooking.getMember().getLoyaltyTier());
            System.out.println("Room ID: " + allocatedRoom.getRoomID());

        } else {
            System.out.println("\nNo vacant room is currently available.");
            System.out.println("Booking remains in the priority waiting list.");
        }
    }

    private void displayWaitingList() {

        System.out.println("\n--- Priority Waiting List ---");

        ListInterface<Booking> bookings = control.getBookingList();

        if (bookings.isEmpty()) {
            System.out.println("No booking requests in the waiting list.");
            return;
        }

        for (int i = 1; i <= bookings.getNumberOfEntries(); i++) {

            Booking booking = bookings.getEntry(i);
            Member member = booking.getMember();

            System.out.println("\nPosition: " + i);
            System.out.println("Booking ID: " + booking.getBookingID());
            System.out.println("Member: " + member.getMemberName());
            System.out.println("Loyalty Tier: " + member.getLoyaltyTier());
            System.out.println("Booking Date: " + booking.getBookingDate());
            System.out.println("Registration Time: " + booking.getRegistrationTime());

            if (booking.getRoom() == null) {
                System.out.println("Room Status: Waiting for Allocation");
            } else {
                System.out.println("Room Status: Room Assigned");
                System.out.println("Room ID: " + booking.getRoom().getRoomID());
            }
        }
    }

    private void searchBooking() {

        System.out.println("\n--- Search Booking ---");

        String bookingID = ValidationUtility.inputBookingID(scanner);

        Booking booking = control.findBookingByID(bookingID);

        if (booking == null) {
            System.out.println("Booking ID not found.");
            return;
        }

        System.out.println("\n--- Booking Details ---");
        System.out.println("Booking ID: " + booking.getBookingID());
        System.out.println("Booking Date: " + booking.getBookingDate());
        System.out.println("Check-In Date: " + booking.getCheckInDate());
        System.out.println("Check-Out Date: " + booking.getCheckOutDate());

        System.out.println("Member: "
                + booking.getMember().getMemberName());

        System.out.println("Loyalty Tier: "
                + booking.getMember().getLoyaltyTier());

        if (booking.getRoom() == null) {
            System.out.println("Room Status: Waiting for Allocation");
        } else {
            System.out.println("Room ID: "
                    + booking.getRoom().getRoomID());

            System.out.println("Room Status: "
                    + booking.getRoom().getRoomStatus());
        }
    }

    private void generateAllocationReport() {

        LocalDateTime reportTime = LocalDateTime.now();

        ListInterface<Booking> bookings = control.getBookingList();
        ListInterface<Room> rooms = control.getRoomList();

        int totalBookings = bookings.getNumberOfEntries();
        int allocatedBookings = 0;
        int waitingBookings = 0;

        int eliteCount = 0;
        int diamondCount = 0;
        int platinumCount = 0;
        int standardCount = 0;

        int vacantRooms = 0;
        int occupiedRooms = 0;

        for (int i = 1; i <= totalBookings; i++) {

            Booking booking = bookings.getEntry(i);

            if (booking.getRoom() == null) {
                waitingBookings++;
            } else {
                allocatedBookings++;
            }

            String tier = booking.getMember().getLoyaltyTier();

            switch (tier) {
                case "Elite":
                    eliteCount++;
                    break;
                case "Diamond":
                    diamondCount++;
                    break;
                case "Platinum":
                    platinumCount++;
                    break;
                case "Standard":
                    standardCount++;
                    break;
            }
        }

        for (int i = 1; i <= rooms.getNumberOfEntries(); i++) {

            Room room = rooms.getEntry(i);

            if (room.getRoomStatus().equals("Vacant")) {
                vacantRooms++;
            } else {
                occupiedRooms++;
            }
        }

        System.out.println("\n==============================================================");
        System.out.println("             ROOM ALLOCATION SUMMARY REPORT");
        System.out.println("==============================================================");
        System.out.println("Generated At       : " + reportTime);
        System.out.println("--------------------------------------------------------------");

        System.out.printf("%-25s : %d%n", "Total Booking Requests", totalBookings);
        System.out.printf("%-25s : %d%n", "Allocated Bookings", allocatedBookings);
        System.out.printf("%-25s : %d%n", "Waiting Bookings", waitingBookings);

        System.out.println("--------------------------------------------------------------");

        System.out.printf("%-25s : %d%n", "Elite Bookings", eliteCount);
        System.out.printf("%-25s : %d%n", "Diamond Bookings", diamondCount);
        System.out.printf("%-25s : %d%n", "Platinum Bookings", platinumCount);
        System.out.printf("%-25s : %d%n", "Standard Bookings", standardCount);

        System.out.println("--------------------------------------------------------------");

        System.out.printf("%-25s : %d%n", "Total Rooms", rooms.getNumberOfEntries());
        System.out.printf("%-25s : %d%n", "Occupied Rooms", occupiedRooms);
        System.out.printf("%-25s : %d%n", "Vacant Rooms", vacantRooms);
        System.out.println();
        System.out.println("=======================================================================================");
        System.out.println("             CURRENT BOOKING SUMMARY");
        System.out.println("=======================================================================================");

        System.out.printf("%-10s %-12s %-15s %-12s %-15s %-10s %-12s%n",
                "Priority",
                "Booking ID",
                "Member",
                "Tier",
                "Booking Date",
                "Room",
                "Status");

        System.out.println("---------------------------------------------------------------------------------------");

        for (int i = 1; i <= bookings.getNumberOfEntries(); i++) {

            Booking booking = bookings.getEntry(i);

            String roomID;
            String status;

            if (booking.getRoom() == null) {
                roomID = "-";
                status = "Waiting";
            } else {
                roomID = booking.getRoom().getRoomID();
                status = "Allocated";
            }

            System.out.printf("%-10d %-12s %-15s %-12s %-15s %-10s %-12s%n",
                    i,
                    booking.getBookingID(),
                    booking.getMember().getMemberName(),
                    booking.getMember().getLoyaltyTier(),
                    booking.getBookingDate(),
                    roomID,
                    status);
        }
        System.out.println("=======================================================================================");
    }

    private void generateFilteredPriorityReport() {

        System.out.println("\n--- Filtered Priority Allocation Report ---");

        System.out.println("Select Loyalty Tier:");
        System.out.println("1. Elite");
        System.out.println("2. Diamond");
        System.out.println("3. Platinum");
        System.out.println("4. Standard");
        System.out.println("5. All");

        System.out.print("Enter choice: ");
        int tierChoice = scanner.nextInt();
        scanner.nextLine();

        String selectedTier = "";

        switch (tierChoice) {
            case 1:
                selectedTier = "Elite";
                break;
            case 2:
                selectedTier = "Diamond";
                break;
            case 3:
                selectedTier = "Platinum";
                break;
            case 4:
                selectedTier = "Standard";
                break;
            case 5:
                selectedTier = "All";
                break;
            default:
                System.out.println("Invalid choice.");
                return;
        }

        System.out.println("\nSelect Allocation Status:");
        System.out.println("1. Waiting");
        System.out.println("2. Allocated");
        System.out.println("3. All");

        System.out.print("Enter choice: ");
        int statusChoice = scanner.nextInt();
        scanner.nextLine();

        String selectedStatus = "";

        switch (statusChoice) {
            case 1:
                selectedStatus = "Waiting";
                break;
            case 2:
                selectedStatus = "Allocated";
                break;
            case 3:
                selectedStatus = "All";
                break;
            default:
                System.out.println("Invalid choice.");
                return;
        }

        System.out.println("\nSelect Check-In Date:");
        System.out.println("1. Today");
        System.out.println("2. Tomorrow");
        System.out.println("3. Custom Date");
        System.out.println("4. All Dates");

        System.out.print("Enter choice: ");
        int dateChoice = scanner.nextInt();
        scanner.nextLine();

        LocalDate selectedDate = null;

        switch (dateChoice) {
            case 1:
                selectedDate = LocalDate.now(); //today
                break;
            case 2:
                selectedDate = LocalDate.now().plusDays(1); //tomorrow
                break;
            case 3:
                selectedDate = ValidationUtility.inputDate(scanner, //choose date
                        "Enter Check-In Date (yyyy-MM-dd): ");
                break;
            case 4:
                selectedDate = null; //semua
                break;
            default:
                System.out.println("Invalid choice.");
                return;
        }

        ListInterface<Booking> filteredBookings = new DoublyLinkedList<>();

        ListInterface<Booking> bookings = control.getBookingList();

        for (int i = 1; i <= bookings.getNumberOfEntries(); i++) {

            Booking booking = bookings.getEntry(i);

            String bookingTier = booking.getMember().getLoyaltyTier();

            boolean tierMatch
                    = selectedTier.equals("All")
                    || bookingTier.equals(selectedTier);

            boolean statusMatch;

            if (selectedStatus.equals("Waiting")) {
                statusMatch = booking.getRoom() == null;

            } else if (selectedStatus.equals("Allocated")) {
                statusMatch = booking.getRoom() != null;

            } else {
                statusMatch = true;
            }

            boolean dateMatch
                    = selectedDate == null
                    || booking.getCheckInDate().equals(selectedDate);

            if (tierMatch && statusMatch && dateMatch) {
                filteredBookings.add(booking);
            }
        }
        for (int i = 1; i <= filteredBookings.getNumberOfEntries(); i++) {

            for (int j = i + 1; j <= filteredBookings.getNumberOfEntries(); j++) {

                Booking bookingA = filteredBookings.getEntry(i);
                Booking bookingB = filteredBookings.getEntry(j);

                if (control.comparePriority(bookingA, bookingB) < 0) {
                    filteredBookings.swap(i, j);
                }
            }
        }

        System.out.println("\n===============================================");
        System.out.println("     FILTERED PRIORITY ALLOCATION REPORT");
        System.out.println("===============================================");

        System.out.println("Generated At: " + LocalDateTime.now());
        System.out.println("Loyalty Tier: " + selectedTier);
        System.out.println("Allocation Status: " + selectedStatus);

        if (selectedDate == null) {
            System.out.println("Check-In Date: All Dates");
        } else {
            System.out.println("Check-In Date: " + selectedDate);
        }

        System.out.println("-----------------------------------------------");
        System.out.println("Total Matching Bookings: " + filteredBookings.getNumberOfEntries());
        System.out.println("-----------------------------------------------");

        if (filteredBookings.isEmpty()) {
            System.out.println("No bookings match the selected criteria.");
            System.out.println("===============================================");
            return;
        }

        System.out.printf("%-13s %-15s %-12s %-20s %-15s %-15s%n",
                "Booking ID",
                "Member",
                "Tier",
                "Registration Time",
                "Check-In",
                "Status");

        System.out.println("--------------------------------------------------------------------------------");

        for (int i = 1; i <= filteredBookings.getNumberOfEntries(); i++) {

            Booking booking = filteredBookings.getEntry(i);
            String status;

            if (booking.getRoom() == null) {
                status = "Waiting";
            } else {
                status = "Allocated";
            }

            System.out.printf("%-13s %-15s %-12s %-20s %-15s %-15s%n",
                    booking.getBookingID(),
                    booking.getMember().getMemberName(),
                    booking.getMember().getLoyaltyTier(),
                    booking.getRegistrationTime(),
                    booking.getCheckInDate(),
                    status);
        }

        System.out.println("===============================================");
    }

    public static void main(String[] args) {

        PriorityAllocationUI ui = new PriorityAllocationUI();
        ui.startUI();
    }
}
