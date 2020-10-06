package cs451;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

class GroundLayer {
    static Layer transport = null;
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
            listen();
        });
        thread.start();
    }

    public static void deliverTo(Layer transport) {
        GroundLayer.transport = transport;
    }

    public static void receive(Host source, String message) {
        System.err.println("Incorrect use of GroundLayer");
    }

    public static void listen() {
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

            String ipAddress = senderAddress.getHostAddress();
            Host senderHost = HostList.getHost(ipAddress, senderPort);
            transport.receive(senderHost, rcvdPayload);

            if ("**STOP**".equals(rcvdPayload)) {
                receiving = false;
            }
        }
        socket.close();
    }

    public static void send(Host host, String payload) {
        String destHost = host.getIp();
        int destPort = host.getPort();
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

    public void handleCrash(Host crashedHost) {
    }
    
}