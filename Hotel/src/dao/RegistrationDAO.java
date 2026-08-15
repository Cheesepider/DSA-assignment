/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import entity.Member;
import entity.Member.LoyaltyTier;
import entity.Room;
import entity.Room.RoomStatus;
import entity.Room.RoomType;
import main.App;

public class RegistrationDAO {

    // Data initialization happens here.
    // Populate App's global lists with seed data for demo/testing.

    public static void initializeData() {
        initializeMemberData();
        initializeRoomData();
    }

    // -------------------------
    // ROOMS
    // -------------------------
    public static void initializeRoomData() {
        // Floor 1 - Single rooms (base rate $100/night)
        App.roomList.add(new Room("101", RoomType.SINGLE, RoomStatus.Ready_for_Check_In));
        App.roomList.add(new Room("102", RoomType.SINGLE, RoomStatus.Ready_for_Check_In));
        App.roomList.add(new Room("103", RoomType.SINGLE, RoomStatus.Dirty));
        App.roomList.add(new Room("104", RoomType.SINGLE, RoomStatus.Inspected));

        // Floor 2 - Double rooms (base rate $150/night)
        App.roomList.add(new Room("201", RoomType.DOUBLE, RoomStatus.Ready_for_Check_In));
        App.roomList.add(new Room("202", RoomType.DOUBLE, RoomStatus.Ready_for_Check_In));
        App.roomList.add(new Room("203", RoomType.DOUBLE, RoomStatus.Cleaning_In_Progress));

        // Floor 3 - Suites (base rate $300/night)
        App.roomList.add(new Room("301", RoomType.SUITE, RoomStatus.Ready_for_Check_In));
        App.roomList.add(new Room("302", RoomType.SUITE, RoomStatus.Inspected));

        System.out.println("[DAO] " + App.roomList.getNumberOfEntries() + " rooms loaded.");
    }

    // -------------------------
    // MEMBERS
    // -------------------------
    public static void initializeMemberData() {
        // Regular tier members
        App.memberList.add(new Member("Alice Tan",   "91234567", "alice@mail.com",   LoyaltyTier.Regular,  0));
        App.memberList.add(new Member("Bob Lim",     "87654321", "bob@mail.com",     LoyaltyTier.Regular,  50));

        // Platinum tier members
        App.memberList.add(new Member("Carol Wong",  "93456789", "carol@mail.com",   LoyaltyTier.Platinum, 200));
        App.memberList.add(new Member("David Ng",    "81234567", "david@mail.com",   LoyaltyTier.Platinum, 350));

        // Diamond tier members
        App.memberList.add(new Member("Eve Chua",    "99887766", "eve@mail.com",     LoyaltyTier.Diamond,  800));

        // Elite tier member
        App.memberList.add(new Member("Frank Ho",    "98765432", "frank@mail.com",   LoyaltyTier.Elite,    1500));

        System.out.println("[DAO] " + App.memberList.getNumberOfEntries() + " members loaded.");
    }
}