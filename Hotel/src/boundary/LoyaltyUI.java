package boundary;

import java.util.Scanner;

import adt.ListInterface;
import control.LoyaltyControl;
import entity.Member;
import entity.Member.LoyaltyTier;
import utility.ValidationUtility;
import utility.VirtualClock;

/**
 
 * @author : Kao Yong Feng
 */

public class LoyaltyUI {

    private LoyaltyControl loyaltyControl;
    private Scanner scanner = new Scanner(System.in);

    public LoyaltyUI() {
        loyaltyControl = new LoyaltyControl();
    }

    public LoyaltyUI(ListInterface<Member> sharedMemberList) {
        // integrated mode: wired to the application-wide shared memberList
        loyaltyControl = new LoyaltyControl(sharedMemberList);
    }

    public void displayMenu() {
        int choice;

        // scan for stays that finished checkout since this module was last
        // opened, and QUEUE their loyalty points for accumulation (they are
        // NOT credited yet - see LoyaltyControl.queueCompletedStayPoints()
        // for why points are queued instead of credited automatically).
        String newlyQueuedSummary = LoyaltyControl.queueCompletedStayPoints();
        if (!newlyQueuedSummary.isEmpty()) {
            System.out.println("\n--- LOYALTY PROGRAM: NEW STAYS QUEUED FOR POINTS ACCUMULATION ---");
            System.out.println(newlyQueuedSummary);
        }

        // forfeit any points whose expiryDate has already passed (relative
        // to the current VirtualClock date) since this module was last
        // opened - see LoyaltyControl.expireOverduePoints() for why this
        // is done eagerly instead of only being reported as an alert.
        checkPointsExpiry();

        // one-time notification banners shown when the module is opened
        int expiringCount = loyaltyControl.getExpiringTransactionCount(LoyaltyControl.DEFAULT_EXPIRY_ALERT_DAYS);
        if (expiringCount > 0) {
            System.out.println("\n\u26A0 ALERT: " + expiringCount +
                    " points transaction(s) are expiring within " +
                    LoyaltyControl.DEFAULT_EXPIRY_ALERT_DAYS + " days! See option 7 for details.");
        }

        int pendingCount = loyaltyControl.getPendingPointsQueueCount();
        if (pendingCount > 0) {
            System.out.println("\n\u26A0 ALERT: " + pendingCount +
                    " points credit(s) are waiting to be processed! See option 5 to review and credit them.");
        }

        do {
            System.out.println("\n===============================================");
            System.out.println("      LOYALTY & REWARD SERVICE MODULE");
            System.out.println("  " + VirtualClock.getInstance().toString());
            System.out.println("===============================================");
            System.out.println("(Completed stays are QUEUED for points accumulation");
            System.out.println(" whenever this module is opened - use option 5 to");
            System.out.println(" review and credit them to members)");
            System.out.println("-----------------------------------------------");
            System.out.println("1. Redeem Reward");
            System.out.println("2. Search Member");
            System.out.println("3. View Reward Catalog");
            System.out.println("4. Manage Reward Catalog (Add / Update / Delete)");
            System.out.println("5. Points Accumulation Queue (View / Process / Reject / Grant Promo)");
            System.out.println("6. Generate Loyalty Report (Ranked by Points, Search & Sort)");
            System.out.println("7. View Points Transactions (Alerts / Full History)");
            System.out.println("8. View Redemption History (Search & Sort, Table & Chart)");
            System.out.println("9. Exit Loyalty Module");
            System.out.println("===============================================");
            System.out.println("0. Advance Time");
            System.out.println("===============================================");
            System.out.print("Please select an option: ");

            choice = ValidationUtility.inputChoice(scanner);

            switch (choice) {
                case 1:
                    redeemRewardUI();
                    break;
                case 2:
                    searchMemberUI();
                    break;
                case 3:
                    System.out.println(loyaltyControl.displayRewardCatalog());
                    break;
                case 4:
                    manageRewardCatalogUI();
                    break;
                case 5:
                    pointsAccumulationQueueUI();
                    break;
                case 6:
                    generateLoyaltyReportUI();
                    break;
                case 7:
                    viewTransactionsUI();
                    break;
                case 8:
                    viewRedemptionsUI();
                    break;
                case 9:
                    System.out.println("Exiting Loyalty & Reward Module...");
                    break;
                case 0:
                    TimeProgressionUI.showTimeMenu();
                    // time may have moved past one or more members' points
                    // expiryDate - re-check immediately rather than waiting
                    // for the module to be reopened
                    checkPointsExpiry();
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        } while (choice != 9);
    }

    // scans for and forfeits any overdue points, printing a banner if
    // anything actually expired. Shared by the module-open check and the
    // post-Advance-Time check so both paths report expiries the same way.
    private void checkPointsExpiry() {
        String expiredSummary = LoyaltyControl.expireOverduePoints();
        if (!expiredSummary.isEmpty()) {
            System.out.println("\n--- LOYALTY PROGRAM: POINTS EXPIRED ---");
            System.out.println(expiredSummary);
        }
    }

    private void redeemRewardUI() {
        System.out.print("Enter Member ID: ");
        int memberID = ValidationUtility.inputChoice(scanner);
        System.out.println(loyaltyControl.displayRewardCatalog());
        System.out.print("Enter Reward ID to redeem: ");
        int rewardID = ValidationUtility.inputChoice(scanner);
        System.out.println(loyaltyControl.redeemReward(memberID, rewardID));
    }

    private void searchMemberUI() {
        System.out.println("Search by: 1. Member ID   2. Name   3. Loyalty Tier");
        System.out.print("Please select an option: ");
        int option = ValidationUtility.inputChoice(scanner);

        switch (option) {
            case 1:
                System.out.print("Enter Member ID: ");
                int id = ValidationUtility.inputChoice(scanner);
                Member m = loyaltyControl.searchMemberByID(id);
                if (m != null) {
                    System.out.println(m);
                    System.out.println("Lifetime Points Earned: " + loyaltyControl.getLifetimeEarnedPoints(id) +
                            " (this determines Tier; current balance above is what's left to spend)");
                } else {
                    System.out.println("Member not found.");
                }
                break;
            case 2:
                System.out.print("Enter name keyword: ");
                String name = scanner.nextLine();
                printMemberList(loyaltyControl.searchMemberByName(name));
                break;
            case 3:
                System.out.print("Enter tier (Regular / Platinum / Diamond / Elite): ");
                String tierInput = scanner.nextLine();
                try {
                    LoyaltyTier tier = LoyaltyTier.valueOf(tierInput.trim());
                    printMemberList(loyaltyControl.searchMemberByTier(tier));
                } catch (IllegalArgumentException e) {
                    System.out.println("Invalid tier entered.");
                }
                break;
            default:
                System.out.println("Invalid option.");
        }
    }

    // =========================================================
    // Points Accumulation Queue submenu
    // -----------------------------------------------------------
    // Everything that adds points to a member - a completed stay OR a
    // staff-granted personalized promotion - lands in this queue first.
    // Nothing is credited to a member's balance/tier until staff explicitly
    // processes (or rejects) it here.
    // =========================================================
    private void pointsAccumulationQueueUI() {
        int option;
        do {
            System.out.println("\n--- Points Accumulation Queue ---");
            System.out.println("1. View Pending Queue");
            System.out.println("2. Process Next Pending Credit (oldest first)");
            System.out.println("3. Process ALL Pending Credits");
            System.out.println("4. Reject a Pending Credit");
            System.out.println("5. Grant Personalized Promotional Points");
            System.out.println("0. Back");
            System.out.print("Please select an option: ");
            option = ValidationUtility.inputChoice(scanner);

            switch (option) {
                case 1:
                    System.out.println(loyaltyControl.viewPendingPointsQueue());
                    break;
                case 2:
                    System.out.println(loyaltyControl.processNextPendingPointsCredit());
                    break;
                case 3:
                    System.out.println(loyaltyControl.processAllPendingPointsCredits());
                    break;
                case 4:
                    System.out.println(loyaltyControl.viewPendingPointsQueue());
                    System.out.print("Enter Credit ID to reject: ");
                    int creditID = ValidationUtility.inputChoice(scanner);
                    System.out.println(loyaltyControl.rejectPendingPointsCredit(creditID));
                    break;
                case 5:
                    grantPromotionalPointsUI();
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        } while (option != 0);
    }

    private void grantPromotionalPointsUI() {
        System.out.print("Enter Member ID: ");
        int memberID = ValidationUtility.inputChoice(scanner);
        System.out.print("Enter promotional points to grant: ");
        int points = ValidationUtility.inputChoice(scanner);
        System.out.print("Enter reason for this promotion (e.g. Birthday Promotion): ");
        String reason = scanner.nextLine();
        System.out.println(loyaltyControl.grantPromotionalPoints(memberID, points, reason));
    }

    // =========================================================
    // Generate Loyalty Report (merges the old "Ranked by Points" report
    // and the old "Tier Distribution" report into one searchable,
    // sortable report with a table + bar charts - see
    // LoyaltyControl.generateLoyaltyReport(String, int))
    // =========================================================
    private void generateLoyaltyReportUI() {
        System.out.println("\n--- Generate Loyalty Report (Ranked by Points) ---");
        System.out.print("Search by Member ID / Name / Tier (leave blank for all members): ");
        String keyword = scanner.nextLine();

        System.out.println("Sort by: 1. Points (High-Low)   2. Name (A-Z)   3. Tier (High-Low)   4. Member ID");
        System.out.print("Please select an option (default 1): ");
        int sortOption = readSortChoice();

        System.out.println(loyaltyControl.generateLoyaltyReport(keyword, sortOption));
    }

    private void viewTransactionsUI() {
        System.out.println("1. Points Expiry Alerts (Next " + LoyaltyControl.DEFAULT_EXPIRY_ALERT_DAYS + " Days)");
        System.out.println("2. View All Points Transactions (Full History)");
        System.out.print("Please select an option: ");
        int option = ValidationUtility.inputChoice(scanner);

        switch (option) {
            case 1:
                System.out.println(loyaltyControl.generateExpiryAlertReport(LoyaltyControl.DEFAULT_EXPIRY_ALERT_DAYS));
                break;
            case 2:
                System.out.println(loyaltyControl.generateAllTransactionsReport());
                break;
            default:
                System.out.println("Invalid option.");
        }
    }

    // =========================================================
    // View Redemption History (now a single searchable, sortable report
    // with a table + bar chart, replacing the old "by member" / "full
    // history" submenu - see LoyaltyControl.generateRedemptionReport(String, int))
    // =========================================================
    private void viewRedemptionsUI() {
        System.out.println("\n--- View Redemption History ---");
        System.out.print("Search by Member ID / Member Name / Reward Name (leave blank for all): ");
        String keyword = scanner.nextLine();

        System.out.println("Sort by: 1. Date (Newest First)   2. Points Used (High-Low)   " +
                "3. Member Name (A-Z)   4. Reward Name (A-Z)");
        System.out.print("Please select an option (default 1): ");
        int sortOption = readSortChoice();

        System.out.println(loyaltyControl.generateRedemptionReport(keyword, sortOption));
    }

    // shared helper: reads a sort-option number, defaulting to 1 (blank /
    // non-numeric input) rather than rejecting the report request outright
    private int readSortChoice() {
        String input = scanner.nextLine();
        try {
            return Integer.parseInt(input.trim());
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private void manageRewardCatalogUI() {
        System.out.println(loyaltyControl.displayRewardCatalog());
        System.out.println("1. Add New Reward");
        System.out.println("2. Update Existing Reward");
        System.out.println("3. Delete Reward");
        System.out.print("Please select an option: ");
        int option = ValidationUtility.inputChoice(scanner);

        switch (option) {
            case 1:
                System.out.print("Enter reward name: ");
                String name = scanner.nextLine();
                System.out.print("Enter points required: ");
                int points = ValidationUtility.inputChoice(scanner);
                System.out.println(loyaltyControl.addRewardItem(name, "", points));
                break;
            case 2:
                System.out.print("Enter Reward ID to update: ");
                int updateID = ValidationUtility.inputChoice(scanner);
                System.out.print("Enter new reward name: ");
                String newName = scanner.nextLine();
                System.out.print("Enter new points required: ");
                int newPoints = ValidationUtility.inputChoice(scanner);
                System.out.println(loyaltyControl.updateRewardItem(updateID, newName, newPoints));
                break;
            case 3:
                System.out.print("Enter Reward ID to delete: ");
                int deleteID = ValidationUtility.inputChoice(scanner);
                System.out.println(loyaltyControl.deleteRewardItem(deleteID));
                break;
            default:
                System.out.println("Invalid option.");
        }
    }

    private void printMemberList(ListInterface<Member> list) {
        if (list.isEmpty()) {
            System.out.println("No matching members found.");
            return;
        }
        for (int i = 1; i <= list.getNumberOfEntries(); i++) {
            Member m = list.getEntry(i);
            System.out.println(m);
            System.out.println("   Lifetime Points Earned: " + loyaltyControl.getLifetimeEarnedPoints(m.getMemberID()));
        }
    }

    public static void main(String[] args) {
        LoyaltyUI ui = new LoyaltyUI();
        ui.displayMenu();
    }
}