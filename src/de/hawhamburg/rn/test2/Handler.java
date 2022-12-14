package de.hawhamburg.rn.test2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class Handler implements Runnable {
  private PrintWriter writer;
  private BufferedReader reader;
  private String username;
  private Socket socket;
  ArrayList<String> usernames = new ArrayList<>();
  private static ArrayList<PrintWriter> userWriters = new ArrayList<>();

  @Override
  public void run() {

    try {
      // simple PrintWriter and BufferedReader is used to write and read data to/from streams
      this.writer = new PrintWriter(socket.getOutputStream(), true);
      this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      try {
        this.username = reader.readLine();
      } catch (IOException e) {
        e.printStackTrace();
      }
      System.out.println("User " + username + " connected");
      // avoid array list from being updated at the same time by different threads
      synchronized (usernames) {
        usernames.add(username);
      }
      userWriters.add(writer);
      // reading socket inputs from stream
      while (true) {
        String input = null;
        while (true) {
          try {
            if (!((input = reader.readLine()) != null)) break;
          } catch (IOException e) {
            e.printStackTrace();
          }
          if (input.equals("--exit--")){
            System.out.println("--exit-- " +username);
            return;
          }
          for (PrintWriter printWriter : userWriters) {
            printWriter.println(username + " : " + input);
          }
        }
      }
    }catch (IOException e){
      e.printStackTrace();
    }finally {
      // closing streams and socket on user exit
      if (writer != null)
        userWriters.remove(writer);
      System.out.println("Client quited :" +username);
      System.out.println(userWriters.size() + " " + usernames.size());
      for (PrintWriter printWriter : userWriters) {
        printWriter.println("--" + username + "--" + " has left");
      }
      try {
        writer.close();
        reader.close();
        socket.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public Handler(Socket socket) throws IOException {
    this.socket = socket;
  }
}
