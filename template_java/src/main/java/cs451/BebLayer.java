package cs451;

import java.util.List;
import java.util.Set;

public class BebLayer implements Layer {
    Layer transport;
    Layer upperLayer = null;

    public BebLayer(List<Host> hosts){

        transport = new TransportLayer();
        transport.deliverTo(this);
        PingLayer.start(hosts);
    }

    public void send(Host useless, String message){
        Set<Host> correctProcesses = PingLayer.getCorrectProcesses();
        synchronized (correctProcesses) {
            for(Host host : correctProcesses){
                transport.send(host, message);
            }
        }
    }

    public void receive(Host host, String message) {
        if (upperLayer != null) {
            // System.out.println("Beb : " + host + " - " + message);
            upperLayer.receive(host, message);
        }
        else {
            System.out.println("Beb : " + host + " - " + message);
        }
    }

    public void deliverTo(Layer layer) {
        this.upperLayer = layer;
    }

    public void handleCrash(Host crashedHost) {
        transport.handleCrash(crashedHost);
    }
    
}
