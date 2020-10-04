package cs451;

import java.util.List;

public class ApplicationLayer {
    int port;
    List<Host> hosts;

    TransportLayer transport;

    public ApplicationLayer(int port, List<Host> hosts){
        this.port = port;
        this.hosts = hosts;

        transport = new TransportLayer(port);
        PingLayer.start(hosts);
    }

    public void send(String destAddress, int destPort, String message){
        transport.send(destAddress, destPort, message);
    }

    public void receive(String sourceAddress, int sourcePort, String message) {
        System.out.println(sourceAddress + ":" + sourcePort + "-" + message);
    }
    
}
