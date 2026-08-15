/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package boundary;

import java.util.Scanner;
import utility.VirtualClock;
import control.BookingControl;
import entity.Member;

public class BookingUI {
    private Member currentMember = null;

    public void displayBookingMenu() {
        Scanner scanner = new Scanner(System.in);
        int choice = 0;

        while (choice != 8) {
            System.out.println("\n==========================================");
            System.out.println("   HOTEL BOOKING MODULE  ");
            System.out.println("  " + VirtualClock.getInstance().toString());
            System.out.println("==========================================\n");
            System.out.println("User Menu:");
            // print current member if exist
            if (currentMember == null) {
                System.out.println("Currently logged out");
            } else {
                System.out.println("Currently logged in as: " + currentMember.getMemberName() + " (ID: "
                        + currentMember.getMemberID() + ")");
            }
            // action 1: select member to operate (login)
            System.out.println("1. Login");
            System.out.println("2. Logout");
            System.out.println("==========================================");
            System.out.println("3. Make Booking");
            System.out.println("4. Cancel Booking");
            System.out.println("5. View My Bookings");
            System.out.println("6. Update Booking");
            System.out.println("7. Check Room Availability");
            System.out.println("8. Exit");
            System.out.println("==========================================");
            System.out.println("0. Advance Time");

            System.out.print("Please select an option: ");

            try {
                choice = Integer.parseInt(scanner.nextLine().trim());
            } catch (Exception e) {
                choice = 0;
            }

            switch (choice) {
                case 1:
                    login();
                    break;
                case 2:
                    logout();
                    break;
                case 3:
                    BookingControl.makeBooking(currentMember);
                    break;
                case 4:
                    BookingControl.cancelBooking(currentMember);
                    break;
                case 5:
                    BookingControl.viewBookings(currentMember);
                    break;
                case 6:
                    BookingControl.updateBooking(currentMember);
                    break;
                case 7:
                    BookingControl.checkRoomAvailability();
                    break;
                case 8:
                    System.out.println("Exiting Booking Module...");
                    break;
                case 0:
                    TimeProgressionUI.showTimeMenu();
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
                    break;
            }
        }
        // Do NOT close scanner here — it wraps System.in which is owned by App.main()
    }

    private void login() {
        Member temp = BookingControl.loginMember();
        if (temp != null) {
            currentMember = temp;
        }
    }

    private void logout() {
        currentMember = null;
    }
}
