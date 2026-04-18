package Client;

import java.io.*;
import java.net.*;

import Common.MyLogger;
import Common.Packet;
import Common.PacketType;
import Common.Settings;

public class Receiver {
    private MyLogger logger = new MyLogger("Client.Receiver");

    public void getFile(String fn) {
        Packet packet = new Packet(PacketType.REQ, 0, fn.length(), fn.getBytes());

        try {
            receiveFile(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendAck(int sequenceNumber, InetAddress IPAddress, DatagramSocket clientSocket) throws IOException {
        Packet packet = new Packet(PacketType.ACK, sequenceNumber, 0, new byte[0]);

        DatagramPacket sendPacket = new DatagramPacket(
                packet.getBytes(),
                packet.getBytes().length,
                IPAddress,
                Settings.PORT
        );

        clientSocket.send(sendPacket);
        logger.sent(packet.getBytes());
    }

    private void receiveFile(Packet packet) throws Exception {
        byte[] packetBytes = packet.getBytes();
        byte[] receiveData = new byte[1000];

        DatagramSocket clientSocket = new DatagramSocket();
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

        String file = "";

        int packetsReceived = 0;
        int nextSeqno = 0;
        int lastSeqno = 0;
        int totalData = 0;

        clientSocket.setSoTimeout(ONE_SECOND_IN_MS);

        InetAddress IPAddress = InetAddress.getByName(LOCALHOST);

        DatagramPacket sendPacket = new DatagramPacket(
                packetBytes,
                packetBytes.length,
                IPAddress,
                Settings.PORT
        );

        clientSocket.send(sendPacket);
        logger.sent(packetBytes);

        boolean haveEOT = false;
        while (!haveEOT) {
            try {
                clientSocket.receive(receivePacket);

                byte[] incomingBytes = new byte[receivePacket.getLength()];
                System.arraycopy(receivePacket.getData(), 0, incomingBytes, 0, receivePacket.getLength());

                logger.received(incomingBytes);

                Packet incoming = new Packet(incomingBytes);

                if (incoming.getType() == PacketType.ERR) {
                    logger.info("Error: " + new String(incoming.getData()));
                    clientSocket.close();
                    return;
                }

                if (incoming.getSeqNo() == nextSeqno) {
                    if (incoming.getType() != PacketType.EOT) {
                        file += new String(incoming.getData(), 0, incoming.getSize());
                        totalData += incoming.getSize();
                    }

                    if (incoming.getType() == PacketType.EOT) {
                        haveEOT = true;
                    }

                    lastSeqno = incoming.getSeqNo();
                    sendAck(incoming.getSeqNo(), IPAddress, clientSocket);

                    nextSeqno = (nextSeqno + 1) % Settings.WINDOW_SIZE;
                    packetsReceived++;
                } else if (packetsReceived == 0) {
                    // do nothing on unexpected first packet
                } else {
                    logger.info("Received a packet, but not the one we're expecting.");
                    sendAck(lastSeqno, IPAddress, clientSocket);
                }

                receivePacket.setLength(receiveData.length);

            } catch (SocketTimeoutException e) {
                logger.info("Receive timeout");
            }
        }

        clientSocket.close();
        logger.info("Received the file containing: " + totalData + "(bytes) of data");
        System.out.println(file);
    }

    private static int ONE_SECOND_IN_MS = 1000;
    private static String LOCALHOST = "127.0.0.1";
}