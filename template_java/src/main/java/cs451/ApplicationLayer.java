package cs451;

import java.util.List;

public class ApplicationLayer {
    int port;
    List<Host> hosts;

    TransportLayer transport;

    public ApplicationLayer(int port, List<Host> hosts){
        this.port = port;
        this.hosts = hosts;

        this.transport = new TransportLayer(port);
    }

    public void send(String destHostname, int destPort, String message){
        transport.send(destHostname, destPort, message);
    }

    public void receive(String sourceHostname, int sourcePort, String message) {
        System.out.println(sourceHostname + ":" + sourcePort + "-" + message);
    }
    
}
