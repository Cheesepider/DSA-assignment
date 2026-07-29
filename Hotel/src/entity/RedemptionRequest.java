/*
 * @author <Your Name>
 */
package entity;

import java.time.LocalDateTime;

public class RedemptionRequest {

    private String requestID;
    private Member member;
    private Reward reward;
    private LocalDateTime requestDate;
    private String status; // "PENDING", "COMPLETED", "REJECTED"

    public RedemptionRequest() {
    }

    public RedemptionRequest(String requestID, Member member, Reward reward,
            LocalDateTime requestDate, String status) {

        this.requestID = requestID;
        this.member = member;
        this.reward = reward;
        this.requestDate = requestDate;
        this.status = status;
    }

    public String getRequestID() {
        return requestID;
    }

    public void setRequestID(String requestID) {
        this.requestID = requestID;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public Reward getReward() {
        return reward;
    }

    public void setReward(Reward reward) {
        this.reward = reward;
    }

    public LocalDateTime getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDateTime requestDate) {
        this.requestDate = requestDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        RedemptionRequest other = (RedemptionRequest) obj;
        return requestID.equals(other.requestID);
    }

    @Override
    public String toString() {
        return "RedemptionRequest{"
                + "requestID='" + requestID + '\''
                + ", member=" + (member == null ? "null" : member.getMemberID())
                + ", reward=" + (reward == null ? "null" : reward.getRewardID())
                + ", requestDate=" + requestDate
                + ", status='" + status + '\''
                + '}';
    }
}
