import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class ThreadClient extends Thread{
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private static final String LOOP_BREAKER = "!END!";
    private static final String BREAK_CONNECTION = "!TERMINATE!";
    private String hostName;
    private int portNumber;
    private String menuSelected;
    private static final int threadCount = 5;
    public static ThreadClient[] theThreads = new ThreadClient[threadCount];
    public static boolean breakConnection = false;

    public ThreadClient(String hostName, int portNumber, String menuSelected) throws IOException {
        this.hostName = hostName;
        this.portNumber = portNumber;
        this.menuSelected = menuSelected;

        this.clientSocket = new Socket(hostName, portNumber);
        this.out = new PrintWriter(clientSocket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    /**
     * This main method accepts a hostName and portNumber to instantiate a number
     * of ClientThread instances. It also allows the user to select a menu item for
     * which Linux command to run on the server. Next it calls the runThreads method
     * to send a request to the server and display the runtime for each instance.
     *
     * @param args hostName and portNumber
     */
    public static void main(String[] args) throws IOException {
        // Validate args (server, port number)
        if (args.length < 2) {
            System.out.println("Did not receive both hostname and port number");
            return;
        }
        if (args[0].length() < 3) {
            // Invalid host name, quit XXXXX
            System.out.println("hostname invalid");
            return;
        }

        // validate port number input
        int portNumber = 0;
        try {
            portNumber = Integer.parseInt(args[1]);
            if (portNumber < 1000 || portNumber > 65000) {
                // Invalid port number XXXXXX
                System.out.println("port number invalid");
                return;
            }
        }
        catch (Exception e) {
            // Bad port number, quit XXXXXX
            System.out.println("port number invalid");
            return;
        }

        String hostName = args[0];
        System.out.print("You are trying to connect to \"");
        System.out.println(hostName + "\" on port " + portNumber);

        boolean connected = true;

        while (connected) {
            if (breakConnection) {
                break;
            }
            String input = selectFromMenu();
            if (validateInput(input)) {
                for (int i = 0; i < threadCount; i++) {
                    theThreads[i] = new ThreadClient(hostName, portNumber, input);
                }
                runThreads(theThreads);
            }else {
                System.out.println("Invalid input!");
            }
        }
    }



    /**
     *  create n number of client threads
     */
    public static void runThreads(ThreadClient[] theThreads) {

        for(int index = 0; index < theThreads.length; index++) {

            System.out.println("Thread # " + (index + 1));
            theThreads[index].run();

        }// end for loop

        for(int index2 = 0; index2 < theThreads.length; index2++) {

            try {
                theThreads[index2].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }// end try catch statement

        }// end for loop

    }// end runThreads method

    /**
     * Displays a menu with 7 options as dictated by project requirements
     *
     * @return int representing the user's selection from the menu or -1 for a non-int value
     */
    private static String selectFromMenu(){
        System.out.println("Select an option:");
        System.out.println("\t1. Host current Date and Time");
        System.out.println("\t2. Host uptime");
        System.out.println("\t3. Host memory use");
        System.out.println("\t4. Host Netstat");
        System.out.println("\t5. Host current users");
        System.out.println("\t6. Host running processes");
        System.out.println("\t7. Quit");

        Scanner input = new Scanner(System.in);
        // Need to do input validation XXXXXXX
        return input.nextLine();

    }

    private static boolean validateInput(String input){
        switch (input) {
            case "1":
            case "2":
            case "3":
            case "4":
            case "5":
            case "6":
            case "7":
                return true;
            default:
                return false;
        }
    }

    //Outline of run() method for client thread:
    @Override
    public void run() {

        double timeStart = 0;
        double timeEnd;

        try {
            timeStart = System.currentTimeMillis();

            // XXXXXXX Your code to send command to server here.
            this.out.println(this.menuSelected);
            String answer;

            while ((answer = this.in.readLine()) != null && !answer.equals(LOOP_BREAKER)) {
                if (answer.equals(BREAK_CONNECTION)) {
                    breakConnection = true;

                    break;
                }
                System.out.println(answer);


            }// end while answer loop
            timeEnd = System.currentTimeMillis();
            System.out.println("Response time = " + (timeEnd - timeStart));
            this.breakConnection();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }



    }

    private void breakConnection() throws IOException {
        this.out.close();
        this.in.close();
        this.clientSocket.close();
    }


}// end ThreadClient class




