// file: WbClientImpl.java by pmateti@wright.edu

package WhiteBoard;

import java.awt.*;
import java.rmi.Naming;

public class WbClientImpl extends java.rmi.server.UnicastRemoteObject implements WbClient {

    private final WbServer wbServer;
    private final String thisMcnm;
    private final String myBoardNm;
    private final String myURL;
    private final String myServerURL;
    private final Color myColor;
    private LinesFrame myLinesFrame;

    public WbClientImpl(String[] args) throws Exception {
        // args = [clientId, brdNm, displayMcnm, wbserverURL, color]
        super();

        myBoardNm = args[1];
        myURL = Invoke.makeURL('C', args[0]);
        Naming.rebind(myURL, this);
        Invoke.myPrint("WbClientImpl", "did Naming.rebind " + myURL);

        thisMcnm = java.net.InetAddress.getLocalHost().getHostName();
        makeMyLinesFrame(args);
        myServerURL = args[3];
        wbServer = (WbServer) Invoke.lookup(myServerURL);
        myColor = new Color(Integer.parseInt(args[4], 16));
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

    // create our lines frame process, which will do recvDisplayObj()
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
}

// -eof-
