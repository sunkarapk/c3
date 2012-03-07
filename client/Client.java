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
    public static int zonalServerPort = 3333;

    public static String gridNodeIp = null;
    public static int gridNodePort = 4444;

    private static HttpClient client = new HttpClient();

    // function to do the compute use case
    public void compute(String fileName) {
        GetMethod method = new GetMethod(url + "/compute");

        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));

        try {
            int statusCode = client.executeMethod(method);

            if (statusCode != HttpStatus.SC_OK) {
                System.err.println("Method failed: " + method.getStatusLine());
            }

            byte[] responseBody = method.getResponseBody();
            String ipAndPort = new String(responseBody);

           //that string will contain ip and port
        } catch (HttpException e) {
            System.err.println("Fatal protocol violation: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Fatal transport error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            method.releaseConnnection();
        }
    }

    //function to do the join use case
    public void share() {
        PostMethod method = new PostMethod(url + "/share");
        method.addParameter("username", username);

        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));

        try {
            int statusCode = client.executeMethod(method);

            if (statusCode != HttpStatus.SC_OK) {
                System.err.println("Method failed: " + method.getStatusLine());
            }

            byte[] responseBody = method.getResponseBody();
            string ipAndPort = new String(responseBody);

            //that string will contain ip and port
        } catch (HttpException e) {
            System.err.println("Fatal protocol violation: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Fatal transport error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            method.releaseConnnection();
        }
    }

    public static void main(String args[]) {
        System.out.println("Vishwa Based Campus Compute Cloud");
        System.out.println("1. Login");
        System.out.println("2. Join the Grid");
        System.out.println("3. Avail the compute facility");
        System.out.println("4. Quit");
        System.out.printf("Enter your choice: ");

        if (ch == 1) {
            //Get input and set username
        } else if( ch == 2){
            share();
        } else if(ch == 3){
            System.out.println("Give the file name which has to be put in the grid for computation");
            compute();
        } else {
            System.exit(0);
        }
    }
}
