/*
 * @author <Your Name>
 */
package entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class PointsTransaction {

    private String transactionID;
    private Member member;
    private String transactionType; // "EARN" or "REDEEM"
    private int points;
    private LocalDateTime transactionDate;
    private LocalDate expiryDate; // only applicable to "EARN" transactions
    private String description;

    public PointsTransaction() {
    }

    public PointsTransaction(String transactionID, Member member,
            String transactionType, int points, LocalDateTime transactionDate,
            LocalDate expiryDate, String description) {

        this.transactionID = transactionID;
        this.member = member;
        this.transactionType = transactionType;
        this.points = points;
        this.transactionDate = transactionDate;
        this.expiryDate = expiryDate;
        this.description = description;
    }

    public String getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(String transactionID) {
        this.transactionID = transactionID;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        PointsTransaction other = (PointsTransaction) obj;
        return transactionID.equals(other.transactionID);
    }

    @Override
    public String toString() {
        return "PointsTransaction{"
                + "transactionID='" + transactionID + '\''
                + ", member=" + (member == null ? "null" : member.getMemberID())
                + ", transactionType='" + transactionType + '\''
                + ", points=" + points
                + ", transactionDate=" + transactionDate
                + ", expiryDate=" + expiryDate
                + ", description='" + description + '\''
                + '}';
    }
}
