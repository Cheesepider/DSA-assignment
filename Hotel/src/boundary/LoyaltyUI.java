package boundary;

import java.util.Scanner;

import adt.ListInterface;
import control.LoyaltyControl;
import entity.Member;
import entity.Member.LoyaltyTier;
import utility.ValidationUtility;

/**
 
 * @author : Kao Yong Feng
 */
public class LoyaltyUI {

    private LoyaltyControl loyaltyControl;
    private Scanner scanner = new Scanner(System.in);

    public LoyaltyUI() {
        // standalone mode: uses this module's own hardcoded member data
        loyaltyControl = new LoyaltyControl();
    }

    public LoyaltyUI(ListInterface<Member> sharedMemberList) {
        // integrated mode: wired to the application-wide shared memberList
        loyaltyControl = new LoyaltyControl(sharedMemberList);
    }

    public void displayMenu() {
        int choice;

        // one-time notification banner shown when the module is opened
        int expiringCount = loyaltyControl.getExpiringTransactionCount(LoyaltyControl.DEFAULT_EXPIRY_ALERT_DAYS);
        if (expiringCount > 0) {
            System.out.println("\n\u26A0 ALERT: " + expiringCount +
                    " points transaction(s) are expiring within " +
                    LoyaltyControl.DEFAULT_EXPIRY_ALERT_DAYS + " days! See option 8 for details.");
        }

        do {
            System.out.println("\n===============================================");
            System.out.println("      LOYALTY & REWARD SERVICE MODULE");
            System.out.println("===============================================");
            System.out.println("1. Earn Loyalty Points");
            System.out.println("2. Redeem Reward");
            System.out.println("3. Search Member");
            System.out.println("4. View Reward Catalog");
            System.out.println("5. Manage Reward Catalog (Add / Update / Delete)");
            System.out.println("6. Generate Loyalty Report (Ranked by Points)");
            System.out.println("7. Generate Tier Distribution Report");
            System.out.println("8. View Points Transactions (Alerts / Full History)");
            System.out.println("9. Generate VIP Eligibility & Reward Readiness Report");
            System.out.println("0. Exit Loyalty Module");
            System.out.println("===============================================");
            System.out.print("Please select an option: ");

            choice = ValidationUtility.inputChoice(scanner);

            switch (choice) {
                case 1:
                    earnPointsUI();
                    break;
                case 2:
                    redeemRewardUI();
                    break;
                case 3:
                    searchMemberUI();
                    break;
                case 4:
                    System.out.println(loyaltyControl.displayRewardCatalog());
                    break;
                case 5:
                    manageRewardCatalogUI();
                    break;
                case 6:
                    System.out.println(loyaltyControl.generateLoyaltyReport());
                    break;
                case 7:
                    System.out.println(loyaltyControl.generateTierDistributionReport());
                    break;
                case 8:
                    viewTransactionsUI();
                    break;
                case 9:
                    vipEligibilityReportUI();
                    break;
                case 0:
                    System.out.println("Exiting Loyalty & Reward Module...");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        } while (choice != 0);
    }

    private void earnPointsUI() {
        System.out.print("Enter Member ID: ");
        int memberID = ValidationUtility.inputChoice(scanner);
        System.out.print("Enter points earned this stay: ");
        int points = ValidationUtility.inputChoice(scanner);
        System.out.println(loyaltyControl.earnPoints(memberID, points));
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
                System.out.println(m != null ? m : "Member not found.");
                break;
            case 2:
                System.out.print("Enter name keyword: ");
                String name = scanner.nextLine();
                System.out.println(loyaltyControl.formatMemberList(loyaltyControl.searchMemberByName(name)));
                break;
            case 3:
                System.out.print("Enter tier (Regular / Platinum / Diamond / Elite): ");
                String tierInput = scanner.nextLine();
                try {
                    LoyaltyTier tier = LoyaltyTier.valueOf(tierInput.trim());
                    System.out.println(loyaltyControl.formatMemberList(loyaltyControl.searchMemberByTier(tier)));
                } catch (IllegalArgumentException e) {
                    System.out.println("Invalid tier entered.");
                }
                break;
            default:
                System.out.println("Invalid option.");
        }
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

    private void vipEligibilityReportUI() {
        System.out.print("Minimum tier to include (Regular / Platinum / Diamond / Elite): ");
        String tierInput = scanner.nextLine();
        LoyaltyTier minTier;
        try {
            minTier = LoyaltyTier.valueOf(tierInput.trim());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid tier entered.");
            return;
        }
        System.out.print("Minimum points required: ");
        int minPoints = ValidationUtility.inputChoice(scanner);
        System.out.println(loyaltyControl.displayRewardCatalog());
        System.out.print("Enter Reward ID to check redemption readiness for: ");
        int rewardID = ValidationUtility.inputChoice(scanner);
        System.out.println(loyaltyControl.generateVIPEligibilityReport(minTier, minPoints, rewardID));
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
                System.out.print("Enter description: ");
                String description = scanner.nextLine();
                System.out.print("Enter points required: ");
                int points = ValidationUtility.inputChoice(scanner);
                System.out.println(loyaltyControl.addRewardItem(name, description, points));
                break;
            case 2:
                System.out.print("Enter Reward ID to update: ");
                int updateID = ValidationUtility.inputChoice(scanner);
                System.out.print("Enter new reward name: ");
                String newName = scanner.nextLine();
                System.out.print("Enter new description: ");
                String newDescription = scanner.nextLine();
                System.out.print("Enter new points required: ");
                int newPoints = ValidationUtility.inputChoice(scanner);
                System.out.println(loyaltyControl.updateRewardItem(updateID, newName, newDescription, newPoints));
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

    public static void main(String[] args) {
        LoyaltyUI ui = new LoyaltyUI();
        ui.displayMenu();
    }
}