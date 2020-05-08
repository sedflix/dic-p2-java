// @file WbServerImpl.java @author pmateti@wright.edu

package WhiteBoard;

import java.io.Serializable;
import java.rmi.Naming;
import java.rmi.server.UnicastRemoteObject;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

class ABoard implements Serializable {
    String boardName;        // Name of this board
    Vector<LineCoords> vLines;    // all lines on this board
    Vector<WbClient> vClients;    // all clients on this board

    public ABoard(String brdnm) {
        boardName = brdnm;
        vLines = new Vector<LineCoords>();
        vClients = new Vector<WbClient>();
    }
}

public class WbServerImpl
        extends UnicastRemoteObject
        implements WbServer {

    private final Vector<ABoard> vBoards; // all boards on this server
    private final String myURL;

    public WbServerImpl(String[] args) throws Exception {
        // args = [serverID, serverMcnm]
        vBoards = new Vector<ABoard>();
        myURL = Invoke.makeURL('S', args[0]);
        Naming.rebind(myURL, this); // rmi register ourselves
        Invoke.myPrint("WbServerImpl", myURL + " started");
    }

    public static void main(String[] args) {
        try {
            WbServerImpl wsi = new WbServerImpl(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void pleaseDie() {
        int delay = 5000;    // in msec, delayed death
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                try {
                    Naming.unbind(myURL);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Invoke.myPrint("WbServerImpl", myURL + " exits");
                System.exit(0);
            }
        }, delay);
    }

    private ABoard findAboard(String brdnm) {
        for (Enumeration e = vBoards.elements(); e.hasMoreElements(); ) {
            ABoard b = (ABoard) e.nextElement();
            if (brdnm.equals(b.boardName))
                return b;
        }
        return null;
    }

    public void sendAllLines(WbClient wc, String brdnm)
            throws java.rmi.RemoteException {
        ABoard ab = findAboard(brdnm);
        sendAllLines(wc, ab);
    }

    private void sendAllLines(WbClient wc, ABoard ab) {
        for (Enumeration e = ab.vLines.elements(); e.hasMoreElements(); ) {
            try {
                wc.updateBoard((LineCoords) e.nextElement());
            } catch (Exception x) {
                x.printStackTrace();
            }
        }
    }

    public void addClient(WbClient wc, String brdnm)
            throws java.rmi.RemoteException {
        ABoard ab = findAboard(brdnm);
        if (ab == null) {
            ab = new ABoard(brdnm);
            vBoards.addElement(ab);
        } else {
            sendAllLines(wc, ab); // new client on an old board
        }
        ab.vClients.addElement(wc);
    }

    public void delClient(WbClient wc, String brdnm)
            throws java.rmi.RemoteException {
        ABoard ab = findAboard(brdnm);
        if (ab == null) return;

        ab.vClients.removeElement(wc);

        // If this is the last client in board, delete board
        if (ab.vClients.size() == 0) vBoards.removeElement(ab);

        // If this was the last board, terminate this server
        if (vBoards.size() == 0) pleaseDie();
    }

    public void addLine(LineCoords ln, String brdnm)
            throws java.rmi.RemoteException {
        ABoard ab = findAboard(brdnm);
        if (ab == null) return;

        ab.vLines.addElement(ln);

        // Broadcast to all the clients on this board
        for (Enumeration e = ab.vClients.elements(); e.hasMoreElements(); ) {
            WbClient wc = (WbClient) e.nextElement();
            try {
                wc.updateBoard(ln);
            } catch (Exception x) {
                x.printStackTrace();
            }
        }
    }

    /**
     * This functions simply returns all the info about the current server in the form of a list of Aboard structure
     *
     * @return A list of WhiteBoards present in the current server instance
     * @throws java.rmi.RemoteException
     */
    public Vector<ABoard> query() throws java.rmi.RemoteException {

        // make sure that we are not returning a null
        assert this.vBoards != null;

        return this.vBoards;
    }

    /**
     * This function is responsible for transferring WhiteBoard, :boardName, on this server
     * to an new Server, :toServerURL.
     * <p>
     * First, it transfers all the information about :boardName to :toServerURL.
     * Then, it asks each client of :boardName to reconnect to the :toServerURL.
     *
     * @param toServerURL the RMI URL of the server to which we need to transfer the WhiteBoard
     * @param boardName   The name of the WhiteBoard that needs to be transferred
     * @return true, if transfer is completely successful. it returns false, even if one of the client was unable to update its server
     * @throws java.rmi.RemoteException
     */
    public boolean transferWhiteBoard(String toServerURL, String boardName) throws java.rmi.RemoteException {

        // make sure that inputs are not null
        assert toServerURL != null && toServerURL.length() > 0 && boardName != null;

        int assert_board_num = vBoards.size(); // for assertion use later

        // find the WhiteBoard with name boardName on this server
        ABoard board = findAboard(boardName);
        if (board == null) {
            // if white board with that specific name is not present
            System.err.printf("Can't find whiteboard '%s' on server '%s'\n", boardName, this.myURL);
            return false;
        }


        // connect with the new server to which we need to tranfer board
        WbServer toWbServer = (WbServer) Invoke.lookup(toServerURL);
        if (toWbServer == null) {
            // if we are not able to find or connect to the second server
            System.err.printf("Current Server '%s' can not find '%s'\n", this.myURL, toServerURL);
            return false;
        }


        // send info the board to the new server :toWbServer
        try {
            boolean result = toWbServer.recieveWhiteBoard(board);
            if (!result) {
                System.err.printf("New Server '%s' already has '%s'\n", toServerURL, boardName);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        boolean success = true;

        // ask all the client of :boardName to update their server to the new server, :toServerURL
        for (WbClient wbClient : board.vClients) {
            try {
                boolean result = wbClient.updateServer(toServerURL);
                if (!result) {
                    System.err.printf("Client: '%s' to update it's server\n", wbClient.toString());
                    System.err.println("transferWhiteBoard: will be partially successful");
                    success = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("transferWhiteBoard: will be partially successful");
                success = false;
            }
        }
        

        // finally remove board from the current server
        this.vBoards.remove(board);

        // make sure that we have deleted only one board
        assert assert_board_num - 1 == vBoards.size();

        return success;
    }

    /**
     * This function is responsible for receiving a new WhiteBoard.
     * It updates the current list of boards.
     *
     * @param board :ABoard: the new board this server is going to receive
     * @return true if successfully updated, false if whiteboard with same name is already present in this server
     * @throws java.rmi.RemoteException
     */
    public boolean recieveWhiteBoard(ABoard board) throws java.rmi.RemoteException {

        // make sure that input is not null
        assert board != null; 
        int assert_board_num = vBoards.size(); // for future assertion

        if (findAboard(board.boardName) != null) {
            System.err.printf("the receiving server: '%s' already has a whiteboard with name '%s'", this.myURL, board.boardName);
            return false;
        }

        // we need to remove all the previous clients information
        // the previous server will ask all the clients to addClient to this new Server
        board.vClients.removeAllElements();

        // finally add the board to the list of boards handled by the this server
        this.vBoards.add(board);

        // make sure that one board was added
        assert assert_board_num + 1 == vBoards.size();

        return true;
    }
}

// -eof-
