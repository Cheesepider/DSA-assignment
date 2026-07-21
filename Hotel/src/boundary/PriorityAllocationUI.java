/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package boundary;

/**
 *
 * @author jlohz
 */
public class PriorityAllocationUI {
    public void displayMenu() {

        System.out.println("\n===============================================");
        System.out.println(" VIP & Loyalty Tier Room Allocation");
        System.out.println("===============================================");
        System.out.println("1. Add Booking Request");
        System.out.println("2. Allocate Next Room");
        System.out.println("3. Display Waiting List");
        System.out.println("4. Search Booking");
        System.out.println("5. Generate Allocation Report");
        System.out.println("0. Exit");
        System.out.println("===============================================");
    }
    public static void main(String[] args) {
            
            PriorityAllocationUI ui = new PriorityAllocationUI();
            ui.displayMenu();
    }
}


