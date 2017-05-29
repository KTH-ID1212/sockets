/*
 * The MIT License
 *
 * Copyright 2017 Leif Lindbäck <leifl@kth.se>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package se.kth.id1212.sockets.webserver;

import java.net.Socket;
import java.net.ServerSocket;
import java.io.IOException;
import java.io.File;

/**
 * A simple HTTP server.
 */
public class HttpServer {
    private static final int LINGER_TIME = 5000;
    private File rootDir = new File("www");
    private int portNo = 8080;

    /**
     * @param args The first command line argument is the directory where the server will look for
     *             requested files, the default value is <code>www</code>. The second argument is
     *             the port number on which the server will listen, the default is
     *             <code>8080</code>.
     */
    public static void main(String[] args) {
        HttpServer server = new HttpServer();
        server.parseArguments(args);
        server.start();
    }

    private void start() {
        try {
            ServerSocket listeningSocket = new ServerSocket(portNo);
            while (true) {
                Socket clientSocket = listeningSocket.accept();
                clientSocket.setSoLinger(true, LINGER_TIME);
                Thread handler = new Thread(new RequestHandler(clientSocket, rootDir));
                handler.setPriority(Thread.MAX_PRIORITY);
                handler.start();
            }
        } catch (IOException e) {
            System.err.println("Server failure.");
        }
    }

    private void parseArguments(String[] arguments) {
        if (arguments.length > 0) {
            rootDir = new File(arguments[0]);
        }
        if (arguments.length > 1) {
            try {
                portNo = Integer.parseInt(arguments[1]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number, using default.");
            }
        }
    }
}
