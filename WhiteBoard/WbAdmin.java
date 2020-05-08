// @file WbAdmin.java @author pmateti@wright.edu
// This is a "shell" for the WhiteBoard project of CEG 7370

package WhiteBoard;

import java.util.Vector;

public class WbAdmin {

    private static final String menu = "\nWbAdmin: create a " +
            "[s]erver, [a]dd client, [q]uery, [t]ransfer, e[x]it";
    private final Vector vServers;

    public WbAdmin() {
        vServers = new Vector();
    }

    public static void main(String[] args) {
        WbAdmin wa = new WbAdmin();
        wa.userInteract();
    }

    private void serverCreate() {
        String args = Invoke.promptAndGet("ServerMachineName");
        Invoke.javaVM('S', args);
    }

    private void addClientReq() {
        String args = Invoke.promptAndGet("BoardName DisplayOn ServerURL");
        Invoke.javaVM('C', args);
    }

    // transfer request
    private void transferReq() {
        String[] args = Invoke.promptAndGet("ServelURL WhiteBoardName NewServerURL").split(" ");
        WbServer wbServer = (WbServer) Invoke.lookup(args[0]);
        try {
            boolean result = wbServer.transferWhiteBoard(args[2], args[1]);
            System.out.println("Transfer: " + result);
            System.out.println("Old Server State: ");
            query(args[0]);
            System.out.println("New Server State: ");
            query(args[2]);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Transfer failed");
        }
    }
    
    private void printQueryInfo(String serverURL, Vector<ABoard> vBoards) {
        StringBuilder str = new StringBuilder("WbServerImpl{ myURL='" + serverURL + "'}\n");
        for (ABoard board : vBoards) {
            str.append("board name : '").append(board.boardName).append("'\n");
            str.append("\t total numbers of clients : ").append(board.vClients.size()).append("\n");
            str.append("\t total numbers of lines : ").append(board.vLines.size()).append("\n");
        }
        System.out.println(str);
    }

    private void query(String serverURL) {
        WbServer wbServer = (WbServer) Invoke.lookup(serverURL);
        try {
            Vector<ABoard> vBoards = wbServer.query();
            printQueryInfo(serverURL, vBoards);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void queryReq() {
        String args = Invoke.promptAndGet("ServelURL");
        query(args);
    }

    private void userInteract() {
        while (true) {
            String choice = Invoke.promptAndGet(menu);
            switch (choice.charAt(0)) {
                case 's':
                    serverCreate();
                    break;
                case 'a':
                    addClientReq();
                    break;
                case 'q':
                    queryReq();
                    break;
                case 't':
                    transferReq();
                    break;
                case 'x':
                    System.exit(0);
                    break;
            }
        }
    }
}

// -eof-
