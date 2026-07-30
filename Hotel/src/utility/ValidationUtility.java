/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utility;

/**
 *
 * @author jlohz
 */
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class ValidationUtility {

    public static int inputChoice(Scanner scanner) {

        while (true) {
            try {
                int choice = Integer.parseInt(scanner.nextLine());
                return choice;

            } catch (NumberFormatException e) {

                System.out.print("Invalid input. Please enter a number: ");
            }
        }
    }

    public static LocalDate inputDate(Scanner scanner, String prompt) {

        while (true) {
            try {
                System.out.print(prompt);
                return LocalDate.parse(scanner.nextLine());

            } catch (DateTimeParseException e) {

                System.out.println("Invalid date format. Please use yyyy-MM-dd.");
            }
        }
    }

    public static String inputBookingID(Scanner scanner) {

        while (true) {

            System.out.print("Enter Booking ID: ");
            String bookingID = scanner.nextLine().trim();

            if (!bookingID.isEmpty()) {
                return bookingID;
            }

            System.out.println("Booking ID cannot be empty.");
        }
    }

    public static String inputMemberID(Scanner scanner) {

        while (true) {

            System.out.print("Enter Member ID: ");

            String memberID = scanner.nextLine().trim();

            if (!memberID.isEmpty()) {
                return memberID;
            }

            System.out.println("Member ID cannot be empty.");
        }
    }
}
