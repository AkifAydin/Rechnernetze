package de.hawhamburg.rn.praktikum2;

/**
 * Periodically starts isAliveNeighbor threads.
 */
public class AliveFunction extends Thread {

  public void run() {
    try {
      while (!Thread.currentThread().isInterrupted()) {
        for (RoutingTable.TableEntry entry : Main.routingTable.getEntries()) {
          if (entry.hopCount == 1) { // table entry for neighbor
            new IsNeighborAlive(entry.destIP).start();
          }
        }
        Thread.currentThread().wait(Main.ALIVE_WAIT);
      }
    } catch (InterruptedException e) {
      // do nothing
    }
  }
}