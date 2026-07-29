/*
 * @author <Your Name>
 */
package boundary;

import adt.ListInterface;
import control.LoyaltyRewardControl;
import entity.Member;
import entity.PointsTransaction;
import entity.RedemptionRequest;
import entity.Reward;
import java.time.LocalDateTime;
import java.util.Scanner;

public class LoyaltyRewardUI {

    private Scanner scanner = new Scanner(System.in);
    private LoyaltyRewardControl control = new LoyaltyRewardControl();

    public void startUI() {

        int choice = -1;

        while (choice != 0) {

            displayMenu();

            System.out.print("Enter your choice: ");
            choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    viewMemberProfile();
                    break;
                case 2:
                    earnPointsMenu();
                    break;
                case 3:
                    viewRewardCatalog();
                    break;
                case 4:
                    requestRedemptionMenu();
                    break;
                case 5:
                    processNextRedemptionRequest();
                    break;
                case 6:
                    viewTransactionHistory();
                    break;
                case 7:
                    viewExpiringPointsAlerts();
                    break;
                case 8:
                    generatePointsTransactionReport();
                    break;
                case 9:
                    generateTierRedemptionSummaryReport();
                    break;
                case 0:
                    System.out.println("Returning to Main Menu...");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void displayMenu() {

        System.out.println("\n===============================================");
        System.out.println("      Loyalty & Rewards Service");
        System.out.println("===============================================");
        System.out.println("1. View Member Profile");
        System.out.println("2. Earn Points (Record Stay/Purchase)");
        System.out.println("3. View Reward Catalog");
        System.out.println("4. Request Redemption");
        System.out.println("5. Process Next Redemption Request");
        System.out.println("6. View Points Transaction History");
        System.out.println("7. View Expiring Points Alerts");
        System.out.println("8. Generate Points Transaction Report");
        System.out.println("9. Generate Tier & Redemption Summary Report");
        System.out.println("0. Back to Main Menu");
        System.out.println("===============================================");
    }

    private void viewMemberProfile() {

        System.out.println("\n--- View Member Profile ---");
        System.out.print("Enter Member ID: ");
        String memberID = scanner.nextLine();

        Member member = control.findMemberByID(memberID);

        if (member == null) {
            System.out.println("Member ID not found.");
            return;
        }

        System.out.println("\n--- Member Profile ---");
        System.out.println("Member ID    : " + member.getMemberID());
        System.out.println("Name         : " + member.getMemberName());
        System.out.println("Email        : " + member.getEmail());
        System.out.println("Loyalty Tier : " + member.getLoyaltyTier());
        System.out.println("Points       : " + member.getLoyaltyPoints());
        System.out.println("Promotion    : " + control.getPersonalizedPromotion(member));
    }

    private void earnPointsMenu() {

        System.out.println("\n--- Earn Points ---");
        System.out.print("Enter Member ID: ");
        String memberID = scanner.nextLine();

        Member member = control.findMemberByID(memberID);
        if (member == null) {
            System.out.println("Member ID not found.");
            return;
        }

        System.out.print("Enter Points Earned: ");
        int points = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Enter Description (e.g. stay/purchase): ");
        String description = scanner.nextLine();

        String previousTier = member.getLoyaltyTier();
        boolean success = control.earnPoints(memberID, points, description);

        if (!success) {
            System.out.println("Unable to record points. Please check the points value.");
            return;
        }

        System.out.println("\nPoints recorded successfully!");
        System.out.println("Member       : " + member.getMemberName());
        System.out.println("New Balance  : " + member.getLoyaltyPoints());

        if (!previousTier.equals(member.getLoyaltyTier())) {
            System.out.println("Congratulations! " + member.getMemberName()
                    + " has been upgraded from " + previousTier
                    + " to " + member.getLoyaltyTier() + " tier!");
        }
    }

    private void viewRewardCatalog() {

        System.out.println("\n--- Reward Catalog ---");

        ListInterface<Reward> rewards = control.getRewardList();

        System.out.printf("%-8s %-30s %-12s %-10s%n",
                "ID", "Reward", "Category", "Points");
        System.out.println("--------------------------------------------------------------");

        for (int i = 1; i <= rewards.getNumberOfEntries(); i++) {
            Reward reward = rewards.getEntry(i);
            System.out.printf("%-8s %-30s %-12s %-10d%n",
                    reward.getRewardID(), reward.getRewardName(),
                    reward.getCategory(), reward.getPointsRequired());
        }
    }

    private void requestRedemptionMenu() {

        System.out.println("\n--- Request Redemption ---");
        System.out.print("Enter Member ID: ");
        String memberID = scanner.nextLine();

        viewRewardCatalog();

        System.out.print("\nEnter Reward ID: ");
        String rewardID = scanner.nextLine();

        String result = control.requestRedemption(memberID, rewardID);
        System.out.println("\n" + result);
    }

    private void processNextRedemptionRequest() {

        System.out.println("\n--- Process Next Redemption Request ---");

        RedemptionRequest request = control.processNextRedemptionRequest();

        if (request == null) {
            System.out.println("No pending redemption requests.");
            return;
        }

        System.out.println("Redemption request " + request.getRequestID() + " completed!");
        System.out.println("Member       : " + request.getMember().getMemberName());
        System.out.println("Reward       : " + request.getReward().getRewardName());
        System.out.println("Points Used  : " + request.getReward().getPointsRequired());
        System.out.println("New Balance  : " + request.getMember().getLoyaltyPoints());
    }

    private void viewTransactionHistory() {

        System.out.println("\n--- Points Transaction History ---");
        System.out.print("Enter Member ID: ");
        String memberID = scanner.nextLine();

        ListInterface<PointsTransaction> transactions
                = control.getFilteredTransactions(memberID, "All");

        if (transactions.isEmpty()) {
            System.out.println("No transactions found for this member.");
            return;
        }

        printTransactionTable(transactions);
    }

    private void viewExpiringPointsAlerts() {

        System.out.println("\n--- Expiring Points Alerts ---");
        System.out.print("Show points expiring within how many days? ");
        int days = scanner.nextInt();
        scanner.nextLine();

        ListInterface<PointsTransaction> expiring = control.getExpiringPointsTransactions(days);

        if (expiring.isEmpty()) {
            System.out.println("No points are expiring within " + days + " days.");
            return;
        }

        System.out.printf("%-15s %-15s %-10s %-15s%n",
                "Transaction ID", "Member", "Points", "Expiry Date");
        System.out.println("--------------------------------------------------------------");

        for (int i = 1; i <= expiring.getNumberOfEntries(); i++) {
            PointsTransaction transaction = expiring.getEntry(i);
            System.out.printf("%-15s %-15s %-10d %-15s%n",
                    transaction.getTransactionID(),
                    transaction.getMember().getMemberName(),
                    transaction.getPoints(),
                    transaction.getExpiryDate());
        }
    }

    private void generatePointsTransactionReport() {

        System.out.println("\n--- Generate Points Transaction Report ---");

        System.out.print("Filter by Member ID (or type 'All'): ");
        String memberID = scanner.nextLine();

        System.out.println("Select Transaction Type:");
        System.out.println("1. EARN");
        System.out.println("2. REDEEM");
        System.out.println("3. All");
        System.out.print("Enter choice: ");
        int typeChoice = scanner.nextInt();
        scanner.nextLine();

        String type;
        switch (typeChoice) {
            case 1:
                type = "EARN";
                break;
            case 2:
                type = "REDEEM";
                break;
            case 3:
                type = "All";
                break;
            default:
                System.out.println("Invalid choice.");
                return;
        }

        ListInterface<PointsTransaction> filtered
                = control.getFilteredTransactions(memberID, type);

        System.out.println("\n===============================================");
        System.out.println("      POINTS TRANSACTION REPORT");
        System.out.println("===============================================");
        System.out.println("Generated At : " + LocalDateTime.now());
        System.out.println("Member Filter: " + memberID);
        System.out.println("Type Filter  : " + type);
        System.out.println("-----------------------------------------------");

        if (filtered.isEmpty()) {
            System.out.println("No transactions match the selected criteria.");
            System.out.println("===============================================");
            return;
        }

        printTransactionTable(filtered);

        int totalEarned = 0;
        int totalRedeemed = 0;

        for (int i = 1; i <= filtered.getNumberOfEntries(); i++) {
            PointsTransaction transaction = filtered.getEntry(i);
            if (transaction.getTransactionType().equals("EARN")) {
                totalEarned += transaction.getPoints();
            } else {
                totalRedeemed += transaction.getPoints();
            }
        }

        System.out.println("-----------------------------------------------");
        System.out.println("Total Records  : " + filtered.getNumberOfEntries());
        System.out.println("Total Earned   : " + totalEarned);
        System.out.println("Total Redeemed : " + totalRedeemed);
        System.out.println("===============================================");
    }

    private void generateTierRedemptionSummaryReport() {

        System.out.println("\n--- Generate Tier & Redemption Summary Report ---");

        ListInterface<Member> ranked = control.getMembersRankedByPoints();

        int eliteCount = 0;
        int diamondCount = 0;
        int platinumCount = 0;
        int standardCount = 0;

        for (int i = 1; i <= ranked.getNumberOfEntries(); i++) {
            switch (ranked.getEntry(i).getLoyaltyTier()) {
                case "Elite":
                    eliteCount++;
                    break;
                case "Diamond":
                    diamondCount++;
                    break;
                case "Platinum":
                    platinumCount++;
                    break;
                default:
                    standardCount++;
            }
        }

        System.out.println("\n===============================================");
        System.out.println("      TIER & REDEMPTION SUMMARY REPORT");
        System.out.println("===============================================");
        System.out.println("Generated At: " + LocalDateTime.now());
        System.out.println("-----------------------------------------------");
        System.out.printf("%-12s : %d%n", "Elite", eliteCount);
        System.out.printf("%-12s : %d%n", "Diamond", diamondCount);
        System.out.printf("%-12s : %d%n", "Platinum", platinumCount);
        System.out.printf("%-12s : %d%n", "Standard", standardCount);
        System.out.println("-----------------------------------------------");

        System.out.printf("%-4s %-15s %-12s %-10s %-18s%n",
                "Rank", "Member", "Tier", "Points", "Completed Redeems");
        System.out.println("--------------------------------------------------------------");

        for (int i = 1; i <= ranked.getNumberOfEntries(); i++) {
            Member member = ranked.getEntry(i);
            int completedRedemptions = control.countCompletedRedemptions(member.getMemberID());

            System.out.printf("%-4d %-15s %-12s %-10d %-18d%n",
                    i, member.getMemberName(), member.getLoyaltyTier(),
                    member.getLoyaltyPoints(), completedRedemptions);
        }

        System.out.println("===============================================");
    }

    private void printTransactionTable(ListInterface<PointsTransaction> transactions) {

        System.out.printf("%-15s %-15s %-8s %-8s %-22s %-25s%n",
                "Transaction ID", "Member", "Type", "Points", "Date", "Description");
        System.out.println("--------------------------------------------------------------------------------------");

        for (int i = 1; i <= transactions.getNumberOfEntries(); i++) {
            PointsTransaction transaction = transactions.getEntry(i);

            System.out.printf("%-15s %-15s %-8s %-8d %-22s %-25s%n",
                    transaction.getTransactionID(),
                    transaction.getMember().getMemberName(),
                    transaction.getTransactionType(),
                    transaction.getPoints(),
                    transaction.getTransactionDate(),
                    transaction.getDescription());
        }
    }

    public static void main(String[] args) {
        LoyaltyRewardUI ui = new LoyaltyRewardUI();
        ui.startUI();
    }
}
