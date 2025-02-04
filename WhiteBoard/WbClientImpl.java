// file: WbClientImpl.java by pmateti@wright.edu

package WhiteBoard;

import java.awt.*;
import java.rmi.Naming;

public class WbClientImpl extends java.rmi.server.UnicastRemoteObject implements WbClient {

    // Server Info
    private WbServer wbServer; // Server proxy object (RMI)
    private String myServerURL; // thi RMI URL of this server

    // Info the GUI
    private LinesFrame myLinesFrame; // LinesFrameImpl proxy oject (RMI)

    // This client config
    private final String thisMcnm; // the name of this machine
    private final String myBoardNm; // the nae of this whiteboard
    private final String myURL; // the RMI URL of this client
    private final Color myColor; // the color of the line in this whiteboard


    /**
     * This is the main client program. It invokes LinesFrameImpl internally for the GUI.
     * <p>
     * Example Invocation: hava WhiteBoard.WbClientImpl 22 b0 localhost //localhost/S1 FF0000
     *
     * @param args clientId, brdNm, displayMcnm, wbserverURL, color
     * @throws Exception
     */
    public WbClientImpl(String[] args) throws Exception {
        // args = [clientId, brdNm, displayMcnm, wbserverURL, color]
        super();

        this.myBoardNm = args[1];

        //
        this.myURL = Invoke.makeURL('C', args[0]);
        Naming.rebind(myURL, this);
        Invoke.myPrint("WbClientImpl", "did Naming.rebind " + myURL);

        //
        this.thisMcnm = java.net.InetAddress.getLocalHost().getHostName();

        //
        makeMyLinesFrame(args);

        //
        this.myServerURL = args[3];
        this.wbServer = (WbServer) Invoke.lookup(myServerURL);

        //
        this.myColor = new Color(Integer.parseInt(args[4], 16));
        Invoke.myPrint("WbClient waiting for", "recvDisplayObj");

        // addClient() occurs in recvDisplayObj()
    }

    public static void main(String[] args) {
        try {
            WbClientImpl me = new WbClientImpl(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts LinesFrameImpl class. This handles the GUI and calls the addClient function
     *
     * @param args [myId, bnm, displayMcnm, clientMcnm, clientId]
     * @throws Exception
     */
    private void makeMyLinesFrame(String[] args) throws Exception {
        Invoke.javaVM
                ('L', args[1] + " " + args[2] + " " + thisMcnm + " " + args[0]);
    }

    // the rest come from our LinesFrame

    // this comes from wbServer
    public void updateBoard(LineCoords ln) throws java.rmi.RemoteException {
        myLinesFrame.recvOneLine(ln);
    }

    public void sendAllLines() throws java.rmi.RemoteException {
        wbServer.sendAllLines(this, myBoardNm);
    }

    public void sendLine(LineCoords ln) {
        ln.c = myColor;
        try {
            wbServer.addLine(ln, myBoardNm);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void recvDisplayObj(LinesFrame s) {
        Invoke.myPrint("WbClient waiting Ended", "" + s);
        myLinesFrame = s;
        try {
            wbServer.addClient(this, myBoardNm);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void pleaseDie() throws java.rmi.RemoteException {
        try {
            wbServer.delClient(this, myBoardNm);
            Naming.unbind(myURL);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Invoke.myPrint("WbClient ", myURL + " exits");
        System.exit(0);
    }

    /**
     * WbClient maintains an object of the server, this.wbServer, it's connected. We need to update this.wbServer
     * whenever, we transfer the whiteboard this client is concerned with to a different server, newServerURL.
     *
     * This function updates all the properties of the client related to the server it's connected to.
     * After that update, it asks the new server to add itself as a client.
     *
     * @param newServerURL the RMI URL of the new server it needs to connect to
     * @return true, if connection to the new server was succesfull
     * @throws java.rmi.RemoteException
     */
    public boolean updateServer(String newServerURL) throws java.rmi.RemoteException {

        // make sure that inputs are not null
        assert newServerURL != null && newServerURL.length() > 0;
        
        // try to connect to the new server
        WbServer newWebServer = (WbServer) Invoke.lookup(newServerURL);
        if (newWebServer == null) {
            System.err.printf("Client: '%s' unable to connect to the new server: '%s'\n", this.myURL, newServerURL);
            return false;
        }

        assert newWebServer != null;

        // update properties of current client
        this.myServerURL = newServerURL;
        this.wbServer = newWebServer;

        // finally ask the new server to add this client to it
        try {
            this.wbServer.addClient(this, this.myBoardNm);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}

// -eof-
