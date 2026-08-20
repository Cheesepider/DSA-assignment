package control;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import dao.LoyaltyDAO;
import dao.RegistrationDAO;
import adt.DoublyLinkedList;
import adt.ListInterface;
import entity.Booking;
import entity.Member;
import entity.Member.LoyaltyTier;
import entity.PendingPointsCredit;
import entity.PendingPointsCredit.CreditSource;
import entity.PointsTransaction;
import entity.RedemptionRecord;
import entity.RewardItem;
import entity.Room;
import main.App;
import utility.ReportFormatUtility;
import utility.VirtualClock;

/**
 
 *
 * @author : Kao Yong Feng
 */
public class LoyaltyControl {

    private ListInterface<Member> memberList;

    
    private static ListInterface<RewardItem> rewardCatalog;
    private static ListInterface<PointsTransaction> transactionList;

  
    private static ListInterface<RedemptionRecord> redemptionHistory;

    
    private static ListInterface<PendingPointsCredit> pendingPointsQueue;

    
    private static ListInterface<Integer> queuedStayBookingIDs;

   
    private static class LifetimeEarnedPoints {
        private int memberID;
        private int totalEarned;

    
        LifetimeEarnedPoints() {
        }

        LifetimeEarnedPoints(int memberID, int totalEarned) {
            this.memberID = memberID;
            this.totalEarned = totalEarned;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof LifetimeEarnedPoints)) {
                return false;
            }
            return this.memberID == ((LifetimeEarnedPoints) obj).memberID;
        }

        @Override
        public int hashCode() {
            return Integer.hashCode(memberID);
        }
    }

    private static ListInterface<LifetimeEarnedPoints> lifetimeEarnedList;

    private static LoyaltyDAO loyaltyDAO = new LoyaltyDAO();

    private static final int PLATINUM_THRESHOLD = 1000;
    private static final int DIAMOND_THRESHOLD = 3000;
    private static final int ELITE_THRESHOLD = 6000;

    private static final int POINTS_VALIDITY_MONTHS = 12;
    public static final int DEFAULT_EXPIRY_ALERT_DAYS = 30;

    private static final double DOLLARS_PER_POINT = 1.0;

    
    public LoyaltyControl() {
        if (App.memberList.isEmpty()) {
            RegistrationDAO.initializeMemberData();
        }
        memberList = App.memberList;
        ensureSharedDataInitialized();
    }

   
    public LoyaltyControl(ListInterface<Member> sharedMemberList) {
        memberList = sharedMemberList;
        ensureSharedDataInitialized();
    }

 
    private static void ensureSharedDataInitialized() {
        if (rewardCatalog == null) {
            rewardCatalog = loyaltyDAO.initializeRewardCatalog();
        }
        if (transactionList == null) {
            transactionList = loyaltyDAO.initializeTransactionData(App.memberList, POINTS_VALIDITY_MONTHS);
        }
        if (redemptionHistory == null) {
            redemptionHistory = new DoublyLinkedList<>();
        }
        if (pendingPointsQueue == null) {
            pendingPointsQueue = new DoublyLinkedList<>();
        }
        if (queuedStayBookingIDs == null) {
            queuedStayBookingIDs = new DoublyLinkedList<>();
        }
        if (lifetimeEarnedList == null) {
            lifetimeEarnedList = new DoublyLinkedList<>();
            
            for (int i = 1; i <= App.memberList.getNumberOfEntries(); i++) {
                Member m = App.memberList.getEntry(i);
                if (m.getLoyaltyPoints() > 0) {
                    lifetimeEarnedList.add(new LifetimeEarnedPoints(m.getMemberID(), m.getLoyaltyPoints()));
                }
            }
        }
    }

   
    private static int findLifetimeEarnedPosition(int memberID) {
        LifetimeEarnedPoints probe = new LifetimeEarnedPoints();
        probe.memberID = memberID;
        return lifetimeEarnedList.indexOf(probe);
    }

    private static int getLifetimeEarned(int memberID) {
        int position = findLifetimeEarnedPosition(memberID);
        return position == -1 ? 0 : lifetimeEarnedList.getEntry(position).totalEarned;
    }

  
    public int getLifetimeEarnedPoints(int memberID) {
        return getLifetimeEarned(memberID);
    }

 
    private static void addLifetimeEarned(int memberID, int pointsToAdd) {
        int position = findLifetimeEarnedPosition(memberID);
        if (position == -1) {
            lifetimeEarnedList.add(new LifetimeEarnedPoints(memberID, pointsToAdd));
        } else {
            lifetimeEarnedList.getEntry(position).totalEarned += pointsToAdd;
        }
    }

  
    public static String queueCompletedStayPoints() {
        ensureSharedDataInitialized();
        StringBuilder sb = new StringBuilder();
        int newlyQueuedCount = 0;

        for (int i = 1; i <= App.bookingHistoryList.getNumberOfEntries(); i++) {
            Booking b = App.bookingHistoryList.getEntry(i);
            if (b.getBookingStatus() != Booking.BookingStatus.CHECKED_OUT) {
                continue; // only completed, paid stays earn points - not cancellations
            }
            if (queuedStayBookingIDs.contains(b.getBookingID())) {
                continue; // already queued on a previous scan
            }

          
            queuedStayBookingIDs.add(b.getBookingID());

            Member member = b.getMember();
            double amountSpent = calculateStayBill(b);
            int pointsEarned = (int) (amountSpent / DOLLARS_PER_POINT);
            if (pointsEarned <= 0 || member == null) {
                continue;
            }

            String sourceDetail = "Booking #" + b.getBookingID() + " ($" +
                    String.format("%.2f", amountSpent) + " spent)";
            PendingPointsCredit credit = new PendingPointsCredit(member.getMemberID(), member.getMemberName(),
                    CreditSource.STAY, sourceDetail, pointsEarned, VirtualClock.getInstance().today());
            pendingPointsQueue.add(credit);
            newlyQueuedCount++;

            sb.append(member.getMemberName()).append(" - ").append(sourceDetail)
              .append(": ").append(pointsEarned).append(" point(s) pending accumulation.\n");
        }

        if (newlyQueuedCount == 0) {
            return "";
        }
        sb.append("\n").append(newlyQueuedCount)
          .append(" new completed stay(s) queued for points accumulation. ")
          .append("Go to 'Points Accumulation Queue' to review and credit them.");
        return sb.toString();
    }

 
    private static double calculateStayBill(Booking b) {
        Room room = b.getRoom();
        if (room == null) {
            return 0;
        }
        double rate = room.getRoomType().getBaseRate();

        LocalDate checkIn = b.getCheckInDate();
        LocalDate scheduledCheckOut = b.getCheckOutDate();
        LocalDate actualCheckOut = b.getBookingDate();

        long scheduledNights = ChronoUnit.DAYS.between(checkIn, scheduledCheckOut);
        if (scheduledNights <= 0) {
            scheduledNights = 1;
        }

        long overstayDays = ChronoUnit.DAYS.between(scheduledCheckOut, actualCheckOut);
        if (overstayDays > 0) {
            double normalCharge = scheduledNights * rate;
            double penaltyCharge = overstayDays * rate * 1.5;
            return normalCharge + penaltyCharge;
        } else {
            long actualNights = ChronoUnit.DAYS.between(checkIn, actualCheckOut);
            if (actualNights <= 0) {
                actualNights = 1;
            }
            return actualNights * rate;
        }
    }

  

   
    public String grantPromotionalPoints(int memberID, int points, String reason) {
        Member member = findMemberByID(memberID);
        if (member == null) {
            return "Member with ID " + memberID + " not found.";
        }
        if (points <= 0) {
            return "Promotional points must be a positive value.";
        }
        if (reason == null || reason.trim().isEmpty()) {
            return "A reason is required for a personalized promotion grant.";
        }

        PendingPointsCredit credit = new PendingPointsCredit(member.getMemberID(), member.getMemberName(),
                CreditSource.PROMOTION, reason.trim(), points, VirtualClock.getInstance().today());
        pendingPointsQueue.add(credit);
        return "Queued " + points + " promotional point(s) for " + member.getMemberName() +
                " (reason: " + reason.trim() + "). Process the queue to credit them.";
    }

   
    public int getPendingPointsQueueCount() {
        return pendingPointsQueue.getNumberOfEntries();
    }

    // ---- Read ----
    public String viewPendingPointsQueue() {
        StringBuilder sb = new StringBuilder();
        sb.append(ReportFormatUtility.buildHeader("PENDING POINTS ACCUMULATION QUEUE", VirtualClock.getInstance().now()));
        sb.append(String.format("%-8s %-10s %-18s %-10s %-8s %-12s %-30s%n",
                "CreditID", "MemberID", "Member Name", "Source", "Points", "Queued On", "Detail"));
        sb.append(ReportFormatUtility.separatorLine());

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        int total = pendingPointsQueue.getNumberOfEntries();
        for (int i = 1; i <= total; i++) {
            PendingPointsCredit c = pendingPointsQueue.getEntry(i);
            sb.append(String.format("%-8d %-10d %-18s %-10s %-8d %-12s %-30s%n",
                    c.getCreditID(), c.getMemberID(), c.getMemberName(), c.getSource(),
                    c.getPointsToCredit(), c.getDateQueued().format(dateFormatter), c.getSourceDetail()));
        }
        sb.append(ReportFormatUtility.buildFooter("Total pending credits", total,
                "No points are currently pending accumulation. The next-position (front) entry is processed first."));
        return sb.toString();
    }

    public String processNextPendingPointsCredit() {
        if (pendingPointsQueue.isEmpty()) {
            return "No pending points credits to process.";
        }
        PendingPointsCredit next = pendingPointsQueue.remove(1); // dequeue from front (FIFO)
        Member member = findMemberByID(next.getMemberID());
        if (member == null) {
            return "Skipped credit ID " + next.getCreditID() + ": member " + next.getMemberID() + " no longer exists.";
        }
        String label = next.getSource() + " - " + next.getSourceDetail();
        return creditPointsToMember(member, next.getPointsToCredit(), label);
    }

    public String processAllPendingPointsCredits() {
        if (pendingPointsQueue.isEmpty()) {
            return "No pending points credits to process.";
        }
        StringBuilder sb = new StringBuilder();
        int total = pendingPointsQueue.getNumberOfEntries();
        for (int i = 0; i < total; i++) {
            sb.append(processNextPendingPointsCredit()).append("\n\n");
        }
        return sb.toString().trim();
    }

    public String rejectPendingPointsCredit(int creditID) {
        
        PendingPointsCredit probe = new PendingPointsCredit();
        probe.setCreditID(creditID);
        int position = pendingPointsQueue.indexOf(probe);
        if (position == -1) {
            return "Pending credit with ID " + creditID + " not found in queue.";
        }
        PendingPointsCredit removed = pendingPointsQueue.remove(position);
        return "Rejected credit ID " + creditID + ": " + removed.getPointsToCredit() +
                " point(s) for " + removed.getMemberName() + " (" + removed.getSourceDetail() +
                ") will NOT be accumulated.";
    }

    
    private static String creditPointsToMember(Member member, int pointsToCredit, String sourceLabel) {
        if (member == null || pointsToCredit <= 0) {
            return "";
        }

        
        member.setLoyaltyPoints(member.getLoyaltyPoints() + pointsToCredit);
       
        addLifetimeEarned(member.getMemberID(), pointsToCredit);
        String tierMessage = updateTier(member);

        LocalDate earnedDate = VirtualClock.getInstance().today();
        LocalDate expiryDate = earnedDate.plusMonths(POINTS_VALIDITY_MONTHS);

      
        PointsTransaction existing = findTransactionByMemberID(member.getMemberID());
        if (existing != null) {
            existing.setPointsEarned(existing.getPointsEarned() + pointsToCredit);
            existing.setEarnedDate(earnedDate);
            existing.setExpiryDate(expiryDate);
        } else {
            transactionList.add(new PointsTransaction(member.getMemberID(), member.getMemberName(),
                    pointsToCredit, earnedDate, expiryDate));
        }

        StringBuilder sb = new StringBuilder();
        sb.append(member.getMemberName()).append(" credited with ").append(pointsToCredit)
          .append(" point(s) [").append(sourceLabel).append("]. New balance: ")
          .append(member.getLoyaltyPoints()).append(" points.");
        if (!tierMessage.isEmpty()) {
            sb.append("\n").append(tierMessage);
        }
        return sb.toString();
    }

   
    private static PointsTransaction findTransactionByMemberID(int memberID) {
        for (int i = 1; i <= transactionList.getNumberOfEntries(); i++) {
            PointsTransaction t = transactionList.getEntry(i);
            if (t.getMemberID() == memberID) {
                return t;
            }
        }
        return null;
    }

  
    public String redeemReward(int memberID, int rewardID) {
        Member member = findMemberByID(memberID);
        if (member == null) {
            return "Member with ID " + memberID + " not found.";
        }
        RewardItem reward = findRewardByID(rewardID);
        if (reward == null) {
            return "Reward with ID " + rewardID + " not found in catalog.";
        }
        if (member.getLoyaltyPoints() < reward.getPointsRequired()) {
            return member.getMemberName() + " has insufficient points to redeem \"" +
                    reward.getRewardName() + "\" (needs " + reward.getPointsRequired() +
                    ", has " + member.getLoyaltyPoints() + ").";
        }

       
        member.setLoyaltyPoints(member.getLoyaltyPoints() - reward.getPointsRequired());

  
        PointsTransaction existing = findTransactionByMemberID(member.getMemberID());
        if (existing != null) {
            int remainingPoints = existing.getPointsEarned() - reward.getPointsRequired();
            existing.setPointsEarned(Math.max(remainingPoints, 0));
        }

        
        RedemptionRecord record = new RedemptionRecord(member.getMemberID(), member.getMemberName(),
                reward.getRewardID(), reward.getRewardName(), reward.getPointsRequired(),
                VirtualClock.getInstance().today());
        redemptionHistory.add(1, record);

        return member.getMemberName() + " redeemed \"" + reward.getRewardName() +
                "\" for " + reward.getPointsRequired() + " points. Remaining balance: " +
                member.getLoyaltyPoints() + " points. (Tier is unaffected by redemption.)";
    }

   
    private static String updateTier(Member member) {
        LoyaltyTier oldTier = member.getLoyaltyTier();
        LoyaltyTier newTier;
        int points = getLifetimeEarned(member.getMemberID());

        if (points >= ELITE_THRESHOLD) {
            newTier = LoyaltyTier.Elite;
        } else if (points >= DIAMOND_THRESHOLD) {
            newTier = LoyaltyTier.Diamond;
        } else if (points >= PLATINUM_THRESHOLD) {
            newTier = LoyaltyTier.Platinum;
        } else {
            newTier = LoyaltyTier.Regular;
        }

        if (newTier != oldTier) {
            member.setLoyaltyTier(newTier);
            if (newTier.ordinal() > oldTier.ordinal()) {
                return "Congratulations! " + member.getMemberName() + " has been upgraded from "
                        + oldTier + " to " + newTier + "!";
            } else {
                return member.getMemberName() + " has been moved down from "
                        + oldTier + " to " + newTier + ".";
            }
        }
        return "";
    }

 

    public Member searchMemberByID(int memberID) {
        return findMemberByID(memberID);
    }

    public ListInterface<Member> searchMemberByName(String nameKeyword) {
        ListInterface<Member> results = new DoublyLinkedList<>();
        for (int i = 1; i <= memberList.getNumberOfEntries(); i++) {
            Member m = memberList.getEntry(i);
            if (m.getMemberName().toLowerCase().contains(nameKeyword.toLowerCase())) {
                results.add(m);
            }
        }
        return results;
    }

    public ListInterface<Member> searchMemberByTier(LoyaltyTier tier) {
        ListInterface<Member> results = new DoublyLinkedList<>();
        for (int i = 1; i <= memberList.getNumberOfEntries(); i++) {
            Member m = memberList.getEntry(i);
            if (m.getLoyaltyTier() == tier) {
                results.add(m);
            }
        }
        return results;
    }

    private Member findMemberByID(int memberID) {
        ListInterface<Member> sortedCopy = memberList.copy();
        selectionSortByID(sortedCopy);

        int low = 1;
        int high = sortedCopy.getNumberOfEntries();
        while (low <= high) {
            int mid = (low + high) / 2;
            Member midMember = sortedCopy.getEntry(mid);
            if (midMember.getMemberID() == memberID) {
                return midMember; // same object reference as the one stored in memberList
            } else if (midMember.getMemberID() < memberID) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return null;
    }

    private RewardItem findRewardByID(int rewardID) {
        int position = findRewardPosition(rewardID);
        return position == -1 ? null : rewardCatalog.getEntry(position);
    }

    private int findRewardPosition(int rewardID) {
        RewardItem probe = new RewardItem();
        probe.setRewardID(rewardID);
        return rewardCatalog.indexOf(probe);
    }

   

    private void selectionSortByID(ListInterface<Member> list) {
        int n = list.getNumberOfEntries();
        for (int i = 1; i <= n - 1; i++) {
            int minPos = i;
            for (int j = i + 1; j <= n; j++) {
                if (list.getEntry(j).getMemberID() < list.getEntry(minPos).getMemberID()) {
                    minPos = j;
                }
            }
            if (minPos != i) {
                list.swap(i, minPos);
            }
        }
    }

    private void bubbleSortByPointsDescending(ListInterface<Member> list) {
        int n = list.getNumberOfEntries();
        for (int i = 1; i <= n - 1; i++) {
            for (int j = 1; j <= n - i; j++) {
                if (list.getEntry(j).getLoyaltyPoints() < list.getEntry(j + 1).getLoyaltyPoints()) {
                    list.swap(j, j + 1);
                }
            }
        }
    }

    private void insertionSortByExpiryDate(ListInterface<PointsTransaction> list) {
        int n = list.getNumberOfEntries();
        for (int i = 2; i <= n; i++) {
            int j = i;
            while (j > 1 && list.getEntry(j).getExpiryDate().isBefore(list.getEntry(j - 1).getExpiryDate())) {
                list.swap(j, j - 1);
                j--;
            }
        }
    }

    
    public String generateLoyaltyReport() {
        ListInterface<Member> sortedList = memberList.copy();
        bubbleSortByPointsDescending(sortedList);

        StringBuilder sb = new StringBuilder();
        sb.append(ReportFormatUtility.buildHeader("MEMBER RANKING REPORT (BY POINTS)", VirtualClock.getInstance().now()));
        sb.append(String.format("%-5s %-10s %-20s %-12s %-10s %-15s%n",
                "Rank", "Member ID", "Member Name", "Tier", "Points", "Lifetime Earned"));
        sb.append(ReportFormatUtility.separatorLine());

        int total = sortedList.getNumberOfEntries();
        String[] labels = new String[total];
        int[] points = new int[total];
        for (int i = 1; i <= total; i++) {
            Member m = sortedList.getEntry(i);
            sb.append(String.format("%-5d %-10d %-20s %-12s %-10d %-15d%n",
                    i, m.getMemberID(), m.getMemberName(), m.getLoyaltyTier(), m.getLoyaltyPoints(),
                    getLifetimeEarned(m.getMemberID())));
            labels[i - 1] = m.getMemberName();
            points[i - 1] = m.getLoyaltyPoints();
        }

        sb.append(ReportFormatUtility.separatorLine());
        sb.append("Note: Points = current spendable balance. Lifetime Earned = total ever earned")
          .append(" (used to determine Tier; unaffected by redemption or expiry).\n");
        sb.append(ReportFormatUtility.buildBarChart("POINTS DISTRIBUTION", labels, points, "points"));
        sb.append(ReportFormatUtility.buildFooter("Total members displayed", total));
        return sb.toString();
    }

 
    public String generateTierDistributionReport() {
        LoyaltyTier[] tiers = LoyaltyTier.values();
        int[] memberCount = new int[tiers.length];
        int[] totalPoints = new int[tiers.length];

        for (int i = 1; i <= memberList.getNumberOfEntries(); i++) {
            Member m = memberList.getEntry(i);
            int tierIndex = m.getLoyaltyTier().ordinal();
            memberCount[tierIndex]++;
            totalPoints[tierIndex] += m.getLoyaltyPoints();
        }

        int totalMembers = memberList.getNumberOfEntries();

        StringBuilder sb = new StringBuilder();
        sb.append(ReportFormatUtility.buildHeader("TIER DISTRIBUTION SUMMARY REPORT", VirtualClock.getInstance().now()));
        sb.append(String.format("%-12s %-14s %-16s %-12s%n", "Tier", "No. Members", "Total Points", "Avg Points"));
        sb.append(ReportFormatUtility.separatorLine());

        int total = tiers.length;
        String[] tierLabels = new String[total];
        int[] tierMemberCounts = new int[total];
        int chartIndex = 0;
        for (int i = tiers.length - 1; i >= 0; i--) { // display highest tier (Elite) first
            int count = memberCount[i];
            int avg = count == 0 ? 0 : totalPoints[i] / count;
            sb.append(String.format("%-12s %-14d %-16d %-12d%n", tiers[i], count, totalPoints[i], avg));
            tierLabels[chartIndex] = tiers[i].toString();
            tierMemberCounts[chartIndex] = count;
            chartIndex++;
        }

        sb.append(ReportFormatUtility.separatorLine());
        sb.append(ReportFormatUtility.buildBarChart("MEMBER COUNT BY TIER", tierLabels, tierMemberCounts, "member(s)"));
        sb.append(ReportFormatUtility.buildFooter("Total members in program", totalMembers));
        return sb.toString();
    }


    public static String expireOverduePoints() {
        ensureSharedDataInitialized();
        LocalDate today = VirtualClock.getInstance().today();
        StringBuilder sb = new StringBuilder();
        int expiredCount = 0;

        for (int i = transactionList.getNumberOfEntries(); i >= 1; i--) {
            PointsTransaction t = transactionList.getEntry(i);
            if (!today.isAfter(t.getExpiryDate())) {
                continue; // today == expiryDate is still the last valid day, not expired yet
            }

            Member member = findMemberByIDStatic(t.getMemberID());
            if (member != null && t.getPointsEarned() > 0) {
                int forfeited = Math.min(t.getPointsEarned(), member.getLoyaltyPoints());
                member.setLoyaltyPoints(member.getLoyaltyPoints() - forfeited);

                sb.append(member.getMemberName()).append(" (ID ").append(member.getMemberID())
                  .append(") - ").append(forfeited).append(" point(s) EXPIRED on ")
                  .append(t.getExpiryDate()).append(" and have been forfeited. New spendable balance: ")
                  .append(member.getLoyaltyPoints()).append(" points. (Tier is unaffected by expiry.)\n");
            }

            transactionList.remove(i);
            expiredCount++;
        }

        if (expiredCount == 0) {
            return "";
        }
        sb.append("\n").append(expiredCount)
          .append(" points transaction(s) expired and were forfeited.");
        return sb.toString();
    }

 
    private static Member findMemberByIDStatic(int memberID) {
        for (int i = 1; i <= App.memberList.getNumberOfEntries(); i++) {
            Member m = App.memberList.getEntry(i);
            if (m.getMemberID() == memberID) {
                return m;
            }
        }
        return null;
    }

  
    public int getExpiringTransactionCount(int daysThreshold) {
        int count = 0;
        LocalDate today = VirtualClock.getInstance().today();
        for (int i = 1; i <= transactionList.getNumberOfEntries(); i++) {
            PointsTransaction t = transactionList.getEntry(i);
            long daysLeft = ChronoUnit.DAYS.between(today, t.getExpiryDate());
            if (daysLeft >= 0 && daysLeft <= daysThreshold) {
                count++;
            }
        }
        return count;
    }

    public String generateExpiryAlertReport(int daysThreshold) {
        LocalDate today = VirtualClock.getInstance().today();

        // collect only the transactions expiring within the threshold
        ListInterface<PointsTransaction> expiringList = new DoublyLinkedList<>();
        for (int i = 1; i <= transactionList.getNumberOfEntries(); i++) {
            PointsTransaction t = transactionList.getEntry(i);
            long daysLeft = ChronoUnit.DAYS.between(today, t.getExpiryDate());
            if (daysLeft >= 0 && daysLeft <= daysThreshold) {
                expiringList.add(t);
            }
        }
        insertionSortByExpiryDate(expiringList); // soonest-expiring first

        return buildTransactionReport(expiringList,
                "POINTS EXPIRY ALERT (Next " + daysThreshold + " Days)",
                "No points are expiring within the next " + daysThreshold + " days.");
    }

    public String generateAllTransactionsReport() {
        ListInterface<PointsTransaction> allTransactions = transactionList.copy();
        insertionSortByExpiryDate(allTransactions);

        return buildTransactionReport(allTransactions,
                "ALL POINTS TRANSACTIONS (Full History)",
                "No points transactions have been recorded yet.");
    }

    private String buildTransactionReport(ListInterface<PointsTransaction> list, String subtitle, String emptyMessage) {
        LocalDate today = VirtualClock.getInstance().today();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        StringBuilder sb = new StringBuilder();
        sb.append(ReportFormatUtility.buildHeader(subtitle, VirtualClock.getInstance().now()));
        sb.append(String.format("%-10s %-18s %-8s %-13s %-13s %-10s%n",
                "MemberID", "Member Name", "Points", "Earned Date", "Expires On", "Days Left"));
        sb.append(ReportFormatUtility.separatorLine());

        int total = list.getNumberOfEntries();
        String[] labels = new String[total];
        int[] daysLeftValues = new int[total];
        for (int i = 1; i <= total; i++) {
            PointsTransaction t = list.getEntry(i);
            long daysLeft = ChronoUnit.DAYS.between(today, t.getExpiryDate());
            sb.append(String.format("%-10d %-18s %-8d %-13s %-13s %-10d%n",
                    t.getMemberID(), t.getMemberName(), t.getPointsEarned(),
                    t.getEarnedDate().format(dateFormatter), t.getExpiryDate().format(dateFormatter), daysLeft));
            labels[i - 1] = t.getMemberName();
            daysLeftValues[i - 1] = (int) Math.max(0, daysLeft);
        }

        if (total > 0) {
            sb.append(ReportFormatUtility.separatorLine());
            sb.append(ReportFormatUtility.buildBarChart("DAYS LEFT UNTIL EXPIRY", labels, daysLeftValues, "days"));
        }
        sb.append(ReportFormatUtility.buildFooter("Total transactions", total, emptyMessage));
        return sb.toString();
    }

    public ListInterface<RedemptionRecord> searchRedemptionsByMemberID(int memberID) {
        ListInterface<RedemptionRecord> results = new DoublyLinkedList<>();
        for (int i = 1; i <= redemptionHistory.getNumberOfEntries(); i++) {
            RedemptionRecord r = redemptionHistory.getEntry(i);
            if (r.getMemberID() == memberID) {
                results.add(r);
            }
        }
        return results;
    }

    public String generateMemberRedemptionReport(int memberID) {
        Member member = findMemberByID(memberID);
        if (member == null) {
            return "Member with ID " + memberID + " not found.";
        }
        ListInterface<RedemptionRecord> memberRedemptions = searchRedemptionsByMemberID(memberID);
        return buildRedemptionReport(memberRedemptions,
                "REDEMPTION HISTORY - " + member.getMemberName() + " (ID " + memberID + ")",
                member.getMemberName() + " has not redeemed any rewards yet.");
    }

    public String generateAllRedemptionsReport() {
        return buildRedemptionReport(redemptionHistory,
                "ALL REWARD REDEMPTIONS (Full History)",
                "No rewards have been redeemed yet.");
    }

    // shared formatting logic for both the per-member and full-history redemption reports
    private String buildRedemptionReport(ListInterface<RedemptionRecord> list, String subtitle, String emptyMessage) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        StringBuilder sb = new StringBuilder();
        sb.append(ReportFormatUtility.buildHeader(subtitle, VirtualClock.getInstance().now()));
        sb.append(String.format("%-10s %-18s %-30s %-8s %-12s%n",
                "MemberID", "Member Name", "Reward Redeemed", "Points", "Date"));
        sb.append(ReportFormatUtility.separatorLine());

        int total = list.getNumberOfEntries();
        for (int i = 1; i <= total; i++) {
            RedemptionRecord r = list.getEntry(i);
            sb.append(String.format("%-10d %-18s %-30s %-8d %-12s%n",
                    r.getMemberID(), r.getMemberName(), r.getRewardName(),
                    r.getPointsUsed(), r.getRedeemedDate().format(dateFormatter)));
        }

        sb.append(ReportFormatUtility.buildFooter("Total redemptions", total, emptyMessage));
        return sb.toString();
    }

  
    public String addRewardItem(String rewardName, String description, int pointsRequired) {
        if (rewardName == null || rewardName.trim().isEmpty()) {
            return "Reward name cannot be empty.";
        }
        if (pointsRequired <= 0) {
            return "Points required must be a positive value.";
        }
        RewardItem newReward = new RewardItem(rewardName, description, pointsRequired);
        rewardCatalog.add(newReward);
        return "New reward added: " + newReward.getRewardName() +
                " (ID " + newReward.getRewardID() + ", " + newReward.getPointsRequired() + " points).";
    }

    // ---- Update ----
    public String updateRewardItem(int rewardID, String newName, int newPointsRequired) {
        int position = findRewardPosition(rewardID);
        if (position == -1) {
            return "Reward with ID " + rewardID + " not found in catalog.";
        }
        if (newPointsRequired <= 0) {
            return "Points required must be a positive value.";
        }
        
        RewardItem existingReward = rewardCatalog.getEntry(position);
        existingReward.setRewardName(newName);
        existingReward.setPointsRequired(newPointsRequired);
        return "Reward ID " + rewardID + " updated successfully.";
    }

    public String deleteRewardItem(int rewardID) {
        int position = findRewardPosition(rewardID);
        if (position == -1) {
            return "Reward with ID " + rewardID + " not found in catalog.";
        }
        RewardItem removed = rewardCatalog.remove(position);
        return "Reward removed: " + removed.getRewardName() + " (ID " + removed.getRewardID() + ").";
    }

   
    public String displayRewardCatalog() {
        StringBuilder sb = new StringBuilder();
        sb.append("=============================================================\n");
        sb.append("                    REWARD CATALOG\n");
        sb.append("=============================================================\n");
        sb.append(String.format("%-4s %-25s %-15s%n", "ID", "Reward Name", "Points Needed"));
        sb.append("-------------------------------------------------------------\n");

        for (int i = 1; i <= rewardCatalog.getNumberOfEntries(); i++) {
            RewardItem r = rewardCatalog.getEntry(i);
            sb.append(String.format("%-4d %-25s %-15d%n", r.getRewardID(), r.getRewardName(), r.getPointsRequired()));
        }
        sb.append("=============================================================\n");
        return sb.toString();
    }

    public ListInterface<Member> getMemberList() {
        return memberList;
    }
}