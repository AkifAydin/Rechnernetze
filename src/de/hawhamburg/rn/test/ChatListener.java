package de.hawhamburg.rn.test;

import java.io.*;

public class ChatListener extends Thread {

  private String name;
  private DataInputStream in;

  public ChatListener(DataInputStream in, String name) {
    this.in = in;
    this.name = name;
  }

  @Override
  public void run() {

    while (true) {
      try {
        Message msg = Util.readNextMessage(in);
        System.out.println(name + ": " + new String(msg.getPayload()));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
