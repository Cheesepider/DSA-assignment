/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utility;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class VirtualClock {
    private static VirtualClock instance;
    private LocalDateTime currentTime;

    private VirtualClock() {
        // Initialize with real current time or a fixed default (e.g., today at 09:00 AM)
        this.currentTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(9, 0));
    }

    public static synchronized VirtualClock getInstance() {
        if (instance == null) {
            instance = new VirtualClock();
        }
        return instance;
    }

    // --- Read Methods ---
    public LocalDateTime now() {
        return currentTime;
    }

    public LocalDate today() {
        return currentTime.toLocalDate();
    }

    public LocalTime time() {
        return currentTime.toLocalTime();
    }

    // --- Control Methods for Presentation/Demo ---
    
    /** Advance clock by a set number of hours (e.g., advance 4 hours to cross 12 PM) */
    public void advanceHours(long hours) {
        this.currentTime = this.currentTime.plusHours(hours);
    }

    /** Advance clock by days (e.g., jump to check-out day) */
    public void advanceDays(long days) {
        this.currentTime = this.currentTime.plusDays(days);
    }

    /** Set specific time directly (e.g., "2026-08-07 14:30") */
    public void setDateTime(int year, int month, int day, int hour, int minute) {
        this.currentTime = LocalDateTime.of(year, month, day, hour, minute);
    }

    /** Set just the time for today (e.g., jump to 11:30 AM or 02:00 PM) */
    public void setTimeOfDay(int hour, int minute) {
        this.currentTime = LocalDateTime.of(this.currentTime.toLocalDate(), LocalTime.of(hour, minute));
    }

    @Override
    public String toString() {
        return currentTime.toString().replace("T", " ");
    }
}
