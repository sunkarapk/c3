import java.io.*;
import java.util.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.client.*;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.*;

public class Client {

    private static String url = "http://10.6.9.199/";

    private static String username = null;
    public static String connIp = null;

    private static HttpClient client = new DefaultHttpClient();

    // function to do the compute use case
    public static void compute() throws Exception {
        HttpPost method = new HttpPost(url + "/compute");

        method.setEntity(new StringEntity(username, "UTF-8"));

        try {
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            connIp = client.execute(method, responseHandler);
        } catch (IOException e) {
            System.err.println("Fatal transport error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            client.getConnectionManager().shutdown();
        }

        System.out.println("Give the file name which has to be put in the grid for computation");

        //input of the file name to be computed
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String name = in.readLine();

        //get the absolute path of the current working directory
        File directory = new File (".");
        String pwd = directory.getAbsolutePath();

        //get present time
        Date date = new Date();
        long start = date.getTime();

        //Execute the vishwa compute process
        Process p = Runtime.getRuntime().exec("java -classpath " + pwd + "/vishwa/JVishwa.jar:. " + name + " " + connIp);

        // wait till the compute process is completed
        //check for the status code (0 for successful termination)
        int status = p.waitFor();

        if(status ==0){
            System.out.println("Compute operation successful. Check the directory for results");
        }

        long end = date.getTime();
        long durationInt = end - start;

        String duration = String.valueof(durationInt);
        HttpPost method = new HttpPost(url + "/computeAck");
        method.setEntity(new StringEntity(duration, "UTF-8"));
        try {
            client.execute(method);
        } catch (IOException e) {
            System.err.println("Fatal transport error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            client.getConnectionManager().shutdown();
        }
    }

    //function to check login
    public static boolean login(String pass) throws Exception {
        HttpPost method = new HttpPost(url + "/login");

        method.setEntity(new StringEntity(username + ';' + pass, "UTF-8"));

        try {
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            connIp = client.execute(method, responseHandler);
        } catch (IOException e) {
            System.err.println("Fatal transport error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            client.getConnectionManager().shutdown();
        }

        return connIp.equals("true");
    }

    //function to do the join use case
    public static void share() throws Exception {
        HttpPost method = new HttpPost(url + "/share");
        String ipAddress = null;

        Enumeration e = NetworkInterface.getNetworkInterfaces();
        while(e.hasMoreElements()) {
            NetworkInterface ni = (NetworkInterface) e.nextElement();
            if(ni.getName().equals("eth0")) {
                Enumeration e2 = ni.getInetAddresses();
                while (e2.hasMoreElements()) {
                    InetAddress ip = (InetAddress) e2.nextElement();
                    if(ip instanceof Inet4Address) {
                        ipAddress = ip.getHostAddress();
                        break;
                    }
                }
                break;
            }
        }

        method.setEntity(new StringEntity(username + ';' + ipAddress, "UTF-8"));
        try {
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            connIp = client.execute(method, responseHandler);
        } catch (IOException e) {
            System.err.println("Fatal transport error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            client.getConnectionManager().shutdown();
        }

        //get present time
        Date date = new Date();
        long start = date.getTime();

        //Execute the vishwa share process
        Process p = Runtime.getRuntime().exec("java -jar vishwa/JVishwa.jar " + connIp);

        String ch = "alive";
        System.out.println("Type kill to unjoin from the grid");

        while(ch == "alive"){
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            ch = in.readLine();
        }

        long end = date.getTime();
        long durationInt = end - start;

        String duration = String.valueof(durationInt);
        HttpPost method = new HttpPost(url + "/shareAck");
        method.setEntity(new StringEntity(duration, "UTF-8"));
        try {
            client.execute(method);
        } catch (IOException e) {
            System.err.println("Fatal transport error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            client.getConnectionManager().shutdown();
        }
    }

    public static void main(String args[]) throws Exception {
        int choice;

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Vishwa Based Campus Compute Cloud");

        while (true) {
            System.out.println("");

            if (username != null) {
                System.out.println("1. Logout");
            } else {
                System.out.println("1. Login");
            }

            System.out.println("2. Join the Grid");
            System.out.println("3. Avail the compute facility");

            System.out.println("4. Quit");
            System.out.printf("Enter your choice: ");

            try {
                choice = Integer.parseInt(in.readLine());
            } catch (NumberFormatException ex) {
                System.out.println("WARNING: Enter a number only!");
                continue;
            }

            if (choice == 1) {
                if (username != null) {
                    username = null;
                } else {
                    System.out.printf("Enter your username: ");
                    username = in.readLine();
                    System.out.printf("Enter your password: ");
                    if (login(in.readLine())) {
                        System.out.println("Successfully logged in!");
                    } else {
                        System.out.println("Unable to login. Please check your credentials.");
                        username = null;
                    }
                }
            } else if(choice == 2) {
                share();
            } else if(choice == 3) {
                compute();
            } else {
                System.exit(0);
            }
        }
    }
}
