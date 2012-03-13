import java.io.*;
import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.client.*;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.*;

public class Client {

    private static String url = "http://10.6.9.199/";

    private static String username = null;

    public static String zonalServerIp = null;
    public static String gridNodeIp = null;

    public static String connIp = null;

    private static HttpClient client = new DefaultHttpClient();

    // function to do the compute use case
    public static void compute() throws Exception {
        HttpPost method = new HttpPost(url + "/compute");

        HttpEntity reqEntity = new StringEntity(username, "UTF-8");
        method.setEntity(reqEntity);

        String connIp = null;

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

        //Execute the vishwa compute process
        Process p = Runtime.getRuntime().exec("java -classpath " + pwd + "/vishwa/JVishwa.jar:. " + name + " " + connIp);

    }

    //function to do the join use case
    public static void share() throws Exception {
        HttpPost method = new HttpPost(url + "/share");

        HttpEntity reqEntity = new StringEntity(username, "UTF-8");
        method.setEntity(reqEntity);

        String connIp = null;

        try {
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            connIp = client.execute(method, responseHandler);
        } catch (IOException e) {
            System.err.println("Fatal transport error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            client.getConnectionManager().shutdown();
        }

        //Execute the vishwa share process
        Process p = Runtime.getRuntime().exec("java -jar vishwa/JVishwa.jar " + connIp);
    }

    public static void main(String args[]) throws Exception {
        int choice;

        System.out.println("Vishwa Based Campus Compute Cloud");
        System.out.println("1. Login");
        System.out.println("2. Join the Grid");
        System.out.println("3. Avail the compute facility");
        System.out.println("4. Quit");
        System.out.printf("Enter your choice: ");

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        choice = Integer.parseInt(in.readLine());

        if (choice == 1) {
            System.out.printf("Enter your username: ");
            username = in.readline();
        } else if(choice == 2) {
            share();
        } else if(choice == 3) {
            compute();
        } else {
            System.exit(0);
        }
    }
}
