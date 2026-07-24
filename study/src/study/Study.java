/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package study;

/**
 *
 * @author jlohz
 */
public class Study {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        long firstOperand = 7562;
        long secondOperand = 20;
        long product = 0;
            for(long ctr = secondOperand; ctr > 0; ctr--)
                product = product + firstOperand;
        System.out.println(product);
    }
    
}
