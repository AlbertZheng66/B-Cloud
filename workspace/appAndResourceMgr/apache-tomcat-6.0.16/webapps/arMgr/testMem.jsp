<%
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
        sb.append("]");  
        out.println(sb.toString());
%>