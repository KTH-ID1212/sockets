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
package se.kth.id1212.sockets.udpstream.sender;

import java.io.IOException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

/**
 * Simulates a UDP live stream, by sending a sequence of numbers as fast as possible.
 */
public class UdpStreamSender {
    private static final int PORT_NO = 5000;
    private static final int PACKET_SIZE = Integer.BYTES;

    /**
     * @param args There is one command line parameter, the IP address of the stream destination.
     * @throws IOException If failed to write stream.
     */
    public static void main(String[] args) throws IOException {
        String destination = args[0];
        new UdpStreamSender().sendStream(destination);
    }

    private void sendStream(String destination) throws IOException {
        DatagramSocket toStream = new DatagramSocket();
        int dataToSend = 0;
        for (;;) {
            byte[] outBuffer = intToByteBuffer(dataToSend++);
            DatagramPacket packetToSend = new DatagramPacket(outBuffer, outBuffer.length,
                                                             new InetSocketAddress(destination,
                                                                                   PORT_NO));
            toStream.send(packetToSend);
        }
    }

    private byte[] intToByteBuffer(int i) {
        return BigInteger.valueOf(i).toByteArray();
    }
}
