package de.hawhamburg.rn.praktikum2;

import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RoutingTable {

  public static void main(String[] args) {
    List<Integer> list = new ArrayList<>();
    list.set(4, 17);
    System.out.println(list);
  }

  // TODO zweidimensionale Listen in der Map

  private Map<Inet4Address, List<Integer>> table;
  protected List<Inet4Address> neighbors = new ArrayList<>();

  private void updateTable(Map<Inet4Address, Integer> routingMap, Inet4Address neighbor) {
    if (!neighbors.contains(neighbor)) {
      neighbors.add(neighbor);
    }
    int neighborIndex = neighbors.indexOf(neighbor);
    for (Map.Entry<Inet4Address, Integer> entry : routingMap.entrySet()) {
      if (!table.containsKey(entry.getKey())) {
        List<Integer> hops = new ArrayList<>();
        hops.set(neighborIndex, entry.getValue());
        table.put(entry.getKey(), hops);
      }
    }
  }

  // returns index of the neighbor with the lowest hop count for given destination IP
  public int getNeighborIndex(Inet4Address address) {
    List<Integer> hopCounts = table.get(address);
    return hopCounts.indexOf(Collections.min(hopCounts));
  }

  public Map<Inet4Address, List<Integer>> getTable() {
    return table;
  }

  public List<Inet4Address> getNeighbors() {
    return neighbors;
  }
}
