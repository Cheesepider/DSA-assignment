package entity;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskLog {
    private static final AtomicInteger logIDCounter = new AtomicInteger(1);

    private int logID;
    private Room room;
    private Room.RoomStatus oldStatus;
    private Room.RoomStatus newStatus;
    private LocalDateTime actionTime;

    public TaskLog() {
    }

    public TaskLog(Room room, Room.RoomStatus oldStatus, Room.RoomStatus newStatus, LocalDateTime actionTime) {
        this.logID = logIDCounter.getAndIncrement();
        this.room = room;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.actionTime = actionTime;
    }

    public int getLogID() { return logID; }
    public void setLogID(int logID) { this.logID = logID; }

    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }

    public Room.RoomStatus getOldStatus() { return oldStatus; }
    public void setOldStatus(Room.RoomStatus oldStatus) { this.oldStatus = oldStatus; }

    public Room.RoomStatus getNewStatus() { return newStatus; }
    public void setNewStatus(Room.RoomStatus newStatus) { this.newStatus = newStatus; }

    public LocalDateTime getActionTime() { return actionTime; }
    public void setActionTime(LocalDateTime actionTime) { this.actionTime = actionTime; }

    @Override
    public String toString() {
        return "TaskLog{" +
                "logID=" + logID +
                ", room=" + (room != null ? room.getRoomNumber() : "null") +
                ", oldStatus=" + oldStatus +
                ", newStatus=" + newStatus +
                ", actionTime=" + actionTime +
                '}';
    }
}