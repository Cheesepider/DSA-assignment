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
import entity.TaskLog;
import entity.RewardItem;
import entity.PointsTransaction;
import entity.PendingPointsCredit;
import entity.RedemptionRecord;
import dao.RegistrationDAO;
import boundary.BookingUI;
import boundary.HousekeepingUI;
import boundary.RegistrationUI;
import boundary.PriorityAllocationUI;
import boundary.LoyaltyUI;
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

    public static ListInterface<TaskLog> taskLogStack = new DoublyLinkedList<>(); // use for undo stack
    public static ListInterface<TaskLog> cleaningHistoryList = new DoublyLinkedList<>(); // use for cleaning history

    public static ListInterface<RewardItem> rewardCatalog = new DoublyLinkedList<>(); // rewards members can redeem with points
    public static ListInterface<PointsTransaction> pointsTransactionList = new DoublyLinkedList<>(); // active earned-points batches (for expiry tracking)
    public static ListInterface<PendingPointsCredit> pendingPointsQueue = new DoublyLinkedList<>(); // points credits awaiting staff processing
    public static ListInterface<RedemptionRecord> redemptionHistoryList = new DoublyLinkedList<>(); // full reward redemption history
    public static ListInterface<Integer> queuedStayBookingIDs = new DoublyLinkedList<>(); // booking IDs already queued for points (avoids double-queueing)

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

        while (module != 6) {
            System.out.println("\n==========================================");
            System.out.println("   HOTEL MANAGEMENT SYSTEM   ");
            System.out.println("  Time: " + VirtualClock.getInstance().toString());
            System.out.println("==========================================");
            System.out.println("Select Module:");
            System.out.println("1. Booking Module");
            System.out.println("2. Registration Module  (Check-In / Check-Out)");
            System.out.println("3. VIP & Loyalty Tier Room Allocation");
            System.out.println("4. Loyalty & Reward Service Module");
            System.out.println("5. Housekeeping Module");
            System.out.println("6. Exit");
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
                    new PriorityAllocationUI().startUI();
                    break;

                case 4:
                    new LoyaltyUI(memberList).displayMenu();
                    break;

                case 5:
                    new HousekeepingUI().displayHousekeepingMenu();
                    break;

                case 6:
                    System.out.println("Exiting system. Goodbye!");
                    break;
            }
        }

        scanner.close();
    }
}