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
package se.kth.id1212.sockets.textprotocolchat.server.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.net.Socket;
import java.util.StringJoiner;
import se.kth.id1212.sockets.textprotocolchat.common.Constants;
import se.kth.id1212.sockets.textprotocolchat.common.MessageException;
import se.kth.id1212.sockets.textprotocolchat.common.MsgType;

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
    private BufferedReader fromClient;
    private PrintWriter toClient;
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
            boolean autoFlush = true;
            fromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            toClient = new PrintWriter(clientSocket.getOutputStream(), autoFlush);
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
        for (String entry : conversationWhenStarting) {
            sendMsg(entry);
        }
        while (connected) {
            try {
                Message msg = new Message(fromClient.readLine());
                switch (msg.msgType) {
                    case USER:
                        username = msg.msgBody;
                        server.broadcast(username + JOIN_MESSAGE);
                        break;
                    case ENTRY:
                        server.broadcast(username + USERNAME_DELIMETER + msg.msgBody);
                        break;
                    case DISCONNECT:
                        disconnectClient();
                        server.broadcast(username + LEAVE_MESSAGE);
                        break;
                    default:
                        throw new MessageException("Received corrupt message: " + msg.receivedString);
                }
            } catch (IOException ioe) {
                disconnectClient();
                throw new MessageException(ioe);
            }
        }
    }

    /**
     * Sends the specified message to the connected client.
     *
     * @param msg The message to send.
     */
    void sendMsg(String msg) {
        StringJoiner joiner = new StringJoiner(Constants.MSG_DELIMETER);
        joiner.add(MsgType.BROADCAST.toString());
        joiner.add(msg);
        toClient.println(joiner.toString());
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

    private static class Message {
        private MsgType msgType;
        private String msgBody;
        private String receivedString;

        private Message(String receivedString) {
            parse(receivedString);
            this.receivedString = receivedString;
        }

        private void parse(String strToParse) {
            try {
                String[] msgTokens = strToParse.split(Constants.MSG_DELIMETER);
                msgType = MsgType.valueOf(msgTokens[Constants.MSG_TYPE_INDEX].toUpperCase());
                if (hasBody(msgTokens)) {
                    msgBody = msgTokens[Constants.MSG_BODY_INDEX];
                }
            } catch (Throwable throwable) {
                throw new MessageException(throwable);
            }
        }
        
        private boolean hasBody(String[] msgTokens) {
            return msgTokens.length > 1;
        }
    }
}
