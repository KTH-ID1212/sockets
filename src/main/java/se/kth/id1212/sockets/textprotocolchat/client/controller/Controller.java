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
package se.kth.id1212.sockets.textprotocolchat.client.controller;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.CompletableFuture;
import se.kth.id1212.sockets.textprotocolchat.client.net.ServerConnection;
import se.kth.id1212.sockets.textprotocolchat.client.net.OutputHandler;

/**
 * This controller decouples the view from the network layer. All methods, except
 * <code>disconnect</code>, submit their task to the common thread pool, provided by
 * <code>ForkJoinPool.commonPool</code>, and then return immediately.
 */
public class Controller {
    private final ServerConnection serverConnection = new ServerConnection();

    /**
     * @see ServerConnection#connect(java.lang.String, int,
     * se.kth.id1212.sockets.textprotocolchat.client.net.BroadcastHandler)
     */
    public void connect(String host, int port, OutputHandler outputHandler) {
        CompletableFuture.runAsync(() -> {
            try {
                serverConnection.connect(host, port, outputHandler);
            } catch (IOException ioe) {
                throw new UncheckedIOException(ioe);
            }
        }).thenRun(() -> outputHandler.handleMsg("Connected to " + host + ":" + port));
    }

    /**
     * @see ServerConnection#disconnect() Blocks until disconnection is completed.
     */
    public void disconnect() throws IOException {
        serverConnection.disconnect();
    }

    /**
     * @see ServerConnection#sendUsername(java.lang.String)
     */
    public void sendUsername(String username) {
        CompletableFuture.runAsync(() -> serverConnection.sendUsername(username));
    }

    /**
     * @see ServerConnection#sendChatEntry(java.lang.String)
     */
    public void sendMsg(String msg) {
        CompletableFuture.runAsync(() -> serverConnection.sendChatEntry(msg));
    }
}
