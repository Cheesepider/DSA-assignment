package entity;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Entity class representing a reward that a member can redeem using loyalty
 * points. Plain old Java object (POJO) - no input/output statements here,
 * as per assignment guidelines.
 *
 * @author: Kao Yong FEng
 */
public class RewardItem {

    private static final AtomicInteger rewardIDCounter = new AtomicInteger(1);

    private int rewardID;
    private String rewardName;
    private String description;
    private int pointsRequired;

    public RewardItem() {
    }

    public RewardItem(String rewardName, String description, int pointsRequired) {
        this.rewardID = rewardIDCounter.getAndIncrement();
        this.rewardName = rewardName;
        this.description = description;
        this.pointsRequired = pointsRequired;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPointsRequired() {
        return pointsRequired;
    }

    public void setPointsRequired(int pointsRequired) {
        this.pointsRequired = pointsRequired;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RewardItem)) {
            return false;
        }
        RewardItem other = (RewardItem) obj;
        return this.rewardID == other.rewardID;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(rewardID);
    }

    @Override
    public String toString() {
        return "RewardItem{" +
                "rewardID=" + rewardID +
                ", rewardName='" + rewardName + '\'' +
                ", description='" + description + '\'' +
                ", pointsRequired=" + pointsRequired +
                '}';
    }
}