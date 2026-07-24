/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entity;

/**
 *
 * @author jlohz
 */
public class Room {

    private String roomID;
    private String roomType;
    private String roomStatus;

    public Room() {
    }

    public Room(String roomID, String roomType, String roomStatus) {
        this.roomID = roomID;
        this.roomType = roomType;
        this.roomStatus = roomStatus;
    }

    public String getRoomID() {
        return roomID;
    }

    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public String getRoomStatus() {
        return roomStatus;
    }

    public void setRoomStatus(String roomStatus) {
        this.roomStatus = roomStatus;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Room other = (Room) obj;
        return roomID.equals(other.roomID);
    }

    @Override
    public String toString() {
        return "Room{" +
                "roomID='" + roomID + '\'' +
                ", roomType='" + roomType + '\'' +
                ", roomStatus='" + roomStatus + '\'' +
                '}';
    }
    
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Room otherRoom = (Room) obj;
        return this.roomID.equals(otherRoom.roomID);
    }
}
