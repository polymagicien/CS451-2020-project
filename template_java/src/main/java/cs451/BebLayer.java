package cs451;

import java.util.List;
import java.util.Set;

public class BebLayer implements Layer {
    Layer transport;
    Layer upperLayer = null;

    public BebLayer(List<Host> hosts){

        transport = new TransportLayer();
        transport.deliverTo(this);
    }

    public void send(Host doNotSendTo, String message){
        Set<Host> correctProcesses = PingLayer.getCorrectProcesses();
        synchronized (correctProcesses) {
            for(Host host : correctProcesses){
                if (host.equals(doNotSendTo))
                    continue;
                transport.send(host, message);
            }
        }
    }

    public void receive(Host host, String message) {
        this.deliver(host, message);
    }

    public void deliver(Host host, String message) {
        if (upperLayer != null) {
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

    public String waitFinishBroadcasting(boolean retString) {
        throw new UnsupportedOperationException();
    }
    
}
