/*
 * @author <Your Name>
 */
package entity;

public class Reward {

    private String rewardID;
    private String rewardName;
    private String category;
    private int pointsRequired;
    private String description;

    public Reward() {
    }

    public Reward(String rewardID, String rewardName, String category,
            int pointsRequired, String description) {

        this.rewardID = rewardID;
        this.rewardName = rewardName;
        this.category = category;
        this.pointsRequired = pointsRequired;
        this.description = description;
    }

    public String getRewardID() {
        return rewardID;
    }

    public void setRewardID(String rewardID) {
        this.rewardID = rewardID;
    }

    public String getRewardName() {
        return rewardName;
    }

    public void setRewardName(String rewardName) {
        this.rewardName = rewardName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getPointsRequired() {
        return pointsRequired;
    }

    public void setPointsRequired(int pointsRequired) {
        this.pointsRequired = pointsRequired;
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
        Reward other = (Reward) obj;
        return rewardID.equals(other.rewardID);
    }

    @Override
    public String toString() {
        return "Reward{"
                + "rewardID='" + rewardID + '\''
                + ", rewardName='" + rewardName + '\''
                + ", category='" + category + '\''
                + ", pointsRequired=" + pointsRequired
                + ", description='" + description + '\''
                + '}';
    }
}
