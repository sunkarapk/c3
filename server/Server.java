import java.net.*;
import java.io.*;
import java.util.*;
import org.apache.http.*;
import org.apache.http.entity.*;
import org.apache.http.impl.*;
import org.apache.http.params.*;
import org.apache.http.protocol.*;
import org.apache.http.util.*;

public class Server {

    public static void main(String[] args) throws Exception {
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

            String username = null;

            if (!method.equals("POST")) {
                throw new MethodNotSupportedException(method + " method not supported");
            }

            if (req instanceof HttpEntityEnclosingRequest) {
                username = EntityUtils.toString(((HttpEntityEnclosingRequest) req).getEntity());
            }

            if (target.equals("/share")) {
                res.setStatusCode(HttpStatus.SC_OK);
                res.setEntity(new StringEntity("10.6.9.199", "UTF-8"));
            } else if (target.equals("/compute")) {
                res.setStatusCode(HttpStatus.SC_OK);
                res.setEntity(new StringEntity("10.6.15.151", "UTF-8"));
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
