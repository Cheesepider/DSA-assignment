/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package main;

import adt.DoublyLinkedList;
import adt.ListInterface;
import entity.Booking;
import entity.Member;
import entity.Room;
import dao.RegistrationDAO;
import boundary.BookingUI;
import boundary.RegistrationUI;
import utility.VirtualClock;

public class App {

    // =========================================================
    // Global application state — shared across all modules
    // =========================================================
    public static ListInterface<Member> memberList = new DoublyLinkedList<>(); // all registered members
    public static ListInterface<Room> roomList = new DoublyLinkedList<>(); // all hotel rooms

    public static ListInterface<Member> checkInWaitlist = new DoublyLinkedList<>(); // members queued for check-in
    public static ListInterface<Member> checkOutWaitlist = new DoublyLinkedList<>(); // members queued for check-out

    public static ListInterface<Booking> bookingList = new DoublyLinkedList<>(); // active / upcoming bookings
    public static ListInterface<Booking> bookingHistoryList = new DoublyLinkedList<>(); // completed / cancelled bookings
    public static ListInterface<Booking> bookingRequestsQueue = new DoublyLinkedList<>(); // priority waitlist for rooms

    // =========================================================
    // Entry point
    // =========================================================
    public static void main(String[] args) {

        // --- 1. Load seed data ---
        System.out.println("==========================================");
        System.out.println("   HOTEL MANAGEMENT SYSTEM - STARTING   ");
        System.out.println("==========================================");
        RegistrationDAO.initializeData();
        System.out.println("==========================================\n");

        // --- 2. Core system loop ---
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        int module = 0;

        while (module != 3) {
            System.out.println("\n==========================================");
            System.out.println("   HOTEL MANAGEMENT SYSTEM   ");
            System.out.println("  Time: " + VirtualClock.getInstance().toString());
            System.out.println("==========================================");
            System.out.println("Select Module:");
            System.out.println("1. Booking Module");
            System.out.println("2. Registration Module  (Check-In / Check-Out)");
            System.out.println("3. Exit");
            System.out.print("Choice: ");

            try {
                module = Integer.parseInt(scanner.nextLine().trim());
            } catch (Exception e) {
                module = 0;
            }

            switch (module) {
                case 1:
                    new BookingUI().displayBookingMenu();
                    break;
                case 2:
                    new RegistrationUI().displayRegistrationMenu();
                    break;
                case 3:
                    System.out.println("Exiting system. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice. Please enter 1, 2, or 3.");
            }
        }

        scanner.close();
    }
}
