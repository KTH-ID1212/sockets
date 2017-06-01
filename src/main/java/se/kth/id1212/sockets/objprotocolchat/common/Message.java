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
package se.kth.id1212.sockets.objprotocolchat.common;

import java.io.Serializable;

/**
 * A message between a chat client and the chat server.
 */
public class Message implements Serializable {
    private final MsgType type;
    private final String body;

    /**
     * Constructs a new <code>Message</code>, with the specified type and body.
     *
     * @param type The message type.
     * @param body The message body.
     */
    public Message(MsgType type, String body) {
        this.type = type;
        this.body = body;
    }

    /**
     * @return the message body
     */
    public String getBody() {
        return body;
    }

    /**
     * @return the message type
     */
    public MsgType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Message{" + "type=" + type + ", body=" + body + '}';
    }
}
