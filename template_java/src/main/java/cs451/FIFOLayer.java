package cs451;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;

public class FIFOLayer implements Layer {
    private static final long INITIAL_ORDERING_NUMBER = 1L;

    private Host me;
    private Layer urbLayer;
    private Layer upperLayer;
    private long orderingNumber;

    private Map<Host, PriorityQueue<PacketParser>> hostToMessages;
    private Map<Host, Long> hostToLastDelivered;

    private Set<Long> broadcastSent;
    private List<String> log;

    public FIFOLayer(List<Host> hosts, Host me) {
        this.me = me;
        this.urbLayer = new UrbLayer(hosts, me);
        this.urbLayer.deliverTo(this);
        this.orderingNumber = INITIAL_ORDERING_NUMBER;

        this.hostToMessages = Collections.synchronizedMap(new HashMap<>());
        this.hostToLastDelivered = Collections.synchronizedMap(new HashMap<>());

        this.broadcastSent = Collections.synchronizedSet(new HashSet<>());
        this.log = Collections.synchronizedList(new LinkedList<>());
    }

    @Override
    public void send(Host host, String message) {
        broadcastSent.add(orderingNumber);
        log.add("b " + orderingNumber + "\n");

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
                log.add("d " + host.getId() + " " + p.getSequenceNumber() + "\n");

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

    public String waitFinishBroadcasting(boolean retString) {
        while (broadcastSent.size() > 0) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        if (retString) {
            return log.stream().collect(Collectors.joining(""));
        }
        return "";

    }
    
}
