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
 * Control class for the Loyalty & Reward Service module.
 * Handles: member profile search, points ACCUMULATION (queued + explicitly
 * processed by staff, from completed stays or personalized promotions),
 * reward redemption, automatic tier progression, reward catalog CRUD, and
 * generating reports (ranking, tier distribution, points expiry).
 *
 * -------------------------------------------------------------------
 * DESIGN NOTE - Points Accumulation Queue (refactored)
 * -------------------------------------------------------------------
 * Previously, a completed/paid stay credited points to the member's
 * balance immediately and silently, the instant the Loyalty module was
 * reopened - there was no accumulation step staff could review or control.
 *
 * That is now split into two explicit stages, using the SAME team
 * collection ADT (ListInterface/DoublyLinkedList) as a FIFO queue:
 *   1. QUEUE   - queueCompletedStayPoints() scans for newly completed
 *                stays and enqueues a PendingPointsCredit for each
 *                (rear-insert via add()). grantPromotionalPoints() does
 *                the same for staff-granted personalized promotions.
 *   2. PROCESS - processNextPendingPointsCredit() / processAll... actually
 *                apply a queued credit to the member's balance
 *                (front-remove via remove(1)), at which point tier
 *                progression is (re)evaluated. Staff may instead
 *                rejectPendingPointsCredit() to discard an entry.
 * This makes "points accumulation" its own auditable, staff-controlled use
 * case (Create/Read/Process/Delete on the queue) instead of a hidden side
 * effect, while keeping the ADT usage consistent with the rest of the
 * module (one collection ADT, used creatively for more than one purpose).
 *
 * Sorting and searching algorithms are self-implemented (selection sort,
 * bubble sort, insertion sort, binary search, linear search) using the
 * team's shared ListInterface operations - including swap(). No
 * java.util.Collections / Arrays.sort / Java Collections Framework is used
 * anywhere.
 *
 * All date/time values come from utility.VirtualClock rather than the
 * real system clock, so the module stays in sync with the rest of the
 * application when the demo clock is advanced.
 *
 * @author : Kao Yong Feng
 */
public class LoyaltyControl {

    private ListInterface<Member> memberList;

    // static: shared across every LoyaltyControl instance for the lifetime of
    // the program, so reward catalog / transaction history / queues persist
    // even though App.java creates a brand new LoyaltyUI/LoyaltyControl each
    // time the Loyalty module is re-entered from the main menu.
    private static ListInterface<RewardItem> rewardCatalog;
    private static ListInterface<PointsTransaction> transactionList;

    // records every successful redemption (member + reward + points + date)
    // so members can look back at what they've redeemed, not just their
    // current point balance. Newest redemption is always at position 1
    // (see redeemReward()), so this list doubles as most-recent-first order
    // without needing a separate sort step.
    private static ListInterface<RedemptionRecord> redemptionHistory;

    // FIFO queue of points that have been earned/granted but not yet
    // credited to any member's balance. Rear-insert (add) when a stay
    // completes or a promotion is granted; front-remove (remove(1)) when
    // staff processes an entry. See class-level DESIGN NOTE above.
    private static ListInterface<PendingPointsCredit> pendingPointsQueue;

    // bookingIDs that have already been turned into a pending points
    // credit, so queueCompletedStayPoints() (which re-scans
    // App.bookingHistoryList every time the module opens) never queues the
    // same completed stay twice - regardless of whether that credit has
    // since been processed or rejected.
    private static ListInterface<Integer> queuedStayBookingIDs;

    private static LoyaltyDAO loyaltyDAO = new LoyaltyDAO();

    // Points thresholds that trigger an automatic tier change
    private static final int PLATINUM_THRESHOLD = 1000;
    private static final int DIAMOND_THRESHOLD = 3000;
    private static final int ELITE_THRESHOLD = 6000;

    // Points earned expire this many months after the date they are CREDITED
    private static final int POINTS_VALIDITY_MONTHS = 12;
    // default look-ahead window (in days) for the expiry alert
    public static final int DEFAULT_EXPIRY_ALERT_DAYS = 30;
    // conversion rate for earning points from a stay: every $1 actually
    // spent on a completed, paid stay earns 1 loyalty point.
    private static final double DOLLARS_PER_POINT = 1.0;

    // standalone mode: uses RegistrationDAO's member data (App.memberList)
    // so that this module can still be run/tested independently, without
    // duplicating a separate set of hardcoded members just for this module.
    public LoyaltyControl() {
        if (App.memberList.isEmpty()) {
            RegistrationDAO.initializeMemberData();
        }
        memberList = App.memberList;
        ensureSharedDataInitialized();
    }

    // integrated mode: uses the application-wide shared memberList
    // (App.memberList) so points/tier changes are visible to every other
    // module, and members registered elsewhere are visible here too
    public LoyaltyControl(ListInterface<Member> sharedMemberList) {
        memberList = sharedMemberList;
        ensureSharedDataInitialized();
    }

    // reward catalog / transaction history / queues are only seeded once
    // per program run (guarded by the null check), instead of being
    // rebuilt every time a new LoyaltyControl is constructed.
    //
    // static (not per-instance) because queueCompletedStayPoints() runs as
    // an independent scan of shared App state (not a call triggered by
    // another module), so this needs to work with no LoyaltyControl
    // instance necessarily existing yet.
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
    }

    // =========================================================
    // Use Case 1a: Queue Points From Completed Stays (Create - into queue)
    // =========================================================

    /**
     * Scans App.bookingHistoryList for stays that have finished CHECKED_OUT
     * but haven't been queued for points accumulation yet, and enqueues a
     * PendingPointsCredit for each one. Points are NOT credited to the
     * member here - that only happens when staff explicitly processes the
     * queue (see processNextPendingPointsCredit / processAllPendingPointsCredits).
     *
     * This exists because the Registration module (owned by a teammate) is
     * NOT modified to call into this module directly - instead, this module
     * independently discovers completed stays from the shared App state.
     * queuedStayBookingIDs prevents the same booking from being queued
     * twice across repeated calls (e.g. every time the Loyalty module is
     * reopened, this re-scans the whole history list).
     *
     * static for the same reason as before: it is invoked as soon as the
     * Loyalty module opens, independent of any particular instance.
     *
     * @return a summary of newly queued credits, or an empty string if
     *         there was nothing new to queue
     */
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

            // mark as queued regardless of outcome below, so a $0 stay is
            // never re-evaluated on every future scan either
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

    /**
     * Recomputes the same final bill the Registration module would have
     * shown at checkout time - base nights at the room's rate, plus a 1.5x
     * penalty rate for any overstay days - purely so this module can work
     * out how many loyalty points a completed stay is worth.
     *
     * This duplicates RegistrationControl's billing formula rather than
     * calling into it, because this module cannot depend on or modify
     * teammate-owned control classes outside the Loyalty & Reward module.
     * booking.getBookingDate() is used as the ACTUAL checkout date because
     * RegistrationControl.processCheckout() overwrites bookingDate with the
     * real checkout date when it finalizes a stay (checkInDate/checkOutDate
     * stay as the originally SCHEDULED dates).
     */
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

    // =========================================================
    // Use Case 1b: Personalized Promotion (Create - into queue)
    // =========================================================

    /**
     * Grants a member bonus/promotional points for a staff-specified reason
     * (e.g. birthday promotion, service-recovery gesture, loyalty
     * campaign). Like stay-earned points, promotional points are queued
     * rather than credited immediately - every point that ever reaches a
     * member's balance goes through the same reviewable accumulation
     * queue, so nothing is credited "invisibly", but staff CAN initiate a
     * grant for a specific member's profile at any time (this is the
     * "members' profile - personalized promotion" capability of the
     * module).
     *
     * @param memberID the member to grant points to
     * @param points   number of bonus points to queue (must be positive)
     * @param reason   staff-entered justification, shown in the queue and
     *                 required for audit purposes
     */
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

    // =========================================================
    // Use Case 1c: Points Accumulation Queue - Read / Process / Reject
    // =========================================================

    /** Number of credits currently waiting to be processed - used for the module-open alert banner. */
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

    // ---- Process one (front of queue) ----
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

    // ---- Process all (drains the whole queue, front to rear) ----
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

    // ---- Delete (reject without crediting) ----
    public String rejectPendingPointsCredit(int creditID) {
        // PendingPointsCredit.equals() compares only creditID, so a "probe"
        // object with just the ID set is enough for the ADT's indexOf() to
        // find the real match position - same pattern as findRewardPosition().
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

    // =========================================================
    // Shared crediting logic - the ONLY place a member's balance actually
    // changes upward. Called exclusively from queue processing above, so
    // every point on a member's account can be traced back to a queue
    // entry (a completed stay or an approved promotion).
    // =========================================================
    private static String creditPointsToMember(Member member, int pointsToCredit, String sourceLabel) {
        if (member == null || pointsToCredit <= 0) {
            return "";
        }

        // member is the same object reference stored inside App.memberList
        // (the ADT stores references, not copies), so mutating it here
        // already updates the shared list directly - no replace() call needed
        member.setLoyaltyPoints(member.getLoyaltyPoints() + pointsToCredit);
        String tierMessage = updateTier(member);

        LocalDate earnedDate = VirtualClock.getInstance().today();
        LocalDate expiryDate = earnedDate.plusMonths(POINTS_VALIDITY_MONTHS);

        // Each member has ONE consolidated points-transaction record (not a
        // separate batch per credit) - View Points Transactions should
        // always show a single row per member with their combined point
        // total. Crediting more points adds onto that existing record and
        // resets its expiry to count down from today again; a member with
        // no record yet gets a new one.
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

    // Returns the given member's single consolidated points-transaction
    // record, or null if the member has never had any points credited yet.
    private static PointsTransaction findTransactionByMemberID(int memberID) {
        for (int i = 1; i <= transactionList.getNumberOfEntries(); i++) {
            PointsTransaction t = transactionList.getEntry(i);
            if (t.getMemberID() == memberID) {
                return t;
            }
        }
        return null;
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

        // Redeeming only lowers the points on the member's consolidated
        // transaction record - the expiry date stays exactly as it was.
        // Redemption doesn't earn anything new, so it shouldn't push the
        // countdown back out.
        PointsTransaction existing = findTransactionByMemberID(member.getMemberID());
        if (existing != null) {
            int remainingPoints = existing.getPointsEarned() - reward.getPointsRequired();
            existing.setPointsEarned(Math.max(remainingPoints, 0));
        }

        // record this redemption at the FRONT of the list (position 1) so
        // redemptionHistory naturally stays newest-first, with no separate
        // sort step needed when displaying it later
        RedemptionRecord record = new RedemptionRecord(member.getMemberID(), member.getMemberName(),
                reward.getRewardID(), reward.getRewardName(), reward.getPointsRequired(),
                VirtualClock.getInstance().today());
        redemptionHistory.add(1, record);

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
    // Use Case 3: Automatic Tier Progression (upgrade / downgrade)
    // =========================================================
    // Tier is always a pure function of the CURRENT points balance, so it
    // is re-evaluated at every point where that balance changes -
    // crediting (creditPointsToMember) and redeeming (redeemReward). There
    // is deliberately no separate manual "process tier upgrade" step: a
    // member's tier should never be able to drift out of sync with their
    // points, so it is recalculated automatically the instant the balance
    // that determines it changes.
    private static String updateTier(Member member) {
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

    // catalog is small - linear search is sufficient
    private RewardItem findRewardByID(int rewardID) {
        int position = findRewardPosition(rewardID);
        return position == -1 ? null : rewardCatalog.getEntry(position);
    }

    // RewardItem.equals() compares only rewardID, so a "probe" object with
    // just the ID set is enough for the ADT's indexOf() to find the real
    // match position in rewardCatalog.
    private int findRewardPosition(int rewardID) {
        RewardItem probe = new RewardItem();
        probe.setRewardID(rewardID);
        return rewardCatalog.indexOf(probe);
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

    // =========================================================
    // Use Case 5: Generate Loyalty Ranking Report (by points)
    // =========================================================
    public String generateLoyaltyReport() {
        ListInterface<Member> sortedList = memberList.copy();
        bubbleSortByPointsDescending(sortedList);

        StringBuilder sb = new StringBuilder();
        sb.append(ReportFormatUtility.buildHeader("MEMBER RANKING REPORT (BY POINTS)", VirtualClock.getInstance().now()));
        sb.append(String.format("%-5s %-10s %-20s %-12s %-10s%n", "Rank", "Member ID", "Member Name", "Tier", "Points"));
        sb.append(ReportFormatUtility.separatorLine());

        int total = sortedList.getNumberOfEntries();
        String[] labels = new String[total];
        int[] points = new int[total];
        for (int i = 1; i <= total; i++) {
            Member m = sortedList.getEntry(i);
            sb.append(String.format("%-5d %-10d %-20s %-12s %-10d%n",
                    i, m.getMemberID(), m.getMemberName(), m.getLoyaltyTier(), m.getLoyaltyPoints()));
            labels[i - 1] = m.getMemberName();
            points[i - 1] = m.getLoyaltyPoints();
        }

        sb.append(ReportFormatUtility.separatorLine());
        sb.append(ReportFormatUtility.buildBarChart("POINTS DISTRIBUTION", labels, points, "points"));
        sb.append(ReportFormatUtility.buildFooter("Total members displayed", total));
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

    // =========================================================
    // Use Case 1d: Automatic Points Expiry (Delete - forfeit overdue points)
    // -----------------------------------------------------------
    // Previously, an expiring points transaction was only ever REPORTED
    // (see Use Case 7 below) - nothing ever actually removed the points
    // from a member's balance once expiryDate had passed, so the alert
    // was purely cosmetic. Now, every time the Loyalty module is opened
    // AND every time the VirtualClock is advanced from within this module
    // (see the "Advance Time" menu option, mirroring the same feature in
    // the Booking/Registration modules), this scans transactionList for
    // any record whose expiryDate has already passed and forfeits those
    // points: they are deducted from the member's balance, the member's
    // tier is re-evaluated (losing points can demote a member below a
    // tier threshold), and the now-fully-expired transaction record is
    // removed from transactionList - there is nothing left on it worth
    // tracking or alerting on.
    // =========================================================
    public static String expireOverduePoints() {
        ensureSharedDataInitialized();
        LocalDate today = VirtualClock.getInstance().today();
        StringBuilder sb = new StringBuilder();
        int expiredCount = 0;

        // iterate back-to-front so remove(i) never disturbs the position
        // of entries not yet visited
        for (int i = transactionList.getNumberOfEntries(); i >= 1; i--) {
            PointsTransaction t = transactionList.getEntry(i);
            if (!today.isAfter(t.getExpiryDate())) {
                continue; // today == expiryDate is still the last valid day, not expired yet
            }

            Member member = findMemberByIDStatic(t.getMemberID());
            if (member != null && t.getPointsEarned() > 0) {
                int forfeited = Math.min(t.getPointsEarned(), member.getLoyaltyPoints());
                member.setLoyaltyPoints(member.getLoyaltyPoints() - forfeited);
                String tierMessage = updateTier(member);

                sb.append(member.getMemberName()).append(" (ID ").append(member.getMemberID())
                  .append(") - ").append(forfeited).append(" point(s) EXPIRED on ")
                  .append(t.getExpiryDate()).append(" and have been forfeited. New balance: ")
                  .append(member.getLoyaltyPoints()).append(" points.\n");
                if (!tierMessage.isEmpty()) {
                    sb.append(tierMessage).append("\n");
                }
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

    // static linear-search lookup by member ID, used by expireOverduePoints()
    // since that method runs independently of any particular LoyaltyControl
    // instance (same reasoning as the other static queue/scan methods above)
    private static Member findMemberByIDStatic(int memberID) {
        for (int i = 1; i <= App.memberList.getNumberOfEntries(); i++) {
            Member m = App.memberList.getEntry(i);
            if (m.getMemberID() == memberID) {
                return m;
            }
        }
        return null;
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
        ListInterface<PointsTransaction> allTransactions = transactionList.copy();
        insertionSortByExpiryDate(allTransactions);

        return buildTransactionReport(allTransactions,
                "ALL POINTS TRANSACTIONS (Full History)",
                "No points transactions have been recorded yet.");
    }

    // shared formatting logic for both the expiry-alert report and the full-history report
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

    // =========================================================
    // Use Case 8: View Redemption History (by member / full history)
    // =========================================================

    // linear search - returns only the given member's redemption records,
    // already newest-first (redemptionHistory is maintained in that order)
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
    public String updateRewardItem(int rewardID, String newName, int newPointsRequired) {
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
        // as creditPointsToMember()/redeemReward() mutating Member directly)
        RewardItem existingReward = rewardCatalog.getEntry(position);
        existingReward.setRewardName(newName);
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