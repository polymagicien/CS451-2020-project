package cs451;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class TransportLayer {
    static final String ACK = "**ACK**";
    static final int DELAY = 500;

    HashSet<PacketIdentifier> delivered;
    Set<PacketIdentifier> acknowledged;
    SenderManager senderManager;

    int maxSequence;

    TransportLayer(int listeningPort) {
        delivered = new HashSet<>();
        acknowledged = Collections.synchronizedSet(new HashSet<PacketIdentifier>()); // Multithread proof
        maxSequence = 0;
        GroundLayer.start(listeningPort);
        GroundLayer.deliverTo(this);

        senderManager = new SenderManager();
    }

    public void receive(String sourceAddress, int sourcePort, String rcvdPayload) {
        // Check if ping
        if (rcvdPayload.contains(Constants.PING)) {
            PingLayer.handlePing(sourceAddress, sourcePort);
            return;
        }


        PacketParser parser = new PacketParser(sourceAddress, sourcePort, rcvdPayload);
        PacketIdentifier packetId = parser.getPacketId();
        String rcvdData = parser.getData();
        if (Constants.ACK.equals(rcvdData)) {
            acknowledged.add(packetId);
        }
        else {
            sendAck(sourceAddress, sourcePort, parser.getSequenceNumber());
            if (!delivered.contains(packetId)) {
                // System.out.println("DELIVERED");
                System.out.print("" + parser + "\n");
                delivered.add(packetId);
            } else {
                // System.out.println("Already delivered");
            }
        }
    }

    public void send(String destAddress, int destPort, String payload) {
        int sequenceNumber = ++maxSequence;
        String rawPayload = sequenceNumber + ";" + payload;
        PacketIdentifier packetId = new PacketIdentifier(destAddress, destPort, sequenceNumber);

        senderManager.schedule(destAddress, destPort, rawPayload, packetId);
    }

    public void sendAck(String destAddress, int destPort, int sequenceNumber){
        String rawPayload = sequenceNumber + ";" + Constants.ACK;
        GroundLayer.send(destAddress, destPort, rawPayload);
    }

    class SenderManager {
        private Timer timer;
        
        public SenderManager() {
            this.timer = new Timer();
        }

        public synchronized void schedule(String address, int port, String payload, PacketIdentifier packetId) {
            // Define new task
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    if (acknowledged.contains(packetId)) {
                        // System.out.println("ACKED : " + packetId);
                        this.cancel();
                    }
                    else{
                        // System.out.println("Sending");
                        GroundLayer.send(address, port, payload);
                    }
				}
			};
			this.timer.scheduleAtFixedRate(task, 0, DELAY);
		}
	}
}
