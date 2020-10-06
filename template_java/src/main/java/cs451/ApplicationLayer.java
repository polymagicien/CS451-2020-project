package cs451;

import java.util.List;

public class ApplicationLayer implements Layer {
    Layer lowerLayer;

    public ApplicationLayer(List<Host> hosts, Host me) {
        lowerLayer = new UrbLayer(hosts, me);
        lowerLayer.deliverTo(this);

        PingLayer.setNotifiedLayer(this);
    }

    public void send(Host host, String message) {
        lowerLayer.send(null, message);
    }

    public void receive(Host host, String message) {
        System.out.println("APPLI : " + host + " - " + message);
    }

    public void deliverTo(Layer layer) {
        System.err.println("Incorrect use of ApplicationLayer");
    }

    public void handleCrash(Host crashedHost) {
        lowerLayer.handleCrash(crashedHost);
    }


    
}
