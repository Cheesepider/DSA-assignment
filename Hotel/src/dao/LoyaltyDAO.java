package dao;

import java.time.LocalDate;

import adt.DoublyLinkedList;
import adt.ListInterface;
import entity.Member;
import entity.PointsTransaction;
import entity.RewardItem;
import utility.VirtualClock;

/**
 * Data-initialization class for the Loyalty & Reward Service module.
 * No database / text file is required for this assignment - data is
 * hardcoded and loaded into the collection ADT at startup.
 */
public class LoyaltyDAO {

   
    public ListInterface<RewardItem> initializeRewardCatalog() {
        ListInterface<RewardItem> rewardCatalog = new DoublyLinkedList<>();

        rewardCatalog.add(new RewardItem("Free Room Upgrade", "One-tier room upgrade on next stay", 500));
        rewardCatalog.add(new RewardItem("Late Check-out (2 hrs)", "Extend check-out time by 2 hours", 300));
        rewardCatalog.add(new RewardItem("Complimentary Breakfast", "Breakfast for 2 at the hotel restaurant", 800));
        rewardCatalog.add(new RewardItem("Spa Voucher", "RM100 spa treatment voucher", 1500));
        rewardCatalog.add(new RewardItem("Free Night Stay", "One complimentary night in a Deluxe Room", 5000));
        rewardCatalog.add(new RewardItem("Welcome Drink Voucher", "Complimentary welcome drink at check-in", 100));
        rewardCatalog.add(new RewardItem("Poolside Cabana (Half Day)", "Reserved poolside cabana for half a day", 600));
        rewardCatalog.add(new RewardItem("Airport Limousine Transfer", "One-way airport transfer in a limousine", 1000));
        rewardCatalog.add(new RewardItem("Couple Massage Package", "60-minute couple massage at the spa", 2000));
        rewardCatalog.add(new RewardItem("Executive Lounge Access (1 Day)", "Access to the executive lounge for a day", 700));
        rewardCatalog.add(new RewardItem("Free Laundry Service", "Complimentary laundry for up to 5 items", 250));
        rewardCatalog.add(new RewardItem("Room Service Discount (20%)", "20% off any room service order", 150));
        rewardCatalog.add(new RewardItem("Golf Green Fee (18 Holes)", "One round of 18-hole golf", 3500));
        rewardCatalog.add(new RewardItem("Sunset Cruise for Two", "Private sunset cruise experience for two", 4000));
        rewardCatalog.add(new RewardItem("Presidential Suite Upgrade", "Upgrade to Presidential Suite for one night", 8000));
        rewardCatalog.add(new RewardItem("Wine Tasting Experience", "Guided wine tasting session for two", 1800));
        rewardCatalog.add(new RewardItem("Kids Club Day Pass", "Full-day access to the resort's kids club", 300));
        rewardCatalog.add(new RewardItem("Private Beach Dinner", "Romantic private dinner setup on the beach", 6000));
        rewardCatalog.add(new RewardItem("Car Rental (1 Day)", "Complimentary car rental for one day", 2200));
        rewardCatalog.add(new RewardItem("Anniversary Cake & Flowers", "Celebratory cake and flower arrangement in-room", 400));

        return rewardCatalog;
    }

    // ---------------------------------------------------------
    // Sample points transactions, for demonstrating the Points Expiry
    // Alert feature. Reads member ID/name/points directly from whichever
    // memberList is passed in (this DAO's own standalone seed data, OR
    // the shared App.memberList when integrated) - so the seeded
    // transactions always match real members, regardless of mode.
    //
    // IMPORTANT: each transaction's pointsEarned is taken directly from
    // member.getLoyaltyPoints() (NOT a separately hardcoded number), so
    // the total shown in View Points Transactions is guaranteed to match
    // the balance shown in Generate Loyalty Report from the very start.
    // A member with 0 points gets no transaction record at all (there is
    // nothing outstanding to track). Earned dates are staggered across a
    // small set of demo dates purely so the Points Expiry Alert report
    // has a mix of soon-expiring and not-soon-expiring members to show.
    // ---------------------------------------------------------
    public ListInterface<PointsTransaction> initializeTransactionData(ListInterface<Member> memberList, int pointsValidityMonths) {
        ListInterface<PointsTransaction> transactionList = new DoublyLinkedList<>();
        LocalDate today = VirtualClock.getInstance().today();

        int total = memberList.getNumberOfEntries();
        if (total == 0) {
            return transactionList; // no members available to seed transactions for
        }

        // demo earnedDates cycled across members purely to showcase a mix of
        // "expiring soon" and "not expiring soon" rows in the alert report
        LocalDate[] demoEarnedDates = {
            today.minusMonths(11).minusDays(24), // ~6 days left
            today.minusMonths(11).minusDays(19), // ~11 days left
            today.minusMonths(2),
            today.minusMonths(1),
            today.minusMonths(11).minusDays(12), // ~18 days left
            today.minusDays(5)
        };

        for (int i = 1; i <= total; i++) {
            Member member = memberList.getEntry(i);
            int points = member.getLoyaltyPoints();
            if (points <= 0) {
                continue; // nothing outstanding to track for this member
            }
            LocalDate earnedDate = demoEarnedDates[(i - 1) % demoEarnedDates.length];
            LocalDate expiryDate = earnedDate.plusMonths(pointsValidityMonths);
            transactionList.add(new PointsTransaction(member.getMemberID(), member.getMemberName(),
                    points, earnedDate, expiryDate));
        }

        return transactionList;
    }
}