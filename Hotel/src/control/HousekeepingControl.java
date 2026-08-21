// Author: Lee Shen Fung
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
        // ADT declaration
        
        ListInterface<Room> pendingRooms = new DoublyLinkedList<>();
        
        // ADT method call (getNumberOfEntries)
        
        for (int i = 1; i <= App.roomList.getNumberOfEntries(); i++) {
            // ADT method call (getEntry)
            
            Room r = App.roomList.getEntry(i);
            if (r.getRoomStatus() != Room.RoomStatus.Ready_for_Check_In && 
                r.getRoomStatus() != Room.RoomStatus.Occupied) {
                // ADT method call (add)
                
                pendingRooms.add(r);
            }
        }
        return pendingRooms;
    }

    public static boolean advanceRoomStatus(Room room, String staffName) {
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

        if (staffName == null || staffName.isEmpty()) {
            // ADT method call (getNumberOfEntries)
            
            for (int i = 1; i <= App.taskLogStack.getNumberOfEntries(); i++) {
                // ADT method call (getEntry)
                
                TaskLog prevLog = App.taskLogStack.getEntry(i);
                if (prevLog.getRoom().getRoomID() == room.getRoomID()) {
                    staffName = prevLog.getCleanerName(); 
                    break;
                }
            }
            if (staffName == null || staffName.isEmpty()) staffName = "Unknown Staff";
        }

        room.setRoomStatus(newStatus);
        TaskLog log = new TaskLog(room, oldStatus, newStatus, VirtualClock.getInstance().now(), staffName);
        
        // ADT method call (add with position for Stack logic)
        
        App.taskLogStack.add(1, log); 

        if (newStatus == Room.RoomStatus.Ready_for_Check_In) {
            // ADT method call (add)
            
            App.cleaningHistoryList.add(log);
        }
        return true;
    }

    public static TaskLog undoLastAction() {
        // ADT method call (isEmpty)
        
        if (App.taskLogStack.isEmpty()) {
            return null; 
        }

        // ADT method call (remove for Stack pop logic)
        
        TaskLog lastLog = App.taskLogStack.remove(1);
        Room room = lastLog.getRoom();

        room.setRoomStatus(lastLog.getOldStatus());

        if (lastLog.getNewStatus() == Room.RoomStatus.Ready_for_Check_In) {
            // ADT method call (getNumberOfEntries)
            
            for (int i = 1; i <= App.cleaningHistoryList.getNumberOfEntries(); i++) {
                // ADT method call (getEntry)
                
                if (App.cleaningHistoryList.getEntry(i).getLogID() == lastLog.getLogID()) {
                    // ADT method call (remove)
                    
                    App.cleaningHistoryList.remove(i);
                    break;
                }
            }
        }
        return lastLog;
    }

    public static void generatePendingTaskReport(Room.RoomStatus statusFilter, Room.RoomType typeFilter) {
        System.out.println("\n                        TUNKU ABDUL RAHMAN UNIVERSITY OF MANAGEMENT AND TECHNOLOGY");
        System.out.println("                                         TARUMT RESORTS");
        System.out.println("                                 HOUSEKEEPING MODULE SUBSYSTEM\n");
        System.out.println("                                SUMMARY OF PENDING TASKS REPORT");
        System.out.println("                        -----------------------------------------------------------");
        System.out.println("Generated at: " + VirtualClock.getInstance().toString());
        System.out.println("********************************************************************************************************");
        System.out.println("TUNKU ABDUL RAHMAN UNIVERSITY OF MANAGEMENT AND TECHNOLOGY HIGHLY CONFIDENTIAL DOCUMENT");
        System.out.println("========================================================================================================");
        
        // ADT declaration
        
        ListInterface<Room> filteredList = new DoublyLinkedList<>();
        
        // ADT method call (getNumberOfEntries)
        
        for (int i = 1; i <= App.roomList.getNumberOfEntries(); i++) {
            // ADT method call (getEntry)
            
            Room r = App.roomList.getEntry(i);
            
            if (r.getRoomStatus() == Room.RoomStatus.Ready_for_Check_In || r.getRoomStatus() == Room.RoomStatus.Occupied) {
                continue;
            }
            
            boolean matchStatus = (statusFilter == null) || (r.getRoomStatus() == statusFilter);
            boolean matchType = (typeFilter == null) || (r.getRoomType() == typeFilter);
            
            if (matchStatus && matchType) {
                // ADT method call (add)
                
                filteredList.add(r);
            }
        }

        // ADT method call (isEmpty)
        
        if (filteredList.isEmpty()) {
            System.out.println("No pending tasks match the selected filters.");
            return;
        }

        sortRoomsByBaseRateDescending(filteredList);
        boolean isDelayed = VirtualClock.getInstance().time().isAfter(java.time.LocalTime.of(15, 0));
        int delayedCount = 0;

        System.out.printf("%-10s | %-12s | %-10s | %-15s | %-20s\n", "Room No.", "Room Type", "Base Rate", "Assigned Staff", "Priority/Status");
        System.out.println("--------------------------------------------------------------------------------------------------------");
        
        // ADT method call (getNumberOfEntries)
        
        for (int i = 1; i <= filteredList.getNumberOfEntries(); i++) {
            // ADT method call (getEntry)
            
            Room r = filteredList.getEntry(i);
            String statusStr = r.getRoomStatus().toString();
            
            if (isDelayed) {
                statusStr = "[DELAYED] " + statusStr;
                delayedCount++;
            }

            String assignedStaff = "Unassigned";
            if (r.getRoomStatus() != Room.RoomStatus.Dirty) {
                // ADT method call (getNumberOfEntries)
                
                for (int j = 1; j <= App.taskLogStack.getNumberOfEntries(); j++) {
                    // ADT method call (getEntry)
                    
                    TaskLog tl = App.taskLogStack.getEntry(j);
                    if (tl.getRoom().getRoomID() == r.getRoomID()) {
                        assignedStaff = tl.getCleanerName();
                        break;
                    }
                }
            }
            
            System.out.printf("%-10s | %-12s | $%-9.2f | %-15s | %-20s\n", 
                r.getRoomNumber(), r.getRoomType(), r.getRoomType().getBaseRate(), assignedStaff, statusStr);
        }
        System.out.println("========================================================================================================");
        
        // ADT method call (getNumberOfEntries)
        
        System.out.println("Total Number of Pending Rooms : " + filteredList.getNumberOfEntries());
        System.out.println("Total Number of Delayed Rooms : " + delayedCount);
        
        if (isDelayed) {
            System.out.println("\n*** WARNING: It is past 15:00 Check-in time. Housekeeping is behind schedule! ***");
        }
    }

    public static void generateCleaningHistorySummaryReport(int year, int month, Room.RoomType typeFilter) {
        System.out.println("\n                        TUNKU ABDUL RAHMAN UNIVERSITY OF MANAGEMENT AND TECHNOLOGY");
        System.out.println("                                         TARUMT RESORTS");
        System.out.println("                                 HOUSEKEEPING MODULE SUBSYSTEM\n");
        System.out.println("                             SUMMARY OF KPI & CLEANING HISTORY REPORT");
        System.out.println("                        -----------------------------------------------------------");
        System.out.println("Generated at: " + VirtualClock.getInstance().toString() + "  |  Period: " + year + "-" + String.format("%02d", month));
        System.out.println("********************************************************************************************************");
        System.out.println("TUNKU ABDUL RAHMAN UNIVERSITY OF MANAGEMENT AND TECHNOLOGY HIGHLY CONFIDENTIAL DOCUMENT");
        System.out.println("========================================================================================================");

        // ADT declaration
        
        ListInterface<TaskLog> filteredList = new DoublyLinkedList<>();
        
        // ADT declaration
        
        ListInterface<String> staffNames = new DoublyLinkedList<>();
        // ADT declaration
        
        ListInterface<Integer> staffCounts = new DoublyLinkedList<>();

        int[] weeklyCounts = new int[4]; 
        int totalCleaned = 0;

        // ADT method call (getNumberOfEntries)
        
        for (int i = 1; i <= App.cleaningHistoryList.getNumberOfEntries(); i++) {
            // ADT method call (getEntry)
            
            TaskLog log = App.cleaningHistoryList.getEntry(i);
            LocalDate date = log.getActionTime().toLocalDate();
            
            if (date.getYear() == year && date.getMonthValue() == month) {
                boolean matchType = (typeFilter == null) || (log.getRoom().getRoomType() == typeFilter);
                if (matchType) {
                    // ADT method call (add)
                    
                    filteredList.add(log);
                    totalCleaned++;

                    int day = date.getDayOfMonth();
                    int weekIndex = (day - 1) / 7;
                    if (weekIndex > 3) weekIndex = 3; 
                    weeklyCounts[weekIndex]++;

                    // ADT method call (indexOf)
                    
                    int idx = staffNames.indexOf(log.getCleanerName());
                    if (idx == -1) {
                        // ADT method call (add)
                        
                        staffNames.add(log.getCleanerName());
                        // ADT method call (add)
                        
                        staffCounts.add(1);
                    } else {
                        // ADT method call (getEntry & replace)
                        
                        staffCounts.replace(idx, staffCounts.getEntry(idx) + 1);
                    }
                }
            }
        }

        // ADT method call (isEmpty)
        
        if (filteredList.isEmpty()) {
            System.out.println("No cleaning records found for the specified period/filters.");
            return;
        }

        System.out.printf("%-25s | %-15s\n", "Staff Name", "Rooms Cleaned");
        System.out.println("------------------------------------------------");
        
        // ADT method call (getNumberOfEntries)
        
        for (int i = 1; i <= staffNames.getNumberOfEntries(); i++) {
            // ADT method call (getEntry)
            
            System.out.printf("%-25s | %-15d\n", staffNames.getEntry(i), staffCounts.getEntry(i));
        }

        System.out.println("\nGRAPHICAL REPRESENTATION OF WEEKLY CLEANING VOLUME");
        System.out.println("   ^");
        
        int maxCount = 0;
        for (int count : weeklyCounts) {
            if (count > maxCount) maxCount = count;
        }

        for (int row = maxCount; row >= 1; row--) {
            System.out.printf("%2d | ", row);
            for (int w = 0; w < 4; w++) {
                if (weeklyCounts[w] >= row) {
                    System.out.print("  ██  ");
                } else {
                    System.out.print("      ");
                }
            }
            System.out.println();
        }
        
        System.out.println("   +--------------------------> Weeks");
        System.out.println("      Wk1   Wk2   Wk3   Wk4");
        
        System.out.println("========================================================================================================");
        System.out.println("Total Number of Rooms Cleaned : " + totalCleaned);
        // ADT method call (getNumberOfEntries)
        
        System.out.println("Total Number of Staff Involved : " + staffNames.getNumberOfEntries());
        System.out.println("END OF THE REPORT");
    }

    private static void sortRoomsByBaseRateDescending(ListInterface<Room> list) {
        // ADT method call (getNumberOfEntries)
        
        int n = list.getNumberOfEntries();
        for (int i = 1; i <= n - 1; i++) {
            int maxIndex = i;
            for (int j = i + 1; j <= n; j++) {
                // ADT method call (getEntry)
                
                Room r1 = list.getEntry(j);
                // ADT method call (getEntry)
                
                Room rMax = list.getEntry(maxIndex);                

                if (r1.getRoomType().getBaseRate() > rMax.getRoomType().getBaseRate()) {
                    maxIndex = j;
                }
            }
            if (maxIndex != i) {
                // ADT method call (swap)
                
                list.swap(i, maxIndex);
            }
        }
    }
}