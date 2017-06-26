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
package se.kth.id1212.sockets.udpstream.reader;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * A simple UDP stream reader. Reads data from a UDP socket and prints to <code>System.out</code>.
 *
 * A UDP stream can be produced by <code>UdpStreamSender</code>, or started with for example the
 * following command (linux):
 * <p>
 * <code>gst-launch-1.0 -v ximagesrc
 * use-damage=false xname=/usr/lib/torcs/torcs-bin ! videoconvert ! videoscale !
 * video/x-raw,format=I420,width=800,height=600,framerate=25/1 ! jpegenc !
 * rtpjpegpay ! udpsink host=127.0.0.1 port=5000</code>
 * </p>
 */
public class UdpStreamReaderSingleThreadedSlow {
    private static final int PORT_NO = 5000;
    private static final int PACKET_SIZE = Integer.BYTES;
    private static final int HEX_RADIX = 16;

    private void handleStream() throws IOException, SocketException {
        DatagramSocket fromSender = new DatagramSocket(PORT_NO);
        byte[] receivedData = new byte[PACKET_SIZE];
        DatagramPacket receivedPacket = new DatagramPacket(receivedData,
                receivedData.length);
        for (;;) {
            fromSender.receive(receivedPacket);
            for (int i=0; i< receivedPacket.getLength(); i++) {
                System.out.print(unsignedHexString(receivedData[i]) + " ");
            }
            System.out.println();
        }
    }

    private String unsignedHexString(byte value) {
        return String.format("%02X", value);
    }

    /**
     * @param args There are no command line arguments.
     * @throws IOException If failed to read stream.
     */
    public static void main(String[] args) throws IOException {
        new UdpStreamReaderSingleThreadedSlow().handleStream();
    }
}
