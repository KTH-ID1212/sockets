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
package se.kth.id1212.sockets.textprotocolchat.server.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Holds the entire conversation, including all messages from all clients. All methods are thread
 * safe.
 */
public class Conversation {
    private final List<String> entries = Collections.synchronizedList(new ArrayList<>());

    /**
     * Appends the specified entry to the conversation.
     *
     * @param entry The entry to append.
     */
    public void appendEntry(String entry) {
        entries.add(entry);
    }

    /**
     * @return All entries in the conversation, in the order they were entered.
     */
    public String[] getConversation() {
        return entries.toArray(new String[0]);
    }
}
