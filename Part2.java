/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package part2;

import java.net.InetAddress;

/**
 *
 * @author Aigerim
 */

public class Part2 {
    
    public static void main(String[] args) throws Exception {
        // IP adress may vary, therefore use the current one after checking with ipconfig
        Node node = new Node(InetAddress.getByName("10.100.89.8"));
        node.readFromFile("input.txt");
        node.startRunning();
    }
}
