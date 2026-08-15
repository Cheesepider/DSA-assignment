/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;


public class Booking {

    public enum BookingStatus {
        PENDING,
        CONFIRMED,
        CHECKED_IN,
        CHECKED_OUT,
        CANCELLED,
        COMPLETED
    }

    private static final AtomicInteger bookingIDCounter = new AtomicInteger(1);

    private int bookingID;
    private LocalDate bookingDate;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private LocalDateTime registrationTime;
    private BookingStatus bookingStatus;

    private Member member;
    private Room room;

    public Booking() {
    }

    // partial constructor
    public Booking(LocalDate bookingDate, LocalDate checkInDate,
                    LocalDate checkOutDate, LocalDateTime registrationTime,
                    BookingStatus bookingStatus, Member member, Room room) {

        this.bookingID = bookingIDCounter.getAndIncrement();
        this.bookingDate = bookingDate;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.registrationTime = registrationTime;
        this.bookingStatus = bookingStatus;
        this.member = member;
        this.room = room;
    }

    // full constructor for Booking class
    public Booking(int bookingID, LocalDate bookingDate,
                   LocalDate checkInDate, LocalDate checkOutDate,
                   LocalDateTime registrationTime, BookingStatus bookingStatus, Member member, Room room) {

        this.bookingID = bookingID;
        this.bookingDate = bookingDate;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.registrationTime = registrationTime;
        this.bookingStatus = bookingStatus;
        this.member = member;
        this.room = room;
    }

    public int getBookingID() {
        return bookingID;
    }

    public void setBookingID(int bookingID) {
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

    public BookingStatus getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(BookingStatus bookingStatus) {
        this.bookingStatus = bookingStatus;
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
    public String toString() {
        return "Booking{" +
                "bookingID='" + bookingID + '\'' +
                ", bookingDate='" + bookingDate + '\'' +
                ", checkInDate='" + checkInDate + '\'' +
                ", checkOutDate='" + checkOutDate + '\'' +
                ", registrationTime='" + registrationTime + '\'' +
                ", bookingStatus=" + bookingStatus +
                ", member=" + member +
                ", room=" + room +
                '}';
    }
}
