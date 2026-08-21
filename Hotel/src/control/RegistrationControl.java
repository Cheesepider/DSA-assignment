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
import adt.DoublyLinkedList;
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

    // =========================================================================
    // SUMMARY REPORT 1: Member Booking & Spending Summary Report
    // Demonstrates 3-Class Dependency: Member -> Booking -> Room
    // =========================================================================
    public static void generateMemberBookingSummaryReport(int year, int quarter) {
        String periodTitle = (quarter >= 1 && quarter <= 4) ? (year + " - Q" + quarter + " (" + getQuarterMonths(quarter) + ")") : (year + " - Full Year");

        System.out.println("\n========================================================================================================================");
        System.out.println("                                Tunku Abdul Rahman University of Management & Technology");
        System.out.println("                                                        (TARUMT)");
        System.out.println("                                                RESORT MANAGEMENT SYSTEM");
        System.out.println("                                         HOTEL REGISTRATION & BOOKING SUBSYSTEM");
        System.out.println("========================================================================================================================");
        System.out.println("Report Title : MEMBER BOOKING ACTIVITY & REVENUE CONTRIBUTION SUMMARY REPORT");
        System.out.println("Generated At : " + VirtualClock.getInstance().toString());
        System.out.println("Period       : " + periodTitle);
        System.out.println("========================================================================================================================");

        if (App.memberList == null || App.memberList.isEmpty()) {
            System.out.println("No registered members found in the system.");
            return;
        }

        ListInterface<MemberSummaryEntry> entryList = new DoublyLinkedList<>();

        int totalSystemBookings = 0;
        int totalSystemCompleted = 0;
        int totalSystemCancelled = 0;
        long totalSystemNights = 0;
        double totalSystemRevenue = 0.0;

        for (int i = 1; i <= App.memberList.getNumberOfEntries(); i++) {
            Member m = App.memberList.getEntry(i);
            MemberSummaryEntry entry = new MemberSummaryEntry(m);

            // Scan Active Bookings
            if (App.bookingList != null) {
                for (int j = 1; j <= App.bookingList.getNumberOfEntries(); j++) {
                    Booking b = App.bookingList.getEntry(j);
                    if (b.getMember() != null && b.getMember().getMemberID() == m.getMemberID() && isDateInPeriod(b.getCheckInDate(), year, quarter)) {
                        entry.totalBookings++;
                        entry.activeBookings++;
                        Room r = b.getRoom();
                        long nights = calculateNights(b.getCheckInDate(), b.getCheckOutDate());
                        entry.totalNights += nights;
                        double rate = (r != null && r.getRoomType() != null) ? r.getRoomType().getBaseRate() : 0.0;
                        entry.totalRevenue += (nights * rate);
                        if (r != null && r.getRoomType() != null) {
                            entry.recordRoomType(r.getRoomType());
                        }
                    }
                }
            }

            // Scan Historical Bookings
            if (App.bookingHistoryList != null) {
                for (int j = 1; j <= App.bookingHistoryList.getNumberOfEntries(); j++) {
                    Booking b = App.bookingHistoryList.getEntry(j);
                    if (b.getMember() != null && b.getMember().getMemberID() == m.getMemberID() && isDateInPeriod(b.getCheckInDate(), year, quarter)) {
                        entry.totalBookings++;
                        Room r = b.getRoom();
                        if (b.getBookingStatus() == Booking.BookingStatus.CANCELLED) {
                            entry.cancelledBookings++;
                        } else {
                            entry.completedStays++;
                            long nights = calculateNights(b.getCheckInDate(), b.getCheckOutDate());
                            entry.totalNights += nights;
                            double rate = (r != null && r.getRoomType() != null) ? r.getRoomType().getBaseRate() : 0.0;
                            long overstay = (b.getBookingDate() != null && b.getCheckOutDate() != null)
                                    ? java.time.temporal.ChronoUnit.DAYS.between(b.getCheckOutDate(), b.getBookingDate())
                                    : 0;
                            double revenue = (nights * rate);
                            if (overstay > 0) {
                                revenue += (overstay * rate * 1.5);
                            }
                            entry.totalRevenue += revenue;
                        }
                        if (r != null && r.getRoomType() != null) {
                            entry.recordRoomType(r.getRoomType());
                        }
                    }
                }
            }

            totalSystemBookings += entry.totalBookings;
            totalSystemCompleted += entry.completedStays;
            totalSystemCancelled += entry.cancelledBookings;
            totalSystemNights += entry.totalNights;
            totalSystemRevenue += entry.totalRevenue;

            entryList.add(entry);
        }

        // Sort members descending by totalRevenue, then totalBookings
        for (int i = 1; i <= entryList.getNumberOfEntries(); i++) {
            for (int j = i + 1; j <= entryList.getNumberOfEntries(); j++) {
                MemberSummaryEntry a = entryList.getEntry(i);
                MemberSummaryEntry b = entryList.getEntry(j);
                if (b.totalRevenue > a.totalRevenue || (b.totalRevenue == a.totalRevenue && b.totalBookings > a.totalBookings)) {
                    entryList.replace(i, b);
                    entryList.replace(j, a);
                }
            }
        }

        System.out.printf("%-5s | %-9s | %-18s | %-10s | %-8s | %-9s | %-9s | %-7s | %-10s | %-12s | %-10s\n",
                "Rank", "Member ID", "Member Name", "Tier", "Bookings", "Completed", "Cancelled", "Nights", "Pref. Room", "Revenue ($)", "Contr. Tier");
        System.out.println("------------------------------------------------------------------------------------------------------------------------");

        for (int i = 1; i <= entryList.getNumberOfEntries(); i++) {
            MemberSummaryEntry e = entryList.getEntry(i);
            String contrTier = getContributionTier(e.totalRevenue);
            System.out.printf("%-5d | %-9d | %-18s | %-10s | %-8d | %-9d | %-9d | %-7d | %-10s | $%11.2f | %-10s\n",
                    i,
                    e.member.getMemberID(),
                    truncate(e.member.getMemberName(), 18),
                    e.member.getLoyaltyTier(),
                    e.totalBookings,
                    e.completedStays,
                    e.cancelledBookings,
                    e.totalNights,
                    e.getPreferredRoomType(),
                    e.totalRevenue,
                    contrTier);
        }

        System.out.println("========================================================================================================================");
        System.out.println("SUMMARY INSIGHTS & AGGREGATE METRICS:");
        System.out.printf("• Total Registered Members Analyzed : %d\n", entryList.getNumberOfEntries());
        System.out.printf("• Total Bookings Placed in Period   : %d (Completed: %d, Active: %d, Cancelled: %d)\n",
                totalSystemBookings, totalSystemCompleted, (totalSystemBookings - totalSystemCompleted - totalSystemCancelled), totalSystemCancelled);
        System.out.printf("• Overall Cancellation Rate         : %.2f%%\n",
                totalSystemBookings > 0 ? ((double) totalSystemCancelled / totalSystemBookings * 100.0) : 0.0);
        System.out.printf("• Total Guest Nights Stayed         : %d nights\n", totalSystemNights);
        System.out.printf("• Total Room Revenue Generated      : $%.2f\n", totalSystemRevenue);
        System.out.printf("• Average Revenue Per Member        : $%.2f\n",
                !entryList.isEmpty() ? (totalSystemRevenue / entryList.getNumberOfEntries()) : 0.0);
        if (!entryList.isEmpty() && entryList.getEntry(1).totalRevenue > 0) {
            MemberSummaryEntry top = entryList.getEntry(1);
            System.out.printf("• Top Contributor (VIP Guest)       : %s (ID: %d, Tier: %s) with $%.2f spent (%d bookings)\n",
                    top.member.getMemberName(), top.member.getMemberID(), top.member.getLoyaltyTier(), top.totalRevenue, top.totalBookings);
        }
        System.out.println("------------------------------------------------------------------------------------------------------------------------");
        System.out.println("Contribution Tier Guide: [HIGH] Revenue >= $500 | [MEDIUM] $100 - $499 | [LOW] $1 - $99 | [INACTIVE] $0");
        System.out.println("End of Report.");
    }

    // =========================================================================
    // SUMMARY REPORT 2: Room Type Performance & Member Tier Utilization Report
    // Demonstrates 3-Class Dependency: Room -> Booking -> Member
    // =========================================================================
    public static void generateRoomTypePerformanceReport(int year, int quarter) {
        String periodTitle = (quarter >= 1 && quarter <= 4) ? (year + " - Q" + quarter + " (" + getQuarterMonths(quarter) + ")") : (year + " - Full Year");

        System.out.println("\n========================================================================================================================");
        System.out.println("                                Tunku Abdul Rahman University of Management & Technology");
        System.out.println("                                                        (TARUMT)");
        System.out.println("                                                RESORT MANAGEMENT SYSTEM");
        System.out.println("                                         HOTEL REGISTRATION & BOOKING SUBSYSTEM");
        System.out.println("========================================================================================================================");
        System.out.println("Report Title : ROOM TYPE PERFORMANCE & MEMBER TIER UTILIZATION SUMMARY REPORT");
        System.out.println("Generated At : " + VirtualClock.getInstance().toString());
        System.out.println("Period       : " + periodTitle);
        System.out.println("========================================================================================================================");

        RoomTypeSummaryEntry singleEntry = new RoomTypeSummaryEntry(Room.RoomType.SINGLE);
        RoomTypeSummaryEntry doubleEntry = new RoomTypeSummaryEntry(Room.RoomType.DOUBLE);
        RoomTypeSummaryEntry suiteEntry = new RoomTypeSummaryEntry(Room.RoomType.SUITE);

        ListInterface<RoomTypeSummaryEntry> entryList = new DoublyLinkedList<>();
        entryList.add(singleEntry);
        entryList.add(doubleEntry);
        entryList.add(suiteEntry);

        // Process Active Bookings
        if (App.bookingList != null) {
            for (int i = 1; i <= App.bookingList.getNumberOfEntries(); i++) {
                Booking b = App.bookingList.getEntry(i);
                if (b.getRoom() != null && b.getRoom().getRoomType() != null && isDateInPeriod(b.getCheckInDate(), year, quarter)) {
                    RoomTypeSummaryEntry target = getRoomTypeEntry(entryList, b.getRoom().getRoomType());
                    if (target != null) {
                        target.totalBookings++;
                        target.activeBookings++;
                        long nights = calculateNights(b.getCheckInDate(), b.getCheckOutDate());
                        target.totalNights += nights;
                        target.totalRevenue += (nights * b.getRoom().getRoomType().getBaseRate());
                        if (b.getMember() != null) {
                            target.recordMemberTier(b.getMember().getLoyaltyTier());
                        }
                    }
                }
            }
        }

        // Process Historical Bookings
        if (App.bookingHistoryList != null) {
            for (int i = 1; i <= App.bookingHistoryList.getNumberOfEntries(); i++) {
                Booking b = App.bookingHistoryList.getEntry(i);
                if (b.getRoom() != null && b.getRoom().getRoomType() != null && isDateInPeriod(b.getCheckInDate(), year, quarter)) {
                    RoomTypeSummaryEntry target = getRoomTypeEntry(entryList, b.getRoom().getRoomType());
                    if (target != null) {
                        target.totalBookings++;
                        if (b.getBookingStatus() == Booking.BookingStatus.CANCELLED) {
                            target.cancelledBookings++;
                        } else {
                            target.completedBookings++;
                            long nights = calculateNights(b.getCheckInDate(), b.getCheckOutDate());
                            target.totalNights += nights;
                            double rate = b.getRoom().getRoomType().getBaseRate();
                            long overstay = (b.getBookingDate() != null && b.getCheckOutDate() != null)
                                    ? java.time.temporal.ChronoUnit.DAYS.between(b.getCheckOutDate(), b.getBookingDate())
                                    : 0;
                            double revenue = (nights * rate);
                            if (overstay > 0) {
                                revenue += (overstay * rate * 1.5);
                            }
                            target.totalRevenue += revenue;
                        }
                        if (b.getMember() != null) {
                            target.recordMemberTier(b.getMember().getLoyaltyTier());
                        }
                    }
                }
            }
        }

        // Sort by totalRevenue descending
        for (int i = 1; i <= entryList.getNumberOfEntries(); i++) {
            for (int j = i + 1; j <= entryList.getNumberOfEntries(); j++) {
                RoomTypeSummaryEntry a = entryList.getEntry(i);
                RoomTypeSummaryEntry b = entryList.getEntry(j);
                if (b.totalRevenue > a.totalRevenue || (b.totalRevenue == a.totalRevenue && b.totalNights > a.totalNights)) {
                    entryList.replace(i, b);
                    entryList.replace(j, a);
                }
            }
        }

        System.out.println("SECTION 1: ROOM TYPE REVENUE & OCCUPANCY PERFORMANCE RANKING");
        System.out.printf("%-5s | %-12s | %-10s | %-10s | %-9s | %-9s | %-12s | %-14s | %-11s | %-11s\n",
                "Rank", "Room Type", "Base Rate", "Bookings", "Completed", "Cancelled", "Nights Booked", "Revenue ($)", "Avg Stay (N)", "Cancel %");
        System.out.println("------------------------------------------------------------------------------------------------------------------------");

        int totalAllBookings = 0;
        long totalAllNights = 0;
        double totalAllRevenue = 0.0;

        for (int i = 1; i <= entryList.getNumberOfEntries(); i++) {
            RoomTypeSummaryEntry e = entryList.getEntry(i);
            totalAllBookings += e.totalBookings;
            totalAllNights += e.totalNights;
            totalAllRevenue += e.totalRevenue;

            System.out.printf("%-5d | %-12s | $%-9.2f | %-10d | %-9d | %-9d | %-12d | $%13.2f | %-11.1f | %9.1f%%\n",
                    i,
                    e.roomType,
                    e.roomType.getBaseRate(),
                    e.totalBookings,
                    e.completedBookings,
                    e.cancelledBookings,
                    e.totalNights,
                    e.totalRevenue,
                    e.getAverageStayNights(),
                    e.getCancellationRate());
        }

        System.out.println("------------------------------------------------------------------------------------------------------------------------");
        System.out.printf("TOTAL | %-12s | %-10s | %-10d | %-9s | %-9s | %-12d | $%13.2f |\n",
                "ALL TYPES", "-", totalAllBookings, "-", "-", totalAllNights, totalAllRevenue);

        System.out.println("\nSECTION 2: MEMBER DEMOGRAPHIC UTILIZATION BY LOYALTY TIER");
        System.out.printf("%-12s | %-14s | %-14s | %-14s | %-14s | %-20s\n",
                "Room Type", "Regular Guests", "Platinum Guests", "Diamond Guests", "Elite Guests", "Dominant Demographic");
        System.out.println("------------------------------------------------------------------------------------------------------------------------");

        for (int i = 1; i <= entryList.getNumberOfEntries(); i++) {
            RoomTypeSummaryEntry e = entryList.getEntry(i);
            System.out.printf("%-12s | %-14s | %-14s | %-14s | %-14s | %-20s\n",
                    e.roomType,
                    e.regularBookings + " (" + formatPct(e.regularBookings, e.totalBookings) + ")",
                    e.platinumBookings + " (" + formatPct(e.platinumBookings, e.totalBookings) + ")",
                    e.diamondBookings + " (" + formatPct(e.diamondBookings, e.totalBookings) + ")",
                    e.eliteBookings + " (" + formatPct(e.eliteBookings, e.totalBookings) + ")",
                    e.getDominantDemographic());
        }

        System.out.println("========================================================================================================================");
        System.out.println("SUMMARY INSIGHTS & STRATEGIC RECOMMENDATIONS:");
        if (!entryList.isEmpty() && entryList.getEntry(1).totalRevenue > 0) {
            RoomTypeSummaryEntry top = entryList.getEntry(1);
            System.out.printf("• Top Revenue Driver Room Category  : %s with $%.2f (%.1f%% of overall revenue)\n",
                    top.roomType, top.totalRevenue, totalAllRevenue > 0 ? (top.totalRevenue / totalAllRevenue * 100.0) : 0.0);
        }
        System.out.printf("• Highest Volume Demand Category    : %s\n", getHighestVolumeType(entryList));
        System.out.printf("• Overall Average Booking Length     : %.2f nights\n",
                (totalAllBookings > 0) ? ((double) totalAllNights / totalAllBookings) : 0.0);
        System.out.println("------------------------------------------------------------------------------------------------------------------------");
        System.out.println("End of Report.");
    }

    // Helper classes and methods for report generation
    private static class MemberSummaryEntry {
        Member member;
        int totalBookings = 0;
        int completedStays = 0;
        int cancelledBookings = 0;
        int activeBookings = 0;
        long totalNights = 0;
        double totalRevenue = 0.0;
        int singleCount = 0;
        int doubleCount = 0;
        int suiteCount = 0;

        MemberSummaryEntry(Member member) {
            this.member = member;
        }

        void recordRoomType(Room.RoomType type) {
            if (type == Room.RoomType.SINGLE) singleCount++;
            else if (type == Room.RoomType.DOUBLE) doubleCount++;
            else if (type == Room.RoomType.SUITE) suiteCount++;
        }

        String getPreferredRoomType() {
            if (singleCount == 0 && doubleCount == 0 && suiteCount == 0) return "-";
            if (singleCount >= doubleCount && singleCount >= suiteCount) return "SINGLE";
            if (doubleCount >= singleCount && doubleCount >= suiteCount) return "DOUBLE";
            return "SUITE";
        }
    }

    private static class RoomTypeSummaryEntry {
        Room.RoomType roomType;
        int totalBookings = 0;
        int completedBookings = 0;
        int cancelledBookings = 0;
        int activeBookings = 0;
        long totalNights = 0;
        double totalRevenue = 0.0;
        int regularBookings = 0;
        int platinumBookings = 0;
        int diamondBookings = 0;
        int eliteBookings = 0;

        RoomTypeSummaryEntry(Room.RoomType roomType) {
            this.roomType = roomType;
        }

        void recordMemberTier(Member.LoyaltyTier tier) {
            if (tier == Member.LoyaltyTier.Regular) regularBookings++;
            else if (tier == Member.LoyaltyTier.Platinum) platinumBookings++;
            else if (tier == Member.LoyaltyTier.Diamond) diamondBookings++;
            else if (tier == Member.LoyaltyTier.Elite) eliteBookings++;
        }

        double getAverageStayNights() {
            int stays = completedBookings + activeBookings;
            return stays > 0 ? (double) totalNights / stays : 0.0;
        }

        double getCancellationRate() {
            return totalBookings > 0 ? ((double) cancelledBookings / totalBookings * 100.0) : 0.0;
        }

        String getDominantDemographic() {
            if (totalBookings == 0) return "-";
            int max = regularBookings;
            String dominant = "Regular";
            if (platinumBookings > max) { max = platinumBookings; dominant = "Platinum"; }
            if (diamondBookings > max) { max = diamondBookings; dominant = "Diamond"; }
            if (eliteBookings > max) { max = eliteBookings; dominant = "Elite"; }
            return dominant;
        }
    }

    private static boolean isDateInPeriod(LocalDate date, int year, int quarter) {
        if (date == null) return false;
        if (date.getYear() != year) return false;
        if (quarter == 0) return true; // Full Year
        int m = date.getMonthValue();
        if (quarter == 1) return m >= 1 && m <= 3;
        if (quarter == 2) return m >= 4 && m <= 6;
        if (quarter == 3) return m >= 7 && m <= 9;
        if (quarter == 4) return m >= 10 && m <= 12;
        return false;
    }

    private static String getQuarterMonths(int quarter) {
        switch (quarter) {
            case 1: return "Jan - Mar";
            case 2: return "Apr - Jun";
            case 3: return "Jul - Sep";
            case 4: return "Oct - Dec";
            default: return "Full Year";
        }
    }

    private static long calculateNights(LocalDate in, LocalDate out) {
        if (in == null || out == null) return 1;
        long n = java.time.temporal.ChronoUnit.DAYS.between(in, out);
        return n <= 0 ? 1 : n;
    }

    private static String getContributionTier(double revenue) {
        if (revenue >= 500.0) return "HIGH";
        if (revenue >= 100.0) return "MEDIUM";
        if (revenue > 0) return "LOW";
        return "INACTIVE";
    }

    private static String truncate(String s, int max) {
        if (s == null) return "-";
        return s.length() <= max ? s : s.substring(0, max - 3) + "...";
    }

    private static String formatPct(int count, int total) {
        if (total == 0) return "0.0%";
        return String.format("%.1f%%", (double) count / total * 100.0);
    }

    private static RoomTypeSummaryEntry getRoomTypeEntry(ListInterface<RoomTypeSummaryEntry> list, Room.RoomType type) {
        for (int i = 1; i <= list.getNumberOfEntries(); i++) {
            if (list.getEntry(i).roomType == type) {
                return list.getEntry(i);
            }
        }
        return null;
    }

    private static String getHighestVolumeType(ListInterface<RoomTypeSummaryEntry> list) {
        RoomTypeSummaryEntry best = null;
        for (int i = 1; i <= list.getNumberOfEntries(); i++) {
            RoomTypeSummaryEntry e = list.getEntry(i);
            if (best == null || e.totalBookings > best.totalBookings) {
                best = e;
            }
        }
        return (best != null && best.totalBookings > 0) ? (best.roomType + " (" + best.totalBookings + " bookings)") : "N/A";
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
