/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entity;

/**
 *
 * @author jlohz
 */
public class Member {

    private String memberID;
    private String memberName;
    private String phoneNumber;
    private String email;
    private String loyaltyTier;
    private int loyaltyPoints;

    public Member() {
    }

    public Member(String memberID, String memberName,
                  String phoneNumber, String email,
                  String loyaltyTier, int loyaltyPoints) {

        this.memberID = memberID;
        this.memberName = memberName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.loyaltyTier = loyaltyTier;
        this.loyaltyPoints = loyaltyPoints;
    }

    public String getMemberID() {
        return memberID;
    }

    public void setMemberID(String memberID) {
        this.memberID = memberID;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLoyaltyTier() {
        return loyaltyTier;
    }

    public void setLoyaltyTier(String loyaltyTier) {
        this.loyaltyTier = loyaltyTier;
    }

    public int getLoyaltyPoints() {
        return loyaltyPoints;
    }

    public void setLoyaltyPoints(int loyaltyPoints) {
        this.loyaltyPoints = loyaltyPoints;
    }

    @Override
    public String toString() {
        return "Member{" +
                "memberID='" + memberID + '\'' +
                ", memberName='" + memberName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", email='" + email + '\'' +
                ", loyaltyTier='" + loyaltyTier + '\'' +
                ", loyaltyPoints=" + loyaltyPoints +
                '}';
    }
}
