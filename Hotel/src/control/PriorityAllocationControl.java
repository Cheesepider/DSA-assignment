/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package control;

import entity.Booking;
import adt.DoublyLinkedList;

/**
 *
 * @author jlohz
 */
public class PriorityAllocationControl {

    private DoublyLinkedList<Booking> bookingList;

    public PriorityAllocationControl() {
        bookingList = new DoublyLinkedList<>();
    }

    public void addBooking(Booking newBooking) {
        bookingList.add(newBooking);
    }

    private int getTierPriority(String loyaltyTier) {
        if (loyaltyTier.equals("Elite")) {
            return 4;
        } else if (loyaltyTier.equals("Diamond")) {
            return 3;
        } else if (loyaltyTier.equals("Platinum")) {
            return 2;
        } else if (loyaltyTier.equals("Standard")) {
            return 1;
        } else {
            throw new IllegalArgumentException("Invalid loyalty tier");
        }
    }

    private int comparePriority(Booking bookingA, Booking bookingB) {

        int tierA = getTierPriority(
                bookingA.getMember().getLoyaltyTier()
        );

        int tierB = getTierPriority(
                bookingB.getMember().getLoyaltyTier()
        );

        // First priority: Loyalty Tier
        if (tierA > tierB) {
            return 1;
        } else if (tierA < tierB) {
            return -1;
        }

        // Second priority: Earlier Booking Date
        if (bookingA.getBookingDate().isBefore(
                bookingB.getBookingDate())) {
            return 1;
        } else if (bookingA.getBookingDate().isAfter(
                bookingB.getBookingDate())) {
            return -1;
        }

        // Same Tier and same Booking Date
        return 0;
    }
}
