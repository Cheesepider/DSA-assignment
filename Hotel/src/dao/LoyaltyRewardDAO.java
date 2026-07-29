/*
 * @author <Your Name>
 */
package dao;

import entity.Member;
import entity.PointsTransaction;
import entity.Reward;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class LoyaltyRewardDAO {

    /**
     * Reuses the team's existing member seed data (PriorityAllocationDAO)
     * instead of duplicating it, so member records stay consistent across
     * modules.
     */
    public Member[] getMembers() {
        PriorityAllocationDAO sharedDAO = new PriorityAllocationDAO();
        return sharedDAO.getMembers();
    }

    public Reward[] getRewards() {

        return new Reward[]{
            new Reward("RW001", "Free Room Upgrade", "Room",
                    300, "Upgrade to the next available room category."),

            new Reward("RW002", "Complimentary Breakfast", "Dining",
                    150, "Breakfast for two at the hotel restaurant."),

            new Reward("RW003", "Spa Voucher", "Wellness",
                    500, "One session at the hotel spa."),

            new Reward("RW004", "Late Check-Out (until 3pm)", "Room",
                    100, "Extend check-out time without extra charge."),

            new Reward("RW005", "Airport Limousine Transfer", "Transport",
                    800, "One-way limousine transfer to/from the airport."),

            new Reward("RW006", "One Night Free Stay", "Room",
                    1200, "One complimentary night in a Standard room.")
        };
    }

    /**
     * Seed points transactions used to demo tier progression and expiring
     * points notifications. One transaction is deliberately set to expire
     * soon so the "expiring points" feature has data to show during demo.
     */
    public PointsTransaction[] getSeedTransactions(Member[] members) {

        LocalDateTime now = LocalDateTime.now();

        return new PointsTransaction[]{
            new PointsTransaction("PT001", members[0], "EARN", 400,
                    now.minusDays(200), LocalDate.now().plusDays(165),
                    "Stay at TARUMT Resorts KL"),

            new PointsTransaction("PT002", members[1], "EARN", 300,
                    now.minusDays(100), LocalDate.now().plusDays(265),
                    "Stay at TARUMT Resorts Penang"),

            new PointsTransaction("PT003", members[2], "EARN", 200,
                    now.minusDays(360), LocalDate.now().plusDays(5),
                    "Stay at TARUMT Resorts Langkawi"),

            new PointsTransaction("PT004", members[3], "EARN", 100,
                    now.minusDays(30), LocalDate.now().plusDays(335),
                    "Stay at TARUMT Resorts Ipoh"),

            new PointsTransaction("PT005", members[4], "EARN", 500,
                    now.minusDays(10), LocalDate.now().plusDays(355),
                    "Stay at TARUMT Resorts KL")
        };
    }
}
