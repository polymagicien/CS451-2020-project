package cs451;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

class GroundLayer {
    TransportLayer transport;
    Thread thread;

    private int listeningPort;
    private boolean receiving = true;
    private DatagramSocket socket;
    private byte[] buf = new byte[256];

    GroundLayer(int listeningPort) {
        this.listeningPort = listeningPort;
        try {
            socket = new DatagramSocket(listeningPort);
        } catch (SocketException e) {
            System.out.println("Error while opening socket");
            e.printStackTrace();
        }

        // Start listening thread
        thread = new Thread(() -> {
            receive();
        });
        thread.start();
    }

    public void receive() {
        while (receiving) {
            DatagramPacket rcvdPacket = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(rcvdPacket);
            } catch (IOException e) {
                System.out.println("Error while receiving packet");
                e.printStackTrace();
            }

            InetAddress senderAddress = rcvdPacket.getAddress();
            int senderPort = rcvdPacket.getPort();
            String rcvdPayload = new String(rcvdPacket.getData(), 0, rcvdPacket.getLength());

            transport.receive(senderAddress.getHostName(), senderPort, rcvdPayload);

            if ("**STOP**".equals(rcvdPayload)) {
                receiving = false;
            }
        }
        socket.close();
    }

    public void send(String destHost, int destPort, String payload) {
        byte[] buf = payload.getBytes();
        InetAddress address;
        try {
            address = InetAddress.getByName(destHost);
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, destPort);
            try {
                socket.send(packet);
            } catch (IOException e) {
                System.out.println("Error while sending payload");
                e.printStackTrace();
            }
        } catch (UnknownHostException e1) {
            System.out.println("Unknown destination hostname");
            e1.printStackTrace();
        }
    }
    
}