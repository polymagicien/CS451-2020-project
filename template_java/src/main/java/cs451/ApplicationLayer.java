package cs451;

import java.util.List;

public class ApplicationLayer implements Layer {
    Layer lowerLayer;
    Integer counter = 0;

    public ApplicationLayer(List<Host> hosts, Host me) {
        lowerLayer = new LCLayer(hosts, me);
        lowerLayer.deliverTo(this);

        PingLayer.setNotifiedLayer(this);
    }

    public void send(Host host, String message) {
        lowerLayer.send(null, message);
    }

    public void receive(Host host, String message) {
        synchronized(counter){
            counter++;

            if (counter % 100 == 0)
                System.out.println("Received " + counter);
        }
    }

    public void deliverTo(Layer layer) {
        System.err.println("Incorrect use of ApplicationLayer");
    }

    public void deliver(Host host, String message) {
        System.err.println("Not supported");
    }

    public void handleCrash(Host crashedHost) {
        lowerLayer.handleCrash(crashedHost);
    }

    public String waitFinishBroadcasting(boolean retString) {
        return lowerLayer.waitFinishBroadcasting(retString);
    }
    
}
