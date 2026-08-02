/*
 * @author <Your Name>
 */
package dao;

import adt.DoublyLinkedList;
import adt.ListInterface;
import entity.Member;
import entity.PointsTransaction;
import entity.Reward;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class LoyaltyRewardDAO {

    /**
     * Reuses the team's existing member seed data (PriorityAllocationDAO)
     * instead of duplicating it, so member records stay consistent across
     * modules. PriorityAllocationDAO already returns a ListInterface<Member>
     * (built with its own DoublyLinkedList), so it's returned as-is.
     */
    public ListInterface<Member> getMembers() {

        PriorityAllocationDAO sharedDAO = new PriorityAllocationDAO();
        return sharedDAO.initializeMemberDAO();
    }

    public ListInterface<Reward> getRewards() {

        ListInterface<Reward> rewardList = new DoublyLinkedList<>();

        rewardList.add(new Reward("RW001", "Free Room Upgrade", "Room",
                300, "Upgrade to the next available room category."));

        rewardList.add(new Reward("RW002", "Complimentary Breakfast", "Dining",
                150, "Breakfast for two at the hotel restaurant."));

        rewardList.add(new Reward("RW003", "Spa Voucher", "Wellness",
                500, "One session at the hotel spa."));

        rewardList.add(new Reward("RW004", "Late Check-Out (until 3pm)", "Room",
                100, "Extend check-out time without extra charge."));

        rewardList.add(new Reward("RW005", "Airport Limousine Transfer", "Transport",
                800, "One-way limousine transfer to/from the airport."));

        rewardList.add(new Reward("RW006", "One Night Free Stay", "Room",
                1200, "One complimentary night in a Standard room."));

        return rewardList;
    }

    /**
     * Seed points transactions used to demo tier progression and expiring
     * points notifications. One transaction is deliberately set to expire
     * soon so the "expiring points" feature has data to show during demo.
     *
     * @param members the member list returned by getMembers(); entries are
     *                accessed via the ADT's 1-based getEntry(int).
     */
    public ListInterface<PointsTransaction> getSeedTransactions(ListInterface<Member> members) {

        LocalDateTime now = LocalDateTime.now();
        ListInterface<PointsTransaction> transactionList = new DoublyLinkedList<>();

        transactionList.add(new PointsTransaction("PT001", members.getEntry(1), "EARN", 400,
                now.minusDays(200), LocalDate.now().plusDays(165),
                "Stay at TARUMT Resorts KL"));

        transactionList.add(new PointsTransaction("PT002", members.getEntry(2), "EARN", 300,
                now.minusDays(100), LocalDate.now().plusDays(265),
                "Stay at TARUMT Resorts Penang"));

        transactionList.add(new PointsTransaction("PT003", members.getEntry(3), "EARN", 200,
                now.minusDays(360), LocalDate.now().plusDays(5),
                "Stay at TARUMT Resorts Langkawi"));

        transactionList.add(new PointsTransaction("PT004", members.getEntry(4), "EARN", 100,
                now.minusDays(30), LocalDate.now().plusDays(335),
                "Stay at TARUMT Resorts Ipoh"));

        transactionList.add(new PointsTransaction("PT005", members.getEntry(5), "EARN", 500,
                now.minusDays(10), LocalDate.now().plusDays(355),
                "Stay at TARUMT Resorts KL"));

        return transactionList;
    }
}