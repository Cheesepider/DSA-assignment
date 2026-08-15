/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package boundary;

import java.util.Scanner;

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
            System.out.println("  " + util.VirtualClock.getInstance().toString());
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
            System.out.println("8. View report 1"); // bookings in time frame
            System.out.println("9. View report 2"); // revenue report of set timeframe
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
                    RegistrationControl.generateBookingReport(scanner);
                    break;
                case 9:
                    RegistrationControl.generateRevenueReport(scanner);
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
        System.out.print("Enter customer name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Enter customer phone number: ");
        String phoneNumber = scanner.nextLine().trim();
        System.out.print("Enter customer email: ");
        String email = scanner.nextLine().trim();
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
}
