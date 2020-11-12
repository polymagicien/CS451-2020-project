package cs451;

import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

public class FIFOLayer implements Layer{

    private Layer urbLayer;
    private Layer upperLayer;
    private long orderingNumber;


    private HashMap<Host, PriorityQueue<PacketParser>> hostToMessages;
    private HashMap<Host, Long> hostToLastDelivered;


    public FIFOLayer(List<Host> hosts, Host me) {
        this.urbLayer = new UrbLayer(hosts, me);
        this.urbLayer.deliverTo(this);
        this.orderingNumber = 0;

        this.hostToMessages = new HashMap<>();
        this.hostToLastDelivered = new HashMap<>();
    }

    @Override
    public void send(Host host, String message) {
        String rawMessage = orderingNumber++ + ";" + message;
        urbLayer.send(null, rawMessage);
    }

    @Override
    public void receive(Host host, String message) {
        PacketParser packet = new PacketParser(host, message);

        if (!hostToMessages.containsKey(host)){
            hostToMessages.put(host, new PriorityQueue<PacketParser>(new SortBySequenceNumber()));
            hostToLastDelivered.put(host, -1L);
        }

        hostToMessages.get(host).add(packet);
        // printFIFO(hostToMessages.get(host));
        long lastDelivered = hostToLastDelivered.get(host);
        if (packet.getSequenceNumber() == lastDelivered + 1) {
            PacketParser p;
            while ( (p = hostToMessages.get(host).peek()) != null && p.getSequenceNumber() == lastDelivered + 1) {
                p = hostToMessages.get(host).poll();
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
        if (upperLayer != null){
            upperLayer.receive(host, message);
        } else {
            System.out.println("FIFO : " + host + " - " + message);
        }
    }

    @Override
    public void handleCrash(Host crashedHost) {
        this.urbLayer.handleCrash(crashedHost);
    }
    
}
