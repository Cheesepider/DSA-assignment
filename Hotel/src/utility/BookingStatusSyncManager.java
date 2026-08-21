/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utility;

/**
 * BookingStatusSyncManager
 *
 * Automatically reconciles all booking statuses whenever the VirtualClock is
 * manipulated (advanced or set to a new date/time). This prevents a flood of
 * status-change messages from accumulating when time is advanced without
 * manually processing bookings through the front-desk queue.
 *
 * Status transitions handled silently on clock change:
 *   1. No-show detection:
 *      CONFIRMED bookings whose checkInDate has passed today
 *      → set CANCELLED, move to bookingHistoryList, free room, resolve waitlist
 *
 *   2. Auto-checkout (overstay / forgotten check-out):
 *      CHECKED_IN bookings whose checkOutDate has passed today
 *      → set CHECKED_OUT, set room to Dirty, move to bookingHistoryList,
 *        resolve waitlist
 *
 * The sync runs silently — it does NOT print per-booking spam.  A single
 * summary line is printed at the end if any bookings were processed.
 *
 * @author Tham Cle Ment
 */

import entity.Booking;
import entity.Room;
import main.App;
import java.time.LocalDate;

public class BookingStatusSyncManager {

    /**
     * Called automatically by VirtualClock every time the virtual time
     * changes. Walks the active bookingList and silently applies any
     * overdue status transitions.
     *
     * @param today the new virtual date after the clock change
     */
    public static void syncAll(LocalDate today) {
        int noShows = 0;

        // No-show detection: CONFIRMED bookings whose check-in date has passed
        // are cancelled automatically. CHECKED_IN bookings are intentionally
        // left alone — overstay charges are calculated when the guest manually
        // checks out through the front-desk queue.
        for (int i = 1; i <= App.bookingList.getNumberOfEntries(); i++) {
            Booking b = App.bookingList.getEntry(i);
            if (b.getBookingStatus() == Booking.BookingStatus.CONFIRMED
                    && b.getCheckInDate() != null
                    && b.getCheckInDate().isBefore(today)) {

                // No-show: cancel and move to history
                b.setBookingStatus(Booking.BookingStatus.CANCELLED);
                App.bookingHistoryList.add(b);
                App.bookingList.remove(i);
                i--;
                noShows++;

                // Free the room and resolve the priority waitlist
                Room room = b.getRoom();
                if (room != null) {
                    resolveWaitlistForRoom(room);
                }
            }
        }

        // --- Summary line (only printed if something changed) ---
        if (noShows > 0) {
            System.out.println("\n[System Auto-Sync] Time advanced to " + today + ".");
            System.out.println("  • " + noShows + " no-show booking(s) automatically cancelled.");
            System.out.println("[System Auto-Sync] Booking statuses are up to date.\n");
        }
    }

    /**
     * Resolves the priority waitlist for a given room by delegating to
     * PriorityAllocationControl. Prints a brief notice if a waitlisted
     * booking was promoted to CONFIRMED.
     */
    private static void resolveWaitlistForRoom(Room freedRoom) {
        if (freedRoom == null) {
            return;
        }
        control.PriorityAllocationControl pac = new control.PriorityAllocationControl();
        Booking promoted = pac.allocateFreedRoom(freedRoom);
        if (promoted != null) {
            System.out.println("  [Waitlist] " + promoted.getMember().getMemberName()
                    + " (" + promoted.getMember().getLoyaltyTier() + ")"
                    + " promoted to CONFIRMED for Room " + freedRoom.getRoomNumber() + ".");
        }
    }
}
