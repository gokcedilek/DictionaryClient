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
  // private int timeout = 1;

  public DictClient(String host, int port) throws Exception {
    this.dictSocket = new Socket(host, port);
    // this.dictSocket = new Socket();
    // this.dictSocket.connect(new InetSocketAddress(host, port), timeout);
    this.socketIn = new BufferedReader(new InputStreamReader(dictSocket.getInputStream()));
    this.socketOut = new PrintWriter(dictSocket.getOutputStream(), true);
    this.dictToUse = "*";
  }

  public void retrieveDictList(boolean debugOn) throws IOException {
    socketOut.println("show db");
    // String fromServer = socketIn.readLine();
    String fromServer;
    while (!(fromServer = socketIn.readLine()).equals(".")) {
      System.out.println(fromServer);
    }

    // while (!(fromServer.contains("250 ok")) && !(fromServer.contains("554"))) {
    // System.out.println("server: " + fromServer);
    // fromServer = socketIn.readLine();
    // }
    // while (!((fromServer.contains("250 ok")) && (fromServer.contains("554")))) {
    // if (debugOn && (fromServer.contains("110"))) {
    // System.out.println("<-- " + fromServer);
    // } else {
    // System.out.println(fromServer);
    // }
    // fromServer = socketIn.readLine();
    // }
    System.out.println("<-- " + fromServer);
  }

  public void setDictToUse(String dict) {
    this.dictToUse = dict;
  }

  // only for testing
  public String getDictToUse() {
    return this.dictToUse;
  }

  public void retrieveDefinitions(String word) throws IOException {
    String cmd = "define " + this.dictToUse + " " + word;
    socketOut.println(cmd);
    String fromServer;
    fromServer = socketIn.readLine();
    while (!((fromServer.contains("250 ok")) || (fromServer.contains("552 no match")))) {
      if (fromServer.contains("151")) {
        String name = fromServer.split(" ")[2];
        System.out.println(name);
        fromServer = socketIn.readLine();
        // String definition = "";
        while (!(fromServer.equals("."))) {
          System.out.println(fromServer);
          fromServer = socketIn.readLine();
          // definition += (fromServer + "\n");
        }
        // System.out.println(definition);
        fromServer = socketIn.readLine();
      } else {
        fromServer = socketIn.readLine();
      }
    }
    if (fromServer.contains("552 no match")) {
      System.out.println("**No definition found**");
      this.retrieveMatchesDefault(word);
    }
  }

  public void retrieveMatchesExact(String word) throws IOException {
    String cmd = "MATCH " + this.dictToUse + " exact " + word;
    socketOut.println(cmd);
    String fromServer;
    fromServer = socketIn.readLine();

    while (!((fromServer.contains("250 ok")) || (fromServer.contains("552 no match")))) {
      if ((!fromServer.contains("152")) && (!fromServer.equals(".")) && (!fromServer.contains("220"))) {
        System.out.println(fromServer);
      }
      fromServer = socketIn.readLine();
    }

    if (fromServer.contains("552 no match")) {
      System.out.println("****No matching word(s) found****");
    }
  }

  public void retrieveMatchesDefault(String word) throws IOException {
    String cmd = "MATCH " + this.dictToUse + " . " + word;
    socketOut.println(cmd);
    String fromServer;
    fromServer = socketIn.readLine();

    while (!((fromServer.contains("250 ok")) && (fromServer.contains("552 no match"))
        && (!fromServer.contains("220")))) {
      if (!fromServer.contains("152") || !fromServer.contains(".")) {
        System.out.println(fromServer);
      }
      fromServer = socketIn.readLine();
    }

    if (fromServer.contains("552 no match")) {
      System.out.println("***No matches found***");
    }
  }

  public void close() throws IOException {
    this.dictSocket.close();
  }

  public boolean isClosed() {
    return this.dictSocket.isClosed();
  }

  public void printInfo() {
    System.out.println(
        "localport: " + this.dictSocket.getLocalPort() + " port: " + this.dictSocket.getPort() + " local address: "
            + this.dictSocket.getLocalAddress() + " local socket address: " + this.dictSocket.getLocalSocketAddress());
    System.out.println();
    System.out.println();
  }

}
