package entity;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

/**

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
   
    private String sourceDetail;
    private int pointsToCredit;
    private LocalDate dateQueued;


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