// @file WbServer.java @author pmateti@wright.edu

package WhiteBoard;

import java.util.Vector;

public interface WbServer extends java.rmi.Remote {
    void addClient(WbClient wc, String brnm) throws java.rmi.RemoteException;

    void delClient(WbClient wc, String brnm) throws java.rmi.RemoteException;

    void addLine(LineCoords ln, String brnm) throws java.rmi.RemoteException;

    void sendAllLines(WbClient wc, String brnm) throws java.rmi.RemoteException;

    Vector<ABoard> query() throws java.rmi.RemoteException;

    boolean transferWhiteBoard(String toServerURL, String boardName) throws java.rmi.RemoteException;

    void recieveWhiteBoard(ABoard board) throws java.rmi.RemoteException;
}

// -eof-
