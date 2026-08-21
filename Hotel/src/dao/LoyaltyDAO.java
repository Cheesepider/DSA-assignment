package dao;

import java.time.LocalDate;

import adt.DoublyLinkedList;
import adt.ListInterface;
import entity.Member;
import entity.PointsTransaction;
import entity.RedemptionRecord;
import entity.RewardItem;
import utility.VirtualClock;

/**

 * @author : Kao Yong Feng
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

 
    public ListInterface<PointsTransaction> initializeTransactionData(ListInterface<Member> memberList, int pointsValidityMonths) {
        ListInterface<PointsTransaction> transactionList = new DoublyLinkedList<>();
        LocalDate today = VirtualClock.getInstance().today();

        int total = memberList.getNumberOfEntries();
        if (total == 0) {
            return transactionList; 
        }

        
        LocalDate[] demoEarnedDates = {
            today.minusMonths(11).minusDays(24), 
            today.minusMonths(11).minusDays(19), 
            today.minusMonths(2),
            today.minusMonths(1),
            today.minusMonths(11).minusDays(12), 
            today.minusDays(5)
        };

        for (int i = 1; i <= total; i++) {
            Member member = memberList.getEntry(i);
            int points = member.getLoyaltyPoints();
            if (points <= 0) {
                continue; 
            }
            LocalDate earnedDate = demoEarnedDates[(i - 1) % demoEarnedDates.length];
            LocalDate expiryDate = earnedDate.plusMonths(pointsValidityMonths);
            transactionList.add(new PointsTransaction(member.getMemberID(), member.getMemberName(),
                    points, earnedDate, expiryDate));
        }

        return transactionList;
    }

    
    public ListInterface<RedemptionRecord> initializeRedemptionHistory(
            ListInterface<Member> memberList, ListInterface<RewardItem> rewardCatalog) {

        ListInterface<RedemptionRecord> redemptionList = new DoublyLinkedList<>();
        LocalDate today = VirtualClock.getInstance().today();

        int totalMembers = memberList.getNumberOfEntries();
        int totalRewards = rewardCatalog.getNumberOfEntries();

        if (totalMembers == 0 || totalRewards == 0) {
            return redemptionList; 
        }

        
        LocalDate[] demoRedeemedDates = {
            today.minusDays(2),
            today.minusDays(5),
            today.minusDays(7),
            today.minusDays(9),
            today.minusDays(12),
            today.minusDays(15),
            today.minusDays(18),
            today.minusDays(20),
            today.minusDays(23),
            today.minusDays(25),
            today.minusDays(28),
            today.minusDays(30),
            today.minusDays(34),
            today.minusDays(38),
            today.minusDays(42)
        };

       
        int[][] sampleRedemptions = {
            {1, 5},
            {1, 1},
            {1, 11},
            {2, 3},
            {2, 6},
            {2, 12},
            {3, 8},
            {3, 2},
            {3, 17},
            {4, 2},
            {4, 9},
            {4, 15},
            {5, 6},
            {6, 5},
            {6, 4}
        };

        for (int i = 0; i < sampleRedemptions.length; i++) {

            int memberPosition = sampleRedemptions[i][0];
            int rewardPosition = sampleRedemptions[i][1];

            if (memberPosition > totalMembers || rewardPosition > totalRewards) {
                continue; // seed data references a position this dataset doesn't have
            }

            Member member = memberList.getEntry(memberPosition);
            RewardItem reward = rewardCatalog.getEntry(rewardPosition);
            LocalDate redeemedDate = demoRedeemedDates[i % demoRedeemedDates.length];

            redemptionList.add(new RedemptionRecord(
                    member.getMemberID(),
                    member.getMemberName(),
                    reward.getRewardID(),
                    reward.getRewardName(),
                    reward.getPointsRequired(),
                    redeemedDate
            ));
        }

        return redemptionList;
    }
}