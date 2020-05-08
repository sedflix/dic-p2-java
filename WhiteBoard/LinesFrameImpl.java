// file LinesFrameImpl.java  by pmateti@wright.edu

package WhiteBoard;

import java.awt.*;
import java.awt.event.*;
import java.rmi.Naming;


public class LinesFrameImpl
        extends java.rmi.server.UnicastRemoteObject
        implements LinesFrame {
    private static final int WIDTH = 300, HEIGHT = 300;
    private int x0 = 0, y0 = 0, x1, y1;
    private final Frame frame;
    private final WbClient myWbClient;
    WindowListener raisedWindow = new WindowAdapter() {
        public void windowActivated(WindowEvent w) {
            try {
                myWbClient.sendAllLines();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    private final String myURL;
	private final String myCLientURL;
    WindowListener destroyWindow = new WindowAdapter() {
        public void windowClosing(WindowEvent w) {
            deleteAndExit();
        }
    };
    MouseListener getCoords = new MouseAdapter() {
        public void mouseClicked(MouseEvent m) {
            if (m.getModifiers() == InputEvent.BUTTON3_MASK)
                deleteAndExit();

            try {
                // get the coords, and send a line to our client
                LineCoords ln = new LineCoords();
                ln.x2 = m.getX();
                ln.y2 = m.getY();
                ln.x1 = x0;
                ln.y1 = y0;
                x0 = ln.x2;    // store current point
                y0 = ln.y2;
                myWbClient.sendLine(ln);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    /**
     *
     * @param args [myId, bnm, displayMcnm, clientMcnm, clientId]
     * @throws Exception
     */
    public LinesFrameImpl(String[] args) throws Exception {
        // args = [myId, bnm, displayMcnm, clientMcnm, clientId]
        super();

        myCLientURL = Invoke.makeURL('C', args[4]);

        frame = new Frame(args[1] + "@" + myCLientURL + "@" + args[3]);
        frame.setSize(WIDTH, HEIGHT);
        frame.setVisible(true);
        frame.addWindowListener(destroyWindow);
        frame.addWindowListener(raisedWindow);
        frame.addMouseListener(getCoords);

        myURL = Invoke.makeURL('D', args[4]);
        myWbClient = (WbClient) Invoke.lookup(myCLientURL);
        myWbClient.recvDisplayObj(this);
        Invoke.myPrint("LinesFrameImpl", myURL);
        Invoke.myPrint("LinesFrameImpl", "" + myWbClient);
        Invoke.myPrint("LinesFrameImpl", "Done");
    }

    public static void main(String[] args) {
        try {
            LinesFrameImpl f = new LinesFrameImpl(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteAndExit() {
        try {
            myWbClient.pleaseDie();
            Naming.unbind(myURL);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    public void recvOneLine(LineCoords ln) {
        Graphics g = frame.getGraphics();
        g.setColor(ln.c);
        g.drawLine(ln.x1, ln.y1, ln.x2, ln.y2);
    }
}

// -eof-
