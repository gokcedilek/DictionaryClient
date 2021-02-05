import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class DictClient {
  private Socket dictSocket;
  private BufferedReader socketIn;
  private PrintWriter socketOut;
  private String dictToUse;
  private int timeout = 30000;

  public DictClient(String host, int port, boolean debugOn) throws Exception {
    this.dictSocket = new Socket();
    this.dictSocket.connect(new InetSocketAddress(host, port), timeout);
    this.socketIn = new BufferedReader(new InputStreamReader(dictSocket.getInputStream()));
    this.socketOut = new PrintWriter(dictSocket.getOutputStream(), true);
    this.dictToUse = "*";
    String fromServer = socketIn.readLine();
    if (debugOn) {
      System.out.println("<-- " + fromServer);
    }
  }

  public void retrieveDictList(boolean debugOn) throws IOException {
    socketOut.println("show db");
    String fromServer = socketIn.readLine();
    while (!(fromServer.contains("250 ok")) && !(fromServer.contains("554"))) {
      if (fromServer.contains("110")) {
        if (debugOn) {
          System.out.println("<-- " + fromServer);
        }
      } else {
        System.out.println(fromServer);
      }
      fromServer = socketIn.readLine();
    }
    if (debugOn)
      System.out.println("<-- " + fromServer);
  }

  public void setDictToUse(String dict) {
    this.dictToUse = dict;
  }

  public void retrieveDefinitions(String word, boolean debugOn) throws IOException {
    String cmd = "define " + this.dictToUse + " " + word;
    socketOut.println(cmd);
    String fromServer;
    fromServer = socketIn.readLine();
    while (!((fromServer.contains("250 ok")) || (fromServer.contains("552 no match"))
        || (fromServer.contains("550")))) {
      if (fromServer.contains("150") && debugOn) {
        System.out.println("<-- " + fromServer);
        fromServer = socketIn.readLine();
        continue;
      }
      if (fromServer.contains("151")) {
        if (debugOn) {
          System.out.println("<-- " + fromServer);
        }
        String name = fromServer.split("\"\\s")[1];
        System.out.println("@ " + name);
        fromServer = socketIn.readLine();
        while (!(fromServer.equals("."))) {
          System.out.println(fromServer);
          fromServer = socketIn.readLine();
        }
        System.out.println(fromServer);
        fromServer = socketIn.readLine();
      } else {
        fromServer = socketIn.readLine();
      }
    }
    if (debugOn) {
      System.out.println("<-- " + fromServer);
    }
    if (fromServer.contains("552 no match")) {
      System.out.println("**No definition found**");
      this.retrieveMatchesDefault(word, debugOn);
    }
  }

  public void retrieveMatches(String word, boolean debugOn, String strategy) throws IOException {
    String cmd = "MATCH " + this.dictToUse + " " + strategy + " " + word;
    socketOut.println(cmd);
    String fromServer;
    fromServer = socketIn.readLine();

    while (!((fromServer.contains("250 ok")) || (fromServer.contains("552 no match")) || (fromServer.contains("550"))
        || (fromServer.contains("551")))) {
      if (fromServer.contains("152")) {
        if (debugOn) {
          System.out.println("<-- " + fromServer);
        }
      } else {
        System.out.println(fromServer);
      }
      fromServer = socketIn.readLine();
    }
    if (debugOn) {
      System.out.println("<-- " + fromServer);
    }
    if (fromServer.contains("552 no match")) {
      System.out.println("****No matching word(s) found****");
    }
  }

  public void retrieveMatchesDefault(String word, boolean debugOn) throws IOException {
    String cmd = "MATCH " + this.dictToUse + " . " + word;
    socketOut.println(cmd);
    String fromServer;
    fromServer = socketIn.readLine();

    while (!((fromServer.contains("250 ok")) || (fromServer.contains("552 no match")) || (fromServer.contains("550"))
        || (fromServer.contains("551")))) {
      if (fromServer.contains("152")) {
        if (debugOn) {
          System.out.println("<-- " + fromServer);
        }
      } else {
        System.out.println(fromServer);
      }
      fromServer = socketIn.readLine();
    }
    if (debugOn) {
      System.out.println("<-- " + fromServer);
    }
    if (fromServer.contains("552 no match")) {
      System.out.println("***No matches found***");
    }
  }

  public void close(boolean debugOn) throws IOException {
    String cmd = "quit";
    socketOut.println(cmd);
    String fromServer = socketIn.readLine();
    if (debugOn) {
      System.out.println("<-- " + fromServer);
    }
    this.dictSocket.close();
  }

  public boolean isClosed() {
    return this.dictSocket.isClosed();
  }

}
