package entity;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Entity class representing a single reward-redemption event: which member
 * redeemed which reward, how many points it cost, and when. Kept as a
 * separate record (rather than only mutating Member.loyaltyPoints) so that
 * the Loyalty & Reward Service module can show a member's full redemption
 * history, not just their current point balance.
 *
 * Plain old Java object (POJO) - no input/output statements here, as per
 * assignment guidelines.
 *
 * @author: Kao Yong Feng
 */
public class RedemptionRecord {

    private static final AtomicInteger redemptionIDCounter = new AtomicInteger(1);

    private int redemptionID;
    private int memberID;
    private String memberName;
    private int rewardID;
    private String rewardName;
    private int pointsUsed;
    private LocalDate redeemedDate;

    public RedemptionRecord() {
    }

    public RedemptionRecord(int memberID, String memberName, int rewardID,
                             String rewardName, int pointsUsed, LocalDate redeemedDate) {
        this.redemptionID = redemptionIDCounter.getAndIncrement();
        this.memberID = memberID;
        this.memberName = memberName;
        this.rewardID = rewardID;
        this.rewardName = rewardName;
        this.pointsUsed = pointsUsed;
        this.redeemedDate = redeemedDate;
    }

    public int getRedemptionID() {
        return redemptionID;
    }

    public void setRedemptionID(int redemptionID) {
        this.redemptionID = redemptionID;
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

    public int getRewardID() {
        return rewardID;
    }

    public void setRewardID(int rewardID) {
        this.rewardID = rewardID;
    }

    public String getRewardName() {
        return rewardName;
    }

    public void setRewardName(String rewardName) {
        this.rewardName = rewardName;
    }

    public int getPointsUsed() {
        return pointsUsed;
    }

    public void setPointsUsed(int pointsUsed) {
        this.pointsUsed = pointsUsed;
    }

    public LocalDate getRedeemedDate() {
        return redeemedDate;
    }

    public void setRedeemedDate(LocalDate redeemedDate) {
        this.redeemedDate = redeemedDate;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RedemptionRecord)) {
            return false;
        }
        RedemptionRecord other = (RedemptionRecord) obj;
        return this.redemptionID == other.redemptionID;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(redemptionID);
    }

    @Override
    public String toString() {
        return "RedemptionRecord{" +
                "redemptionID=" + redemptionID +
                ", memberID=" + memberID +
                ", memberName='" + memberName + '\'' +
                ", rewardID=" + rewardID +
                ", rewardName='" + rewardName + '\'' +
                ", pointsUsed=" + pointsUsed +
                ", redeemedDate=" + redeemedDate +
                '}';
    }
}