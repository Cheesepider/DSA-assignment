package entity;

/**
 
 * @author : Kao Yong Feng
 */
public class LifetimeEarnedPoints {

    private int memberID;
    private int totalEarned;

  
    public LifetimeEarnedPoints() {
    }

    public LifetimeEarnedPoints(int memberID, int totalEarned) {
        this.memberID = memberID;
        this.totalEarned = totalEarned;
    }

    public int getMemberID() {
        return memberID;
    }

    public void setMemberID(int memberID) {
        this.memberID = memberID;
    }

    public int getTotalEarned() {
        return totalEarned;
    }

    public void setTotalEarned(int totalEarned) {
        this.totalEarned = totalEarned;
    }

    public void addTotalEarned(int pointsToAdd) {
        this.totalEarned += pointsToAdd;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof LifetimeEarnedPoints)) {
            return false;
        }
        LifetimeEarnedPoints other = (LifetimeEarnedPoints) obj;
        return this.memberID == other.memberID;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(memberID);
    }

    @Override
    public String toString() {
        return "LifetimeEarnedPoints{" +
                "memberID=" + memberID +
                ", totalEarned=" + totalEarned +
                '}';
    }
}