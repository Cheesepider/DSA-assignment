package utility;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Pure helper class for building the report header/footer banners used
 * across every report in the Loyalty & Reward Service module
 * (generateLoyaltyReport, generateTierDistributionReport,
 * generateExpiryAlertReport, generateAllTransactionsReport,
 * displayRewardCatalog).
 *
 * Before this class existed, each report method in LoyaltyControl
 * duplicated the same banner-building code (separator lines, program
 * title, timestamp formatting). This class centralizes that formatting
 * so every report stays visually consistent, and a change to the
 * banner style only needs to happen in one place.
 *
 * This is a plain formatting utility - it contains NO input statements
 * (no Scanner), NO business logic, and does NOT implement or replace
 * any Collection ADT. It only builds Strings that are handed to it,
 * same category as VirtualClock / ValidationUtility.
 * Allowed per Assignment Q&A 1.1.6: "Yes, you may use any Java
 * interfaces and classes that are not collections."
 *
 * @author: Kao Yong Feng
 */
public class ReportFormatUtility {

    private static final String LINE = "=".repeat(61);
    private static final String SUBLINE = "-".repeat(61);
    private static final DateTimeFormatter TIMESTAMP_FMT =
            DateTimeFormatter.ofPattern("EEEE, MMM dd yyyy, hh:mm a");

    private ReportFormatUtility() {
        // static utility class - no instances needed
    }

    // Builds the standard report banner, e.g.:
    // =============================================================
    //           TARUMT RESORTS - LOYALTY & REWARD PROGRAM
    //                MEMBER RANKING REPORT (BY POINTS)
    // -------------------------------------------------------------
    // Generated at: Tuesday, Aug 18 2026, 09:15 PM
    // =============================================================
    public static String buildHeader(String reportSubtitle, LocalDateTime generatedAt) {
        StringBuilder sb = new StringBuilder();
        sb.append(LINE).append("\n");
        sb.append("          TARUMT RESORTS - LOYALTY & REWARD PROGRAM\n");
        sb.append("        ").append(reportSubtitle).append("\n");
        sb.append(SUBLINE).append("\n");
        sb.append("Generated at: ").append(generatedAt.format(TIMESTAMP_FMT)).append("\n");
        sb.append(LINE).append("\n");
        return sb.toString();
    }

    // Builds the standard report footer, e.g.:
    // -------------------------------------------------------------
    // Total members displayed: 6
    // =============================================================
    public static String buildFooter(String totalLabel, int total) {
        StringBuilder sb = new StringBuilder();
        sb.append(SUBLINE).append("\n");
        sb.append(totalLabel).append(": ").append(total).append("\n");
        sb.append(LINE).append("\n");
        return sb.toString();
    }

    // Convenience overload for a footer that also needs to show an
    // "empty" message when total == 0 (used by the transaction reports)
    public static String buildFooter(String totalLabel, int total, String emptyMessage) {
        if (total == 0) {
            StringBuilder sb = new StringBuilder();
            sb.append(SUBLINE).append("\n");
            sb.append(emptyMessage).append("\n");
            sb.append(LINE).append("\n");
            return sb.toString();
        }
        return buildFooter(totalLabel, total);
    }

    // Returns the same separator line used in every report table
    // (exposed so control classes can reuse it between the column
    // header row and the data rows without hardcoding the string)
    public static String separatorLine() {
        return SUBLINE + "\n";
    }

    // Builds a simple horizontal ASCII bar chart, e.g.:
    // POINTS DISTRIBUTION (each * = 200 points)
    // -------------------------------------------------------------
    // Frank Ho     | ************************************** 6000
    // Eve Chua     | *********************** 3800
    // Bob Lim      | *** 500
    // =============================================================
    //
    // labels[i] and values[i] must correspond to the same row. The bar
    // length auto-scales so the largest value never exceeds a fixed max
    // width (40 chars), keeping the chart readable whether the values are
    // small (e.g. member counts) or large (e.g. points). Purely a
    // display/formatting helper - no business logic, no collection ADT
    // usage, same category as buildHeader()/buildFooter().
    public static String buildBarChart(String chartTitle, String[] labels, int[] values, String unitLabel) {
        if (labels == null || labels.length == 0) {
            return "";
        }

        int maxValue = 0;
        for (int value : values) {
            if (value > maxValue) {
                maxValue = value;
            }
        }

        final int MAX_BAR_WIDTH = 40; // widest bar allowed, in characters
        int scale = Math.max(1, (int) Math.ceil(maxValue / (double) MAX_BAR_WIDTH));

        int maxLabelWidth = 0;
        for (String label : labels) {
            maxLabelWidth = Math.max(maxLabelWidth, label.length());
        }

        StringBuilder sb = new StringBuilder();
        sb.append(chartTitle).append(" (each * = ").append(scale).append(" ").append(unitLabel).append(")\n");
        sb.append(SUBLINE).append("\n");
        for (int i = 0; i < labels.length; i++) {
            int barLength = Math.max(0, values[i]) / scale;
            sb.append(String.format("%-" + maxLabelWidth + "s | ", labels[i]));
            sb.append("*".repeat(barLength));
            sb.append(" ").append(values[i]).append("\n");
        }
        sb.append(LINE).append("\n");
        return sb.toString();
    }
}