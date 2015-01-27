/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xt.bcloud.pf;

/**
 *
 * @author Albert
 */
public class NewMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        StringBuilder sb = new StringBuilder("Memory:");  
  
        java.lang.management.MemoryMXBean mmbean = java.lang.management.ManagementFactory.getMemoryMXBean();  
        java.lang.management.MemoryUsage hmu = mmbean.getHeapMemoryUsage();  
        sb.append("[HeapMemoryUsage:");  
        sb.append(" Used=" + hmu.getUsed() / (1024*1024) + "M");  
        sb.append(" Committed=" + hmu.getCommitted() / (1024*1024)+ "M");  
        sb.append(" Max=" + hmu.getMax() / (1024*1024)+ "M");  
        sb.append(" Init=" + hmu.getInit()/ (1024*1024)+ "M");  
        sb.append("]");  
  
        java.lang.management.MemoryUsage nhmu = mmbean.getNonHeapMemoryUsage();  
        sb.append("[NonHeapMemoryUsage:");  
        sb.append(" Used=" + nhmu.getUsed() / (1024*1024)+ "M");  
        sb.append(" Committed=" + nhmu.getCommitted() / (1024*1024)+ "M");  
        sb.append(" Max=" + nhmu.getMax() / (1024*1024)+ "M");  
        sb.append("]");  
        System.out.println(sb.toString());
        
    }
}
