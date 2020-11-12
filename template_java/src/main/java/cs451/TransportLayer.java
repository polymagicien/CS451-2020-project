package cs451;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class TransportLayer implements Layer {
    Set<PacketIdentifier> delivered;
    Set<PacketIdentifier> acknowledged;
    SenderManager senderManager;
    Layer upperLayer = null;

    long maxSequence;

    TransportLayer() {
        delivered = Collections.synchronizedSet(new HashSet<>());
        acknowledged = Collections.synchronizedSet(new HashSet<PacketIdentifier>()); // Multi-thread proof
        maxSequence = 0;
        GroundLayer.deliverTo(this);

        senderManager = new SenderManager();
    }

    public void deliverTo(Layer layer) {
        this.upperLayer = layer; 
    }

    public void receive(Host sourceHost, String rcvdPayload) {
        // Check if ping
        if (rcvdPayload.contains(Constants.PING)) {
            PingLayer.handlePing(sourceHost.getIp(), sourceHost.getPort());
            return;
        }

        // Otherwise parse packet
        PacketParser parser = new PacketParser(sourceHost, rcvdPayload);
        PacketIdentifier packetId = parser.getPacketId();
        String rcvdData = parser.getData();
        if (Constants.ACK.equals(rcvdData)) {
            acknowledged.add(packetId);
        }
        else {
            sendAck(sourceHost, parser.getSequenceNumber());
            if (!delivered.contains(packetId)) {
                delivered.add(packetId);
                if (upperLayer != null){
                    upperLayer.receive(sourceHost, rcvdData);
                }
                else{
                    System.out.print("Transport : " + parser + "\n");
                }
            }
        }
    }

    public void send(Host destHost, String payload) {
        long sequenceNumber = ++maxSequence;
        String rawPayload = sequenceNumber + ";" + payload;
        PacketIdentifier packetId = new PacketIdentifier(destHost, sequenceNumber);

        senderManager.schedule(destHost, rawPayload, packetId);
    }

    public void sendAck(Host destHost, long sequenceNumber){
        String rawPayload = sequenceNumber + ";" + Constants.ACK;
        GroundLayer.send(destHost, rawPayload);
    }

    public void handleCrash(Host crashedHost) {
        senderManager.cancelMessageTo(crashedHost);
    }


    class SenderManager {
        private Timer timer;
        private Map<Host, LinkedList<TimerTask>> hostToTasks;
        private Set<Host> cancelled;
        
        public SenderManager() {
            this.timer = new Timer();
            this.hostToTasks = Collections.synchronizedMap(new HashMap<>());
            this.cancelled = Collections.synchronizedSet(new HashSet<>());
        }

        public void schedule(Host destHost, String payload, PacketIdentifier packetId) {
            // Define new task
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    
                    if (cancelled.contains(destHost) && hostToTasks.get(destHost) !=  null) {
                        for (TimerTask task : hostToTasks.get(destHost)){
                            if (task != null)
                                task.cancel();
                        }
                    }

                    if (acknowledged.contains(packetId)) {
                        this.cancel();
                    }
                    else{
                        GroundLayer.send(destHost, payload);
                    }
				}
            };

            if(!this.hostToTasks.containsKey(destHost)) {
                this.hostToTasks.put(destHost, new LinkedList<TimerTask>());
            }
            this.hostToTasks.get(destHost).add(task);

			this.timer.scheduleAtFixedRate(task, 0, Constants.DELAY_RETRANSMIT);
        }
        
        public void cancelMessageTo(Host host){
            cancelled.add(host);
        }
    }

    @Override
    public String waitFinishBroadcasting() {
        // TODO Auto-generated method stub
        return null;
    }
}