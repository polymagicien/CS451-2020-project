package cs451;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UrbLayer implements Layer {

    Layer bebLayer;
    Layer upperLayer = null;
    Set<BroadcastMessage> forward;
    Set<BroadcastMessage> delivered;
    Map<BroadcastMessage, LinkedList<Host>> acked;

    Host me;

    public UrbLayer(List<Host> hosts, Host me){
        this.me = me;
        this.forward = Collections.synchronizedSet(new HashSet<>());
        this.delivered = Collections.synchronizedSet(new HashSet<>());
        this.acked = Collections.synchronizedMap(new HashMap<>());
        
        this.bebLayer = new BebLayer(hosts);
        this.bebLayer.deliverTo(this);
    }
    
    public void send(Host useless, String message) {
        BroadcastMessage broadcastMessage = new BroadcastMessage(me, message);
        forward.add(broadcastMessage);

        String formattedMessage = me.getId() + ";" + message;
        bebLayer.send(null, formattedMessage);
    }

    public void receive(Host immediateSender, String formattedMessage) {
        // Formating
        int originalSenderId = Integer.valueOf(formattedMessage.split(";", 2)[0]);
        Host originalSender = HostList.getHost(originalSenderId);
        String message = formattedMessage.split(";", 2)[1];
        BroadcastMessage broadcastMessage = new BroadcastMessage(originalSender, message);

        if (!acked.containsKey(broadcastMessage)){
            acked.put(broadcastMessage, new LinkedList<Host>());
        }
        acked.get(broadcastMessage).add(immediateSender);

        if(!forward.contains(broadcastMessage)) {
            forward.add(broadcastMessage);
            bebLayer.send(null, formattedMessage);
        }

        checkForDelivery(broadcastMessage);
    }

    public void checkForDelivery(BroadcastMessage broadcastMessage) {
        // System.out.println("-----------------------------");
        // System.out.println("Received : " + broadcastMessage);
        // for(Host host : acked.get(broadcastMessage)){
        //     System.out.print(host.getId() + "  ");
        // }
        // System.out.println("\n-----------------------------");
        synchronized(acked) {
            if (!delivered.contains(broadcastMessage) && acked.get(broadcastMessage).containsAll(PingLayer.getCorrectProcesses()) ) {
                delivered.add(broadcastMessage);
                deliver(broadcastMessage.getHost(), broadcastMessage.getMessage());
            }
        }
    }

    public void deliver(Host host, String message) {
        if (upperLayer != null)
            upperLayer.receive(host, message);
        else
            System.out.println("URB : " + host + " - " + message);
    }
    
    public void deliverTo(Layer layer) {
        this.upperLayer = layer;
    }

    public void handleCrash(Host crashedHost) {
        bebLayer.handleCrash(crashedHost);
    }
    
}
