/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package boundary;

/**
 * @author Tham Cle Ment
 */

import java.util.Scanner;
import utility.VirtualClock;
import control.BookingControl;
import control.RegistrationControl;
import entity.Member;

public class RegistrationUI {
    // boundary class, used for user interface, to interact with the user and get
    // input for registration process

    // display registration menu
    public void displayRegistrationMenu() {
        Scanner scanner = new Scanner(System.in);
        int choice = 0;

        while (choice != 10) {
            System.out.println("\n==========================================");
            System.out.println("   HOTEL REGISTRATION MODULE  ");
            System.out.println("  " + VirtualClock.getInstance().toString());
            System.out.println("  Period: " + RegistrationControl.getCurrentTimePeriod());
            System.out.println("==========================================\n");
            System.out.println("Customer Registration Menu:");
            System.out.println("1. Register New Customer");
            System.out.println("2. View members");
            System.out.println("==========================================");
            System.out.println("Front Desk Simulation");
            System.out.println("3. Check-in Enqueue"); // if person has valid booking, add to check in queue
            System.out.println("4. Check-out Enqueue"); // if person has valid booking, add to check out queue
            System.out.println("5. View check-in waitlist");
            System.out.println("6. View check-out waitlist");
            System.out.println("7. Walk-In Booking");
            System.out.println("==========================================");
            System.out.println("8. Generate Booking Summary & Trend Report"); // bookings in time frame
            System.out.println("9. Generate Revenue Summary & Trend Report"); // revenue report of set timeframe
            System.out.println("10. Exit Registration Module");
            System.out.println("==========================================");
            System.out.println("0. Advance Time");

            System.out.print("Please select an option: ");

            try {
                choice = Integer.parseInt(scanner.nextLine().trim());
            } catch (Exception e) {
                choice = -1;
            }

            switch (choice) {
                case 1:
                    System.out.println("Registering new customer...");
                    registerNewCustomer(scanner);
                    break;
                case 2:
                    System.out.println("Displaying existing members...");
                    RegistrationControl.listMembers();
                    break;
                case 3:
                    checkinProcess(scanner);
                    break;
                case 4:
                    checkoutProcess(scanner);
                    break;
                case 5:
                    viewCheckInWaitlist(scanner);
                    break;
                case 6:
                    viewCheckOutWaitlist(scanner);
                    break;
                case 7:
                    walkInProcess(scanner);
                    break;
                case 8:
                    generateBookingReportUI(scanner);
                    break;
                case 9:
                    generateRevenueReportUI(scanner);
                    break;
                case 10:
                    System.out.println("Exiting Registration Module...");
                    break;
                case 0:
                    TimeProgressionUI.showTimeMenu();
                    break;
                default:
                    System.out.println("Invalid option. Please select a valid option.");
            }
        }
        // Do NOT close scanner here — it wraps System.in which is owned by App.main()
    }

    private Member registerNewCustomer(Scanner scanner) {
        System.out.print("Enter customer name (or type 'cancel' to abort): ");
        String name = scanner.nextLine().trim();
        if (name.equalsIgnoreCase("cancel")) {
            System.out.println("Registration aborted. Returning to menu.");
            return null;
        }
        while (name.isEmpty()) {
            System.out.println("Name cannot be empty. Please try again.");
            System.out.print("Enter customer name (or type 'cancel' to abort): ");
            name = scanner.nextLine().trim();
            if (name.equalsIgnoreCase("cancel")) {
                System.out.println("Registration aborted. Returning to menu.");
                return null;
            }
        }
        System.out.print("Enter customer phone number (or type 'cancel' to abort): ");
        String phoneNumber = scanner.nextLine().trim();
        if (phoneNumber.equalsIgnoreCase("cancel")) {
            System.out.println("Registration aborted. Returning to menu.");
            return null;
        }
        while (!phoneNumber.matches("\\d{7,15}")) {
            System.out.println("Phone number must be 7-15 digits. Please try again.");
            System.out.print("Enter customer phone number (or type 'cancel' to abort): ");
            phoneNumber = scanner.nextLine().trim();
            if (phoneNumber.equalsIgnoreCase("cancel")) {
                System.out.println("Registration aborted. Returning to menu.");
                return null;
            }
        }
        System.out.print("Enter customer email (or type 'cancel' to abort): ");
        String email = scanner.nextLine().trim();
        if (email.equalsIgnoreCase("cancel")) {
            System.out.println("Registration aborted. Returning to menu.");
            return null;
        }
        while (!email.matches("^\\S+@\\S+\\.\\S+$")) {
            System.out.println("Invalid email format. Please try again.");
            System.out.print("Enter customer email (or type 'cancel' to abort): ");
            email = scanner.nextLine().trim();
            if (email.equalsIgnoreCase("cancel")) {
                System.out.println("Registration aborted. Returning to menu.");
                return null;
            }
        }
        return RegistrationControl.registerNewCustomer(name, phoneNumber, email);
    }

    private void walkInProcess(Scanner scanner) {
        System.out.println("--- Walk-In Booking ---");
        System.out.println("Please identify the customer first.");
        Member member = BookingControl.loginMember();
        if (member == null) {
            System.out.print("Customer not found. Would you like to register a new customer? (Y/N): ");
            String registerChoice = scanner.nextLine().trim();
            if (registerChoice.equalsIgnoreCase("y")) {
            member = registerNewCustomer(scanner);
            if (member == null) {
                // Registration was aborted
                System.out.println("Returning to menu.");
                return;
            }
        } else {
            System.out.println("Walk-in booking cancelled.");
            return;
        }
        }
        System.out.println("Proceeding to make booking for " + member.getMemberName() + "...");
        BookingControl.makeBooking(member);
    }

    private void checkinProcess(Scanner scanner) {
        if (RegistrationControl.getCurrentTimePeriod() != RegistrationControl.TimePeriod.CHECKIN) {
            System.out.println("Error: Check-ins are only allowed during the Check-in period (15:00 - 23:59).");
            return;
        }

        System.out.println("Search for member to check-in:");
        Member member = BookingControl.loginMember();
        if (member == null) {
            System.out.println("Check-in cancelled.");
            return;
        }

        RegistrationControl.enqueueCheckin(member);

        System.out.print("\nWould you like to process the next check-in in the queue? (Y/N): ");
        if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
            RegistrationControl.processCheckin(scanner);
        }
    }

    private void viewCheckInWaitlist(Scanner scanner) {
        RegistrationControl.viewCheckInWaitlist();
        if (!main.App.checkInWaitlist.isEmpty()) {
            System.out.print("\nWould you like to process the next check-in in the queue? (Y/N): ");
            if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
                if (RegistrationControl.getCurrentTimePeriod() != RegistrationControl.TimePeriod.CHECKIN) {
                    System.out.println("Error: Check-ins are only allowed during the Check-in period (15:00 - 23:59).");
                    return;
                }
                RegistrationControl.processCheckin(scanner);
            }
        }
    }

    private void checkoutProcess(Scanner scanner) {
        if (RegistrationControl.getCurrentTimePeriod() != RegistrationControl.TimePeriod.CHECKOUT) {
            System.out.println("Error: Check-outs are only allowed during the Checkout period (00:00 - 12:00).");
            return;
        }

        System.out.println("Search for member to check-out:");
        Member member = BookingControl.loginMember();
        if (member == null) {
            System.out.println("Check-out cancelled.");
            return;
        }

        RegistrationControl.enqueueCheckout(member);

        System.out.print("\nWould you like to process the next check-out in the queue? (Y/N): ");
        if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
            RegistrationControl.processCheckout(scanner);
        }
    }

    private void viewCheckOutWaitlist(Scanner scanner) {
        RegistrationControl.viewCheckOutWaitlist();
        if (!main.App.checkOutWaitlist.isEmpty()) {
            System.out.print("\nWould you like to process the next check-out in the queue? (Y/N): ");
            if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
                if (RegistrationControl.getCurrentTimePeriod() != RegistrationControl.TimePeriod.CHECKOUT) {
                    System.out
                            .println("Error: Check-outs are only allowed during the Checkout period (00:00 - 12:00).");
                    return;
                }
                RegistrationControl.processCheckout(scanner);
            }
        }
    }

    private void generateBookingReportUI(Scanner scanner) {
        System.out.println("\n--- Booking Report Parameters ---");
        int year = VirtualClock.getInstance().today().getYear();
        int month = VirtualClock.getInstance().today().getMonthValue();

        System.out.print("Enter Year (YYYY) or press Enter for current year [" + year + "]: ");
        String yearStr = scanner.nextLine().trim();
        if (!yearStr.isEmpty()) {
            try {
                year = Integer.parseInt(yearStr);
            } catch (Exception ignored) {
            }
        }

        System.out.print("Enter Month (1-12) or press Enter for current month [" + month + "]: ");
        String monthStr = scanner.nextLine().trim();
        if (!monthStr.isEmpty()) {
            try {
                month = Integer.parseInt(monthStr);
            } catch (Exception ignored) {
            }
        }

        System.out.println("Select Booking Status Filter:");
        System.out.println("1. CONFIRMED");
        System.out.println("2. CHECKED IN");
        System.out.println("3. CHECKED OUT");
        System.out.println("4. CANCELLED");
        System.out.println("0. All Statuses");
        System.out.print("Choice: ");
        String statusChoice = scanner.nextLine().trim();
        entity.Booking.BookingStatus sFilter = null;
        if (statusChoice.equals("1"))
            sFilter = entity.Booking.BookingStatus.CONFIRMED;
        else if (statusChoice.equals("2"))
            sFilter = entity.Booking.BookingStatus.CHECKED_IN;
        else if (statusChoice.equals("3"))
            sFilter = entity.Booking.BookingStatus.CHECKED_OUT;
        else if (statusChoice.equals("4"))
            sFilter = entity.Booking.BookingStatus.CANCELLED;

        RegistrationControl.generateBookingSummaryReport(year, month, sFilter);
    }

    private void generateRevenueReportUI(Scanner scanner) {
        System.out.println("\n--- Revenue Report Parameters ---");
        int year = VirtualClock.getInstance().today().getYear();
        int month = VirtualClock.getInstance().today().getMonthValue();

        System.out.print("Enter Year (YYYY) or press Enter for current year [" + year + "]: ");
        String yearStr = scanner.nextLine().trim();
        if (!yearStr.isEmpty()) {
            try {
                year = Integer.parseInt(yearStr);
            } catch (Exception ignored) {
            }
        }

        System.out.print("Enter Month (1-12) or press Enter for current month [" + month + "]: ");
        String monthStr = scanner.nextLine().trim();
        if (!monthStr.isEmpty()) {
            try {
                month = Integer.parseInt(monthStr);
            } catch (Exception ignored) {
            }
        }

        System.out.println("Select Room Type Filter:");
        System.out.println("1. SINGLE");
        System.out.println("2. DOUBLE");
        System.out.println("3. SUITE");
        System.out.println("0. All Types");
        System.out.print("Choice: ");
        String typeChoice = scanner.nextLine().trim();
        entity.Room.RoomType tFilter = null;
        if (typeChoice.equals("1"))
            tFilter = entity.Room.RoomType.SINGLE;
        else if (typeChoice.equals("2"))
            tFilter = entity.Room.RoomType.DOUBLE;
        else if (typeChoice.equals("3"))
            tFilter = entity.Room.RoomType.SUITE;

        RegistrationControl.generateRevenueSummaryReport(year, month, tFilter);
    }
}