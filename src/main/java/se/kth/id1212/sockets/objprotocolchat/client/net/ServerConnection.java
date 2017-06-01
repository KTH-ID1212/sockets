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
package se.kth.id1212.sockets.objprotocolchat.client.net;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import se.kth.id1212.sockets.objprotocolchat.common.Message;
import se.kth.id1212.sockets.objprotocolchat.common.MessageException;
import se.kth.id1212.sockets.objprotocolchat.common.MsgType;

/**
 * Manages all communication with the server.
 */
public class ServerConnection {
    private Socket socket;
    private ObjectOutputStream toServer;
    private ObjectInputStream fromServer;
    private boolean connected;

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
        socket = new Socket(host, port);
        connected = true;
        toServer = new ObjectOutputStream(socket.getOutputStream());
        fromServer = new ObjectInputStream(socket.getInputStream());
        new Thread(new Listener(broadcastHandler)).start();
    }

    /**
     * Closes the connection with the server and stops the broadcast listener thread.
     *
     * @throws IOException If failed to close socket.
     */
    public void disconnect() throws IOException {
        sendMsg(MsgType.DISCONNECT, null);
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
    public void sendUsername(String username) throws IOException {
        sendMsg(MsgType.USER, username);
    }

    /**
     * Sends a chat entry to the server, which will broadcast it to all clients, including the
     * sending client.
     *
     * @param msg The message to broadcast.
     */
    public void sendChatEntry(String msg) throws IOException {
        sendMsg(MsgType.ENTRY, msg);
    }

    private void sendMsg(MsgType type, String body) throws IOException {
        Message msg = new Message(type, body);
        toServer.writeObject(msg);
        toServer.flush();
        toServer.reset();
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
                    outputHandler.handleMsg(extractMsgBody((Message) fromServer.readObject()));
                }
            } catch (Throwable connectionFailure) {
                if (connected) {
                    outputHandler.handleMsg("Lost connection.");
                }
            }
        }

        private String extractMsgBody(Message msg) {
            if (msg.getType() != MsgType.BROADCAST) {
                throw new MessageException("Received corrupt message: " + msg);
            }
            return msg.getBody();
        }
    }
}
