package cs451;

import java.util.List;

public class ApplicationLayer {
    int port;

    TransportLayer transport;

    public ApplicationLayer(int port, List<Host> hosts){
        this.port = port;

        transport = new TransportLayer(port);
        PingLayer.start(hosts);
    }

    public void broadcast(String message) {
        for (Host host : PingLayer.getCorrectProcesses()) {
            transport.send(host.getIp(), host.getPort(), message);
        }
    }

    public void send(String destAddress, int destPort, String message){
        transport.send(destAddress, destPort, message);
    }

    public void receive(String sourceAddress, int sourcePort, String message) {
        System.out.println(sourceAddress + ":" + sourcePort + "-" + message);
    }
    
}
