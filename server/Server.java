import java.net.*;
import java.io.*;
import java.util.*;
import org.apache.http.*;
import org.apache.http.entity.*;
import org.apache.http.impl.*;
import org.apache.http.params.*;
import org.apache.http.protocol.*;
import org.apache.http.util.*;
import java.sql.*;

public class Server {
    //Database credentials
    private static final String DB_URL = "jdbc:mysql://localhost/vishwa";
    private static final String USER = "root";
    private static final String PASS = "vishwa";

    public static Connection conn = null;
    public static Statement stmt = null;

    public static void main(String[] args) throws Exception {
        try{
            Class.forName("com.mysql.jdbc.Driver");
            System.out.println("Connecting to a selected database...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
        }catch(SQLException se){
                se.printStackTrace();
        }catch(Exception e){
                e.printStackTrace();
        }

        Thread t = new RequestListenerThread(8080);
        t.setDaemon(false);
        t.start();
    }

    public static class ApiHandler implements HttpRequestHandler {
        public ApiHandler() {
            super();
        }

        public void handle(final HttpRequest req, final HttpResponse res, final HttpContext con) throws HttpException, IOException {
            String method = req.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);
            String target = req.getRequestLine().getUri();

            String[] details = null;

            if (!method.equals("POST")) {
                throw new MethodNotSupportedException(method + " method not supported");
            }

            if (req instanceof HttpEntityEnclosingRequest) {
                String tmp = EntityUtils.toString(((HttpEntityEnclosingRequest) req).getEntity());
                details = tmp.split(";");
            }

            if (target.equals("/login")) {
                int valid = 0;
                try {
                    ResultSet r = stmt.executeQuery("SELECT COUNT(*) as rowcount FROM users where username='" + details[0] + "' and password='" + details[1] + "'");
                    r.next();
                    valid = r.getInt("rowcount");
                    r.close();
                } catch (SQLException se) {
                    se.printStackTrace();
                }

                res.setStatusCode(HttpStatus.SC_OK);
                if (valid == 1) {
                    res.setEntity(new StringEntity("true", "UTF-8"));
                } else {
                    res.setEntity(new StringEntity("false", "UTF-8"));
                }
            } else if (target.equals("/share")) {
                try {
                    ResultSet r = stmt.executeQuery("SELECT * FROM users where username='" + details[0] + "'");
                    r.next();
                    r.updateInt("on", 1);
                    r.updateString("ip", details[1]);
                    r.updateRow();
                    r.close();
                } catch (SQLException se) {
                    se.printStackTrace();
                }

                res.setStatusCode(HttpStatus.SC_OK);
                res.setEntity(new StringEntity("10.6.9.199", "UTF-8"));
            } else if (target.equals("/shareAck")) {
                try {
                    ResultSet r = stmt.executeQuery("SELECT * FROM users where username='" + details[0] + "'");
                    r.next();
                    r.updateInt("on", 0);
                    r.updateLong("credits", Credits.getAfterAdding(r.getLong("credits"), Long.parseLong(details[1])));
                    r.updateRow();
                    r.close();
                } catch (SQLException se) {
                    se.printStackTrace();
                }

                res.setStatusCode(HttpStatus.SC_OK);
            } else if (target.equals("/compute")) {
                String ip = null;
                try {
                    ResultSet r = stmt.executeQuery("SELECT ip FROM users where on='1'");
                    r.last();
                    r.absolute((int) (Math.random()*r.getRow()));
                    ip = r.getString("ip");
                    r.close();
                } catch (SQLException se) {
                    se.printStackTrace();
                }

                res.setStatusCode(HttpStatus.SC_OK);
                res.setEntity(new StringEntity(ip, "UTF-8"));
            } else if (target.equals("/computeAck")) {
                try {
                    ResultSet r = stmt.executeQuery("SELECT * FROM users where username='" + details[0] + "'");
                    r.next();
                    r.updateLong("credits", Credits.getAfterBurning(r.getLong("credits"), Long.parseLong(details[1])));
                    r.updateRow();
                    r.close();
                } catch (SQLException se) {
                    se.printStackTrace();
                }
                res.setStatusCode(HttpStatus.SC_OK);
            } else {
                res.setStatusCode(HttpStatus.SC_NOT_FOUND);
            }
        }
    }

    static class RequestListenerThread extends Thread {

        private final ServerSocket serversocket;
        private final HttpParams params;
        private final HttpService httpService;

        public RequestListenerThread(int port) throws IOException {
            this.serversocket = new ServerSocket(port);
            this.params = new SyncBasicHttpParams();

            this.params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 5000);
            this.params.setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024);
            this.params.setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false);
            this.params.setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true);
            this.params.setParameter(CoreProtocolPNames.ORIGIN_SERVER, "HttpComponents/1.1");

            // Set up the HTTP protocol processor
            HttpProcessor httpproc = new ImmutableHttpProcessor(new HttpResponseInterceptor[] {
                    new ResponseDate(),
                    new ResponseServer(),
                    new ResponseContent(),
                    new ResponseConnControl()
            });

            // Set up request handlers
            HttpRequestHandlerRegistry reqistry = new HttpRequestHandlerRegistry();
            reqistry.register("*", new ApiHandler());

            // Set up the HTTP service
            this.httpService = new HttpService(httpproc, new DefaultConnectionReuseStrategy(), new DefaultHttpResponseFactory(), reqistry, this.params);
        }

        public void run() {
            System.out.println("Listening on port " + this.serversocket.getLocalPort());
            while (!Thread.interrupted()) {
                try {
                    // Set up HTTP connection
                    Socket socket = this.serversocket.accept();
                    DefaultHttpServerConnection conn = new DefaultHttpServerConnection();
                    System.out.println("Incoming connection from " + socket.getInetAddress());
                    conn.bind(socket, this.params);

                    // Start worker thread
                    Thread t = new WorkerThread(this.httpService, conn);
                    t.setDaemon(true);
                    t.start();
                } catch (InterruptedIOException ex) {
                    break;
                } catch (IOException e) {
                    System.err.println("I/O error initialising connection thread: "
                            + e.getMessage());
                    break;
                }
            }
        }
    }

    public static class WorkerThread extends Thread {

        private final HttpService httpservice;
        private final HttpServerConnection conn;

        public WorkerThread(final HttpService httpservice, final HttpServerConnection conn) {
            super();
            this.httpservice = httpservice;
            this.conn = conn;
        }

        public void run() {
            System.out.println("New connection thread");
            HttpContext context = new BasicHttpContext(null);
            try {
                while (!Thread.interrupted() && this.conn.isOpen()) {
                    this.httpservice.handleRequest(this.conn, context);
                }
            } catch (ConnectionClosedException ex) {
                System.err.println("Client closed connection");
            } catch (IOException ex) {
                System.err.println("I/O error: " + ex.getMessage());
            } catch (HttpException ex) {
                System.err.println("Unrecoverable HTTP protocol violation: " + ex.getMessage());
            } finally {
                try {
                    this.conn.shutdown();
                } catch (IOException ignore) {}
            }
        }

    }
}
