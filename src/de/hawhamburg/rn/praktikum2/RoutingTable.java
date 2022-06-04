package de.hawhamburg.rn.praktikum2;

import java.net.Inet4Address;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RoutingTable {

  private Map<Inet4Address, List<Integer>> table;

  // returns index of the neighbor with the lowest hop count for given destination IP
  protected int getNeighborIndex(Inet4Address address) {
    List<Integer> hopCounts = table.get(address);
    return hopCounts.indexOf(Collections.min(hopCounts));
  }

  public Map<Inet4Address, List<Integer>> getTable() {
    return table;
  }
}
