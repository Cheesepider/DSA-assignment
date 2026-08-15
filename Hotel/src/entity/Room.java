/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entity;

import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author jlohz
 */
public class Room {
    private static final AtomicInteger roomIDCounter = new AtomicInteger(1);

    private int roomID;
    private String roomNumber;
    private RoomType roomType;
    private RoomStatus roomStatus;

    // added enum class for room status
    public enum RoomStatus {
        Dirty,
        Cleaning_In_Progress,
        Inspected,
        Ready_for_Check_In,
        Occupied,
    }

    public enum RoomType {
        SINGLE(100.0), // room types (room rate)
        DOUBLE(150.0),
        SUITE(300.0);

        private final double baseRate;

        RoomType(double baseRate) {
            this.baseRate = baseRate;
        }

        public double getBaseRate() {
            return baseRate;
        }
    }

    public Room() {
    }

    // new constructor for default room status
    public Room(String roomNumber, RoomType roomType) {
        this.roomID = roomIDCounter.getAndIncrement();
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.roomStatus = RoomStatus.Ready_for_Check_In; // Default status
    }

    // modified full constructor from string to enum for room status, and counter for ID
    public Room(String roomNumber, RoomType roomType, RoomStatus roomStatus) {
        this.roomID = roomIDCounter.getAndIncrement();
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.roomStatus = roomStatus;
    }

    // modified string > int
    public int getRoomID() {
        return roomID;
    }

    // modified
    public void setRoomID(int roomID) {
        this.roomID = roomID;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }

    public RoomStatus getRoomStatus() {
        return roomStatus;
    }

    public void setRoomStatus(RoomStatus roomStatus) {
        this.roomStatus = roomStatus;
    }

    @Override
    public String toString() {
        return "Room{" +
                "roomID='" + roomID + '\'' +
                ", roomNumber='" + roomNumber + '\'' +
                ", roomType='" + roomType + '\'' +
                ", roomStatus='" + roomStatus + '\'' +
                '}';
    }
}
