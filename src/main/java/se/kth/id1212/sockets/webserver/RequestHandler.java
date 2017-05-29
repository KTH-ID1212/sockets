package se.kth.id1212.sockets.webserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A very simple HTTP server. All it can do is deliver a file in response to an HTTP GET request.
 */
public class RequestHandler implements Runnable {
    private static final String SERVER_ID_HEADER = "Server: Httpd 1.0";
    private static final String HTTP_GET_METHOD = "GET";
    private static final String HTTP_OK_RESPONSE = "HTTP/1.0 200 OK";
    private static final String NOT_FOUND_RESPONSE = "HTTP/1.0 404 File Not Found";
    private static final String NOT_FOUND_HTML = "<HTML><HEAD><TITLE>File Not Found</TITLE></HEAD><BODY><H1>HTTP Error 404: File Not Found</H1></BODY></HTML>";
    private static final String HTTP_NOT_IMPL_RESPONSE = "HTTP/1.0 501 Not Implemented";
    private static final String NOT_IMPL_HTML = "<HTML><HEAD><TITLE>Not Implemented</TITLE></HEAD><BODY><H1>HTTP Error 501: Not Implemented</H1></BODY></HTML>";
    private Socket clientSock;
    private File rootDir;

    RequestHandler(Socket clientSock, File rootDir) {
        this.clientSock = clientSock;
        this.rootDir = rootDir;
    }

    @Override
    public void run() {
        try {
            HttpRequest request = readRequest();
            if (request == null) {
                return;
            }
            if (request.httpMethod.equals(HTTP_GET_METHOD)) {
                handleGetRequest(request);
            } else {
                sendErrorMessage(HTTP_NOT_IMPL_RESPONSE, NOT_IMPL_HTML, request.httpVersion);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                clientSock.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    private HttpRequest readRequest() throws IOException {
        BufferedReader fromClient = new BufferedReader(
                new InputStreamReader(clientSock.getInputStream()));
        String requestLine = fromClient.readLine();
        if (requestLine == null) {
            return null;
        }
        String[] requestTokens = requestLine.split(" ");
        HttpRequest request = new HttpRequest(requestTokens[0], requestTokens[1],
                                              requestTokens[2]);
        while ((requestLine = fromClient.readLine()) != null && !requestLine.trim().equals("")) {
            request.addHeader(requestLine);
        }
        return request;
    }

    private void handleGetRequest(HttpRequest request) throws IOException {
        OutputStream toClient = clientSock.getOutputStream();
        if (request.path.endsWith("/")) {
            request.path = request.path + "index.html";
        }
        try {
            byte[] fileContent = readFile(removeInitialSlash(request.path));
            if (request.httpVersion.startsWith("HTTP/")) {
                PrintWriter pw = new PrintWriter(toClient);
                pw.println(HTTP_OK_RESPONSE);
                pw.println("Date:" + LocalDateTime.now());
                pw.println(SERVER_ID_HEADER);
                pw.println("Content-length: " + fileContent.length);
                pw.println("Content-type: " + getMimeFromExtension(request.path));
                pw.println();
                pw.flush();
            }
            toClient.write(fileContent);
        } catch (IOException ioe) {
            sendErrorMessage(NOT_FOUND_RESPONSE, NOT_FOUND_HTML, request.httpVersion);
            ioe.printStackTrace();
        }
    }

    private String removeInitialSlash(String source) {
        return source.substring(1, source.length());
    }

    private byte[] readFile(String filePathRelativeRootDir) throws IOException {
        File file = new File(rootDir, filePathRelativeRootDir);
        try (FileInputStream fromFile = new FileInputStream(file)) {
            byte[] buf = new byte[(int) file.length()];
            fromFile.read(buf);
            return buf;
        }
    }

    private String getMimeFromExtension(String name) {
        if (name.endsWith(".html") || name.endsWith(".htm")) {
            return "text/html";
        } else if (name.endsWith(".txt") || name.endsWith(".java")) {
            return "text/plain";
        } else if (name.endsWith(".gif")) {
            return "image/gif";
        } else if (name.endsWith(".class")) {
            return "application/octet-stream";
        } else if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
            return "image/jpeg";
        } else {
            return "text/plain";
        }
    }

    private void sendErrorMessage(String code, String html, String version) throws IOException {
        PrintWriter pw = new PrintWriter(clientSock.getOutputStream());
        if (version.startsWith("HTTP/")) {
            pw.println(code);
            pw.println("Date:" + (new Date()));
            pw.println(SERVER_ID_HEADER);
            pw.println("Content-type: text/html");
            pw.println();
        }
        pw.println(html);
        pw.flush();
    }

    private static class HttpRequest {
        private String httpMethod;
        private String path;
        private String httpVersion;
        private List<String> headers = new ArrayList<>();

        private HttpRequest(String httpMethod, String path, String httpVersion) {
            this.httpMethod = httpMethod;
            this.path = path;
            this.httpVersion = httpVersion;
        }

        private void addHeader(String header) {
            headers.add(header);
        }
    }
}
