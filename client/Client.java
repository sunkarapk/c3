import java.net.*;
import java.io.*;
import java.util.*;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.params.HttpMethodParams;

public class Client {

    private static String url = "http://10.6.15.118/";

    private static String username = null;

    public static String zonalServerIp = null;
    public static String gridNodeIp = null;

    public static String connIp = null;

    private static HttpClient client = new HttpClient();

    // function to do the compute use case
    public static void compute() {
        PostMethod method = new PostMethod(url + "/compute");
        method.addParameter("username", username);

        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));

        try {
            int statusCode = client.executeMethod(method);

            if (statusCode != HttpStatus.SC_OK) {
                System.err.println("Method failed: " + method.getStatusLine());
            }

            byte[] responseBody = method.getResponseBody();
            connIp = new String(responseBody);
        } catch (HttpException e) {
            System.err.println("Fatal protocol violation: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Fatal transport error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            method.releaseConnnection();
        }
        System.out.println("Give the file name which has to be put in the grid for computation");

        //input of the file name to be computed
        BufferedReader in=new BufferedReader(new InputStreamReader(System.in));
        String name=in.readLine();

        //get the absolute path of the current working directory
        File directory = new File (".");
        String pwd = directory.getAbsolutePath();

        //Execute the vishwa compute process
        Process p = Runtime.getRuntime().exec("java -classpath " + pwd + "/JVishwa.jar:. " + name + " " + connIp);

    }

    //function to do the join use case
    public static void share() {
        PostMethod method = new PostMethod(url + "/share");
        method.addParameter("username", username);

        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));

        try {
            int statusCode = client.executeMethod(method);

            if (statusCode != HttpStatus.SC_OK) {
                System.err.println("Method failed: " + method.getStatusLine());
            }

            byte[] responseBody = method.getResponseBody();
            connIp = new String(responseBody);
        } catch (HttpException e) {
            System.err.println("Fatal protocol violation: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Fatal transport error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            method.releaseConnnection();
        }

        //Execute the vishwa share process
        Process p = Runtime.getRuntime().exec("java -jar JVishwa.jar " + connIp);
    }

    public static void main(String args[]) {
        int ch;
        System.out.println("Vishwa Based Campus Compute Cloud");
        System.out.println("1. Login");
        System.out.println("2. Join the Grid");
        System.out.println("3. Avail the compute facility");
        System.out.println("4. Quit");
        System.out.println("Enter your choice: ");

        BufferedReader in=new BufferedReader(new InputStreamReader(System.in));
        String name=in.readLine();
        ch = Integer.parseInt(name);

        if (ch == 1) {
            //Get input and set username
        } else if( ch == 2) {
            share();
        } else if(ch == 3) {
            compute();
        } else {
            System.exit(0);
        }
    }
}
