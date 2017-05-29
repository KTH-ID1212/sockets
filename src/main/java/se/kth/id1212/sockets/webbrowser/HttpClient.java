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
package se.kth.id1212.sockets.webbrowser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Sends an HTTP request and prints the response.
 */
public class HttpClient {
    /**
     * @param args The first command line argument is the host and the second is the port.
     */
    public static void main(String[] args) {
        String httpServer = args[0];
        int serverPort = Integer.parseInt(args[1]);
        int timeoutMillis = 10000;
        String httpRequest = "GET / HTTP/1.1";
        String hostHeader = "Host: " + httpServer;

        try (Socket socket = new Socket(httpServer, serverPort)) {
            socket.setSoTimeout(timeoutMillis);
            PrintWriter toServer = new PrintWriter(socket.getOutputStream());
            toServer.println(httpRequest);
            toServer.println(hostHeader);
            toServer.println();
            toServer.flush();
            BufferedReader fromServer
                    = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String str;
            while ((str = fromServer.readLine()) != null) {
                System.out.println(str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
