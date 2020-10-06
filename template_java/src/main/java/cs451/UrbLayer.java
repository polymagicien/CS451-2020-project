package cs451;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UrbLayer implements Layer {

    Layer bebLayer;
    Layer upperLayer;
    Set<BroadcastMessage> forward;
    Set<BroadcastMessage> delivered;
    HashMap<BroadcastMessage, HashSet<Host>> acked;

    Host me;


    public UrbLayer(List<Host> hosts, Host me){
        this.me = me;
        this.forward = Collections.synchronizedSet(new HashSet<>());
        this.delivered = Collections.synchronizedSet(new HashSet<>());
        this.acked = new HashMap<>();
        
        this.bebLayer = new BebLayer(hosts);


    }
    
    public void send(Host useless, String message) {
        BroadcastMessage broadcastMessage = new BroadcastMessage(me, message);
        forward.add(broadcastMessage);
        acked.put(broadcastMessage, new HashSet<>());

        String formattedMessage = me.getId() + ";" + message;
        bebLayer.send(null, formattedMessage);
    }

    public void receive(Host immediateSender, String formattedMessage) {
        // Formating
        int originalSenderId = Integer.valueOf(formattedMessage.split(";", 2)[0]);
        Host originalSender = HostList.getHost(originalSenderId);
        String message = formattedMessage.split(";", 2)[1];
        BroadcastMessage broadcastMessage = new BroadcastMessage(originalSender, message);

        acked.get(broadcastMessage).add(immediateSender);
        if(!forward.contains(broadcastMessage)) {
            forward.add(broadcastMessage);
            bebLayer.send(null, formattedMessage);
        }

        checkForDelivery(broadcastMessage);
    }

    public void checkForDelivery(BroadcastMessage broadcastMessage) {
        if (forward.containsAll(PingLayer.getCorrectProcesses()) && !delivered.contains(broadcastMessage)) {
            delivered.add(broadcastMessage);
            upperLayer.receive(broadcastMessage.getHost(), broadcastMessage.getMessage());
        }
    }
    
    public void deliverTo(Layer layer) {
        System.err.println("Incorrect use of UrbLayer");
    }

    public void handleCrash(Host crashedHost) {
        bebLayer.handleCrash(crashedHost);
    }
    
}
