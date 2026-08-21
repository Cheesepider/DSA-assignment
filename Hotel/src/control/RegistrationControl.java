/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package control;

/**
 * @author Tham Cle Ment
 */

import java.util.Objects;
import utility.VirtualClock;
import adt.ListInterface;
import entity.Member;
import entity.Booking;
import entity.Room;
import java.time.LocalDate;
import main.App;

public class RegistrationControl {

    public enum TimePeriod {
        CHECKOUT, // 00:00 - 12:00
        HOUSEKEEPING, // 12:00 - 15:00
        CHECKIN // 15:00 - 23:59
    }

    public static TimePeriod getCurrentTimePeriod() {
        java.time.LocalTime nowTime = VirtualClock.getInstance().time();
        if (nowTime.isBefore(java.time.LocalTime.of(12, 0))) {
            return TimePeriod.CHECKOUT;
        } else if (nowTime.isBefore(java.time.LocalTime.of(15, 0))) {
            return TimePeriod.HOUSEKEEPING;
        } else {
            return TimePeriod.CHECKIN;
        }
    }
    // Control classes implement the business logic for use cases.
    // They orchestrate the execution of commands coming from boundary objects
    // by interacting with entity and boundary objects.

    // this is control class for registration module
    // for RegistrationControl, main use is to handle registration process
    // main tasks is registration of new customers, and check-in/check-out of
    // existing customers
    // methods methods methods methods methods
    // methods methods methods methods methods
    // methods methods methods methods methods
    // methods methods methods methods methods
    // methods methods methods methods methods
    // methods methods methods methods methods
    // list members
    public static void listMembers() {
        if (App.memberList.isEmpty()) {
            System.out.println("No members found.");
            return;
        }

        System.out.println("\n=========================================================================================================");
        System.out.println("                                            MEMBER LIST                                                  ");
        System.out.println("=========================================================================================================");
        System.out.printf("%-10s | %-20s | %-15s | %-25s | %-12s | %-14s\n",
                "Member ID", "Name", "Phone Number", "Email", "Loyalty Tier", "Loyalty Points");
        System.out.println("---------------------------------------------------------------------------------------------------------");
        for (int i = 1; i <= App.memberList.getNumberOfEntries(); i++) {
            Member member = App.memberList.getEntry(i);
            System.out.printf("%-10d | %-20s | %-15s | %-25s | %-12s | %-14d\n",
                    member.getMemberID(),
                    member.getMemberName() != null ? member.getMemberName() : "N/A",
                    member.getPhoneNumber() != null ? member.getPhoneNumber() : "N/A",
                    member.getEmail() != null ? member.getEmail() : "N/A",
                    member.getLoyaltyTier() != null ? member.getLoyaltyTier().toString() : "Regular",
                    member.getLoyaltyPoints());
        }
        System.out.println("=========================================================================================================");
        System.out.println("Total Members: " + App.memberList.getNumberOfEntries());
    }

    // customer registration into a waitlist
    public static Member registerNewCustomer(String memberName, String phoneNumber, String email) {
        // logic to register new customer

        // step 1 make sure customer is not already registered as member
        // catch cases where member exists already, and return 2 if so
        if (!App.memberList.isEmpty()) { // list is not empty, check if customer is already registered
            for (int i = 1; i <= App.memberList.getNumberOfEntries(); i++) {
                Member existingMember = App.memberList.getEntry(i);
                if (Objects.equals(existingMember.getMemberName(), memberName)
                        && Objects.equals(existingMember.getPhoneNumber(), phoneNumber)
                        && Objects.equals(existingMember.getEmail(), email)) {
                    // customer exists inside memberList

                    System.out.println("Welcome back, " + memberName + "!");
                    return existingMember; // return existing member object
                }
            }
        }
        // empty list or not found will arrive here, register new customer
        Member newMember = new Member(memberName, phoneNumber, email);
        App.memberList.add(newMember);
        System.out.println("New customer registered: " + memberName);
        // customer is registered into member list
        return newMember; // return new member object
    }

    public static void runNoShowCheck() {
        LocalDate today = VirtualClock.getInstance().today();
        for (int i = 1; i <= App.bookingList.getNumberOfEntries(); i++) {
            Booking b = App.bookingList.getEntry(i);
            if (b.getBookingStatus() == Booking.BookingStatus.CONFIRMED && b.getCheckInDate().isBefore(today)) {
                System.out.println("No-show detected: Booking ID " + b.getBookingID() + " for "
                        + b.getMember().getMemberName() + " has been cancelled.");
                b.setBookingStatus(Booking.BookingStatus.CANCELLED);
                App.bookingHistoryList.add(b);
                App.bookingList.remove(i);
                i--;

                Room freedRoom = b.getRoom();
                if (freedRoom != null) {
                    resolveWaitlistForRoom(freedRoom);
                }
            }
        }
    }

    public static void resolveWaitlistForRoom(Room freedRoom) {

        if (freedRoom == null) {
            return;
        }

        PriorityAllocationControl priorityControl
                = new PriorityAllocationControl();

        Booking allocatedBooking
                = priorityControl.allocateFreedRoom(freedRoom);

        if (allocatedBooking != null) {

            System.out.println(
                    "Priority waitlist automatically resolved.");

            System.out.println(
                    allocatedBooking.getMember().getMemberName()
                    + " ("
                    + allocatedBooking.getMember().getLoyaltyTier()
                    + ") has been promoted to CONFIRMED for room "
                    + freedRoom.getRoomNumber());
        }
    }

    // add existing customer to check-in waitlist
    public static void enqueueCheckin(Member member) {
        runNoShowCheck();

        for (int i = 1; i <= App.checkInWaitlist.getNumberOfEntries(); i++) {
            if (App.checkInWaitlist.getEntry(i).getMemberID() == member.getMemberID()) {
                System.out.println("Member is already in the check-in waitlist.");
                return;
            }
        }

        Booking todayBooking = null;
        LocalDate today = VirtualClock.getInstance().today();
        for (int i = 1; i <= App.bookingList.getNumberOfEntries(); i++) {
            Booking b = App.bookingList.getEntry(i);
            if (b.getMember().getMemberID() == member.getMemberID()
                    && b.getBookingStatus() == Booking.BookingStatus.CONFIRMED
                    && b.getCheckInDate().equals(today)) {
                todayBooking = b;
                break;
            }
        }

        if (todayBooking == null) {
            System.out.println("No confirmed booking found starting today (" + today + ") for this member.");
            return;
        }

        if (App.checkInWaitlist.isEmpty() || member.getLoyaltyTier() == Member.LoyaltyTier.Regular) {
            App.checkInWaitlist.add(member);
            System.out.println("Member added to queue at position " + App.checkInWaitlist.getNumberOfEntries());
        } else {
            simulateEnqueue(App.checkInWaitlist, member);
        }
    }

    // add existing customer to check-out waitlist
    public static void enqueueCheckout(Member member) {
        for (int i = 1; i <= App.checkOutWaitlist.getNumberOfEntries(); i++) {
            if (App.checkOutWaitlist.getEntry(i).getMemberID() == member.getMemberID()) {
                System.out.println("Member is already in the check-out waitlist.");
                return;
            }
        }

        Booking checkedInBooking = null;
        for (int i = 1; i <= App.bookingList.getNumberOfEntries(); i++) {
            Booking b = App.bookingList.getEntry(i);
            if (b.getMember().getMemberID() == member.getMemberID()
                    && b.getBookingStatus() == Booking.BookingStatus.CHECKED_IN) {
                checkedInBooking = b;
                break;
            }
        }

        if (checkedInBooking == null) {
            System.out.println("No checked-in booking found for this member.");
            return;
        }

        if (App.checkOutWaitlist.isEmpty() || member.getLoyaltyTier() == Member.LoyaltyTier.Regular) {
            App.checkOutWaitlist.add(member);
            System.out.println("Member added to queue at position " + App.checkOutWaitlist.getNumberOfEntries());
        } else {
            simulateEnqueue(App.checkOutWaitlist, member);
        }
    }

    public static Member dequeueCheckin() {
        return simulateDequeue(App.checkInWaitlist);
    }

    public static Member dequeueCheckOut() {
        return simulateDequeue(App.checkOutWaitlist);
    }

    public static void processCheckin(java.util.Scanner scanner) {
        runNoShowCheck();

        if (App.checkInWaitlist.isEmpty()) {
            System.out.println("Check-in queue is empty.");
            return;
        }

        Member member = null;
        for (int i = 1; i <= App.checkInWaitlist.getNumberOfEntries(); i++) {
            Member m = App.checkInWaitlist.getEntry(i);
            System.out.print("Process check-in for " + m.getMemberName() + "? (Y=Yes, S=Skip, N=Cancel): ");
            String ans = scanner.nextLine().trim();
            if (ans.equalsIgnoreCase("y")) {
                member = App.checkInWaitlist.remove(i);
                break;
            } else if (ans.equalsIgnoreCase("n")) {
                System.out.println("Check-in processing cancelled.");
                return;
            } else if (ans.equalsIgnoreCase("s")) {
                continue;
            } else {
                System.out.println("Invalid option. Skipping...");
            }
        }

        if (member == null) {
            System.out.println("No one was selected from the queue.");
            return;
        }

        LocalDate today = VirtualClock.getInstance().today();
        Booking booking = null;
        for (int i = 1; i <= App.bookingList.getNumberOfEntries(); i++) {
            Booking b = App.bookingList.getEntry(i);
            if (b.getMember().getMemberID() == member.getMemberID()
                    && b.getBookingStatus() == Booking.BookingStatus.CONFIRMED
                    && b.getCheckInDate().equals(today)) {
                booking = b;
                break;
            }
        }

        if (booking == null) {
            System.out.println("Could not find an active confirmed booking for " + member.getMemberName() + " today.");
            return;
        }

        Room room = booking.getRoom();
        if (room == null) {
            System.out.println("No room is assigned to this booking.");
            return;
        }

        System.out
                .println("Processing check-in for: " + member.getMemberName() + " (ID: " + member.getMemberID() + ")");
        System.out.println("Assigned Room: " + room.getRoomNumber() + " (Status: " + room.getRoomStatus() + ")");

        boolean canCheckIn = (room.getRoomStatus() == Room.RoomStatus.Ready_for_Check_In
                || room.getRoomStatus() == Room.RoomStatus.Inspected);
        if (!canCheckIn) {
            System.out.println("Warning: The room is currently " + room.getRoomStatus() + ".");
            System.out.print("Force check-in anyway? (Y/N): ");
            String force = scanner.nextLine().trim();
            if (!force.equalsIgnoreCase("y")) {
                enqueueCheckin(member);
                System.out.println("Check-in postponed. Member re-queued.");
                return;
            }
        }

        booking.setBookingStatus(Booking.BookingStatus.CHECKED_IN);
        room.setRoomStatus(Room.RoomStatus.Occupied);
        System.out.println("Check-in completed successfully! Room " + room.getRoomNumber() + " is now Occupied.");
    }

    public static void viewCheckInWaitlist() {
        runNoShowCheck();
        System.out.println("\n--- Check-In Waitlist ---");
        if (App.checkInWaitlist.isEmpty()) {
            System.out.println("Queue is empty.");
            return;
        }
        for (int i = 1; i <= App.checkInWaitlist.getNumberOfEntries(); i++) {
            Member m = App.checkInWaitlist.getEntry(i);
            System.out.println(
                    i + ". " + m.getMemberName() + " (ID: " + m.getMemberID() + ", Tier: " + m.getLoyaltyTier() + ")");
        }
    }

    public static void viewCheckOutWaitlist() {
        System.out.println("\n--- Check-Out Waitlist ---");
        if (App.checkOutWaitlist.isEmpty()) {
            System.out.println("Queue is empty.");
            return;
        }
        for (int i = 1; i <= App.checkOutWaitlist.getNumberOfEntries(); i++) {
            Member m = App.checkOutWaitlist.getEntry(i);
            System.out.println(
                    i + ". " + m.getMemberName() + " (ID: " + m.getMemberID() + ", Tier: " + m.getLoyaltyTier() + ")");
        }
    }

    public static void processCheckout(java.util.Scanner scanner) {
        if (App.checkOutWaitlist.isEmpty()) {
            System.out.println("Check-out queue is empty.");
            return;
        }

        Member member = null;
        for (int i = 1; i <= App.checkOutWaitlist.getNumberOfEntries(); i++) {
            Member m = App.checkOutWaitlist.getEntry(i);
            System.out.print("Process check-out for " + m.getMemberName() + "? (Y=Yes, S=Skip, N=Cancel): ");
            String ans = scanner.nextLine().trim();
            if (ans.equalsIgnoreCase("y")) {
                member = App.checkOutWaitlist.remove(i);
                break;
            } else if (ans.equalsIgnoreCase("n")) {
                System.out.println("Check-out processing cancelled.");
                return;
            } else if (ans.equalsIgnoreCase("s")) {
                continue;
            } else {
                System.out.println("Invalid option. Skipping...");
            }
        }

        if (member == null) {
            System.out.println("No one was selected from the queue.");
            return;
        }

        Booking booking = null;
        for (int i = 1; i <= App.bookingList.getNumberOfEntries(); i++) {
            Booking b = App.bookingList.getEntry(i);
            if (b.getMember().getMemberID() == member.getMemberID()
                    && b.getBookingStatus() == Booking.BookingStatus.CHECKED_IN) {
                booking = b;
                break;
            }
        }

        if (booking == null) {
            System.out.println("Could not find a checked-in booking for " + member.getMemberName() + ".");
            return;
        }

        Room room = booking.getRoom();
        LocalDate checkIn = booking.getCheckInDate();
        LocalDate scheduledCheckOut = booking.getCheckOutDate();
        LocalDate actualCheckOut = VirtualClock.getInstance().today();
        double rate = (room != null) ? room.getRoomType().getBaseRate() : 0.0;

        System.out.println(
                "\nProcessing check-out for: " + member.getMemberName() + " (ID: " + member.getMemberID() + ")");
        if (room != null) {
            System.out.println("Room: " + room.getRoomNumber() + " (" + room.getRoomType() + ")");
        }

        long scheduledNights = java.time.temporal.ChronoUnit.DAYS.between(checkIn, scheduledCheckOut);
        if (scheduledNights <= 0) {
            scheduledNights = 1;
        }

        double totalCharge = 0;
        long overstayDays = java.time.temporal.ChronoUnit.DAYS.between(scheduledCheckOut, actualCheckOut);

        if (overstayDays > 0) {
            double normalCharge = scheduledNights * rate;
            double penaltyCharge = overstayDays * rate * 1.5;
            totalCharge = normalCharge + penaltyCharge;
            System.out.println("--- BILL SUMMARY (OVERSTAY) ---");
            System.out
                    .println("Normal Nights Stayed: " + scheduledNights + " @ $" + rate + "/night = $" + normalCharge);
            System.out.println("Overstay Days: " + overstayDays + " @ $" + (rate * 1.5) + "/night (1.5x penalty) = $"
                    + penaltyCharge);
            System.out.println("Total Due: $" + totalCharge);
        } else {
            long actualNights = java.time.temporal.ChronoUnit.DAYS.between(checkIn, actualCheckOut);
            if (actualNights <= 0) {
                actualNights = 1;
            }
            totalCharge = actualNights * rate;
            System.out.println("--- BILL SUMMARY ---");
            System.out.println("Nights Stayed: " + actualNights + " @ $" + rate + "/night = $" + totalCharge);
        }

        // Finalize checkout
        booking.setBookingStatus(Booking.BookingStatus.CHECKED_OUT);
        booking.setBookingDate(actualCheckOut); // Store actual checkout date in bookingDate
        if (room != null) {
            room.setRoomStatus(Room.RoomStatus.Dirty);
            System.out.println("Room " + room.getRoomNumber() + " is now Dirty.");
        }

        // Move to history
        for (int i = 1; i <= App.bookingList.getNumberOfEntries(); i++) {
            if (App.bookingList.getEntry(i).getBookingID() == booking.getBookingID()) {
                App.bookingList.remove(i);
                break;
            }
        }
        App.bookingHistoryList.add(booking);
        System.out.println("Check-out completed. Booking moved to history.");

        // Waitlist resolution: A room type was freed, check waitlist requests
        if (room != null) {
            resolveWaitlistForRoom(room);
        }
    }

    public static void generateBookingSummaryReport(int year, int month, Booking.BookingStatus statusFilter) {
        System.out.println("\n=======================================================");
        System.out.println("         BOOKING SUMMARY & TREND REPORT                ");
        System.out.printf("               Period: %04d-%02d\n", year, month);
        System.out.println("=======================================================");

        int[] dailyCounts = new int[32];
        int totalBookings = 0;
        int confirmed = 0, checkedIn = 0, checkedOut = 0, cancelled = 0;

        // Process active
        for (int i = 1; i <= App.bookingList.getNumberOfEntries(); i++) {
            Booking b = App.bookingList.getEntry(i);
            LocalDate date = b.getCheckInDate();

            if (date.getYear() == year && date.getMonthValue() == month) {
                if (statusFilter == null || b.getBookingStatus() == statusFilter) {
                    dailyCounts[date.getDayOfMonth()]++;
                    totalBookings++;

                    if (b.getBookingStatus() == Booking.BookingStatus.CONFIRMED) {
                        confirmed++;
                    } else if (b.getBookingStatus() == Booking.BookingStatus.CHECKED_IN) {
                        checkedIn++;
                    }
                }
            }
        }

        // Process history
        for (int i = 1; i <= App.bookingHistoryList.getNumberOfEntries(); i++) {
            Booking b = App.bookingHistoryList.getEntry(i);
            LocalDate date = b.getCheckInDate();

            if (date.getYear() == year && date.getMonthValue() == month) {
                if (statusFilter == null || b.getBookingStatus() == statusFilter) {
                    dailyCounts[date.getDayOfMonth()]++;
                    totalBookings++;

                    if (b.getBookingStatus() == Booking.BookingStatus.CHECKED_OUT) {
                        checkedOut++;
                    } else if (b.getBookingStatus() == Booking.BookingStatus.CANCELLED) {
                        cancelled++;
                    }
                }
            }
        }

        System.out.println("\n--- BOOKING STATUS SUMMARY ---");
        System.out.println("Confirmed Bookings (Active): " + confirmed);
        System.out.println("Checked-In Bookings (Active): " + checkedIn);
        System.out.println("Checked-Out Bookings (Completed): " + checkedOut);
        System.out.println("Cancelled Bookings: " + cancelled);
        System.out.println("Total Bookings in Period: " + totalBookings);

        System.out.println("\n--- DAILY BOOKING VOLUME TREND ---");
        for (int day = 1; day <= 31; day++) {
            if (dailyCounts[day] > 0 || (day <= LocalDate.of(year, month, 1).lengthOfMonth() && totalBookings > 0)) {
                System.out.printf("Day %02d | %2d | ", day, dailyCounts[day]);
                for (int star = 0; star < dailyCounts[day]; star++) {
                    System.out.print("■ ");
                }
                System.out.println();
            }
        }
        System.out.println("-------------------------------------------------------");
        System.out.println("End of Report.");
    }

    public static void generateRevenueSummaryReport(int year, int month, Room.RoomType typeFilter) {
        System.out.println("\n=======================================================");
        System.out.println("         REVENUE SUMMARY & TREND REPORT                ");
        System.out.printf("               Period: %04d-%02d\n", year, month);
        System.out.println("=======================================================");

        double[] dailyRevenue = new double[32];
        double totalNormal = 0, totalPenalty = 0;

        for (int i = 1; i <= App.bookingHistoryList.getNumberOfEntries(); i++) {
            Booking b = App.bookingHistoryList.getEntry(i);

            // Assuming revenue is realized on checkout date (bookingDate in history)
            LocalDate date = b.getBookingDate();

            if (b.getBookingStatus() == Booking.BookingStatus.CHECKED_OUT
                    && date != null && date.getYear() == year && date.getMonthValue() == month) {

                Room room = b.getRoom();
                if (typeFilter != null && (room == null || room.getRoomType() != typeFilter)) {
                    continue;
                }

                double rate = (room != null) ? room.getRoomType().getBaseRate() : 0.0;

                LocalDate checkIn = b.getCheckInDate();
                LocalDate scheduledCheckOut = b.getCheckOutDate();

                long scheduledNights = java.time.temporal.ChronoUnit.DAYS.between(checkIn, scheduledCheckOut);
                if (scheduledNights <= 0) {
                    scheduledNights = 1;
                }

                long overstayDays = java.time.temporal.ChronoUnit.DAYS.between(scheduledCheckOut, date);
                double revenueForBooking = 0;

                if (overstayDays > 0) {
                    double normal = (scheduledNights * rate);
                    double penalty = (overstayDays * rate * 1.5);
                    totalNormal += normal;
                    totalPenalty += penalty;
                    revenueForBooking = normal + penalty;
                } else {
                    long actualNights = java.time.temporal.ChronoUnit.DAYS.between(checkIn, date);
                    if (actualNights <= 0) {
                        actualNights = 1;
                    }
                    double normal = (actualNights * rate);
                    totalNormal += normal;
                    revenueForBooking = normal;
                }

                dailyRevenue[date.getDayOfMonth()] += revenueForBooking;
            }
        }

        System.out.println("\n--- REVENUE SUMMARY ---");
        System.out.printf("Normal Room Revenue: $%.2f\n", totalNormal);
        System.out.printf("Overstay Penalty Revenue: $%.2f\n", totalPenalty);
        System.out.printf("Total Revenue: $%.2f\n", (totalNormal + totalPenalty));

        System.out.println("\n--- DAILY REVENUE TREND ---");
        for (int day = 1; day <= 31; day++) {
            if (dailyRevenue[day] > 0 || (day <= LocalDate.of(year, month, 1).lengthOfMonth() && (totalNormal + totalPenalty) > 0)) {
                System.out.printf("Day %02d | $%7.2f | ", day, dailyRevenue[day]);
                // Scale the graph so it doesn't get ridiculously long. e.g., 1 block = $100
                int blocks = (int) (dailyRevenue[day] / 100.0);
                for (int star = 0; star < blocks; star++) {
                    System.out.print("■ ");
                }
                System.out.println();
            }
        }
        System.out.println("-------------------------------------------------------");
        System.out.println("End of Report.");
    }

    private static void simulateEnqueue(ListInterface<Member> waitlist, Member member) {
        // logic to add member to waitlist based on loyalty tier
        // higher tier members are added to the front of the list
        // lower tier members are added to the back of the list
        int newMemberTier = member.getLoyaltyTier().ordinal(); // get the ordinal value of the new member's loyalty tier
        int position = 1; // default position to add new member
        int totalEntries = waitlist.getNumberOfEntries(); // get the total number of entries in the waitlist

        while (position <= totalEntries) {
            Member currentMember = waitlist.getEntry(position);
            int existingMemberTier = currentMember.getLoyaltyTier().ordinal();
            if (newMemberTier > existingMemberTier) {
                break;
            }
            position++;
        }
        waitlist.add(position, member); // adt add at position
        System.out.println("Member added to queue at position: " + position);
    }

    private static Member simulateDequeue(ListInterface<Member> waitlist) {
        // logic for removing member from queue
        if (!waitlist.isEmpty()) {
            return waitlist.remove(1); // adt remove
        }
        return null;
    }
}
