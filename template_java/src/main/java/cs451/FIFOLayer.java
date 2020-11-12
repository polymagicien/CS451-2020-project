package cs451;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

public class FIFOLayer implements Layer {
    private static final long INITIAL_ORDERING_NUMBER = 1L;

    private Host me;
    private Layer urbLayer;
    private Layer upperLayer;
    private long orderingNumber;

    private HashMap<Host, PriorityQueue<PacketParser>> hostToMessages;
    private HashMap<Host, Long> hostToLastDelivered;

    private Set<Long> broadcastSent;
    private String log;

    public FIFOLayer(List<Host> hosts, Host me) {
        this.me = me;
        this.urbLayer = new UrbLayer(hosts, me);
        this.urbLayer.deliverTo(this);
        this.orderingNumber = INITIAL_ORDERING_NUMBER;

        this.hostToMessages = new HashMap<>();
        this.hostToLastDelivered = new HashMap<>();

        this.broadcastSent = Collections.synchronizedSet(new HashSet<>());
        this.log = "";
    }

    @Override
    public void send(Host host, String message) {
        broadcastSent.add(orderingNumber);
        log += "b " + orderingNumber + "\n";

        String rawMessage = orderingNumber + ";" + message;
        urbLayer.send(null, rawMessage);
        orderingNumber++;
    }

    @Override
    public void receive(Host host, String message) {
        PacketParser packet = new PacketParser(host, message);

        if (!hostToMessages.containsKey(host)) {
            hostToMessages.put(host, new PriorityQueue<PacketParser>(new SortBySequenceNumber()));
            hostToLastDelivered.put(host, INITIAL_ORDERING_NUMBER - 1);
        }

        hostToMessages.get(host).add(packet);
        // printFIFO(hostToMessages.get(host));
        long lastDelivered = hostToLastDelivered.get(host);
        if (packet.getSequenceNumber() == lastDelivered + 1) {
            PacketParser p;
            while ((p = hostToMessages.get(host).peek()) != null && p.getSequenceNumber() == lastDelivered + 1) {
                p = hostToMessages.get(host).poll();

                if (host.equals(me) ) {
                    broadcastSent.remove(p.getSequenceNumber());
                }
                log += "d " + host.getId() + " " + p.getSequenceNumber() + "\n";

                deliver(host, p.getData());
                lastDelivered++;
            }
            hostToLastDelivered.put(host, lastDelivered);
        }
    }

    private void printFIFO(PriorityQueue<PacketParser> priorityQueue) {
        System.out.println("---------------------------------");
        for (PacketParser p : priorityQueue) {
            System.out.print(p.getSequenceNumber() + ", ");
        }
        System.out.println("\n---------------------------------");
    }

    @Override
    public void deliverTo(Layer layer) {
        this.upperLayer = layer;
    }

    public void deliver(Host host, String message) {
        if (upperLayer != null) {
            upperLayer.receive(host, message);
        } else {
            System.out.println("FIFO : " + host + " - " + message);
        }
    }

    @Override
    public void handleCrash(Host crashedHost) {
        this.urbLayer.handleCrash(crashedHost);
    }

    public String waitFinishBroadcasting() {
        while (broadcastSent.size() > 0) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return log;
    }
    
}
