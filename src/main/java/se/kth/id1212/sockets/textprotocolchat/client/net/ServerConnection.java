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
package se.kth.id1212.sockets.textprotocolchat.client.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.StringJoiner;
import se.kth.id1212.sockets.textprotocolchat.common.Constants;
import se.kth.id1212.sockets.textprotocolchat.common.MessageException;
import se.kth.id1212.sockets.textprotocolchat.common.MsgType;

/**
 * Manages all communication with the server.
 */
public class ServerConnection {
    private static final int TIMEOUT_HALF_HOUR = 1800000;
    private static final int TIMEOUT_HALF_MINUTE = 30000;
    private Socket socket;
    private PrintWriter toServer;
    private BufferedReader fromServer;
    private volatile boolean connected;

    /**
     * Creates a new instance and connects to the specified server. Also starts a listener thread
     * receiving broadcast messages from server.
     *
     * @param host             Host name or IP address of server.
     * @param port             Server's port number.
     * @param broadcastHandler Called whenever a broadcast is received from server.
     * @throws IOException If failed to connect.
     */
    public void connect(String host, int port, OutputHandler broadcastHandler) throws
            IOException {
        socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), TIMEOUT_HALF_MINUTE);
        socket.setSoTimeout(TIMEOUT_HALF_HOUR);
        connected = true;
        boolean autoFlush = true;
        toServer = new PrintWriter(socket.getOutputStream(), autoFlush);
        fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        new Thread(new Listener(broadcastHandler)).start();
    }

    /**
     * Closes the connection with the server and stops the broadcast listener thread.
     *
     * @throws IOException If failed to close socket.
     */
    public void disconnect() throws IOException {
        sendMsg(MsgType.DISCONNECT.toString());
        socket.close();
        socket = null;
        connected = false;
    }

    /**
     * Sends the user's username to the server. That username will be prepended to all messages
     * originating from this client, until a new username is specified.
     *
     * @param username The current user's username.
     */
    public void sendUsername(String username) {
        sendMsg(MsgType.USER.toString(), username);
    }

    /**
     * Sends a chat entry to the server, which will broadcast it to all clients, including the
     * sending client.
     *
     * @param msg The message to broadcast.
     */
    public void sendChatEntry(String msg) {
        sendMsg(MsgType.ENTRY.toString(), msg);
    }

    private void sendMsg(String... parts) {
        StringJoiner joiner = new StringJoiner(Constants.MSG_DELIMETER);
        for (String part : parts) {
            joiner.add(part);
        }
        toServer.println(joiner.toString());
    }

    private class Listener implements Runnable {
        private final OutputHandler outputHandler;

        private Listener(OutputHandler outputHandler) {
            this.outputHandler = outputHandler;
        }

        @Override
        public void run() {
            try {
                for (;;) {
                    outputHandler.handleMsg(extractMsgBody(fromServer.readLine()));
                }
            } catch (Throwable connectionFailure) {
                if (connected) {
                    outputHandler.handleMsg("Lost connection.");
                }
            }
        }

        private String extractMsgBody(String entireMsg) {
            String[] msgParts = entireMsg.split(Constants.MSG_DELIMETER);
            if (MsgType.valueOf(msgParts[Constants.MSG_TYPE_INDEX].toUpperCase()) != MsgType.BROADCAST) {
                throw new MessageException("Received corrupt message: " + entireMsg);
            }
            return msgParts[Constants.MSG_BODY_INDEX];
        }
    }
}
