/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package part2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Айгерим
 */
public class Node {
    private static final int INF = Integer.MAX_VALUE;
    
    InetAddress myAddress;
    //saves distances of nodes
    private Map <InetAddress, Integer> distance;
    private Map <InetAddress, InetAddress> forwardTo;
    //saves list of neighbors
    private List <InetAddress> neighbors;
    //stores costs of neighbors
    private List <Integer> cost;
    private List <Boolean> dead;
    private Network network;
    
    private boolean changed;
    public Node() {
        this.myAddress = null;
        distance = new HashMap<>();
        forwardTo = new HashMap<>();
        dead = new ArrayList<>();
        neighbors  = new ArrayList<>();
        cost = new ArrayList<>();
        network = new Network();
        changed = true;
    }

    public Node(InetAddress myAddress) {
        this.myAddress = myAddress;
        distance = new HashMap<>();
        forwardTo = new HashMap<>();
        dead = new ArrayList<>();
        neighbors  = new ArrayList<>();
        cost = new ArrayList<>();
        
        distance.put(myAddress, 0);
        forwardTo.put(myAddress, myAddress);
        
        network = new Network();
        changed = true;
    }
    
    public void addNeighbor(InetAddress other, int costNeighbor){
        if (!neighbors.contains(other)){
            neighbors.add(other);
            cost.add(costNeighbor);
            dead.add(true);
        }
    }
    
    public void runDV() {
        changed = false;
        
        for (int i = 0; i < network.addrBuffer.size(); i++) {
            InetAddress neighborAddress = network.addrBuffer.get(i);
            List <InetAddress> addresses = network.totalAddrBuffer.get(i);
            List <Integer> costs = network.totalCostBuffer.get(i);
            
            int index = neighbors.indexOf(neighborAddress);
            
            if (index == -1) {
                continue;
            }

            int neighbCost = cost.get(index);
            for (int j = 0; j < addresses.size(); j++) {
                InetAddress addr = addresses.get(j);
                int addrCost = costs.get(j);
                
                if (addrCost != Integer.MAX_VALUE) {
                    addrCost += neighbCost;
                }
                
                if (distance.containsKey(addr)) {
                    if (distance.get(addr) > addrCost) {
                        distance.put(addr, addrCost);
                        forwardTo.put(addr, neighborAddress);
                        changed = true;
                    }
                } else {
                    distance.put(addr, addrCost);
                    forwardTo.put(addr, neighborAddress);
                    changed = true;
                }
            }
        }
        
        network.addrBuffer.clear();
        network.totalAddrBuffer.clear();
        network.totalCostBuffer.clear();
    }
    
    public void sendDV() {
        List <InetAddress> totalAddr = new ArrayList <>();
        List <Integer> totalCost = new ArrayList <>();
        
        for (Map.Entry <InetAddress, Integer> entry: distance.entrySet()) {
            totalAddr.add(entry.getKey());
            totalCost.add(entry.getValue());
        }
        
        for (int i = 0; i < neighbors.size(); i++) {
            InetAddress neighbor = neighbors.get(i);
            int neighbCost = cost.get(i);
            boolean neighbDead = dead.get(i);
            
            if (network.sendDistVector(myAddress, neighbor, totalAddr, totalCost)) {
                if (neighbDead) {
                    dead.set(i, false);
                    distance.put(neighbor, neighbCost);
                    forwardTo.put(neighbor, neighbor);
                    changed = true;
                }
            } else {
                if (!neighbDead) {
                    dead.set(i, true);
                    distance.put(neighbor, Integer.MAX_VALUE);
                    forwardTo.put(neighbor, myAddress);
                    changed = true;
                }
            }
        }
    }
    
    public void printTable() {
        if (changed) {
            changed = false;
            
            System.out.println("The list of the nodes:");
            for (InetAddress key: distance.keySet()) {
                System.out.print(key + " " );
            }
            System.out.println();
            System.out.println("Distances to nodes from Node list:");
            for (InetAddress key: distance.keySet()) {
                System.out.print(distance.get(key) + " " );
            }
            System.out.println();
            System.out.println("Forwarding table:");
            for (InetAddress key: distance.keySet()) {
                System.out.print(forwardTo.get(key) + " " );
            }
            System.out.println();
        }
    }
    
    public void startRunning() {
        Thread thread = new Thread(network);
        thread.start();
        while (true) {
            runDV();
            sendDV();
            printTable();
        }
    }
    
    public InetAddress getAddress() {
        return myAddress;
    }
    
    public void readFromFile (String fileName) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String line;
        while ((line = br.readLine()) != null) {
            String [] parts = line.split(" ");
            if (parts.length != 3) {
                throw new IOException("Wrong input format");
            } 
            InetAddress addr1 = InetAddress.getByName(parts[0]);
            InetAddress addr2 = InetAddress.getByName(parts[1]);
            
            int cost = Integer.parseInt(parts[2]);
            addNeighbor(addr2, cost);
        }
    }
}
