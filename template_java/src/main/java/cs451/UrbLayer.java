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

    Integer messagesInBroadcast;
    SynchronizedLinkedList<String> messagesToBroadcast;

    Host me;

    public UrbLayer(List<Host> hosts, Host me) {
        this.me = me;
        this.forward = Collections.synchronizedSet(new HashSet<>());
        this.delivered = Collections.synchronizedSet(new HashSet<>());
        this.acked = Collections.synchronizedMap(new HashMap<>());

        this.messagesToBroadcast = new SynchronizedLinkedList<>();
        this.messagesInBroadcast = 0;

        this.bebLayer = new BebLayer(hosts);
        this.bebLayer.deliverTo(this);
        PingLayer.start(hosts, me);
    }
    
    public synchronized void send(Host useless, String message) {
        BroadcastMessage broadcastMessage = new BroadcastMessage(me, message);

        if (!acked.containsKey(broadcastMessage)){
            acked.put(broadcastMessage, new LinkedList<Host>());
        }
        acked.get(broadcastMessage).add(me);
        forward.add(broadcastMessage);

        String formattedMessage = me.getId() + ";" + message;
        bebLayer.send(me, formattedMessage);
    }

    public synchronized void receive(Host immediateSender, String formattedMessage) {
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

    public synchronized void checkForDelivery() {
        for (BroadcastMessage broadcastMessage : forward) {
            checkForDelivery(broadcastMessage);
        }
    }

    public synchronized void checkForDelivery(BroadcastMessage broadcastMessage) {
        Set<Host> correctProcesses = PingLayer.getCorrectProcesses();
        if (!delivered.contains(broadcastMessage) && acked.get(broadcastMessage).containsAll(correctProcesses) ) {
            delivered.add(broadcastMessage);
            deliver(broadcastMessage.getHost(), broadcastMessage.getMessage());
        }
    }

    public synchronized void deliver(Host host, String message) {
        if (upperLayer != null){
            upperLayer.receive(host, message);
        } else {
            System.out.println("URB : " + host + " - " + message);
        }
    }
    
    public void deliverTo(Layer layer) {
        this.upperLayer = layer;
    }

    public synchronized void handleCrash(Host crashedHost) {
        bebLayer.handleCrash(crashedHost);
        checkForDelivery();
    }
    
}
