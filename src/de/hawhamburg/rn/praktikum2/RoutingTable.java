package de.hawhamburg.rn.praktikum2;

import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RoutingTable {

  private final List<TableEntry> table = new ArrayList<>();
  //protected List<Inet4Address> neighbors = new ArrayList<>();

  public RoutingTable(Inet4Address myIP) {
    TableEntry entry = new TableEntry(myIP, myIP, 0);
  }

  /**
   * Update the routing table.
   * @param routingMap map containing all the routing table data of the neighbor peer
   * @param neighbor address of the neighbor peer that sent the received package
   * @return whether the table was updated during method call
   */
  public boolean updateTable(Map<Inet4Address, Byte> routingMap, Inet4Address neighbor) {
    boolean isUpdated = false;
    for (Map.Entry<Inet4Address, Byte> entry : routingMap.entrySet()) {
      if (contains(entry.getKey())) {
        if (entry.getValue() + 1 < getEntryByDestIP(entry.getKey()).hopCount) { // if new hop count for existing destIP is smaller
          isUpdated = true;
          getEntryByDestIP(entry.getKey()).neighbor = neighbor;
          getEntryByDestIP(entry.getKey()).hopCount = entry.getValue() + 1;
        }
      } else {
        isUpdated = true;
        table.add(new TableEntry(entry.getKey(), neighbor, entry.getValue() + 1));
      }
    }
    return isUpdated;
  }

  /**
   * Removes an entry from the routing table.
   * @param destIP destination IP to be removed
   */
  public void removeFromTable(Inet4Address destIP) {
    table.remove(getEntryByDestIP(destIP));
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

  public static class TableEntry {
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
