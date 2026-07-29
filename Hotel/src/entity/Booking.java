/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entity;

/**
 *
 * @author jlohz
 */
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Booking {

    private String bookingID;
    private LocalDate bookingDate;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private LocalDateTime registrationTime;

    private Member member;
    private Room room;

    public Booking() {
    }

    public Booking(String bookingID,
            LocalDate bookingDate,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            LocalDateTime registrationTime,
            Member member,
            Room room) {

        this.bookingID = bookingID;
        this.bookingDate = bookingDate;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.registrationTime = registrationTime;
        this.member = member;
        this.room = room;
    }

    public String getBookingID() {
        return bookingID;
    }

    public void setBookingID(String bookingID) {
        this.bookingID = bookingID;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public LocalDateTime getRegistrationTime() {
        return registrationTime;
    }

    public void setRegistrationTime(LocalDateTime registrationTime) {
        this.registrationTime = registrationTime;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Booking other = (Booking) obj;
        return bookingID.equals(other.bookingID);
    }

    @Override
    public String toString() {
        return "Booking{"
                + "bookingID='" + bookingID + '\''
                + ", bookingDate='" + bookingDate + '\''
                + ", checkInDate='" + checkInDate + '\''
                + ", checkOutDate='" + checkOutDate + '\''
                + ", member=" + member
                + ", room=" + room
                + '}';
    }
}
