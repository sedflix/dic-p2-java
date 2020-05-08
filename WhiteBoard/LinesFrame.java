// @file LinesFrame.java

package WhiteBoard;

public interface LinesFrame extends java.rmi.Remote {
    void recvOneLine(LineCoords ln) throws java.rmi.RemoteException;
}

// -eof-
