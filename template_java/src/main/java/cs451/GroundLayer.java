package cs451;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.LinkedList;

class GroundLayer {
    private static Layer transport = null;
    private static Thread thread;

    private static boolean receiving = true;
    private static DatagramSocket socket;
    private static byte[] buf = new byte[256];

    private static SynchronizedLinkedList<BroadcastMessage> receivedMessages;
    private static LinkedList<Thread> threadPool;


    public static void start(int listeningPort) {
        // create socket
        try {
            socket = new DatagramSocket(listeningPort);
        } catch (SocketException e) {
            System.out.println("Error while opening socket");
            e.printStackTrace();
        }

        // create thread pool handling the received messages
        threadPool = new LinkedList<Thread>();
        receivedMessages = new SynchronizedLinkedList<>();
        for (int i = 0; i < Constants.numThreadGroundLayer; i++) {
            Thread t = new Thread(() -> {
                while(true){
                    BroadcastMessage m = receivedMessages.removeFirst();
                    transport.receive(m.getHost(), m.getMessage());
                }
            });
            threadPool.add(t);
            t.start();
        }

        // start listening thread
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
            // save message in pool (will be handled by threads in pool)
            receivedMessages.add(new BroadcastMessage(senderHost, rcvdPayload));

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