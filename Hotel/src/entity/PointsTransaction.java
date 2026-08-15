package entity;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Entity class representing a single loyalty-points-earning transaction.
 * Each time a member earns points, a separate PointsTransaction is recorded
 * so that different batches of points (earned on different dates) can each
 * expire independently - a single Member.loyaltyPoints total alone cannot
 * represent this, since it does not track *when* each portion was earned.
 *
 * Plain old Java object (POJO) - no input/output statements here.
 *
 * @author (Loyalty & Reward Service module)
 */
public class PointsTransaction {

    private static final AtomicInteger transactionIDCounter = new AtomicInteger(1);

    private int transactionID;
    private int memberID;
    private String memberName;
    private int pointsEarned;
    private LocalDate earnedDate;
    private LocalDate expiryDate;

    public PointsTransaction() {
    }

    public PointsTransaction(int memberID, String memberName, int pointsEarned,
                              LocalDate earnedDate, LocalDate expiryDate) {
        this.transactionID = transactionIDCounter.getAndIncrement();
        this.memberID = memberID;
        this.memberName = memberName;
        this.pointsEarned = pointsEarned;
        this.earnedDate = earnedDate;
        this.expiryDate = expiryDate;
    }

    public int getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(int transactionID) {
        this.transactionID = transactionID;
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

    public int getPointsEarned() {
        return pointsEarned;
    }

    public void setPointsEarned(int pointsEarned) {
        this.pointsEarned = pointsEarned;
    }

    public LocalDate getEarnedDate() {
        return earnedDate;
    }

    public void setEarnedDate(LocalDate earnedDate) {
        this.earnedDate = earnedDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PointsTransaction)) {
            return false;
        }
        PointsTransaction other = (PointsTransaction) obj;
        return this.transactionID == other.transactionID;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(transactionID);
    }

    @Override
    public String toString() {
        return "PointsTransaction{" +
                "transactionID=" + transactionID +
                ", memberID=" + memberID +
                ", memberName='" + memberName + '\'' +
                ", pointsEarned=" + pointsEarned +
                ", earnedDate=" + earnedDate +
                ", expiryDate=" + expiryDate +
                '}';
    }
}