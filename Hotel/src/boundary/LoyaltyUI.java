/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package boundary;

/**
 **
 * @author Kao Yong Feng
 */
import adt.DoublyLinkedList;
import adt.ListInterface;
import control.LoyaltyControl;
import entity.Member;
import entity.Member.LoyaltyTier;
import entity.PendingPointsCredit;
import entity.PointsTransaction;
import entity.RedemptionRecord;
import entity.RewardItem;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import utility.ValidationUtility;
import utility.VirtualClock;

public class LoyaltyUI {

    private Scanner scanner = new Scanner(System.in);
    private LoyaltyControl control = new LoyaltyControl();

    public LoyaltyUI() {
        control = new LoyaltyControl();
    }

    public LoyaltyUI(ListInterface<Member> sharedMemberList) {
        control = new LoyaltyControl(sharedMemberList);
    }

    public void startUI() {

        // Scan for completed stays and queue loyalty points
        ListInterface<PendingPointsCredit> newlyQueued = control.queueCompletedStayPoints();

        if (!newlyQueued.isEmpty()) {

            System.out.println("\n--- LOYALTY PROGRAM: NEW STAYS QUEUED FOR POINTS ACCUMULATION ---");

            for (int i = 1; i <= newlyQueued.getNumberOfEntries(); i++) {

                PendingPointsCredit credit = newlyQueued.getEntry(i);

                System.out.println(
                        credit.getMemberName() + " - " + credit.getSourceDetail()
                        + ": " + credit.getPointsToCredit() + " point(s) pending accumulation.");
            }

            System.out.println("\n" + newlyQueued.getNumberOfEntries()
                    + " new completed stay(s) queued for points accumulation. "
                    + "Go to 'Points Accumulation Queue' to review and credit them.");
        }

        // Forfeit any overdue points
        checkPointsExpiry();

        // One-time notification alerts on module entry
        int expiringCount = control.countExpiringTransactions(LoyaltyControl.DEFAULT_EXPIRY_ALERT_DAYS);

        if (expiringCount > 0) {

            System.out.println("\n[ALERT] " + expiringCount
                    + " points transaction(s) are expiring within "
                    + LoyaltyControl.DEFAULT_EXPIRY_ALERT_DAYS + " days! See option 7 for details.");
        }

        int pendingCount = control.countTotalPendingCredits();

        if (pendingCount > 0) {

            System.out.println("\n[ALERT] " + pendingCount
                    + " points credit(s) are waiting to be processed! See option 5 to review and credit them.");
        }

        int choice = -1;

        while (choice != 0) {

            displayMenu();

            System.out.print("Enter your choice: ");
            choice = ValidationUtility.inputChoice(scanner);

            switch (choice) {

                case 1:
                    redeemReward();
                    break;

                case 2:
                    searchMember();
                    break;

                case 3:
                    displayRewardCatalog();
                    break;

                case 4:
                    manageRewardCatalog();
                    break;

                case 5:
                    managePointsAccumulationQueue();
                    break;

                case 6:
                    generateLoyaltyReport();
                    break;

                case 7:
                    viewPointsTransactions();
                    break;

                case 8:
                    generateRedemptionHistoryReport();
                    break;

                case 9:
                    TimeProgressionUI.showTimeMenu();
                    checkPointsExpiry();
                    break;

                case 0:
                    System.out.println("Returning to Main Menu...");
                    break;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    public void displayMenu() {

        System.out.println("\n===============================================");
        System.out.println("      Loyalty & Reward Service Module");
        System.out.println("  " + VirtualClock.getInstance().toString());
        System.out.println("===============================================");
        System.out.println("1. Redeem Reward");
        System.out.println("2. Search Member");
        System.out.println("3. View Reward Catalog");
        System.out.println("4. Manage Reward Catalog (Add / Update / Delete)");
        System.out.println("5. Points Accumulation Queue");
        System.out.println("6. Generate Loyalty Report");
        System.out.println("7. View Points Transactions");
        System.out.println("8. Generate Redemption History Report");
        System.out.println("9. Advance Time");
        System.out.println("0. Back to Main Menu");
        System.out.println("===============================================");
    }

    private void checkPointsExpiry() {

        ListInterface<PointsTransaction> expiredList = control.expireOverduePoints();

        if (!expiredList.isEmpty()) {

            System.out.println("\n--- LOYALTY PROGRAM: POINTS EXPIRED ---");

            for (int i = 1; i <= expiredList.getNumberOfEntries(); i++) {

                PointsTransaction t = expiredList.getEntry(i);

                System.out.println(
                        t.getMemberName() + " (ID " + t.getMemberID() + ") - "
                        + t.getPointsEarned() + " point(s) EXPIRED on "
                        + t.getExpiryDate() + " and have been forfeited.");
            }

            System.out.println("\n" + expiredList.getNumberOfEntries()
                    + " points transaction(s) expired and were forfeited.");
        }
    }

    private void redeemReward() {

        System.out.println("\n--- Redeem Reward ---");

        displayRewardCatalog();

        System.out.print("Enter Member ID: ");
        int memberID = ValidationUtility.inputChoice(scanner);

        Member member = control.findMemberByID(memberID);

        if (member == null) {
            System.out.println("Member with ID " + memberID + " not found.");
            return;
        }

        System.out.println("Member Name: " + member.getMemberName());
        System.out.println("Current Spendable Points: " + member.getLoyaltyPoints());

        System.out.print("Enter Reward ID to redeem: ");
        int rewardID = ValidationUtility.inputChoice(scanner);

        RewardItem reward = control.findRewardByID(rewardID);

        if (reward == null) {
            System.out.println("Reward with ID " + rewardID + " not found in catalog.");
            return;
        }

        int result = control.redeemReward(memberID, rewardID);

        if (result == LoyaltyControl.REDEEM_SUCCESS) {

            System.out.println("\nRedemption successful!");
            System.out.println(
                    member.getMemberName() + " redeemed \"" + reward.getRewardName()
                    + "\" for " + reward.getPointsRequired() + " points.");
            System.out.println(
                    "Remaining balance: " + member.getLoyaltyPoints()
                    + " points. (Tier is unaffected by redemption.)");

        } else if (result == LoyaltyControl.REDEEM_INSUFFICIENT_POINTS) {

            System.out.println(
                    member.getMemberName() + " has insufficient points to redeem \""
                    + reward.getRewardName() + "\" (needs " + reward.getPointsRequired()
                    + ", has " + member.getLoyaltyPoints() + ").");

        } else {
            System.out.println("Redemption failed. Please check inputs.");
        }
    }

    private void searchMember() {

        System.out.println("\n--- Search Member ---");
        System.out.println("1. Search by Member ID");
        System.out.println("2. Search by Name Keyword");
        System.out.println("3. Search by Loyalty Tier");
        System.out.println("0. Back");

        System.out.print("Enter choice: ");
        int option = ValidationUtility.inputChoice(scanner);

        switch (option) {

            case 1:
                System.out.print("Enter Member ID: ");
                int id = ValidationUtility.inputChoice(scanner);
                Member m = control.findMemberByID(id);

                if (m != null) {
                    System.out.println("\n--- Member Details ---");
                    System.out.println("Member ID       : " + m.getMemberID());
                    System.out.println("Member Name     : " + m.getMemberName());
                    System.out.println("Phone Number    : " + m.getPhoneNumber());
                    System.out.println("Email           : " + m.getEmail());
                    System.out.println("Loyalty Tier    : " + m.getLoyaltyTier());
                    System.out.println("Spendable Points: " + m.getLoyaltyPoints());
                    System.out.println("Lifetime Points : " + control.getLifetimeEarnedPoints(id)
                            + " (determines Tier; balance above is spendable)");
                } else {
                    System.out.println("Member ID not found.");
                }
                break;

            case 2:
                System.out.print("Enter name keyword: ");
                String name = scanner.nextLine().trim();
                ListInterface<Member> nameResults = control.searchMemberByName(name);
                printMemberTable(nameResults);
                break;

            case 3:
                System.out.println("\nSelect Loyalty Tier:");
                System.out.println("1. Elite");
                System.out.println("2. Diamond");
                System.out.println("3. Platinum");
                System.out.println("4. Regular");

                System.out.print("Enter choice: ");
                int tierChoice = ValidationUtility.inputChoice(scanner);

                LoyaltyTier selectedTier = null;

                switch (tierChoice) {
                    case 1:
                        selectedTier = LoyaltyTier.Elite;
                        break;
                    case 2:
                        selectedTier = LoyaltyTier.Diamond;
                        break;
                    case 3:
                        selectedTier = LoyaltyTier.Platinum;
                        break;
                    case 4:
                        selectedTier = LoyaltyTier.Regular;
                        break;
                    default:
                        System.out.println("Invalid tier choice.");
                        return;
                }

                ListInterface<Member> tierResults = control.searchMemberByTier(selectedTier);
                printMemberTable(tierResults);
                break;

            case 0:
                break;

            default:
                System.out.println("Invalid choice.");
        }
    }

    private void printMemberTable(ListInterface<Member> list) {

        if (list.isEmpty()) {
            System.out.println("No matching members found.");
            return;
        }

        System.out.println("\n==========================================================================================");
        System.out.printf("%-10s %-20s %-12s %-15s %-10s %-15s%n",
                "Member ID", "Member Name", "Phone", "Tier", "Points", "Lifetime Earned");
        System.out.println("------------------------------------------------------------------------------------------");

        for (int i = 1; i <= list.getNumberOfEntries(); i++) {

            Member m = list.getEntry(i);

            System.out.printf("%-10d %-20s %-12s %-15s %-10d %-15d%n",
                    m.getMemberID(),
                    m.getMemberName(),
                    m.getPhoneNumber(),
                    m.getLoyaltyTier(),
                    m.getLoyaltyPoints(),
                    control.getLifetimeEarnedPoints(m.getMemberID())
            );
        }

        System.out.println("==========================================================================================");
    }

    private void displayRewardCatalog() {

        System.out.println("\n==========================================================================================");
        System.out.println("                                    REWARD CATALOG");
        System.out.println("==========================================================================================");
        System.out.printf("%-6s %-32s %-15s %-30s%n", "ID", "Reward Name", "Points Needed", "Description");
        System.out.println("------------------------------------------------------------------------------------------");

        ListInterface<RewardItem> catalog = control.getRewardCatalog();

        for (int i = 1; i <= catalog.getNumberOfEntries(); i++) {

            RewardItem r = catalog.getEntry(i);

            System.out.printf("%-6d %-32s %-15d %-30s%n",
                    r.getRewardID(),
                    r.getRewardName(),
                    r.getPointsRequired(),
                    r.getDescription() != null ? r.getDescription() : "-"
            );
        }

        System.out.println("==========================================================================================");
    }

    private void manageRewardCatalog() {

        int option = -1;

        while (option != 0) {

            System.out.println("\n--- Manage Reward Catalog ---");
            System.out.println("1. Add New Reward");
            System.out.println("2. Update Existing Reward");
            System.out.println("3. Delete Reward");
            System.out.println("0. Back");

            System.out.print("Enter choice: ");
            option = ValidationUtility.inputChoice(scanner);

            switch (option) {

                case 1:
                    System.out.print("Enter reward name: ");
                    String name = scanner.nextLine().trim();

                    System.out.print("Enter reward description: ");
                    String desc = scanner.nextLine().trim();

                    System.out.print("Enter points required: ");
                    int points = ValidationUtility.inputChoice(scanner);

                    boolean added = control.addRewardItem(name, desc, points);

                    if (added) {
                        System.out.println("Reward added successfully!");
                    } else {
                        System.out.println("Failed to add reward. Name cannot be empty and points must be positive.");
                    }
                    break;

                case 2:
                    displayRewardCatalog();

                    System.out.print("Enter Reward ID to update: ");
                    int updateID = ValidationUtility.inputChoice(scanner);

                    System.out.print("Enter new reward name: ");
                    String newName = scanner.nextLine().trim();

                    System.out.print("Enter new points required: ");
                    int newPoints = ValidationUtility.inputChoice(scanner);

                    boolean updated = control.updateRewardItem(updateID, newName, newPoints);

                    if (updated) {
                        System.out.println("Reward ID " + updateID + " updated successfully!");
                    } else {
                        System.out.println("Failed to update reward. Please verify Reward ID and points.");
                    }
                    break;

                case 3:
                    displayRewardCatalog();

                    System.out.print("Enter Reward ID to delete: ");
                    int deleteID = ValidationUtility.inputChoice(scanner);

                    boolean deleted = control.deleteRewardItem(deleteID);

                    if (deleted) {
                        System.out.println("Reward ID " + deleteID + " removed successfully!");
                    } else {
                        System.out.println("Reward ID " + deleteID + " not found.");
                    }
                    break;

                case 0:
                    break;

                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private void managePointsAccumulationQueue() {

        int option = -1;

        while (option != 0) {

            System.out.println("\n--- Points Accumulation Queue ---");
            System.out.println("1. View Pending Queue");
            System.out.println("2. Process Next Pending Credit (FIFO)");
            System.out.println("3. Process ALL Pending Credits");
            System.out.println("4. Reject a Pending Credit");
            System.out.println("5. Grant Personalized Promotional Points");
            System.out.println("0. Back");

            System.out.print("Enter choice: ");
            option = ValidationUtility.inputChoice(scanner);

            switch (option) {

                case 1:
                    displayPendingPointsQueue();
                    break;

                case 2:
                    PendingPointsCredit processed = control.processNextPendingPointsCredit();

                    if (processed == null) {
                        System.out.println("No pending points credits to process.");
                    } else {
                        Member member = control.findMemberByID(processed.getMemberID());
                        System.out.println("\nSuccessfully processed Credit ID #" + processed.getCreditID() + "!");
                        System.out.println("Member: " + processed.getMemberName() + " (ID " + processed.getMemberID() + ")");
                        System.out.println("Credited: " + processed.getPointsToCredit() + " points [" + processed.getSource() + " - " + processed.getSourceDetail() + "]");
                        if (member != null) {
                            System.out.println("New Spendable Balance: " + member.getLoyaltyPoints() + " points");
                            System.out.println("Current Loyalty Tier : " + member.getLoyaltyTier());
                        }
                    }
                    break;

                case 3:
                    ListInterface<PendingPointsCredit> allProcessed = control.processAllPendingPointsCredits();

                    if (allProcessed.isEmpty()) {
                        System.out.println("No pending points credits to process.");
                    } else {
                        System.out.println("\nSuccessfully processed " + allProcessed.getNumberOfEntries() + " pending credit(s)!");
                        for (int i = 1; i <= allProcessed.getNumberOfEntries(); i++) {
                            PendingPointsCredit c = allProcessed.getEntry(i);
                            Member m = control.findMemberByID(c.getMemberID());
                            System.out.printf("Processed Credit #%d: %s credited with %d points. New Balance: %d points.%n",
                                    c.getCreditID(), c.getMemberName(), c.getPointsToCredit(),
                                    m != null ? m.getLoyaltyPoints() : 0);
                        }
                    }
                    break;

                case 4:
                    displayPendingPointsQueue();

                    System.out.print("Enter Credit ID to reject: ");
                    int creditID = ValidationUtility.inputChoice(scanner);

                    PendingPointsCredit rejected = control.rejectPendingPointsCredit(creditID);

                    if (rejected != null) {
                        System.out.println("Rejected Credit ID #" + creditID + ": "
                                + rejected.getPointsToCredit() + " point(s) for "
                                + rejected.getMemberName() + " (" + rejected.getSourceDetail()
                                + ") will NOT be accumulated.");
                    } else {
                        System.out.println("Pending credit with ID " + creditID + " not found in queue.");
                    }
                    break;

                case 5:
                    grantPromotionalPoints();
                    break;

                case 0:
                    break;

                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private void displayPendingPointsQueue() {

        System.out.println("\n======================================================================================================");
        System.out.println("                                  PENDING POINTS ACCUMULATION QUEUE");
        System.out.println("======================================================================================================");
        System.out.printf("%-9s %-10s %-18s %-12s %-8s %-12s %-30s%n",
                "Credit ID", "Member ID", "Member Name", "Source", "Points", "Queued On", "Detail");
        System.out.println("------------------------------------------------------------------------------------------------------");

        ListInterface<PendingPointsCredit> queue = control.getPendingPointsQueue();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (int i = 1; i <= queue.getNumberOfEntries(); i++) {

            PendingPointsCredit c = queue.getEntry(i);

            System.out.printf("%-9d %-10d %-18s %-12s %-8d %-12s %-30s%n",
                    c.getCreditID(),
                    c.getMemberID(),
                    c.getMemberName(),
                    c.getSource(),
                    c.getPointsToCredit(),
                    c.getDateQueued().format(dateFormatter),
                    c.getSourceDetail()
            );
        }

        System.out.println("======================================================================================================");
        System.out.println("Total Pending Credits: " + queue.getNumberOfEntries());

        if (queue.isEmpty()) {
            System.out.println("No points are currently pending accumulation.");
        }
    }

    private void grantPromotionalPoints() {

        System.out.println("\n--- Grant Personalized Promotional Points ---");

        System.out.print("Enter Member ID: ");
        int memberID = ValidationUtility.inputChoice(scanner);

        Member member = control.findMemberByID(memberID);

        if (member == null) {
            System.out.println("Member with ID " + memberID + " not found.");
            return;
        }

        System.out.println("Member Name: " + member.getMemberName() + " (Current Tier: " + member.getLoyaltyTier() + ")");

        System.out.print("Enter promotional points to grant: ");
        int points = ValidationUtility.inputChoice(scanner);

        System.out.print("Enter reason for promotion (e.g., Birthday Gift, Goodwill Gesture): ");
        String reason = scanner.nextLine().trim();

        boolean queued = control.grantPromotionalPoints(memberID, points, reason);

        if (queued) {
            System.out.println("\nSuccessfully queued " + points + " promotional point(s) for "
                    + member.getMemberName() + " (Reason: " + reason + ").");
            System.out.println("Process the queue from Option 5 to credit these points.");
        } else {
            System.out.println("Failed to grant promotional points. Points must be positive and reason cannot be empty.");
        }
    }

    private void generateLoyaltyReport() {

        System.out.println("\n--- Generate Loyalty Member Report ---");

        ListInterface<Member> filteredList = control.getAllMembers();

        int totalMembers = control.countTotalMembers();
        int totalPoints = control.countTotalSpendablePoints();
        int eliteCount = control.countMembersByTier(LoyaltyTier.Elite);
        int diamondCount = control.countMembersByTier(LoyaltyTier.Diamond);
        int platinumCount = control.countMembersByTier(LoyaltyTier.Platinum);
        int regularCount = control.countMembersByTier(LoyaltyTier.Regular);

        LocalDateTime reportTime = LocalDateTime.now();

        System.out.println("\n==========================================================================");
        System.out.println("    Tunku Abdul Rahman University of Management and Technology Resort");
        System.out.println("                    Loyalty & Reward Service Subsystem");
        System.out.println("\n                 LOYALTY MEMBER SUMMARY REPORT");
        System.out.println("==========================================================================");
        System.out.println("Generated At          : " + reportTime);
        System.out.println("--------------------------------------------------------------------------");
        System.out.printf("%-30s : %d%n", "Total Registered Members", totalMembers);
        System.out.printf("%-30s : %d%n", "Total Spendable Points", totalPoints);
        System.out.println("--------------------------------------------------------------------------");
        System.out.printf("%-30s : %d%n", "Elite Members", eliteCount);
        System.out.printf("%-30s : %d%n", "Diamond Members", diamondCount);
        System.out.printf("%-30s : %d%n", "Platinum Members", platinumCount);
        System.out.printf("%-30s : %d%n", "Regular Members", regularCount);

        System.out.println("\n==========================================================================================");
        System.out.println("                                      MEMBER DETAILS");
        System.out.println("==========================================================================================");
        System.out.printf("%-6s %-10s %-20s %-12s %-12s %-15s%n",
                "No.", "Member ID", "Member Name", "Tier", "Points", "Lifetime Earned");
        System.out.println("------------------------------------------------------------------------------------------");

        for (int i = 1; i <= filteredList.getNumberOfEntries(); i++) {

            Member m = filteredList.getEntry(i);

            System.out.printf("%-6d %-10d %-20s %-12s %-12d %-15d%n",
                    i,
                    m.getMemberID(),
                    m.getMemberName(),
                    m.getLoyaltyTier(),
                    m.getLoyaltyPoints(),
                    control.getLifetimeEarnedPoints(m.getMemberID())
            );
        }

        System.out.println("==========================================================================================");
        System.out.println("Note: Points = current spendable balance. Lifetime Earned = total earned (determines Tier).");

        if (filteredList.isEmpty()) {
            System.out.println("No members found.");
            return;
        }

        System.out.println("\nSelect Graph to Display:");
        System.out.println("1. Current Spendable Points");
        System.out.println("2. Lifetime Earned Points");
        System.out.print("Enter choice (default 1): ");

        String graphChoice = scanner.nextLine().trim();

        if (graphChoice.equals("2")) {
            printMemberLifetimeEarnedBarChart(filteredList);
        } else {
            printMemberPointsBarChart(filteredList);
        }
    }

    private void viewPointsTransactions() {

        System.out.println("\n--- View Points Transactions ---");
        System.out.println("1. Points Expiry Alerts (Expiring within " + LoyaltyControl.DEFAULT_EXPIRY_ALERT_DAYS + " Days)");
        System.out.println("2. View All Points Transactions (Full History)");
        System.out.println("0. Back");

        System.out.print("Enter choice: ");
        int option = ValidationUtility.inputChoice(scanner);

        ListInterface<PointsTransaction> list;
        String subtitle;

        if (option == 1) {
            list = control.getExpiringTransactions(LoyaltyControl.DEFAULT_EXPIRY_ALERT_DAYS);
            subtitle = "POINTS EXPIRY ALERT (Next " + LoyaltyControl.DEFAULT_EXPIRY_ALERT_DAYS + " Days)";
        } else if (option == 2) {
            list = control.getAllTransactions();
            subtitle = "ALL POINTS TRANSACTIONS (Full History)";
        } else {
            return;
        }

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate today = VirtualClock.getInstance().today();

        System.out.println("\n==========================================================================================");
        System.out.println("    Tunku Abdul Rahman University of Management and Technology Resort");
        System.out.println("                    Loyalty & Reward Service Subsystem");
        System.out.println("\n                 " + subtitle);
        System.out.println("==========================================================================================");
        System.out.println("Generated At : " + LocalDateTime.now());
        System.out.println("Current Date : " + today);
        System.out.println("------------------------------------------------------------------------------------------");
        System.out.printf("%-10s %-18s %-10s %-14s %-14s %-10s%n",
                "Member ID", "Member Name", "Points", "Earned Date", "Expires On", "Days Left");
        System.out.println("------------------------------------------------------------------------------------------");

        for (int i = 1; i <= list.getNumberOfEntries(); i++) {

            PointsTransaction t = list.getEntry(i);
            long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(today, t.getExpiryDate());

            System.out.printf("%-10d %-18s %-10d %-14s %-14s %-10d%n",
                    t.getMemberID(),
                    t.getMemberName(),
                    t.getPointsEarned(),
                    t.getEarnedDate().format(dateFormatter),
                    t.getExpiryDate().format(dateFormatter),
                    Math.max(0, daysLeft)
            );
        }

        System.out.println("==========================================================================================");
        System.out.println("Total Transactions: " + list.getNumberOfEntries());

        if (list.isEmpty()) {
            System.out.println("No matching points transactions found.");
        }
    }

    private void generateRedemptionHistoryReport() {

        System.out.println("\n--- Generate Redemption History Report ---");

        ListInterface<RedemptionRecord> list = control.getRedemptionHistory();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        System.out.println("\n==========================================================================================");
        System.out.println("    Tunku Abdul Rahman University of Management and Technology Resort");
        System.out.println("                    Loyalty & Reward Service Subsystem");
        System.out.println("\n                 REWARD REDEMPTION HISTORY REPORT");
        System.out.println("==========================================================================");
        System.out.println("Generated At  : " + LocalDateTime.now());
        System.out.println("--------------------------------------------------------------------------");
        System.out.printf("%-10s %-18s %-30s %-10s %-14s%n",
                "Member ID", "Member Name", "Reward Redeemed", "Points", "Redeemed Date");
        System.out.println("--------------------------------------------------------------------------");

        int totalPointsRedeemed = 0;

        for (int i = 1; i <= list.getNumberOfEntries(); i++) {

            RedemptionRecord r = list.getEntry(i);
            totalPointsRedeemed += r.getPointsUsed();

            System.out.printf("%-10d %-18s %-30s %-10d %-14s%n",
                    r.getMemberID(),
                    r.getMemberName(),
                    r.getRewardName(),
                    r.getPointsUsed(),
                    r.getRedeemedDate().format(dateFormatter)
            );
        }

        System.out.println("==========================================================================================");
        System.out.printf("Total Redemptions: %d  |  Total Points Redeemed: %d%n",
                list.getNumberOfEntries(), totalPointsRedeemed);

        if (list.isEmpty()) {
            System.out.println("No redemption records found.");
            return;
        }

        System.out.println("\nSelect Graph to Display:");
        System.out.println("1. Top 3 Rewards");
        System.out.println("2. Top 10 Rewards");
        System.out.println("3. Back (Skip Graph)");
        System.out.print("Enter choice (default 1): ");

        String graphChoice = scanner.nextLine().trim();

        if (graphChoice.equals("2")) {
            printRedemptionCountByRewardChart(list, 10);
        } else if (graphChoice.equals("3")) {
            // Back - skip the graph
        } else {
            printRedemptionCountByRewardChart(list, 3);
        }
    }

    // Horizontal bar chart of each member's spendable points. Same overall
    // style as before (legend line, top-10-by-value sorting) but drawn as
    // rows instead of columns so full member names are never cut off.
    private void printMemberPointsBarChart(ListInterface<Member> list) {

        if (list.isEmpty()) {
            return;
        }

        int n = list.getNumberOfEntries();
        String[] labels = new String[n];
        int[] values = new int[n];

        for (int i = 1; i <= n; i++) {

            Member m = list.getEntry(i);
            labels[i - 1] = m.getMemberName();
            values[i - 1] = m.getLoyaltyPoints();
        }

        printHorizontalBarChart("MEMBER SPENDABLE POINTS", labels, values, "point(s)", "Spendable Points", 10);
    }

    // Horizontal bar chart of each member's lifetime earned points (the
    // total that determines their loyalty tier, as opposed to their
    // current spendable balance).
    private void printMemberLifetimeEarnedBarChart(ListInterface<Member> list) {

        if (list.isEmpty()) {
            return;
        }

        int n = list.getNumberOfEntries();
        String[] labels = new String[n];
        int[] values = new int[n];

        for (int i = 1; i <= n; i++) {

            Member m = list.getEntry(i);
            labels[i - 1] = m.getMemberName();
            values[i - 1] = control.getLifetimeEarnedPoints(m.getMemberID());
        }

        printHorizontalBarChart("MEMBER LIFETIME EARNED POINTS", labels, values, "point(s)", "Lifetime Earned Points", 10);
    }

    // Horizontal bar chart of how many times each catalog reward has been
    // redeemed. Only rewards that have actually been redeemed at least
    // once are charted, and only the top 10 by redemption count are shown.
    private void printRedemptionCountByRewardChart(ListInterface<RedemptionRecord> redemptions, int topN) {

        ListInterface<RewardItem> catalog = control.getRewardCatalog();

        if (catalog.isEmpty() || redemptions.isEmpty()) {
            return;
        }

        int catalogSize = catalog.getNumberOfEntries();
        String[] allLabels = new String[catalogSize];
        int[] allCounts = new int[catalogSize];
        int redeemedRewardCount = 0;

        for (int i = 1; i <= catalogSize; i++) {

            String rewardName = catalog.getEntry(i).getRewardName();
            int count = 0;

            for (int j = 1; j <= redemptions.getNumberOfEntries(); j++) {

                if (redemptions.getEntry(j).getRewardName().equalsIgnoreCase(rewardName)) {
                    count++;
                }
            }

            if (count > 0) {
                allLabels[redeemedRewardCount] = rewardName;
                allCounts[redeemedRewardCount] = count;
                redeemedRewardCount++;
            }
        }

        String[] labels = new String[redeemedRewardCount];
        int[] values = new int[redeemedRewardCount];

        for (int i = 0; i < redeemedRewardCount; i++) {
            labels[i] = allLabels[i];
            values[i] = allCounts[i];
        }

        printHorizontalBarChart("REWARD REDEMPTION COUNT", labels, values, "redemption(s)", "Redemption Count", topN);
    }

    // Shared horizontal bar chart renderer used by both report graphs.
    // Each row shows the full label (never truncated), its value, and a
    // proportional bar of asterisks with a legend explaining the scale.
    // An X-axis line runs underneath the bars, labeled with what the bar
    // length represents (e.g. "Redemption Count"). Only the top maxRows
    // items by value are shown (sorted descending with a simple
    // selection sort).
    private void printHorizontalBarChart(String title, String[] labels, int[] values, String unitName, String xAxisLabel, int maxRows) {

        if (values.length == 0) {
            return;
        }

        // work on local copies so the caller's arrays are untouched
        String[] sortedLabels = labels.clone();
        int[] sortedValues = values.clone();

        int n = sortedValues.length;

        for (int i = 0; i < n - 1; i++) {

            int maxPos = i;

            for (int j = i + 1; j < n; j++) {

                if (sortedValues[j] > sortedValues[maxPos]) {
                    maxPos = j;
                }
            }

            if (maxPos != i) {

                int tempValue = sortedValues[i];
                sortedValues[i] = sortedValues[maxPos];
                sortedValues[maxPos] = tempValue;

                String tempLabel = sortedLabels[i];
                sortedLabels[i] = sortedLabels[maxPos];
                sortedLabels[maxPos] = tempLabel;
            }
        }

        int rowCount = Math.min(n, maxRows);

        int maxValue = sortedValues[0];

        System.out.println("\n" + title + "\n");

        if (maxValue == 0) {
            System.out.println("No data to chart.");
            return;
        }

        final int MAX_BAR_LENGTH = 40;
        int unitsPerStar = (int) Math.ceil((double) maxValue / MAX_BAR_LENGTH);

        if (unitsPerStar < 1) {
            unitsPerStar = 1;
        }

        System.out.println("(Each * represents approximately " + unitsPerStar + " " + unitName + ")\n");

        int labelWidth = 10;

        for (int i = 0; i < rowCount; i++) {

            if (sortedLabels[i].length() > labelWidth) {
                labelWidth = sortedLabels[i].length();
            }
        }

        for (int i = 0; i < rowCount; i++) {

            int barLength = (int) Math.round((double) sortedValues[i] / unitsPerStar);

            StringBuilder bar = new StringBuilder();

            for (int k = 0; k < barLength; k++) {
                bar.append('*');
            }

            System.out.printf("%-" + labelWidth + "s | %6d | %s%n", sortedLabels[i], sortedValues[i], bar.toString());
        }

        // X-axis line beneath the bars, aligned to start where the bars
        // themselves start (after the label and value columns)
        int axisIndent = labelWidth + 3 + 6 + 3; // label col + " | " + value col + " | "

        StringBuilder axisPrefix = new StringBuilder();

        for (int k = 0; k < axisIndent; k++) {
            axisPrefix.append(' ');
        }

        StringBuilder axisDashes = new StringBuilder();

        for (int k = 0; k < MAX_BAR_LENGTH; k++) {
            axisDashes.append('-');
        }

        System.out.println(axisPrefix + "+" + axisDashes + "> " + xAxisLabel);

        if (n > maxRows) {
            System.out.println("\n(Showing top " + maxRows + " of " + n + " by value)");
        }
    }

    public static void main(String[] args) {

        LoyaltyUI ui = new LoyaltyUI();
        ui.startUI();
    }
}