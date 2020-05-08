// @file WbClient.java

package WhiteBoard;

public interface WbClient
        extends java.rmi.Remote, java.io.Serializable {
    void updateBoard(LineCoords ln) throws java.rmi.RemoteException;

    void sendAllLines() throws java.rmi.RemoteException;

    void sendLine(LineCoords ln) throws java.rmi.RemoteException;

    void pleaseDie() throws java.rmi.RemoteException;

    void recvDisplayObj(LinesFrame f) throws java.rmi.RemoteException;

    void updateServer(String newServerURL) throws java.rmi.RemoteException;
}

// -eof-
