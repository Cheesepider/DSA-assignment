/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utility;

/**
 * @author Tham Cle Ment
 */

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class VirtualClock {

    private static VirtualClock instance;

    // Real system time when the virtual clock was last adjusted
    private LocalDateTime realStartTime;

    // Virtual time corresponding to realStartTime
    private LocalDateTime virtualStartTime;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private VirtualClock() {
        // Fixed demo start date: 2026-08-15 at midnight
        this.realStartTime = LocalDateTime.now();
        this.virtualStartTime = LocalDateTime.of(2026, 8, 15, 0, 0, 0);
    }

    public static synchronized VirtualClock getInstance() {
        if (instance == null) {
            instance = new VirtualClock();
        }
        return instance;
    }

    // --- Read Methods ---

    public LocalDateTime now() {
        Duration elapsed =
                Duration.between(realStartTime, LocalDateTime.now());

        return virtualStartTime.plus(elapsed);
    }

    public LocalDate today() {
        return now().toLocalDate();
    }

    public LocalTime time() {
        return now().toLocalTime();
    }

    // --- Control Methods for Presentation / Demo ---

    public void advanceHours(long hours) {
        setVirtualTime(now().plusHours(hours));
    }

    public void advanceDays(long days) {
        setVirtualTime(now().plusDays(days));
    }

    public void setDateTime(
            int year,
            int month,
            int day,
            int hour,
            int minute) {

        setVirtualTime(
                LocalDateTime.of(
                        year,
                        month,
                        day,
                        hour,
                        minute
                )
        );
    }

    public void setTimeOfDay(int hour, int minute) {
        setVirtualTime(
                LocalDateTime.of(
                        today(),
                        LocalTime.of(hour, minute)
                )
        );
    }

    // Reset the reference point of the virtual clock and auto-sync all booking statuses
    private void setVirtualTime(LocalDateTime newVirtualTime) {
        this.realStartTime = LocalDateTime.now();
        this.virtualStartTime = newVirtualTime;
        // Automatically reconcile booking statuses to prevent flooding on next operation
        BookingStatusSyncManager.syncAll(newVirtualTime.toLocalDate());
    }

    @Override
    public String toString() {
        return now().format(FORMATTER);
    }
}