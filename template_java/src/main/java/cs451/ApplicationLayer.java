package cs451;

import java.util.List;

public class ApplicationLayer implements Layer {
    int port;
    Layer transport;

    public ApplicationLayer(int port, List<Host> hosts){
        this.port = port;

        transport = new TransportLayer(port);
        transport.deliverTo(this);
        PingLayer.start(hosts);
    }

    public void send(Host destHost, String message){
        for (Host host : PingLayer.getCorrectProcesses()) {
            transport.send(host, message);
        }
    }

    public void receive(Host host, String message) {
        System.out.println("Application : " + host + "-" + message);
    }

    public void deliverTo(Layer layer) {
        System.err.println("Incorrect use of Application layer");
    }

    public void handleCrash(Host crashedHost) {
        transport.handleCrash(crashedHost);
    }
    
}
