package cs451;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class TransportLayer {
    static final String ACK = "**ACK**";
    static final int DELAY = 500;

    GroundLayer groundLayer;
    HashSet<PacketIdentifier> delivered;
    Set<PacketIdentifier> acknowledged;
    SenderManager senderManager;

    int maxSequence;

    TransportLayer(int listeningPort) {
        delivered = new HashSet<>();
        acknowledged = Collections.synchronizedSet(new HashSet<PacketIdentifier>()); // Multithread proof
        maxSequence = 0;
        groundLayer = new GroundLayer(listeningPort);
        groundLayer.transport = this;

        senderManager = new SenderManager();
    }

    public void receive(String sourceHostname, int sourcePort, String rcvdPayload) {
        PacketParser parser = new PacketParser(sourceHostname, sourcePort, rcvdPayload);
        PacketIdentifier packetId = parser.getPacketId();
        String rcvdData = parser.getData();


        if (ACK.equals(rcvdData)) {
            // System.out.println("ACK");
            acknowledged.add(packetId);
        }
        else {
            sendAck(sourceHostname, sourcePort, parser.getSequenceNumber());
            if (!delivered.contains(packetId)) {
                // System.out.println("DELIVERED");
                System.out.print("" + parser + "\t");
                delivered.add(packetId);
            } else {
                // System.out.println("Already delivered");
            }
        }
    }

    public void send(String destHostname, int destPort, String payload) {
        int sequenceNumber = ++maxSequence;
        String rawPayload = sequenceNumber + ";" + payload;
        PacketIdentifier packetId = new PacketIdentifier(destHostname, destPort, sequenceNumber);

        senderManager.schedule(destHostname, destPort, rawPayload, packetId);
    }

    public void sendAck(String destHostname, int destPort, int sequenceNumber){
        String rawPayload = sequenceNumber + ";" + ACK;
        groundLayer.send(destHostname, destPort, rawPayload);
    }

    class SenderManager {
        private Timer timer;
        
        public SenderManager() {
            this.timer = new Timer();
        }

        public synchronized void schedule(String hostname, int port, String payload, PacketIdentifier packetId) {
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
                        groundLayer.send(hostname, port, payload);
                    }
				}
			};
			this.timer.scheduleAtFixedRate(task, 0, DELAY);
		}
	}
}
