package boundary;

import adt.ListInterface;
import control.HousekeepingControl;
import entity.Room;
import entity.TaskLog;
import utility.VirtualClock;

import java.time.LocalDate;
import java.util.Scanner;

public class HousekeepingUI {

    public void displayHousekeepingMenu() {
        Scanner scanner = new Scanner(System.in);
        int choice = -1;

        while (choice != 0) {
            System.out.println("\n==========================================");
            System.out.println("   HOUSEKEEPING & TASK LOG MODULE  ");
            System.out.println("  Time: " + VirtualClock.getInstance().toString());
            System.out.println("==========================================\n");
            System.out.println("1. Task List");
            System.out.println("2. Update Room Status (Advance Workflow)");
            System.out.println("3. Undo Last Status Update (Instantly Roll Back)");
            System.out.println("==========================================");
            System.out.println("4. Generate Cleaning History & Summary Report");
            System.out.println("5. Generate Pending Task Report");
            System.out.println("==========================================");
            System.out.println("9. Advance Time");
            System.out.println("0. Back to Main Menu");
            System.out.print("Please select an option: ");

            try {
                choice = Integer.parseInt(scanner.nextLine().trim());
            } catch (Exception e) {
                choice = -1;
            }

            switch (choice) {
                case 1:
                    viewPendingTasks();
                    break;
                case 2:
                    updateRoomStatusUI(scanner);
                    break;
                case 3:
                    undoLastUpdateUI();
                    break;
                case 4:
                    generateHistorySummaryReportUI(scanner);
                    break;
                case 5:
                    generatePendingTaskReportUI(scanner);
                    break;
                case 9:
                    TimeProgressionUI.showTimeMenu();
                    break;
                case 0:
                    System.out.println("Exiting Housekeeping Module...");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private void viewPendingTasks() {
        ListInterface<Room> pendingRooms = HousekeepingControl.getPendingRooms();
        System.out.println("\n--- Waiting for Cleaning Rooms ---");
        if (pendingRooms.isEmpty()) {
            System.out.println("All rooms are clean or occupied! No pending tasks.");
            return;
        }

        System.out.printf("%-5s | %-10s | %-20s\n", "No.", "Room No.", "Current Status");
        System.out.println("---------------------------------------------");
        for (int i = 1; i <= pendingRooms.getNumberOfEntries(); i++) {
            Room r = pendingRooms.getEntry(i);
            System.out.printf("%-5d | %-10s | %-20s\n", i, r.getRoomNumber(), r.getRoomStatus());
        }
    }

    private void updateRoomStatusUI(Scanner scanner) {
        ListInterface<Room> pendingRooms = HousekeepingControl.getPendingRooms();
        if (pendingRooms.isEmpty()) {
            System.out.println("No rooms require status updates currently.");
            return;
        }

        viewPendingTasks();
        System.out.print("\nEnter the No. of the room to advance its status (or 0 to cancel): ");
        try {
            int selection = Integer.parseInt(scanner.nextLine().trim());
            if (selection == 0) return;

            if (selection > 0 && selection <= pendingRooms.getNumberOfEntries()) {
                Room selectedRoom = pendingRooms.getEntry(selection);
                Room.RoomStatus oldStatus = selectedRoom.getRoomStatus();
                
                boolean success = HousekeepingControl.advanceRoomStatus(selectedRoom);
                
                if (success) {
                    System.out.println("Success! Room " + selectedRoom.getRoomNumber() + 
                                       " advanced from [" + oldStatus + "] to [" + selectedRoom.getRoomStatus() + "].");
                } else {
                    System.out.println("Failed to advance status. (Occupied or Ready rooms cannot be advanced here).");
                }
            } else {
                System.out.println("Invalid selection.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
        }
    }

    private void undoLastUpdateUI() {
        System.out.println("\n--- Undo Last Action ---");
        TaskLog undoneLog = HousekeepingControl.undoLastAction();
        
        if (undoneLog != null) {
            System.out.println("Action successfully rolled back!");
            System.out.println("Room " + undoneLog.getRoom().getRoomNumber() + 
                               " has been reverted from [" + undoneLog.getNewStatus() + 
                               "] back to [" + undoneLog.getOldStatus() + "].");
        } else {
            System.out.println("No recent actions found to undo.");
        }
    }

    private void generatePendingTaskReportUI(Scanner scanner) {
        System.out.println("\n--- Pending Task Filter Options ---");
        
        System.out.println("Select Status Filter:");
        System.out.println("1. Dirty");
        System.out.println("2. Cleaning In Progress");
        System.out.println("3. Inspected");
        System.out.println("0. All Pending Statuses");
        System.out.print("Choice: ");
        String statusChoice = scanner.nextLine().trim();
        Room.RoomStatus sFilter = null;
        if (statusChoice.equals("1")) sFilter = Room.RoomStatus.Dirty;
        else if (statusChoice.equals("2")) sFilter = Room.RoomStatus.Cleaning_In_Progress;
        else if (statusChoice.equals("3")) sFilter = Room.RoomStatus.Inspected;

        System.out.println("Select Room Type Filter:");
        System.out.println("1. SINGLE");
        System.out.println("2. DOUBLE");
        System.out.println("3. SUITE");
        System.out.println("0. All Types");
        System.out.print("Choice: ");
        String typeChoice = scanner.nextLine().trim();
        Room.RoomType tFilter = null;
        if (typeChoice.equals("1")) tFilter = Room.RoomType.SINGLE;
        else if (typeChoice.equals("2")) tFilter = Room.RoomType.DOUBLE;
        else if (typeChoice.equals("3")) tFilter = Room.RoomType.SUITE;

        HousekeepingControl.generatePendingTaskReport(sFilter, tFilter);
    }

    private void generateHistorySummaryReportUI(Scanner scanner) {
        System.out.println("\n--- Historical Report Parameters ---");
        int year = VirtualClock.getInstance().today().getYear();
        int month = VirtualClock.getInstance().today().getMonthValue();

        System.out.print("Enter Year (YYYY) or press Enter for current year [" + year + "]: ");
        String yearStr = scanner.nextLine().trim();
        if (!yearStr.isEmpty()) {
            try { year = Integer.parseInt(yearStr); } catch (Exception ignored) {}
        }

        System.out.print("Enter Month (1-12) or press Enter for current month [" + month + "]: ");
        String monthStr = scanner.nextLine().trim();
        if (!monthStr.isEmpty()) {
            try { month = Integer.parseInt(monthStr); } catch (Exception ignored) {}
        }

        System.out.println("Select Room Type Filter (To analyze specific room sales):");
        System.out.println("1. SINGLE");
        System.out.println("2. DOUBLE");
        System.out.println("3. SUITE");
        System.out.println("0. All Types");
        System.out.print("Choice: ");
        String typeChoice = scanner.nextLine().trim();
        Room.RoomType tFilter = null;
        if (typeChoice.equals("1")) tFilter = Room.RoomType.SINGLE;
        else if (typeChoice.equals("2")) tFilter = Room.RoomType.DOUBLE;
        else if (typeChoice.equals("3")) tFilter = Room.RoomType.SUITE;

        HousekeepingControl.generateCleaningHistorySummaryReport(year, month, tFilter);
    }
}