/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package control;

/**
 *
 * @author Kao Yong Feng
 */
import adt.DoublyLinkedList;
import adt.ListInterface;
import dao.LoyaltyDAO;
import dao.RegistrationDAO;
import entity.Booking;
import entity.LifetimeEarnedPoints;
import entity.Member;
import entity.Member.LoyaltyTier;
import entity.PendingPointsCredit;
import entity.PendingPointsCredit.CreditSource;
import entity.PointsTransaction;
import entity.RedemptionRecord;
import entity.RewardItem;
import entity.Room;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import main.App;
import utility.VirtualClock;

public class LoyaltyControl {

    // ==========================================================
    // Loyalty & Reward Module Collection ADTs
    // ==========================================================
    public static ListInterface<RewardItem> rewardCatalog = new DoublyLinkedList<>();
    public static ListInterface<PointsTransaction> pointsTransactionList = new DoublyLinkedList<>();
    public static ListInterface<PendingPointsCredit> pendingPointsQueue = new DoublyLinkedList<>();
    public static ListInterface<RedemptionRecord> redemptionHistoryList = new DoublyLinkedList<>();
    public static ListInterface<LifetimeEarnedPoints> lifetimeEarnedList = new DoublyLinkedList<>();
    public static ListInterface<Integer> queuedStayBookingIDs = new DoublyLinkedList<>();

    private static LoyaltyDAO loyaltyDAO = new LoyaltyDAO();
    private static boolean sharedDataInitialized = false;

    public static final int PLATINUM_THRESHOLD = 1000;
    public static final int DIAMOND_THRESHOLD = 3000;
    public static final int ELITE_THRESHOLD = 6000;

    public static final int POINTS_VALIDITY_MONTHS = 12;
    public static final int DEFAULT_EXPIRY_ALERT_DAYS = 30;
    public static final double DOLLARS_PER_POINT = 1.0;

    public static final int SORT_BY_POINTS = 1;
    public static final int SORT_BY_NAME = 2;
    public static final int SORT_BY_TIER = 3;
    public static final int SORT_BY_MEMBER_ID = 4;

    public static final int REDEMPTION_SORT_BY_DATE = 1;
    public static final int REDEMPTION_SORT_BY_POINTS = 2;
    public static final int REDEMPTION_SORT_BY_MEMBER_NAME = 3;
    public static final int REDEMPTION_SORT_BY_REWARD_NAME = 4;

    public static final int REDEEM_SUCCESS = 0;
    public static final int REDEEM_MEMBER_NOT_FOUND = 1;
    public static final int REDEEM_REWARD_NOT_FOUND = 2;
    public static final int REDEEM_INSUFFICIENT_POINTS = 3;

    public LoyaltyControl() {

        if (App.memberList == null || App.memberList.isEmpty()) {
            RegistrationDAO.initializeMemberData();
        }

        ensureSharedDataInitialized();
    }

    public LoyaltyControl(ListInterface<Member> sharedMemberList) {

        if (sharedMemberList != null && !sharedMemberList.isEmpty()) {
            App.memberList = sharedMemberList;
        }

        ensureSharedDataInitialized();
    }

    public static void ensureSharedDataInitialized() {

        if (sharedDataInitialized) {
            return;
        }

        if (rewardCatalog.isEmpty()) {

            ListInterface<RewardItem> seededCatalog = loyaltyDAO.initializeRewardCatalog();

            for (int i = 1; i <= seededCatalog.getNumberOfEntries(); i++) {
                rewardCatalog.add(seededCatalog.getEntry(i));
            }
        }

        if (pointsTransactionList.isEmpty()) {

            ListInterface<PointsTransaction> seededTransactions
                    = loyaltyDAO.initializeTransactionData(App.memberList, POINTS_VALIDITY_MONTHS);

            for (int i = 1; i <= seededTransactions.getNumberOfEntries(); i++) {
                pointsTransactionList.add(seededTransactions.getEntry(i));
            }
        }

        if (lifetimeEarnedList.isEmpty()) {

            for (int i = 1; i <= App.memberList.getNumberOfEntries(); i++) {

                Member m = App.memberList.getEntry(i);

                if (m.getLoyaltyPoints() > 0) {
                    lifetimeEarnedList.add(new LifetimeEarnedPoints(m.getMemberID(), m.getLoyaltyPoints()));
                }
            }
        }

        sharedDataInitialized = true;
    }

    // Lifetime Points Management
    private static int findLifetimeEarnedPosition(int memberID) {

        LifetimeEarnedPoints probe = new LifetimeEarnedPoints();
        probe.setMemberID(memberID);

        return lifetimeEarnedList.indexOf(probe);
    }

    public int getLifetimeEarnedPoints(int memberID) {

        int position = findLifetimeEarnedPosition(memberID);

        return position == -1 ? 0 : lifetimeEarnedList.getEntry(position).getTotalEarned();
    }

    public void addLifetimeEarned(int memberID, int pointsToAdd) {

        int position = findLifetimeEarnedPosition(memberID);

        if (position == -1) {
            lifetimeEarnedList.add(new LifetimeEarnedPoints(memberID, pointsToAdd));
        } else {
            lifetimeEarnedList.getEntry(position).addTotalEarned(pointsToAdd);
        }
    }

    public LoyaltyTier calculateTier(int lifetimePoints) {

        if (lifetimePoints >= ELITE_THRESHOLD) {
            return LoyaltyTier.Elite;
        } else if (lifetimePoints >= DIAMOND_THRESHOLD) {
            return LoyaltyTier.Diamond;
        } else if (lifetimePoints >= PLATINUM_THRESHOLD) {
            return LoyaltyTier.Platinum;
        } else {
            return LoyaltyTier.Regular;
        }
    }

    public boolean updateTier(Member member) {

        if (member == null) {
            return false;
        }

        LoyaltyTier oldTier = member.getLoyaltyTier();
        int lifetimePoints = getLifetimeEarnedPoints(member.getMemberID());
        LoyaltyTier newTier = calculateTier(lifetimePoints);

        if (newTier != oldTier) {
            member.setLoyaltyTier(newTier);
            return true;
        }

        return false;
    }

    // Bill Calculation & Stay Point Queueing
    public double calculateStayBill(Booking booking) {

        Room room = booking.getRoom();

        if (room == null) {
            return 0.0;
        }

        double rate = room.getRoomType().getBaseRate();
        LocalDate checkIn = booking.getCheckInDate();
        LocalDate scheduledCheckOut = booking.getCheckOutDate();
        LocalDate actualCheckOut = booking.getBookingDate();

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

    public ListInterface<PendingPointsCredit> queueCompletedStayPoints() {

        ensureSharedDataInitialized();

        ListInterface<PendingPointsCredit> newlyQueued = new DoublyLinkedList<>();

        for (int i = 1; i <= App.bookingHistoryList.getNumberOfEntries(); i++) {

            Booking booking = App.bookingHistoryList.getEntry(i);

            if (booking.getBookingStatus() != Booking.BookingStatus.CHECKED_OUT) {
                continue;
            }

            if (queuedStayBookingIDs.contains(booking.getBookingID())) {
                continue;
            }

            queuedStayBookingIDs.add(booking.getBookingID());

            Member member = booking.getMember();
            double amountSpent = calculateStayBill(booking);
            int pointsEarned = (int) (amountSpent / DOLLARS_PER_POINT);

            if (pointsEarned <= 0 || member == null) {
                continue;
            }

            String sourceDetail = "Booking #" + booking.getBookingID() + " ($"
                    + String.format("%.2f", amountSpent) + " spent)";

            PendingPointsCredit credit = new PendingPointsCredit(
                    member.getMemberID(),
                    member.getMemberName(),
                    CreditSource.STAY,
                    sourceDetail,
                    pointsEarned,
                    VirtualClock.getInstance().today()
            );

            pendingPointsQueue.add(credit);
            newlyQueued.add(credit);
        }

        return newlyQueued;
    }

    // Points Accumulation Queue Operations
    public boolean grantPromotionalPoints(int memberID, int points, String reason) {

        Member member = findMemberByID(memberID);

        if (member == null || points <= 0 || reason == null || reason.trim().isEmpty()) {
            return false;
        }

        PendingPointsCredit credit = new PendingPointsCredit(
                member.getMemberID(),
                member.getMemberName(),
                CreditSource.PROMOTION,
                reason.trim(),
                points,
                VirtualClock.getInstance().today()
        );

        pendingPointsQueue.add(credit);

        return true;
    }

    public ListInterface<PendingPointsCredit> getPendingPointsQueue() {
        return pendingPointsQueue;
    }

    public PendingPointsCredit processNextPendingPointsCredit() {

        if (pendingPointsQueue.isEmpty()) {
            return null;
        }

        PendingPointsCredit nextCredit = pendingPointsQueue.remove(1);
        Member member = findMemberByID(nextCredit.getMemberID());

        if (member != null) {
            creditPointsToMember(member, nextCredit.getPointsToCredit());
        }

        return nextCredit;
    }

    public ListInterface<PendingPointsCredit> processAllPendingPointsCredits() {

        ListInterface<PendingPointsCredit> processedList = new DoublyLinkedList<>();

        while (!pendingPointsQueue.isEmpty()) {

            PendingPointsCredit credit = processNextPendingPointsCredit();

            if (credit != null) {
                processedList.add(credit);
            }
        }

        return processedList;
    }

    public PendingPointsCredit rejectPendingPointsCredit(int creditID) {

        PendingPointsCredit probe = new PendingPointsCredit();
        probe.setCreditID(creditID);

        int position = pendingPointsQueue.indexOf(probe);

        if (position == -1) {
            return null;
        }

        return pendingPointsQueue.remove(position);
    }

    public void creditPointsToMember(Member member, int pointsToCredit) {

        if (member == null || pointsToCredit <= 0) {
            return;
        }

        member.setLoyaltyPoints(member.getLoyaltyPoints() + pointsToCredit);
        addLifetimeEarned(member.getMemberID(), pointsToCredit);
        updateTier(member);

        LocalDate earnedDate = VirtualClock.getInstance().today();
        LocalDate expiryDate = earnedDate.plusMonths(POINTS_VALIDITY_MONTHS);

        PointsTransaction existing = findTransactionByMemberID(member.getMemberID());

        if (existing != null) {
            existing.setPointsEarned(existing.getPointsEarned() + pointsToCredit);
            existing.setEarnedDate(earnedDate);
            existing.setExpiryDate(expiryDate);
        } else {
            pointsTransactionList.add(new PointsTransaction(
                    member.getMemberID(),
                    member.getMemberName(),
                    pointsToCredit,
                    earnedDate,
                    expiryDate
            ));
        }
    }

    public PointsTransaction findTransactionByMemberID(int memberID) {

        for (int i = 1; i <= pointsTransactionList.getNumberOfEntries(); i++) {

            PointsTransaction t = pointsTransactionList.getEntry(i);

            if (t.getMemberID() == memberID) {
                return t;
            }
        }

        return null;
    }

    // Expiry Management
    public ListInterface<PointsTransaction> expireOverduePoints() {

        ensureSharedDataInitialized();

        LocalDate today = VirtualClock.getInstance().today();
        ListInterface<PointsTransaction> expiredList = new DoublyLinkedList<>();

        for (int i = pointsTransactionList.getNumberOfEntries(); i >= 1; i--) {

            PointsTransaction t = pointsTransactionList.getEntry(i);

            if (!today.isAfter(t.getExpiryDate())) {
                continue;
            }

            Member member = findMemberByID(t.getMemberID());

            if (member != null && t.getPointsEarned() > 0) {

                int forfeited = Math.min(t.getPointsEarned(), member.getLoyaltyPoints());
                member.setLoyaltyPoints(member.getLoyaltyPoints() - forfeited);

                expiredList.add(t);
            }

            pointsTransactionList.remove(i);
        }

        return expiredList;
    }

    public ListInterface<PointsTransaction> getExpiringTransactions(int daysThreshold) {

        LocalDate today = VirtualClock.getInstance().today();
        ListInterface<PointsTransaction> expiringList = new DoublyLinkedList<>();

        for (int i = 1; i <= pointsTransactionList.getNumberOfEntries(); i++) {

            PointsTransaction t = pointsTransactionList.getEntry(i);
            long daysLeft = ChronoUnit.DAYS.between(today, t.getExpiryDate());

            if (daysLeft >= 0 && daysLeft <= daysThreshold) {
                expiringList.add(t);
            }
        }

        insertionSortByExpiryDate(expiringList);

        return expiringList;
    }

    public ListInterface<PointsTransaction> getAllTransactions() {

        ListInterface<PointsTransaction> allTransactions = pointsTransactionList.copy();
        insertionSortByExpiryDate(allTransactions);

        return allTransactions;
    }

    // Reward Redemption Operations
    public int redeemReward(int memberID, int rewardID) {

        Member member = findMemberByID(memberID);

        if (member == null) {
            return REDEEM_MEMBER_NOT_FOUND;
        }

        RewardItem reward = findRewardByID(rewardID);

        if (reward == null) {
            return REDEEM_REWARD_NOT_FOUND;
        }

        if (member.getLoyaltyPoints() < reward.getPointsRequired()) {
            return REDEEM_INSUFFICIENT_POINTS;
        }

        member.setLoyaltyPoints(member.getLoyaltyPoints() - reward.getPointsRequired());

        PointsTransaction existing = findTransactionByMemberID(member.getMemberID());

        if (existing != null) {
            int remaining = existing.getPointsEarned() - reward.getPointsRequired();
            existing.setPointsEarned(Math.max(remaining, 0));
        }

        RedemptionRecord record = new RedemptionRecord(
                member.getMemberID(),
                member.getMemberName(),
                reward.getRewardID(),
                reward.getRewardName(),
                reward.getPointsRequired(),
                VirtualClock.getInstance().today()
        );

        redemptionHistoryList.add(1, record);

        return REDEEM_SUCCESS;
    }

    public ListInterface<RedemptionRecord> getRedemptionHistory() {
        return redemptionHistoryList;
    }

    public ListInterface<RedemptionRecord> getFilteredAndSortedRedemptions(String searchKeyword, int sortOption) {

        ListInterface<RedemptionRecord> filtered = new DoublyLinkedList<>();

        if (searchKeyword == null || searchKeyword.trim().isEmpty()) {

            for (int i = 1; i <= redemptionHistoryList.getNumberOfEntries(); i++) {
                filtered.add(redemptionHistoryList.getEntry(i));
            }

        } else {

            String keyword = searchKeyword.trim();

            try {
                int memberID = Integer.parseInt(keyword);

                for (int i = 1; i <= redemptionHistoryList.getNumberOfEntries(); i++) {

                    RedemptionRecord r = redemptionHistoryList.getEntry(i);

                    if (r.getMemberID() == memberID) {
                        filtered.add(r);
                    }
                }

            } catch (NumberFormatException e) {

                for (int i = 1; i <= redemptionHistoryList.getNumberOfEntries(); i++) {

                    RedemptionRecord r = redemptionHistoryList.getEntry(i);

                    if (r.getMemberName().toLowerCase().contains(keyword.toLowerCase())
                            || r.getRewardName().toLowerCase().contains(keyword.toLowerCase())) {
                        filtered.add(r);
                    }
                }
            }
        }

        switch (sortOption) {

            case REDEMPTION_SORT_BY_POINTS:
                bubbleSortRedemptionsByPointsDescending(filtered);
                break;

            case REDEMPTION_SORT_BY_MEMBER_NAME:
                insertionSortRedemptionsByMemberName(filtered);
                break;

            case REDEMPTION_SORT_BY_REWARD_NAME:
                insertionSortRedemptionsByRewardName(filtered);
                break;

            case REDEMPTION_SORT_BY_DATE:
            default:
                insertionSortRedemptionsByDateDescending(filtered);
                break;
        }

        return filtered;
    }

    // Reward Catalog Management
    public ListInterface<RewardItem> getRewardCatalog() {
        return rewardCatalog;
    }

    public RewardItem findRewardByID(int rewardID) {

        RewardItem probe = new RewardItem();
        probe.setRewardID(rewardID);

        int position = rewardCatalog.indexOf(probe);

        return position == -1 ? null : rewardCatalog.getEntry(position);
    }

    public boolean addRewardItem(String rewardName, String description, int pointsRequired) {

        if (rewardName == null || rewardName.trim().isEmpty() || pointsRequired <= 0) {
            return false;
        }

        RewardItem newReward = new RewardItem(rewardName.trim(), description, pointsRequired);
        rewardCatalog.add(newReward);

        return true;
    }

    public boolean updateRewardItem(int rewardID, String newName, int newPointsRequired) {

        RewardItem reward = findRewardByID(rewardID);

        if (reward == null || newName == null || newName.trim().isEmpty() || newPointsRequired <= 0) {
            return false;
        }

        reward.setRewardName(newName.trim());
        reward.setPointsRequired(newPointsRequired);

        return true;
    }

    public boolean deleteRewardItem(int rewardID) {

        RewardItem probe = new RewardItem();
        probe.setRewardID(rewardID);

        int position = rewardCatalog.indexOf(probe);

        if (position == -1) {
            return false;
        }

        rewardCatalog.remove(position);

        return true;
    }

    // Member Search & Filtering
    public Member findMemberByID(int memberID) {

        ListInterface<Member> sortedCopy = App.memberList.copy();
        selectionSortByID(sortedCopy);

        int low = 1;
        int high = sortedCopy.getNumberOfEntries();

        while (low <= high) {

            int mid = (low + high) / 2;
            Member midMember = sortedCopy.getEntry(mid);

            if (midMember.getMemberID() == memberID) {
                return midMember;
            } else if (midMember.getMemberID() < memberID) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }

        return null;
    }

    public ListInterface<Member> getAllMembers() {
        return App.memberList;
    }

    public ListInterface<Member> searchMemberByName(String nameKeyword) {

        ListInterface<Member> results = new DoublyLinkedList<>();

        for (int i = 1; i <= App.memberList.getNumberOfEntries(); i++) {

            Member m = App.memberList.getEntry(i);

            if (m.getMemberName().toLowerCase().contains(nameKeyword.toLowerCase())) {
                results.add(m);
            }
        }

        return results;
    }

    public ListInterface<Member> searchMemberByTier(LoyaltyTier tier) {

        ListInterface<Member> results = new DoublyLinkedList<>();

        for (int i = 1; i <= App.memberList.getNumberOfEntries(); i++) {

            Member m = App.memberList.getEntry(i);

            if (m.getLoyaltyTier() == tier) {
                results.add(m);
            }
        }

        return results;
    }

    public ListInterface<Member> getFilteredAndSortedMembers(String searchKeyword, int sortOption) {

        ListInterface<Member> filtered = new DoublyLinkedList<>();

        if (searchKeyword == null || searchKeyword.trim().isEmpty()) {

            for (int i = 1; i <= App.memberList.getNumberOfEntries(); i++) {
                filtered.add(App.memberList.getEntry(i));
            }

        } else {

            String keyword = searchKeyword.trim();

            try {
                int memberID = Integer.parseInt(keyword);
                Member match = findMemberByID(memberID);

                if (match != null) {
                    filtered.add(match);
                }

                return filtered;

            } catch (NumberFormatException e) {
                // Not numeric, proceed to tier and name match
            }

            boolean tierMatched = false;

            for (LoyaltyTier tier : LoyaltyTier.values()) {

                if (tier.name().equalsIgnoreCase(keyword)) {

                    for (int i = 1; i <= App.memberList.getNumberOfEntries(); i++) {

                        Member m = App.memberList.getEntry(i);

                        if (m.getLoyaltyTier() == tier) {
                            filtered.add(m);
                        }
                    }

                    tierMatched = true;
                    break;
                }
            }

            if (!tierMatched) {

                for (int i = 1; i <= App.memberList.getNumberOfEntries(); i++) {

                    Member m = App.memberList.getEntry(i);

                    if (m.getMemberName().toLowerCase().contains(keyword.toLowerCase())) {
                        filtered.add(m);
                    }
                }
            }
        }

        switch (sortOption) {

            case SORT_BY_NAME:
                insertionSortByNameAscending(filtered);
                break;

            case SORT_BY_TIER:
                selectionSortByTierThenPointsDescending(filtered);
                break;

            case SORT_BY_MEMBER_ID:
                selectionSortByID(filtered);
                break;

            case SORT_BY_POINTS:
            default:
                bubbleSortByPointsDescending(filtered);
                break;
        }

        return filtered;
    }

    // Sorting Algorithms
    public void selectionSortByID(ListInterface<Member> list) {

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

    public void bubbleSortByPointsDescending(ListInterface<Member> list) {

        int n = list.getNumberOfEntries();

        for (int i = 1; i <= n - 1; i++) {

            for (int j = 1; j <= n - i; j++) {

                if (list.getEntry(j).getLoyaltyPoints() < list.getEntry(j + 1).getLoyaltyPoints()) {
                    list.swap(j, j + 1);
                }
            }
        }
    }

    public void insertionSortByNameAscending(ListInterface<Member> list) {

        int n = list.getNumberOfEntries();

        for (int i = 2; i <= n; i++) {

            int j = i;

            while (j > 1 && list.getEntry(j).getMemberName()
                    .compareToIgnoreCase(list.getEntry(j - 1).getMemberName()) < 0) {

                list.swap(j, j - 1);
                j--;
            }
        }
    }

    public void selectionSortByTierThenPointsDescending(ListInterface<Member> list) {

        int n = list.getNumberOfEntries();

        for (int i = 1; i <= n - 1; i++) {

            int bestPos = i;

            for (int j = i + 1; j <= n; j++) {

                Member candidate = list.getEntry(j);
                Member best = list.getEntry(bestPos);

                boolean candidateBetter
                        = candidate.getLoyaltyTier().ordinal() != best.getLoyaltyTier().ordinal()
                        ? candidate.getLoyaltyTier().ordinal() > best.getLoyaltyTier().ordinal()
                        : candidate.getLoyaltyPoints() > best.getLoyaltyPoints();

                if (candidateBetter) {
                    bestPos = j;
                }
            }

            if (bestPos != i) {
                list.swap(i, bestPos);
            }
        }
    }

    public void insertionSortByExpiryDate(ListInterface<PointsTransaction> list) {

        int n = list.getNumberOfEntries();

        for (int i = 2; i <= n; i++) {

            int j = i;

            while (j > 1 && list.getEntry(j).getExpiryDate()
                    .isBefore(list.getEntry(j - 1).getExpiryDate())) {

                list.swap(j, j - 1);
                j--;
            }
        }
    }

    public void insertionSortRedemptionsByDateDescending(ListInterface<RedemptionRecord> list) {

        int n = list.getNumberOfEntries();

        for (int i = 2; i <= n; i++) {

            int j = i;

            while (j > 1 && list.getEntry(j).getRedeemedDate()
                    .isAfter(list.getEntry(j - 1).getRedeemedDate())) {

                list.swap(j, j - 1);
                j--;
            }
        }
    }

    public void bubbleSortRedemptionsByPointsDescending(ListInterface<RedemptionRecord> list) {

        int n = list.getNumberOfEntries();

        for (int i = 1; i <= n - 1; i++) {

            for (int j = 1; j <= n - i; j++) {

                if (list.getEntry(j).getPointsUsed() < list.getEntry(j + 1).getPointsUsed()) {
                    list.swap(j, j + 1);
                }
            }
        }
    }

    public void insertionSortRedemptionsByMemberName(ListInterface<RedemptionRecord> list) {

        int n = list.getNumberOfEntries();

        for (int i = 2; i <= n; i++) {

            int j = i;

            while (j > 1 && list.getEntry(j).getMemberName()
                    .compareToIgnoreCase(list.getEntry(j - 1).getMemberName()) < 0) {

                list.swap(j, j - 1);
                j--;
            }
        }
    }

    public void insertionSortRedemptionsByRewardName(ListInterface<RedemptionRecord> list) {

        int n = list.getNumberOfEntries();

        for (int i = 2; i <= n; i++) {

            int j = i;

            while (j > 1 && list.getEntry(j).getRewardName()
                    .compareToIgnoreCase(list.getEntry(j - 1).getRewardName()) < 0) {

                list.swap(j, j - 1);
                j--;
            }
        }
    }

    //Below are counters for displaying in report as summary before displaying table
    ////////////////////////////////////////////////////////////////////////////
    /// @return 
    public int countTotalMembers() {
        return App.memberList.getNumberOfEntries();
    }

    public int countMembersByTier(LoyaltyTier tier) {

        int count = 0;

        for (int i = 1; i <= App.memberList.getNumberOfEntries(); i++) {

            Member m = App.memberList.getEntry(i);

            if (m.getLoyaltyTier() == tier) {
                count++;
            }
        }

        return count;
    }

    public int countTotalSpendablePoints() {

        int total = 0;

        for (int i = 1; i <= App.memberList.getNumberOfEntries(); i++) {
            total += App.memberList.getEntry(i).getLoyaltyPoints();
        }

        return total;
    }

    public int countTotalRewards() {
        return rewardCatalog.getNumberOfEntries();
    }

    public int countTotalTransactions() {
        return pointsTransactionList.getNumberOfEntries();
    }

    public int countTotalRedemptions() {
        return redemptionHistoryList.getNumberOfEntries();
    }

    public int countTotalPendingCredits() {
        return pendingPointsQueue.getNumberOfEntries();
    }

    public int countExpiringTransactions(int daysThreshold) {

        int count = 0;
        LocalDate today = VirtualClock.getInstance().today();

        for (int i = 1; i <= pointsTransactionList.getNumberOfEntries(); i++) {

            PointsTransaction t = pointsTransactionList.getEntry(i);
            long daysLeft = ChronoUnit.DAYS.between(today, t.getExpiryDate());

            if (daysLeft >= 0 && daysLeft <= daysThreshold) {
                count++;
            }
        }

        return count;
    }
}