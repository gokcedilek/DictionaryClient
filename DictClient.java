import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class DictClient {
  private Socket dictSocket;
  private BufferedReader socketIn;
  private PrintWriter socketOut;
  private String dictToUse;

  public DictClient(String host, int port) throws UnknownHostException, IOException {
    this.dictSocket = new Socket(host, port);
    this.socketIn = new BufferedReader(new InputStreamReader(dictSocket.getInputStream()));
    this.socketOut = new PrintWriter(dictSocket.getOutputStream(), true);
    this.dictToUse = "*";
  }

  public void retrieveDictList() throws IOException {
    socketOut.println("show db");
    String fromServer;
    // check if there's no other input
    while (!(fromServer = socketIn.readLine()).equals(".")) {
      System.out.println(fromServer);
    }
  }

  public void setDictToUse(String dict) {
    this.dictToUse = dict;
  }

  // only for testing
  public String getDictToUse() {
    return this.dictToUse;
  }

  public void retrieveWordDefn(String word) throws IOException {
    // define english hello
    String cmd = "define " + this.dictToUse + " " + word;
    System.out.println("cmd is: " + cmd);
    socketOut.println(cmd);
    int count = 0;
    String fromServer;
    fromServer = socketIn.readLine();
    while (!((fromServer.equals("250 ok")) || (fromServer.equals("552 no match")))) {
      System.out.println("line " + count + ": " + fromServer);
      count++;
      fromServer = socketIn.readLine();
    }
    // while (!(fromServer = socketIn.readLine()).equals(".")) {
    // System.out.println(fromServer);
    // }
    System.out.println("we are done!!!!!!!!!");
  }

  public void close() {
    // this.socket.close();
  }

  // only for testing
  public void printInfo() {
    System.out.println(
        "localport: " + this.dictSocket.getLocalPort() + " port: " + this.dictSocket.getPort() + " local address: "
            + this.dictSocket.getLocalAddress() + " local socket address: " + this.dictSocket.getLocalSocketAddress());
    System.out.println();
    System.out.println();
  }

}
