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
package se.kth.id1212.sockets.objprotocolchat.server.net;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UncheckedIOException;
import java.net.Socket;
import se.kth.id1212.sockets.objprotocolchat.common.MessageException;
import se.kth.id1212.sockets.objprotocolchat.common.MsgType;
import se.kth.id1212.sockets.objprotocolchat.common.Message;

/**
 * Handles all communication with one particular chat client.
 */
class ClientHandler implements Runnable {
    private static final String JOIN_MESSAGE = " joined conversation.";
    private static final String LEAVE_MESSAGE = " left conversation.";
    private static final String USERNAME_DELIMETER = ": ";
    private final ChatServer server;
    private final Socket clientSocket;
    private final String[] conversationWhenStarting;
    private ObjectInputStream fromClient;
    private ObjectOutputStream toClient;
    private String username = "anonymous";
    private boolean connected;

    /**
     * Creates a new instance, which will handle communication with one specific client connected to
     * the specified socket.
     *
     * @param clientSocket The socket to which this handler's client is connected.
     */
    ClientHandler(ChatServer server, Socket clientSocket, String[] conversation) {
        this.server = server;
        this.clientSocket = clientSocket;
        this.conversationWhenStarting = conversation;
        connected = true;
    }

    /**
     * The run loop handling all communication with the connected client.
     */
    @Override
    public void run() {
        try {
            fromClient = new ObjectInputStream(clientSocket.getInputStream());
            toClient = new ObjectOutputStream(clientSocket.getOutputStream());
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
        for (String entry : conversationWhenStarting) {
            sendMsg(entry);
        }
        while (connected) {
            try {
                Message msg = (Message) fromClient.readObject();
                switch (msg.getType()) {
                    case USER:
                        username = msg.getBody();
                        server.broadcast(username + JOIN_MESSAGE);
                        break;
                    case ENTRY:
                        server.broadcast(username + USERNAME_DELIMETER + msg.getBody());
                        break;
                    case DISCONNECT:
                        disconnectClient();
                        server.broadcast(username + LEAVE_MESSAGE);
                        break;
                    default:
                        throw new MessageException("Received corrupt message: " + msg);
                }
            } catch (IOException | ClassNotFoundException e) {
                disconnectClient();
                throw new MessageException(e);
            }
        }
    }

    /**
     * Sends the specified message to the connected client.
     *
     * @param msgBody The message to send.
     */
    void sendMsg(String msgBody) throws UncheckedIOException {
        try {
            Message msg = new Message(MsgType.BROADCAST, msgBody);
            toClient.writeObject(msg);
            toClient.flush();
            toClient.reset();
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }

    private void disconnectClient() {
        try {
            clientSocket.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        connected = false;
        server.removeHandler(this);
    }
}
