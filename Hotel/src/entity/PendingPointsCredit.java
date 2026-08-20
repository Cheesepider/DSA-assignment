package entity;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Entity class representing a single loyalty-points credit that has been
 * earned/granted but NOT yet applied to the member's balance.
 *
 * Why this exists: previously, points from a completed/paid stay were
 * credited to the member automatically and silently the moment the Loyalty
 * module was reopened. That gave staff no visibility or control over when
 * accumulation actually happens. Now, every source of points - a completed
 * stay OR a staff-granted personalized promotion - first lands here, in a
 * pending queue (see LoyaltyControl.pendingPointsQueue). Staff must
 * explicitly process (or reject) each entry before it affects the member's
 * balance or tier. This makes "points accumulation" its own auditable use
 * case instead of a hidden side effect of opening a menu.
 *
 * Plain old Java object (POJO) - no input/output statements here, as per
 * assignment guidelines.
 *
 * @author : Kao Yong Feng
 */
public class PendingPointsCredit {

    private static final AtomicInteger creditIDCounter = new AtomicInteger(1);

    public enum CreditSource {
        STAY,       // earned from a completed, paid hotel stay
        PROMOTION   // granted by staff as a personalized promotion / goodwill gesture
    }

    private int creditID;
    private int memberID;
    private String memberName;
    private CreditSource source;
    // human-readable justification: e.g. "Booking #12 ($350.00 spent)" for a
    // STAY credit, or the staff-entered reason for a PROMOTION credit.
    // Keeps every pending/queued credit auditable back to why it exists.
    private String sourceDetail;
    private int pointsToCredit;
    private LocalDate dateQueued;

    // no-arg constructor deliberately does NOT touch creditIDCounter - it is
    // only ever used to build a "probe" object for ListInterface.indexOf()
    // lookups (same pattern as RewardItem's no-arg constructor), never to
    // create a real queue entry.
    public PendingPointsCredit() {
    }

    public PendingPointsCredit(int memberID, String memberName, CreditSource source,
                                String sourceDetail, int pointsToCredit, LocalDate dateQueued) {
        this.creditID = creditIDCounter.getAndIncrement();
        this.memberID = memberID;
        this.memberName = memberName;
        this.source = source;
        this.sourceDetail = sourceDetail;
        this.pointsToCredit = pointsToCredit;
        this.dateQueued = dateQueued;
    }

    public int getCreditID() {
        return creditID;
    }

    public void setCreditID(int creditID) {
        this.creditID = creditID;
    }

    public int getMemberID() {
        return memberID;
    }

    public void setMemberID(int memberID) {
        this.memberID = memberID;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public CreditSource getSource() {
        return source;
    }

    public void setSource(CreditSource source) {
        this.source = source;
    }

    public String getSourceDetail() {
        return sourceDetail;
    }

    public void setSourceDetail(String sourceDetail) {
        this.sourceDetail = sourceDetail;
    }

    public int getPointsToCredit() {
        return pointsToCredit;
    }

    public void setPointsToCredit(int pointsToCredit) {
        this.pointsToCredit = pointsToCredit;
    }

    public LocalDate getDateQueued() {
        return dateQueued;
    }

    public void setDateQueued(LocalDate dateQueued) {
        this.dateQueued = dateQueued;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PendingPointsCredit)) {
            return false;
        }
        PendingPointsCredit other = (PendingPointsCredit) obj;
        return this.creditID == other.creditID;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(creditID);
    }

    @Override
    public String toString() {
        return "PendingPointsCredit{" +
                "creditID=" + creditID +
                ", memberID=" + memberID +
                ", memberName='" + memberName + '\'' +
                ", source=" + source +
                ", sourceDetail='" + sourceDetail + '\'' +
                ", pointsToCredit=" + pointsToCredit +
                ", dateQueued=" + dateQueued +
                '}';
    }
}