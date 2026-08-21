/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package boundary;

/**
 *
 * @author Jerry
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

    private final Scanner scanner = new Scanner(System.in);
    private final PriorityAllocationControl control = new PriorityAllocationControl();

    public void startUI() {

        int choice = -1;
        while (choice != 0) {

            displayMenu();

            System.out.print("Enter your choice: ");
            choice = ValidationUtility.inputChoice(scanner);

            switch (choice) {

                case 1:
                    displayWaitingList();
                    break;

                case 2:
                    searchBooking();
                    break;

                case 3:
                    generatePriorityAllocationReport();
                    break;

                case 4:
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
        System.out.println("1. Display Priority Waiting List");
        System.out.println("2. Search Booking");
        System.out.println("3. Generate Priority Allocation Report");
        System.out.println("4. Generate Filtered Priority Report");
        System.out.println("0. Back to Main Menu");
        System.out.println("===============================================");
    }

    //not used. (testing method)
    //to proof that allocation algorithm can independently locate an available room
    private void allocateNextRoom() {

        System.out.println("\n--- Allocate Next Room ---");

        Booking allocatedBooking = control.allocateNextRoom();

        if (allocatedBooking == null) {

            System.out.println(
                    "No waiting booking can currently be allocated.");

            return;
        }
        System.out.println("\nRoom allocation successful!");
        System.out.println(
                "Booking ID: " + allocatedBooking.getBookingID());

        System.out.println(
                "Member: "
                + allocatedBooking.getMember().getMemberName());

        System.out.println(
                "Loyalty Tier: "
                + allocatedBooking.getMember().getLoyaltyTier());

        System.out.println(
                "Room: "
                + allocatedBooking.getRoom().getRoomNumber());
    }

    private void displayWaitingList() {

        System.out.println("\n--- Priority Waiting List ---");

        ListInterface<Booking> waitingList = control.getWaitingList();

        if (waitingList.isEmpty()) {
            System.out.println("No booking requests are currently waiting.");
            return;
        }

        System.out.println(
                "==============================================================================================");
        System.out.printf(
                "%-9s %-11s %-15s %-12s %-12s %-12s %-20s%n",
                "Priority",
                "Booking ID",
                "Member",
                "Tier",
                "Room Type",
                "Check-In",
                "Registration Time"
        );

        System.out.println(
                "----------------------------------------------------------------------------------------------");

        for (int i = 1; i <= waitingList.getNumberOfEntries(); i++) {

            Booking booking = waitingList.getEntry(i);

            String roomType = "-";

            if (booking.getRoom() != null) {
                roomType = booking.getRoom().getRoomType().toString();
            }

            System.out.printf(
                    "%-9d %-11d %-15s %-12s %-12s %-12s %-20s%n",
                    i,
                    booking.getBookingID(),
                    booking.getMember().getMemberName(),
                    booking.getMember().getLoyaltyTier(),
                    roomType,
                    booking.getCheckInDate(),
                    booking.getRegistrationTime()
            );
        }

        System.out.println(
                "==============================================================================================");
    }

    private void searchBooking() {

        System.out.println("\n--- Search Booking ---");

        System.out.print("Enter Booking ID: ");
        int bookingID = ValidationUtility.inputChoice(scanner);

        Booking booking = control.findBookingByID(bookingID);

        if (booking == null) {
            System.out.println("Booking ID not found.");
            return;
        }

        System.out.println("\n--- Booking Details ---");
        System.out.println("Booking ID: " + booking.getBookingID());
        System.out.println("Member: " + booking.getMember().getMemberName());
        System.out.println("Loyalty Tier: " + booking.getMember().getLoyaltyTier());
        System.out.println("Check-In Date: " + booking.getCheckInDate());
        System.out.println("Check-Out Date: " + booking.getCheckOutDate());
        System.out.println("Registration Time: " + booking.getRegistrationTime());
        System.out.println("Booking Status: " + booking.getBookingStatus());

        //To keep the req   uested room type，eventhough in waiting, that's y !=null
        //may not been allocated, but for searching purpose
        if (booking.getRoom() != null) {

            System.out.println("Room Type: " + booking.getRoom().getRoomType());
            System.out.println("Room Number: " + booking.getRoom().getRoomNumber());
        }
    }

    private void generatePriorityAllocationReport() {

        LocalDateTime reportTime = LocalDateTime.now();

        ListInterface<Booking> waitingList = control.getWaitingList();

        int totalWaiting = control.countWaitingBookings();
        int totalConfirmed = control.countConfirmedBookings();

        //Tier counter
        int eliteCount = control.countWaitingBookingsByTier(Member.LoyaltyTier.Elite);
        int diamondCount = control.countWaitingBookingsByTier(Member.LoyaltyTier.Diamond);
        int platinumCount = control.countWaitingBookingsByTier(Member.LoyaltyTier.Platinum);
        int regularCount = control.countWaitingBookingsByTier(Member.LoyaltyTier.Regular);

        //RoomType counter
        int singleRooms = control.countRoomsByType(Room.RoomType.SINGLE);
        int doubleRooms = control.countRoomsByType(Room.RoomType.DOUBLE);
        int suiteRooms = control.countRoomsByType(Room.RoomType.SUITE);

        System.out.println("\n==========================================================================");
        System.out.println("    Tunku Abdul Rahman University of Management & Technology Resort");
        System.out.println("                    VIP Room Allocation Subsystem");
        System.out.println("\n                 PRIORITY ALLOCATION SUMMARY REPORT");
        System.out.println("==========================================================================");

        System.out.println("Generated At          : " + reportTime);

        System.out.println("--------------------------------------------------------------------------");

        System.out.printf("%-30s : %d%n", "Total Waiting Requests", totalWaiting);
        System.out.printf("%-30s : %d%n", "Total Confirmed Bookings", totalConfirmed);
        System.out.println("--------------------------------------------------------------------------");
        System.out.printf("%-30s : %d%n", "Elite Waiting Requests", eliteCount);
        System.out.printf("%-30s : %d%n", "Diamond Waiting Requests", diamondCount);
        System.out.printf("%-30s : %d%n", "Platinum Waiting Requests", platinumCount);
        System.out.printf("%-30s : %d%n", "Regular Waiting Requests", regularCount);
        System.out.println("--------------------------------------------------------------------------");
        System.out.printf("%-30s : %d%n", "Single Rooms", singleRooms);
        System.out.printf("%-30s : %d%n", "Double Rooms", doubleRooms);
        System.out.printf("%-30s : %d%n", "Suite Rooms", suiteRooms);

        System.out.println("\n======================================================================================");
        System.out.println("                               WAITING LIST DETAILS");
        System.out.println("======================================================================================");

        System.out.printf(
                "%-9s %-11s %-15s %-11s %-12s %-12s %-12s%n",
                "Priority",
                "Booking ID",
                "Member",
                "Tier",
                "Room Type",
                "Check-In",
                "Check-Out"
        );

        System.out.println(
                "--------------------------------------------------------------------------------------");

        for (int i = 1;
                i <= waitingList.getNumberOfEntries();
                i++) {

            Booking booking = waitingList.getEntry(i);

            String roomType = "-";

            if (booking.getRoom() != null) {
                roomType = booking.getRoom().getRoomType().toString();
            }

            System.out.printf(
                    "%-9d %-11d %-15s %-11s %-12s %-12s %-12s%n",
                    i,
                    booking.getBookingID(),
                    booking.getMember().getMemberName(),
                    booking.getMember().getLoyaltyTier(),
                    roomType,
                    booking.getCheckInDate(),
                    booking.getCheckOutDate()
            );
        }
        System.out.println(
                "=======================================================================================");
        
        if (waitingList.isEmpty()) {

            System.out.println("No booking requests are currently waiting.\n");

        } else {
            printVerticalBarChart(
                    eliteCount,
                    diamondCount,
                    platinumCount,
                    regularCount
            );
        }
    }

    private void generateFilteredPriorityReport() {

        System.out.println("\n--- Filtered Priority Waiting List Report ---");

        // Filter 1: Choose Tier
        System.out.println("\nSelect Loyalty Tier:");
        System.out.println("1. Elite");
        System.out.println("2. Diamond");
        System.out.println("3. Platinum");
        System.out.println("4. Regular");
        System.out.println("5. All");

        System.out.print("Enter choice(0 to return): ");
        int tierChoice = ValidationUtility.inputChoice(scanner);

        Member.LoyaltyTier selectedTier = null;

        switch (tierChoice) {
            case 1:
                selectedTier = Member.LoyaltyTier.Elite;
                break;
            case 2:
                selectedTier = Member.LoyaltyTier.Diamond;
                break;
            case 3:
                selectedTier = Member.LoyaltyTier.Platinum;
                break;
            case 4:
                selectedTier = Member.LoyaltyTier.Regular;
                break;
            case 5:
                selectedTier = null; // All
                break;
            case 0:
                return;
            default:
                System.out.println("Invalid choice.");
                return;
        }

        // Filter 2: Choose RoomType
        System.out.println("\nSelect Room Type:");
        System.out.println("1. Single");
        System.out.println("2. Double");
        System.out.println("3. Suite");
        System.out.println("4. All");

        System.out.print("Enter choice(0 to return): ");
        int roomChoice = ValidationUtility.inputChoice(scanner);

        Room.RoomType selectedRoomType = null;

        switch (roomChoice) {
            case 1:
                selectedRoomType = Room.RoomType.SINGLE;
                break;
            case 2:
                selectedRoomType = Room.RoomType.DOUBLE;
                break;
            case 3:
                selectedRoomType = Room.RoomType.SUITE;
                break;
            case 4:
                selectedRoomType = null; // All
                break;
            case 0:
                return;
            default:
                System.out.println("Invalid choice.");
                return;
        }

        // Filter 3: Choose Check-In Date
        System.out.println("\nSelect Check-In Date:");
        System.out.println("1. Today");
        System.out.println("2. Tomorrow");
        System.out.println("3. Custom Date");
        System.out.println("4. All Dates");

        System.out.print("Enter choice(0 to return): ");
        int dateChoice = ValidationUtility.inputChoice(scanner);

        LocalDate selectedDate = null;

        switch (dateChoice) {
            case 1:
                selectedDate = LocalDate.now();
                break;
            case 2:
                selectedDate = LocalDate.now().plusDays(1);
                break;
            case 3:
                selectedDate = ValidationUtility.inputDate(
                        scanner,
                        "Enter Check-In Date (yyyy-MM-dd): ");
                break;
            case 4:
                selectedDate = null;
                break;          
            case 0:
                return;
            
            default:
                System.out.println("Invalid choice.");
                return;
        }

        // =========================
        // Filtering
        // =========================
        ListInterface<Booking> waitingList = control.getWaitingList();

        ListInterface<Booking> filteredBookings
                = new DoublyLinkedList<>();

        for (int i = 1;
                i <= waitingList.getNumberOfEntries();
                i++) {

            Booking booking = waitingList.getEntry(i);

            boolean tierMatch
                    = selectedTier == null
                    || booking.getMember().getLoyaltyTier() == selectedTier;

            boolean roomMatch
                    = selectedRoomType == null
                    || (booking.getRoom() != null
                    && booking.getRoom().getRoomType() == selectedRoomType);

            boolean dateMatch
                    = selectedDate == null
                    || booking.getCheckInDate().equals(selectedDate);

            if (tierMatch && roomMatch && dateMatch) {
                filteredBookings.add(booking);
            }
        }

        // =========================
        // Custom Priority Sorting
        // =========================
        for (int i = 1;
                i <= filteredBookings.getNumberOfEntries();
                i++) {

            for (int j = i + 1;
                    j <= filteredBookings.getNumberOfEntries();
                    j++) {

                Booking bookingA = filteredBookings.getEntry(i);
                Booking bookingB = filteredBookings.getEntry(j);

                if (control.comparePriority(bookingA, bookingB) < 0) {

                    filteredBookings.replace(i, bookingB);
                    filteredBookings.replace(j, bookingA);
                }
            }
        }

        // Report Output
        System.out.println(
                "\n==========================================================================================");
        System.out.println(
                "            Tunku Abdul Rahman University of Management & Technology Resort");

        System.out.println(
                "                           VIP Room Allocation Subsystem");
        System.out.println(
                "\n                       FILTERED PRIORITY WAITING LIST REPORT");
        System.out.println(
                "==========================================================================================");
        System.out.println("Generated At : " + LocalDateTime.now());
        System.out.println("Loyalty Tier : " + (selectedTier == null ? "All" : selectedTier));
        System.out.println("Room Type    : " + (selectedRoomType == null ? "All" : selectedRoomType));
        System.out.println("Check-In Date: " + (selectedDate == null ? "All Dates" : selectedDate));
        System.out.println(
                "----------------------------------------------------------------------------------------------");

        System.out.println("Total Matching Requests: " + filteredBookings.getNumberOfEntries());
        System.out.println("----------------------------------------------------------------------------------------------");

        if (filteredBookings.isEmpty()) {

            System.out.println("No booking requests match the selected criteria.");
            System.out.println("==========================================================================================");

            return;
        }

        System.out.printf(
                "%-9s %-11s %-15s %-12s %-12s %-12s %-20s%n",
                "Priority",
                "Booking ID",
                "Member",
                "Tier",
                "Room Type",
                "Check-In",
                "Registration Time"
        );

        System.out.println(
                "----------------------------------------------------------------------------------------------");

        for (int i = 1;
                i <= filteredBookings.getNumberOfEntries();
                i++) {

            Booking booking = filteredBookings.getEntry(i);

            String roomType = "-";

            if (booking.getRoom() != null) {
                roomType
                        = booking.getRoom().getRoomType().toString();
            }

            System.out.printf(
                    "%-9d %-11d %-15s %-12s %-12s %-12s %-20s%n",
                    i,
                    booking.getBookingID(),
                    booking.getMember().getMemberName(),
                    booking.getMember().getLoyaltyTier(),
                    roomType,
                    booking.getCheckInDate(),
                    booking.getRegistrationTime()
            );
        }

        System.out.println(
                "==============================================================================================");
    }

    private void printVerticalBarChart(
            int eliteCount,
            int diamondCount,
            int platinumCount,
            int regularCount) {

        int[] counts = {
            eliteCount,
            diamondCount,
            platinumCount,
            regularCount
        };

        String[] labels = {
            "Elite",
            "Diamond",
            "Platinum",
            "Regular"
        };

        int max = counts[0];

        // Find highest value for Y-axis
        for (int i = 1; i < counts.length; i++) {
            if (counts[i] > max) {
                max = counts[i];
            }
        }

        System.out.println("\nWAITING REQUESTS BY LOYALTY TIER\n");
        System.out.println("Count");
        System.out.println("  ^");

        // Print bars from top to bottom
        for (int level = max; level >= 1; level--) {

            System.out.printf("%2d | ", level);

            for (int count : counts) {

                if (count >= level) {
                    System.out.printf("%-10s", "*");
                } else {
                    System.out.printf("%-10s", " ");
                }
            }

            System.out.println();
        }

        // X-axis
        System.out.print("   +");
        for (int i = 0; i < counts.length; i++) {
            System.out.print("----------");
        }

        System.out.println("> Loyalty Tier");

        System.out.print("     ");

        for (String label : labels) {
            System.out.printf("%-10s", label);
        }

        System.out.println();
    }

    public static void main(String[] args) {

        PriorityAllocationUI ui = new PriorityAllocationUI();
        ui.startUI();
    }
}
