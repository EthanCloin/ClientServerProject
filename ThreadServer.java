import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ThreadServer extends Thread {
    private ServerSocket serverSocket;
    private static final String LOOP_BREAKER = "!END!";
    private static final String BREAK_CONNECTION = "!TERMINATE!";
    public boolean breakConnection = false;

    public static void main(String[] args) throws IOException {
        // validate port number
        if (args.length != 1) {
            System.err.println("Usage: java server <port number>");
            System.exit(1);
        }
        int portNumber = Integer.parseInt(args[0]);
        ThreadServer myServer = new ThreadServer();
        myServer.start(portNumber);

        // close all resources
        myServer.close();


    }

    public void start(int portNumber) throws IOException {
        // closed by the threads who reference this as parent
        serverSocket = new ServerSocket(portNumber);
        System.out.println("Server is listening on port " + portNumber);
        while (true) {
            if (serverSocket.isClosed()) break;
            new ClientHandler(serverSocket.accept(), this).start();
        }

    }

    public void close() throws IOException {
        serverSocket.close();
        System.out.println("CONNECTION TERMINATED");
    }

    private static class ClientHandler extends Thread {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        private ThreadServer parent;

        public ClientHandler(Socket socket, ThreadServer parent) throws IOException {
            clientSocket = socket;
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.parent = parent;
        }


        @Override
        public void run() {
            String inputLine = null;

            while (true) {
                try {
                    if (!((inputLine = in.readLine()) != null)) break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String cmdString = null;

                switch (inputLine) {
                    case "1":
                        cmdString = "date";
                        break;
                    case "2":
                        cmdString = "uptime";
                        break;
                    case "3":
                        cmdString = "free";
                        break;
                    case "4":
                        cmdString = "netstat";
                        break;
                    case "5":
                        cmdString = "who";
                        break;
                    case "6":
                        cmdString = "ps aux";
                        break;
                    case "7":
                        out.println(BREAK_CONNECTION);
                        cmdString = BREAK_CONNECTION;
                        break;
                    default:
                        cmdString = null;
                        break;
                }
                if (cmdString.equals(BREAK_CONNECTION)) {
                    try {
                        clientSocket.close();
                        in.close();
                        out.close();
                        this.parent.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }

                if (cmdString != null) {
                    // run unix/shell command and send result to client
                    Process process = null;
                    System.out.println("Running: " + cmdString);

                    try {
                        process = Runtime.getRuntime().exec(cmdString); // for Linux
                        // System.out.print(Runtime.getRuntime().exec(cmdString));
                        process.waitFor();
                        BufferedReader processReader = new BufferedReader(
                                new InputStreamReader(process.getInputStream()));
                        String line;
                        // currently only outputting one line
                        while ((line = processReader.readLine()) != null) {
                            // System.out.println(line); // server side
                            out.println(line); // command output
                        }
                        out.println(LOOP_BREAKER);
                    } catch (Exception e) {
                        System.out.println(e);
                    } finally {
                        process.destroy();
                    }
                } else {
                    out.println("Invalid Input!");
                }
            }
        }
    }
}