package control;

import adt.DoublyLinkedList;
import adt.ListInterface;
import entity.Room;
import entity.TaskLog;
import main.App;
import utility.VirtualClock;

import java.time.LocalDate;

public class HousekeepingControl {

    public static ListInterface<Room> getPendingRooms() {
        ListInterface<Room> pendingRooms = new DoublyLinkedList<>();
        for (int i = 1; i <= App.roomList.getNumberOfEntries(); i++) {
            Room r = App.roomList.getEntry(i);
            if (r.getRoomStatus() != Room.RoomStatus.Ready_for_Check_In && 
                r.getRoomStatus() != Room.RoomStatus.Occupied) {
                pendingRooms.add(r);
            }
        }
        return pendingRooms;
    }

    public static boolean advanceRoomStatus(Room room) {
        Room.RoomStatus oldStatus = room.getRoomStatus();
        Room.RoomStatus newStatus = null;

        if (oldStatus == Room.RoomStatus.Dirty) {
            newStatus = Room.RoomStatus.Cleaning_In_Progress;
        } else if (oldStatus == Room.RoomStatus.Cleaning_In_Progress) {
            newStatus = Room.RoomStatus.Inspected;
        } else if (oldStatus == Room.RoomStatus.Inspected) {
            newStatus = Room.RoomStatus.Ready_for_Check_In;
        } else {
            return false; 
        }

        room.setRoomStatus(newStatus);

        TaskLog log = new TaskLog(room, oldStatus, newStatus, VirtualClock.getInstance().now());
        App.taskLogStack.add(1, log); 

        if (newStatus == Room.RoomStatus.Ready_for_Check_In) {
            App.cleaningHistoryList.add(log);
        }
        return true;
    }

    public static TaskLog undoLastAction() {
        if (App.taskLogStack.isEmpty()) {
            return null; 
        }

        TaskLog lastLog = App.taskLogStack.remove(1);
        Room room = lastLog.getRoom();

        room.setRoomStatus(lastLog.getOldStatus());

        if (lastLog.getNewStatus() == Room.RoomStatus.Ready_for_Check_In) {
            for (int i = 1; i <= App.cleaningHistoryList.getNumberOfEntries(); i++) {
                if (App.cleaningHistoryList.getEntry(i).getLogID() == lastLog.getLogID()) {
                    App.cleaningHistoryList.remove(i);
                    break;
                }
            }
        }
        return lastLog;
    }

    public static void generatePendingTaskReport(Room.RoomStatus statusFilter, Room.RoomType typeFilter) {
        System.out.println("\n=======================================================");
        System.out.println("          PENDING CLEANING TASKS REPORT                ");
        System.out.println("=======================================================");
        
        ListInterface<Room> filteredList = new DoublyLinkedList<>();
        
        for (int i = 1; i <= App.roomList.getNumberOfEntries(); i++) {
            Room r = App.roomList.getEntry(i);
            
            if (r.getRoomStatus() == Room.RoomStatus.Ready_for_Check_In || r.getRoomStatus() == Room.RoomStatus.Occupied) {
                continue;
            }
            
            boolean matchStatus = (statusFilter == null) || (r.getRoomStatus() == statusFilter);
            boolean matchType = (typeFilter == null) || (r.getRoomType() == typeFilter);
            
            if (matchStatus && matchType) {
                filteredList.add(r);
            }
        }

        if (filteredList.isEmpty()) {
            System.out.println("No pending tasks match the selected filters.");
            return;
        }

        sortRoomsByBaseRateDescending(filteredList);

        boolean isDelayed = VirtualClock.getInstance().time().isAfter(java.time.LocalTime.of(15, 0));

        System.out.printf("%-10s | %-15s | %-15s | %-20s\n", "Room No.", "Room Type", "Base Rate", "Priority/Status");
        System.out.println("-----------------------------------------------------------------------");
        for (int i = 1; i <= filteredList.getNumberOfEntries(); i++) {
            Room r = filteredList.getEntry(i);
            String statusStr = r.getRoomStatus().toString();
            
            if (isDelayed) {
                statusStr = "[DELAYED] " + statusStr;
            }
            
            System.out.printf("%-10s | %-15s | $%-14.2f | %-20s\n", 
                r.getRoomNumber(), r.getRoomType(), r.getRoomType().getBaseRate(), statusStr);
        }
        System.out.println("-----------------------------------------------------------------------");
        System.out.println("Total Pending Rooms: " + filteredList.getNumberOfEntries());
        if (isDelayed) {
            System.out.println("\n*** WARNING: It is past 15:00 Check-in time. Housekeeping is behind schedule! ***");
        }
    }

    public static void generateCleaningHistorySummaryReport(int year, int month, Room.RoomType typeFilter) {
        System.out.println("\n=======================================================");
        System.out.println("       CLEANING HISTORY & BUSINESS SUMMARY REPORT      ");
        System.out.printf("               Period: %04d-%02d\n", year, month);
        System.out.println("=======================================================");

        ListInterface<TaskLog> filteredList = new DoublyLinkedList<>();
        int[] dailyCounts = new int[32]; 
        int totalCleaned = 0;

        for (int i = 1; i <= App.cleaningHistoryList.getNumberOfEntries(); i++) {
            TaskLog log = App.cleaningHistoryList.getEntry(i);
            LocalDate date = log.getActionTime().toLocalDate();
            
            if (date.getYear() == year && date.getMonthValue() == month) {
                boolean matchType = (typeFilter == null) || (log.getRoom().getRoomType() == typeFilter);
                if (matchType) {
                    filteredList.add(log);
                    dailyCounts[date.getDayOfMonth()]++;
                    totalCleaned++;
                }
            }
        }

        if (filteredList.isEmpty()) {
            System.out.println("No cleaning records found for the specified period/filters.");
            return;
        }

        System.out.println("\n--- ROOM FREQUENCY SUMMARY (Sales Indicator) ---");
        System.out.printf("Total rooms cleaned in this period: %d\n", totalCleaned);
        
        for (int i = 1; i <= App.roomList.getNumberOfEntries(); i++) {
            Room currentRoom = App.roomList.getEntry(i);
            if (typeFilter != null && currentRoom.getRoomType() != typeFilter) continue; 

            int count = 0;
            for (int j = 1; j <= filteredList.getNumberOfEntries(); j++) {
                if (filteredList.getEntry(j).getRoom().getRoomID() == currentRoom.getRoomID()) {
                    count++;
                }
            }
            if (count > 0) {
                System.out.printf("Room %-5s (%-6s) was cleaned %2d times.\n", 
                                  currentRoom.getRoomNumber(), currentRoom.getRoomType(), count);
            }
        }

        System.out.println("\n--- DAILY CLEANING VOLUME TREND ---");

        for (int day = 1; day <= 31; day++) {
            if (dailyCounts[day] > 0 || (day <= LocalDate.of(year, month, 1).lengthOfMonth() && totalCleaned > 0)) {
                System.out.printf("Day %02d | %2d | ", day, dailyCounts[day]);
                for (int star = 0; star < dailyCounts[day]; star++) {
                    System.out.print("■ "); 
                }
                System.out.println();
            }
        }
        System.out.println("-------------------------------------------------------");
        System.out.println("End of Report.");
    }

    private static void sortRoomsByBaseRateDescending(ListInterface<Room> list) {
        int n = list.getNumberOfEntries();
        for (int i = 1; i <= n - 1; i++) {
            int maxIndex = i;
            for (int j = i + 1; j <= n; j++) {
                Room r1 = list.getEntry(j);
                Room rMax = list.getEntry(maxIndex);                

                if (r1.getRoomType().getBaseRate() > rMax.getRoomType().getBaseRate()) {
                    maxIndex = j;
                }
            }
            if (maxIndex != i) {
                Room temp = list.getEntry(maxIndex);
                list.replace(maxIndex, list.getEntry(i));
                list.replace(i, temp);
            }
        }
    }
}