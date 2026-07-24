package boundary;

import control.HousekeepingControl;
import entity.Room;
import adt.ListInterface;
import java.util.Scanner;

public class HousekeepingUI {
    
    private HousekeepingControl control = new HousekeepingControl();
    private Scanner scanner = new Scanner(System.in);

    public void startUI() {
        int choice = -1;
        while (choice != 0) {
            System.out.println("\n===============================================");
            System.out.println("      Housekeeping & Task Log System");
            System.out.println("===============================================");
            System.out.println("1. View All Rooms Status");
            System.out.println("2. Update Room Status");
            System.out.println("3. Undo Last Status Update (Rollback)");
            System.out.println("4. Generate Housekeeping Report (Coming Soon)");
            System.out.println("0. Exit to Main Menu");
            System.out.println("===============================================");
            System.out.print("Enter your choice: ");
            
            choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    displayAllRooms();
                    break;
                case 2:
                    updateStatusMenu();
                    break;
                case 3:
                    undoMenu();
                    break;
                case 4:
                    System.out.println("Report module will be implemented next.");
                    break;
                case 0:
                    System.out.println("Exiting Housekeeping System...");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void displayAllRooms() {
        System.out.println("\n--- Current Room Status ---");
        ListInterface<Room> rooms = control.getRoomList();
        try {
            for (int i = 1; i <= rooms.getNumberOfEntries(); i++) {
                System.out.println(rooms.getEntry(i).toString());
            }
        } catch (Exception e) {
            System.out.println("[Notice] ADT is not yet implemented by the team. Cannot display list.");
        }
    }

    private void updateStatusMenu() {
        System.out.print("Enter Room ID to update (e.g., 101): ");
        String roomId = scanner.nextLine();
        
        System.out.println("Select New Status:");
        System.out.println("A. Dirty");
        System.out.println("B. Cleaning In Progress");
        System.out.println("C. Inspected");
        System.out.println("D. Ready for Check-In");
        System.out.print("Choice: ");
        String statusChoice = scanner.nextLine().toUpperCase();
        
        String newStatus = "";
        switch (statusChoice) {
            case "A": newStatus = "Dirty"; break;
            case "B": newStatus = "Cleaning In Progress"; break;
            case "C": newStatus = "Inspected"; break;
            case "D": newStatus = "Ready"; break;
            default: System.out.println("Invalid status."); return;
        }

        try {
            boolean success = control.updateRoomStatus(roomId, newStatus);
            if (success) {
                System.out.println("Room " + roomId + " status updated to: " + newStatus);
            } else {
                System.out.println("Room ID not found.");
            }
        } catch (Exception e) {
             System.out.println("[Notice] ADT is not yet implemented by the team. Cannot update.");
        }
    }

    private void undoMenu() {
        try {
            String result = control.undoLastAction();
            System.out.println(result);
        } catch (Exception e) {
            System.out.println("[Notice] ADT is not yet implemented by the team. Cannot undo.");
        }
    }

    public static void main(String[] args) {
        HousekeepingUI ui = new HousekeepingUI();
        ui.startUI();
    }
}