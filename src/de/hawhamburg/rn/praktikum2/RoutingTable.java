package de.hawhamburg.rn.praktikum2;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RoutingTable {

  private final List<TableEntry> table = new ArrayList<>();
  //protected List<Inet4Address> neighbors = new ArrayList<>();

  public RoutingTable(Inet4Address myIP) {
    TableEntry entry = new TableEntry(myIP, myIP, 0);
  }

  private void updateTable(Map<Inet4Address, Integer> routingMap, Inet4Address neighbor) {
    for (Map.Entry<Inet4Address, Integer> entry : routingMap.entrySet()) {
      if (contains(entry.getKey())) {
        if (entry.getValue() + 1 < getEntryByDestIP(entry.getKey()).hopCount) { // if new hop count for existing destIP is smaller
          getEntryByDestIP(entry.getKey()).neighbor = neighbor;
          getEntryByDestIP(entry.getKey()).hopCount = entry.getValue() + 1;
        }
      } else {
        table.add(new TableEntry(entry.getKey(), neighbor, entry.getValue() + 1));
      }
    }
  }

  public boolean contains(Inet4Address destIP) {
    List<Inet4Address> result = new ArrayList<>();
    for (TableEntry entry : table) {
      result.add(entry.destIP);
    }
    return result.contains(destIP);
  }

  // returns index of the neighbor with the lowest hop count for given destination IP
//  public int getNeighborIndex(Inet4Address address) {
//    List<Integer> hopCounts = table.get(address);
//    return hopCounts.indexOf(Collections.min(hopCounts));
//  }

  public List<TableEntry> getTable() {
    return table;
  }

  public TableEntry getEntryByDestIP(Inet4Address destIP) {
    List<Inet4Address> temp = new ArrayList<>();
    for (TableEntry entry : table) {
      temp.add(entry.destIP);
    }
    return table.get(temp.indexOf(destIP));
  }

  //public List<Inet4Address> getNeighbors() {
  //   return neighbors;
  //}

  public class TableEntry {
    Inet4Address destIP; // destination IP
    Inet4Address neighbor; // neighbor for next hop
    int hopCount; // metric

    public TableEntry(Inet4Address destIP, Inet4Address neighbor, int hopCount) {
      this.destIP = destIP;
      this.neighbor = neighbor;
      this.hopCount = hopCount;
    }
  }
}
