// @file WbServer.java @author pmateti@wright.edu

package WhiteBoard;

public interface WbServer extends java.rmi.Remote {
    void addClient(WbClient wc, String brnm) throws java.rmi.RemoteException;

    void delClient(WbClient wc, String brnm) throws java.rmi.RemoteException;

    void addLine(LineCoords ln, String brnm) throws java.rmi.RemoteException;

    void sendAllLines(WbClient wc, String brnm) throws java.rmi.RemoteException;
}

// -eof-
