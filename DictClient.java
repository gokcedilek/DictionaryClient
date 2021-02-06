import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

/* class representing a socket connection to a dictionary server */
public class DictClient {
  private Socket dictSocket;
  private BufferedReader socketIn;
  private PrintWriter socketOut;
  private String dictToUse;
  private int timeout = 30000;

  public DictClient(String host, int port, boolean debugOn) throws Exception {
    /* create the connection with 30s connection timeout */
    this.dictSocket = new Socket();
    this.dictSocket.connect(new InetSocketAddress(host, port), timeout);

    /* get input and output stream for the socket */
    this.socketIn = new BufferedReader(new InputStreamReader(dictSocket.getInputStream()));
    this.socketOut = new PrintWriter(dictSocket.getOutputStream(), true);

    /* set default dictionary */
    this.dictToUse = "*";

    /* read server output from initial connection */
    String fromServer = socketIn.readLine();
    if (debugOn) {
      System.out.println("<-- " + fromServer);
    }
  }

  /* retrieve a list of dictionaries that the server supports */
  public void retrieveDictList(boolean debugOn) throws IOException {
    socketOut.println("show db");
    String fromServer = socketIn.readLine();
    /* status 250 or 554 indicate end of server response */
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

  /* set the dictionary to use */
  public void setDictToUse(String dict) {
    this.dictToUse = dict;
  }

  /* retrieve definitions of a word */
  public void retrieveDefinitions(String word, boolean debugOn) throws IOException {
    String cmd = "define " + this.dictToUse + " " + word;
    socketOut.println(cmd);
    String fromServer;
    fromServer = socketIn.readLine();
    /* status 250 or 552 or 550 indicate end of server response */
    while (!((fromServer.contains("250 ok")) || (fromServer.contains("552 no match"))
        || (fromServer.contains("550")))) {
      if (fromServer.contains("150") && debugOn) {
        System.out.println("<-- " + fromServer);
        fromServer = socketIn.readLine();
        continue;
      }
      /* for each definition: */
      if (fromServer.contains("151")) {
        if (debugOn) {
          System.out.println("<-- " + fromServer);
        }
        /* get the name of the dictionary by splitting with quote + space (" ) */
        String name = fromServer.split("\"\\s")[1];
        System.out.println("@ " + name);
        fromServer = socketIn.readLine();
        /* print the definition */
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
    /* if no definitions found, do a default match */
    if (fromServer.contains("552 no match")) {
      System.out.println("**No definition found**");
      this.retrieveMatchesDefault(word, debugOn);
    }
  }

  /* retrieve matches of a word based on a given strategy */
  public void retrieveMatches(String word, boolean debugOn, String strategy) throws IOException {
    String cmd = "match " + this.dictToUse + " " + strategy + " " + word;
    socketOut.println(cmd);
    String fromServer;
    fromServer = socketIn.readLine();

    /* status 250 or 550 or 551 or 552 indicate end of server response */
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

  /* retrieve matches of a word based on server's default strategy */
  public void retrieveMatchesDefault(String word, boolean debugOn) throws IOException {
    String cmd = "match " + this.dictToUse + " . " + word;
    socketOut.println(cmd);
    String fromServer;
    fromServer = socketIn.readLine();

    /* status 250 or 550 or 551 or 552 indicate end of server response */
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

  /* close the socket connection */
  public void close(boolean debugOn) throws IOException {
    String cmd = "quit";
    socketOut.println(cmd);
    String fromServer = socketIn.readLine();
    if (debugOn) {
      System.out.println("<-- " + fromServer);
    }
    this.dictSocket.close();
  }

  /* check if the socket is closed */
  public boolean isClosed() {
    return this.dictSocket.isClosed();
  }
}
