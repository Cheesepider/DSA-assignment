/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entity;

import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author jlohz
 */
public class Member {
    private static final AtomicInteger memberIDCounter = new AtomicInteger(1);

    public enum LoyaltyTier {
        Regular,        // default
        Platinum,       
        Diamond,
        Elite           // highest tier
    }

    private int memberID;
    private String memberName;
    private String phoneNumber;
    private String email; 
    private LoyaltyTier loyaltyTier; // vip status thing
    private int loyaltyPoints;      // points accumulated for loyalty program 

    public Member() {
    }

    // new constructor for default loyalty tier and points
    public Member(String memberName, String phoneNumber, String email) {
        this.memberID = memberIDCounter.getAndIncrement();
        this.memberName = memberName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.loyaltyTier = LoyaltyTier.Regular; // default loyalty tier
        this.loyaltyPoints = 0; // Default points
    }

    // modified full constructor to include memberID assignment and enum for loyalty tier
    public Member(String memberName,
                  String phoneNumber, String email,
                  LoyaltyTier loyaltyTier, int loyaltyPoints) {

        this.memberID = memberIDCounter.getAndIncrement();
        this.memberName = memberName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.loyaltyTier = loyaltyTier;
        this.loyaltyPoints = loyaltyPoints;
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

    public LoyaltyTier getLoyaltyTier() {
        return loyaltyTier;
    }

    public void setLoyaltyTier(LoyaltyTier loyaltyTier) {
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
