package control;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import dao.LoyaltyDAO;
import dao.RegistrationDAO;
import adt.DoublyLinkedList;
import adt.ListInterface;
import entity.Member;
import entity.Member.LoyaltyTier;
import entity.PointsTransaction;
import entity.RewardItem;
import main.App;
import utility.VirtualClock;

/**
 * Control class for the Loyalty & Reward Service module.
 * Handles: earning points, redeeming rewards, automatic tier
 * upgrade/downgrade, member search (by ID/name/tier), reward catalog CRUD,
 * and generating reports (ranking, tier distribution, points expiry).
 *
 * Sorting and searching algorithms are self-implemented (selection sort,
 * bubble sort, insertion sort, binary search, linear search) using the
 * team's shared ListInterface operations - including swap(), the new
 * operation added to the team's ADT. No java.util.Collections /
 * Arrays.sort / Java Collections Framework is used anywhere.
 *
 * All date/time values come from utility.VirtualClock rather than the
 * real system clock, so the module stays in sync with the rest of the
 * application when the demo clock is advanced.
 */
public class LoyaltyControl {

    private ListInterface<Member> memberList;

    // static: shared across every LoyaltyControl instance for the lifetime of
    // the program, so reward catalog / transaction history persist even
    // though App.java creates a brand new LoyaltyUI/LoyaltyControl each time
    // the Loyalty module is re-entered from the main menu. This mirrors how
    // App.memberList already persists (it's a static field on App itself).
    private static ListInterface<RewardItem> rewardCatalog;
    private static ListInterface<PointsTransaction> transactionList;

    private LoyaltyDAO loyaltyDAO = new LoyaltyDAO();

    // Points thresholds that trigger an automatic tier change
    private static final int PLATINUM_THRESHOLD = 1000;
    private static final int DIAMOND_THRESHOLD = 3000;
    private static final int ELITE_THRESHOLD = 6000;

    // Points earned expire this many months after the date they were earned
    private static final int POINTS_VALIDITY_MONTHS = 12;
    // default look-ahead window (in days) for the expiry alert
    public static final int DEFAULT_EXPIRY_ALERT_DAYS = 30;

    // standalone mode: uses RegistrationDAO's member data (App.memberList)
    // so that this module can still be run/tested independently, without
    // duplicating a separate set of hardcoded members just for this module.
    // If App.memberList hasn't been populated yet (e.g. running LoyaltyUI's
    // main() directly, without going through the full App startup), it is
    // seeded here first.
    public LoyaltyControl() {
        if (App.memberList.isEmpty()) {
            RegistrationDAO.initializeMemberData();
        }
        memberList = App.memberList;
        initializeSharedDataIfNeeded();
    }

    // integrated mode: uses the application-wide shared memberList
    // (App.memberList) so points/tier changes are visible to every other
    // module, and members registered elsewhere are visible here too
    public LoyaltyControl(ListInterface<Member> sharedMemberList) {
        memberList = sharedMemberList;
        initializeSharedDataIfNeeded();
    }

    // reward catalog and transaction history are only seeded once per
    // program run (guarded by the null check), instead of being rebuilt
    // every time a new LoyaltyControl is constructed - so data added or
    // changed during one visit to the module is still there the next time
    // the module is opened, without needing any changes to App.java
    private void initializeSharedDataIfNeeded() {
        if (rewardCatalog == null) {
            rewardCatalog = loyaltyDAO.initializeRewardCatalog();
        }
        if (transactionList == null) {
            transactionList = loyaltyDAO.initializeTransactionData(memberList, POINTS_VALIDITY_MONTHS);
        }
    }

    // =========================================================
    // Use Case 1: Earn Loyalty Points
    // =========================================================
    public String earnPoints(int memberID, int pointsEarned) {
        if (pointsEarned <= 0) {
            return "Points earned must be a positive value.";
        }
        Member member = findMemberByID(memberID);
        if (member == null) {
            return "Member with ID " + memberID + " not found.";
        }

        // member is the same object reference stored inside memberList (the
        // ADT stores references, not copies), so mutating it here already
        // updates the shared list directly - no replace() call is needed
        member.setLoyaltyPoints(member.getLoyaltyPoints() + pointsEarned);
        String tierMessage = updateTier(member);

        LocalDate earnedDate = VirtualClock.getInstance().today();
        LocalDate expiryDate = earnedDate.plusMonths(POINTS_VALIDITY_MONTHS);
        transactionList.add(new PointsTransaction(member.getMemberID(), member.getMemberName(),
                pointsEarned, earnedDate, expiryDate));

        StringBuilder sb = new StringBuilder();
        sb.append(member.getMemberName()).append(" earned ").append(pointsEarned)
          .append(" points. New balance: ").append(member.getLoyaltyPoints()).append(" points.");
        if (!tierMessage.isEmpty()) {
            sb.append("\n").append(tierMessage);
        }
        return sb.toString();
    }

    // =========================================================
    // Use Case 2: Redeem Reward
    // =========================================================
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
        String tierMessage = updateTier(member);

        StringBuilder sb = new StringBuilder();
        sb.append(member.getMemberName()).append(" redeemed \"").append(reward.getRewardName())
          .append("\" for ").append(reward.getPointsRequired()).append(" points. Remaining balance: ")
          .append(member.getLoyaltyPoints()).append(" points.");
        if (!tierMessage.isEmpty()) {
            sb.append("\n").append(tierMessage);
        }
        return sb.toString();
    }

    // =========================================================
    // Use Case 3: Automatic Tier Upgrade / Downgrade
    // =========================================================
    private String updateTier(Member member) {
        LoyaltyTier oldTier = member.getLoyaltyTier();
        LoyaltyTier newTier;
        int points = member.getLoyaltyPoints();

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

    // =========================================================
    // Use Case 4: Search Member (by ID / Name / Tier)
    // =========================================================

    public Member searchMemberByID(int memberID) {
        return findMemberByID(memberID);
    }

    // linear search, partial case-insensitive match on name
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

    // linear search, exact match on loyalty tier
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

    // binary search on a member-ID-sorted copy of the list
    private Member findMemberByID(int memberID) {
        ListInterface<Member> sortedCopy = copyMemberList(memberList);
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

    // catalog is small - linear search is sufficient
    private RewardItem findRewardByID(int rewardID) {
        int position = findRewardPosition(rewardID);
        return position == -1 ? null : rewardCatalog.getEntry(position);
    }

    private int findRewardPosition(int rewardID) {
        for (int i = 1; i <= rewardCatalog.getNumberOfEntries(); i++) {
            if (rewardCatalog.getEntry(i).getRewardID() == rewardID) {
                return i;
            }
        }
        return -1;
    }

    // =========================================================
    // Self-implemented sorting algorithms, all using the ADT's swap()
    // operation (no Collections.sort / Arrays.sort)
    // =========================================================

    // Selection Sort - ascending by member ID (used before binary search)
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

    // Bubble Sort - descending by points (used for the ranking report)
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

    // Insertion Sort (via adjacent swaps) - ascending by expiry date
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

    private ListInterface<Member> copyMemberList(ListInterface<Member> source) {
        ListInterface<Member> copy = new DoublyLinkedList<>();
        for (int i = 1; i <= source.getNumberOfEntries(); i++) {
            copy.add(source.getEntry(i));
        }
        return copy;
    }

    private ListInterface<PointsTransaction> copyTransactionList(ListInterface<PointsTransaction> source) {
        ListInterface<PointsTransaction> copy = new DoublyLinkedList<>();
        for (int i = 1; i <= source.getNumberOfEntries(); i++) {
            copy.add(source.getEntry(i));
        }
        return copy;
    }

    // =========================================================
    // Use Case 5: Generate Loyalty Ranking Report (by points)
    // =========================================================
    public String generateLoyaltyReport() {
        ListInterface<Member> sortedList = copyMemberList(memberList);
        bubbleSortByPointsDescending(sortedList);

        DateTimeFormatter headerFormatter = DateTimeFormatter.ofPattern("EEEE, MMM dd yyyy, hh:mm a");
        String generatedAt = VirtualClock.getInstance().now().format(headerFormatter);

        StringBuilder sb = new StringBuilder();
        sb.append("=============================================================\n");
        sb.append("          TARUMT RESORTS - LOYALTY & REWARD PROGRAM\n");
        sb.append("               MEMBER RANKING REPORT (BY POINTS)\n");
        sb.append("-------------------------------------------------------------\n");
        sb.append("Generated at: ").append(generatedAt).append("\n");
        sb.append("=============================================================\n");
        sb.append(String.format("%-5s %-10s %-20s %-12s %-10s%n", "Rank", "Member ID", "Member Name", "Tier", "Points"));
        sb.append("-------------------------------------------------------------\n");

        int total = sortedList.getNumberOfEntries();
        for (int i = 1; i <= total; i++) {
            Member m = sortedList.getEntry(i);
            sb.append(String.format("%-5d %-10d %-20s %-12s %-10d%n",
                    i, m.getMemberID(), m.getMemberName(), m.getLoyaltyTier(), m.getLoyaltyPoints()));
        }
        sb.append("-------------------------------------------------------------\n");
        sb.append("Total members displayed: ").append(total).append("\n");
        sb.append("=============================================================\n");
        return sb.toString();
    }

    // =========================================================
    // Use Case 6: Generate Tier Distribution Report
    // =========================================================
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

        DateTimeFormatter headerFormatter = DateTimeFormatter.ofPattern("EEEE, MMM dd yyyy, hh:mm a");
        String generatedAt = VirtualClock.getInstance().now().format(headerFormatter);
        int totalMembers = memberList.getNumberOfEntries();

        StringBuilder sb = new StringBuilder();
        sb.append("=============================================================\n");
        sb.append("          TARUMT RESORTS - LOYALTY & REWARD PROGRAM\n");
        sb.append("              TIER DISTRIBUTION SUMMARY REPORT\n");
        sb.append("-------------------------------------------------------------\n");
        sb.append("Generated at: ").append(generatedAt).append("\n");
        sb.append("=============================================================\n");
        sb.append(String.format("%-12s %-14s %-16s %-12s%n", "Tier", "No. Members", "Total Points", "Avg Points"));
        sb.append("-------------------------------------------------------------\n");

        for (int i = tiers.length - 1; i >= 0; i--) { // display highest tier (Elite) first
            int count = memberCount[i];
            int avg = count == 0 ? 0 : totalPoints[i] / count;
            sb.append(String.format("%-12s %-14d %-16d %-12d%n", tiers[i], count, totalPoints[i], avg));
        }

        sb.append("-------------------------------------------------------------\n");
        sb.append("Total members in program: ").append(totalMembers).append("\n");
        sb.append("=============================================================\n");
        return sb.toString();
    }

    // =========================================================
    // Use Case 7: Points Expiry Notifications / Alert
    // =========================================================

    /**
     * Returns the number of points transactions expiring within the given
     * number of days from today. Used to show a quick alert banner when the
     * module starts, without needing to build the full report.
     */
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

    /**
     * Returns every recorded points transaction, regardless of expiry date,
     * sorted with the soonest-expiring transaction first. Useful to verify
     * that all earned-points batches (including ones far from expiring) are
     * being tracked correctly.
     */
    public String generateAllTransactionsReport() {
        ListInterface<PointsTransaction> allTransactions = copyTransactionList(transactionList);
        insertionSortByExpiryDate(allTransactions);

        return buildTransactionReport(allTransactions,
                "ALL POINTS TRANSACTIONS (Full History)",
                "No points transactions have been recorded yet.");
    }

    // shared formatting logic for both the expiry-alert report and the full-history report
    private String buildTransactionReport(ListInterface<PointsTransaction> list, String subtitle, String emptyMessage) {
        LocalDate today = VirtualClock.getInstance().today();
        DateTimeFormatter headerFormatter = DateTimeFormatter.ofPattern("EEEE, MMM dd yyyy, hh:mm a");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String generatedAt = VirtualClock.getInstance().now().format(headerFormatter);

        StringBuilder sb = new StringBuilder();
        sb.append("=============================================================\n");
        sb.append("          TARUMT RESORTS - LOYALTY & REWARD PROGRAM\n");
        sb.append("        ").append(subtitle).append("\n");
        sb.append("-------------------------------------------------------------\n");
        sb.append("Generated at: ").append(generatedAt).append("\n");
        sb.append("=============================================================\n");
        sb.append(String.format("%-10s %-18s %-8s %-13s %-13s %-10s%n",
                "MemberID", "Member Name", "Points", "Earned Date", "Expires On", "Days Left"));
        sb.append("-------------------------------------------------------------\n");

        int total = list.getNumberOfEntries();
        for (int i = 1; i <= total; i++) {
            PointsTransaction t = list.getEntry(i);
            long daysLeft = ChronoUnit.DAYS.between(today, t.getExpiryDate());
            sb.append(String.format("%-10d %-18s %-8d %-13s %-13s %-10d%n",
                    t.getMemberID(), t.getMemberName(), t.getPointsEarned(),
                    t.getEarnedDate().format(dateFormatter), t.getExpiryDate().format(dateFormatter), daysLeft));
        }

        sb.append("-------------------------------------------------------------\n");
        if (total == 0) {
            sb.append(emptyMessage).append("\n");
        } else {
            sb.append("Total transactions: ").append(total).append("\n");
        }
        sb.append("=============================================================\n");
        return sb.toString();
    }

    // =========================================================
    // Reward Catalog Management (CRUD)
    // =========================================================

    // ---- Create ----
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
    public String updateRewardItem(int rewardID, String newName, String newDescription, int newPointsRequired) {
        int position = findRewardPosition(rewardID);
        if (position == -1) {
            return "Reward with ID " + rewardID + " not found in catalog.";
        }
        if (newPointsRequired <= 0) {
            return "Points required must be a positive value.";
        }
        // existingReward is the same object reference stored inside
        // rewardCatalog, so mutating its fields here already updates the
        // catalog directly - no replace() call is needed (same reasoning
        // as earnPoints()/redeemReward() mutating Member directly)
        RewardItem existingReward = rewardCatalog.getEntry(position);
        existingReward.setRewardName(newName);
        existingReward.setDescription(newDescription);
        existingReward.setPointsRequired(newPointsRequired);
        return "Reward ID " + rewardID + " updated successfully.";
    }

    // ---- Delete ----
    public String deleteRewardItem(int rewardID) {
        int position = findRewardPosition(rewardID);
        if (position == -1) {
            return "Reward with ID " + rewardID + " not found in catalog.";
        }
        RewardItem removed = rewardCatalog.remove(position);
        return "Reward removed: " + removed.getRewardName() + " (ID " + removed.getRewardID() + ").";
    }

    // =========================================================
    // Reward Catalog Display (Read)
    // =========================================================
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