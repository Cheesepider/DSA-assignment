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

public class TimeProgressionUI {

    public static void showTimeMenu() {
        Scanner scanner = new Scanner(System.in);
        VirtualClock clock = VirtualClock.getInstance();

        System.out.println("\n--- Time Progression ---");
        System.out.println("Current Date/Time: " + clock.toString());
        System.out.println("Current Period: " + control.RegistrationControl.getCurrentTimePeriod());
        System.out.println("1. Advance by hours");
        System.out.println("2. Advance by days");
        System.out.println("3. Set specific date and time");
        System.out.println("0. Back");
        System.out.print("Choice: ");

        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());
            switch (choice) {
                case 1:
                    System.out.print("Enter number of hours to advance: ");
                    long hours = Long.parseLong(scanner.nextLine().trim());
                    clock.advanceHours(hours);
                    System.out.println("Clock advanced. Now: " + clock.toString());
                    break;
                case 2:
                    System.out.print("Enter number of days to advance: ");
                    long days = Long.parseLong(scanner.nextLine().trim());
                    clock.advanceDays(days);
                    System.out.println("Clock advanced. Now: " + clock.toString());
                    break;
                case 3:
                    System.out.print("Enter date and time (YYYY-MM-DD HH:MM): ");
                    String input = scanner.nextLine().trim();
                    String[] parts = input.split(" ");
                    if (parts.length == 2) {
                        String[] dateParts = parts[0].split("-");
                        String[] timeParts = parts[1].split(":");
                        clock.setDateTime(
                            Integer.parseInt(dateParts[0]),
                            Integer.parseInt(dateParts[1]),
                            Integer.parseInt(dateParts[2]),
                            Integer.parseInt(timeParts[0]),
                            Integer.parseInt(timeParts[1])
                        );
                        System.out.println("Clock set. Now: " + clock.toString());
                    } else {
                        System.out.println("Invalid format. Use: YYYY-MM-DD HH:MM");
                    }
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
        }
        System.out.println("Period: " + control.RegistrationControl.getCurrentTimePeriod());
    }
}
