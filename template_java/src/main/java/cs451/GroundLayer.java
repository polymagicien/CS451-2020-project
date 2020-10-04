package cs451;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

class GroundLayer {
    static TransportLayer transport;
    static Thread thread;

    private static int listeningPort;
    private static boolean receiving = true;
    private static DatagramSocket socket;
    private static byte[] buf = new byte[256];

    public static void start(int listeningPort) {
        GroundLayer.listeningPort = listeningPort;
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

    public static void deliverTo (TransportLayer transport) {
        GroundLayer.transport = transport;
    }

    public static void receive() {
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

            transport.receive(senderAddress.getHostAddress(), senderPort, rcvdPayload);

            if ("**STOP**".equals(rcvdPayload)) {
                receiving = false;
            }
        }
        socket.close();
    }

    public static void send(String destHost, int destPort, String payload) {
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