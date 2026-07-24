package control;

import adt.DoublyLinkedList;
import adt.ListInterface;
import entity.Room;

public class HousekeepingControl {
    
    private ListInterface<Room> roomList;
    private ListInterface<String> actionLog;

    public HousekeepingControl() {
        roomList = new DoublyLinkedList<>();
        actionLog = new DoublyLinkedList<>();
        
        roomList.add(new Room("101", "Standard", "Dirty"));
        roomList.add(new Room("102", "Deluxe", "Cleaning In Progress"));
        roomList.add(new Room("103", "VIP", "Ready"));
    }

    public ListInterface<Room> getRoomList() {
        return roomList;
    }

    public boolean updateRoomStatus(String roomID, String newStatus) {
        for (int i = 1; i <= roomList.getNumberOfEntries(); i++) {
            Room currentRoom = roomList.getEntry(i);
            if (currentRoom.getRoomID().equals(roomID)) {

                String logEntry = roomID + ":" + currentRoom.getRoomStatus();
                actionLog.add(logEntry); 
                
                currentRoom.setRoomStatus(newStatus);
                return true;
            }
        }
        return false;
    }

    public String undoLastAction() {
        if (actionLog.isEmpty()) {
            return "No actions to undo.";
        }
        
        int lastIndex = actionLog.getNumberOfEntries();
        String lastLog = actionLog.remove(lastIndex); 
        
        String[] parts = lastLog.split(":");
        String targetRoomID = parts[0];
        String previousStatus = parts[1];
        
        for (int i = 1; i <= roomList.getNumberOfEntries(); i++) {
            Room room = roomList.getEntry(i);
            if (room.getRoomID().equals(targetRoomID)) {
                room.setRoomStatus(previousStatus);
                return "Successfully rolled back Room " + targetRoomID + " to '" + previousStatus + "'.";
            }
        }
        return "Undo failed.";
    }
}