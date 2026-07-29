/*
 * @author <Your Name>
 */
package control;

import adt.DoublyLinkedList;
import adt.ListInterface;
import dao.LoyaltyRewardDAO;
import entity.Member;
import entity.PointsTransaction;
import entity.RedemptionRequest;
import entity.Reward;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class LoyaltyRewardControl {

    // Tier point thresholds (kept consistent with the tiers already used
    // for Member/Booking across the project: Standard, Platinum, Diamond, Elite)
    private static final int THRESHOLD_PLATINUM = 500;
    private static final int THRESHOLD_DIAMOND = 800;
    private static final int THRESHOLD_ELITE = 1000;

    // Assumption: earned points expire 365 days after being earned.
    private static final int POINTS_VALIDITY_DAYS = 365;

    private ListInterface<Member> memberList;
    private ListInterface<Reward> rewardList;
    private ListInterface<PointsTransaction> transactionList;
    private ListInterface<RedemptionRequest> redemptionRequestList;

    public LoyaltyRewardControl() {

        memberList = new DoublyLinkedList<>();
        rewardList = new DoublyLinkedList<>();
        transactionList = new DoublyLinkedList<>();
        redemptionRequestList = new DoublyLinkedList<>();

        LoyaltyRewardDAO dao = new LoyaltyRewardDAO();

        Member[] members = dao.getMembers();
        for (Member member : members) {
            memberList.add(member);
        }

        Reward[] rewards = dao.getRewards();
        for (Reward reward : rewards) {
            rewardList.add(reward);
        }

        PointsTransaction[] seedTransactions = dao.getSeedTransactions(members);
        for (PointsTransaction transaction : seedTransactions) {
            transactionList.add(transaction);
        }
    }

    // ---------- Member profile ----------

    public ListInterface<Member> getMemberList() {
        return memberList;
    }

    public Member findMemberByID(String memberID) {

        for (int i = 1; i <= memberList.getNumberOfEntries(); i++) {
            Member member = memberList.getEntry(i);
            if (member.getMemberID().equals(memberID)) {
                return member;
            }
        }
        return null;
    }

    /**
     * Personalized promotion message based on the member's current tier.
     */
    public String getPersonalizedPromotion(Member member) {

        switch (member.getLoyaltyTier()) {
            case "Elite":
                return "Enjoy a complimentary night on your next stay of 3 nights or more.";
            case "Diamond":
                return "Get 20% off spa treatments this month.";
            case "Platinum":
                return "Free room upgrade, subject to availability.";
            default:
                return "Earn 50 bonus points on your next stay to move closer to Platinum tier.";
        }
    }

    // ---------- Tier progression ----------

    private int getTierRank(String tier) {
        switch (tier) {
            case "Elite":
                return 4;
            case "Diamond":
                return 3;
            case "Platinum":
                return 2;
            default:
                return 1;
        }
    }

    private String determineTier(int points) {
        if (points >= THRESHOLD_ELITE) {
            return "Elite";
        } else if (points >= THRESHOLD_DIAMOND) {
            return "Diamond";
        } else if (points >= THRESHOLD_PLATINUM) {
            return "Platinum";
        } else {
            return "Standard";
        }
    }

    /**
     * Re-evaluates a member's tier based on their current points.
     * Tier upgrades are applied automatically; tiers are not downgraded
     * automatically when points are redeemed (assumption: tier reflects
     * lifetime achievement, not just current point balance).
     *
     * @return true if the member's tier was upgraded
     */
    public boolean checkTierProgression(Member member) {

        String recalculatedTier = determineTier(member.getLoyaltyPoints());

        if (getTierRank(recalculatedTier) > getTierRank(member.getLoyaltyTier())) {
            member.setLoyaltyTier(recalculatedTier);
            return true;
        }
        return false;
    }

    // ---------- Points accumulation ----------

    private String generateTransactionID() {
        return String.format("PT%03d", transactionList.getNumberOfEntries() + 1);
    }

    public boolean earnPoints(String memberID, int points, String description) {

        Member member = findMemberByID(memberID);

        if (member == null || points <= 0) {
            return false;
        }

        member.setLoyaltyPoints(member.getLoyaltyPoints() + points);

        LocalDateTime now = LocalDateTime.now();
        LocalDate expiryDate = now.toLocalDate().plusDays(POINTS_VALIDITY_DAYS);

        PointsTransaction transaction = new PointsTransaction(
                generateTransactionID(), member, "EARN", points,
                now, expiryDate, description);

        transactionList.add(transaction);

        checkTierProgression(member);
        return true;
    }

    public ListInterface<PointsTransaction> getTransactionList() {
        return transactionList;
    }

    // ---------- Redemption ----------

    public ListInterface<Reward> getRewardList() {
        return rewardList;
    }

    public Reward findRewardByID(String rewardID) {

        for (int i = 1; i <= rewardList.getNumberOfEntries(); i++) {
            Reward reward = rewardList.getEntry(i);
            if (reward.getRewardID().equals(rewardID)) {
                return reward;
            }
        }
        return null;
    }

    private String generateRequestID() {
        return String.format("RR%03d", redemptionRequestList.getNumberOfEntries() + 1);
    }

    /**
     * Places a redemption request in the FIFO processing queue if the
     * member has sufficient points. Points are only deducted when the
     * request is processed via processNextRedemptionRequest().
     */
    public String requestRedemption(String memberID, String rewardID) {

        Member member = findMemberByID(memberID);
        if (member == null) {
            return "Member ID not found.";
        }

        Reward reward = findRewardByID(rewardID);
        if (reward == null) {
            return "Reward ID not found.";
        }

        if (member.getLoyaltyPoints() < reward.getPointsRequired()) {
            return "Insufficient points. " + member.getMemberName()
                    + " has " + member.getLoyaltyPoints() + " points, but "
                    + reward.getRewardName() + " requires " + reward.getPointsRequired() + ".";
        }

        RedemptionRequest request = new RedemptionRequest(
                generateRequestID(), member, reward,
                LocalDateTime.now(), "PENDING");

        redemptionRequestList.add(request);

        return "Redemption request " + request.getRequestID()
                + " submitted for " + reward.getRewardName() + ".";
    }

    public ListInterface<RedemptionRequest> getRedemptionRequestList() {
        return redemptionRequestList;
    }

    /**
     * Processes the oldest pending redemption request (FIFO): deducts
     * points from the member, logs a REDEEM transaction, and marks the
     * request as completed.
     *
     * @return the processed request, or null if no request is pending
     */
    public RedemptionRequest processNextRedemptionRequest() {

        for (int i = 1; i <= redemptionRequestList.getNumberOfEntries(); i++) {

            RedemptionRequest request = redemptionRequestList.getEntry(i);

            if (request.getStatus().equals("PENDING")) {

                Member member = request.getMember();
                Reward reward = request.getReward();

                member.setLoyaltyPoints(member.getLoyaltyPoints() - reward.getPointsRequired());

                PointsTransaction transaction = new PointsTransaction(
                        generateTransactionID(), member, "REDEEM",
                        reward.getPointsRequired(), LocalDateTime.now(), null,
                        "Redeemed: " + reward.getRewardName());

                transactionList.add(transaction);

                request.setStatus("COMPLETED");
                return request;
            }
        }
        return null;
    }

    // ---------- Expiring points notifications ----------

    private int compareByExpiry(PointsTransaction a, PointsTransaction b) {

        if (a.getExpiryDate().isBefore(b.getExpiryDate())) {
            return 1;
        } else if (a.getExpiryDate().isAfter(b.getExpiryDate())) {
            return -1;
        }
        return 0;
    }

    /**
     * Returns "EARN" transactions expiring within the given number of days,
     * sorted from soonest to latest expiry (bubble sort using the ADT's
     * swap operation, consistent with the team's collection ADT usage).
     */
    public ListInterface<PointsTransaction> getExpiringPointsTransactions(int withinDays) {

        ListInterface<PointsTransaction> expiringList = new DoublyLinkedList<>();
        LocalDate today = LocalDate.now();
        LocalDate cutoff = today.plusDays(withinDays);

        for (int i = 1; i <= transactionList.getNumberOfEntries(); i++) {

            PointsTransaction transaction = transactionList.getEntry(i);

            if (transaction.getTransactionType().equals("EARN")
                    && transaction.getExpiryDate() != null
                    && !transaction.getExpiryDate().isBefore(today)
                    && !transaction.getExpiryDate().isAfter(cutoff)) {

                expiringList.add(transaction);
            }
        }

        for (int i = 1; i <= expiringList.getNumberOfEntries(); i++) {
            for (int j = i + 1; j <= expiringList.getNumberOfEntries(); j++) {

                PointsTransaction a = expiringList.getEntry(i);
                PointsTransaction b = expiringList.getEntry(j);

                if (compareByExpiry(a, b) < 0) {
                    expiringList.swap(i, j);
                }
            }
        }

        return expiringList;
    }

    // ---------- Reporting support ----------

    /**
     * Returns transactions matching the given member ID and/or type
     * ("All" matches everything), sorted from most recent to oldest.
     */
    public ListInterface<PointsTransaction> getFilteredTransactions(String memberID, String type) {

        ListInterface<PointsTransaction> filtered = new DoublyLinkedList<>();

        for (int i = 1; i <= transactionList.getNumberOfEntries(); i++) {

            PointsTransaction transaction = transactionList.getEntry(i);

            boolean memberMatch = memberID.equals("All")
                    || transaction.getMember().getMemberID().equals(memberID);

            boolean typeMatch = type.equals("All")
                    || transaction.getTransactionType().equals(type);

            if (memberMatch && typeMatch) {
                filtered.add(transaction);
            }
        }

        for (int i = 1; i <= filtered.getNumberOfEntries(); i++) {
            for (int j = i + 1; j <= filtered.getNumberOfEntries(); j++) {

                PointsTransaction a = filtered.getEntry(i);
                PointsTransaction b = filtered.getEntry(j);

                if (a.getTransactionDate().isBefore(b.getTransactionDate())) {
                    filtered.swap(i, j);
                }
            }
        }

        return filtered;
    }

    /**
     * Returns members sorted by loyalty points, highest first (bubble sort
     * using the ADT's swap operation), for the tier leaderboard report.
     */
    public ListInterface<Member> getMembersRankedByPoints() {

        ListInterface<Member> ranked = new DoublyLinkedList<>();

        for (int i = 1; i <= memberList.getNumberOfEntries(); i++) {
            ranked.add(memberList.getEntry(i));
        }

        for (int i = 1; i <= ranked.getNumberOfEntries(); i++) {
            for (int j = i + 1; j <= ranked.getNumberOfEntries(); j++) {

                Member a = ranked.getEntry(i);
                Member b = ranked.getEntry(j);

                if (a.getLoyaltyPoints() < b.getLoyaltyPoints()) {
                    ranked.swap(i, j);
                }
            }
        }

        return ranked;
    }

    public int countCompletedRedemptions(String memberID) {

        int count = 0;
        for (int i = 1; i <= redemptionRequestList.getNumberOfEntries(); i++) {

            RedemptionRequest request = redemptionRequestList.getEntry(i);

            if (request.getMember().getMemberID().equals(memberID)
                    && request.getStatus().equals("COMPLETED")) {
                count++;
            }
        }
        return count;
    }
}
