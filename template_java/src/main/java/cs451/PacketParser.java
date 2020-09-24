package cs451;

public class PacketParser {
    private String hostname;
    private int port;
    private String rawPayload;

    private int sequenceNumber;
    private String data;

    public PacketParser(String hostname, int port, String payload) {
        this.hostname = hostname;
        this.port = port;
        this.rawPayload = payload;
        if (!payload.contains(";")){
            System.out.println("Wrong packet format");
            sequenceNumber = -1;
            data = payload;
        }
        else {
            String arrPayload[] = payload.split(";", 2);
            sequenceNumber = Integer.parseInt(arrPayload[0]);
            data = arrPayload[1];
        }
    }

    @Override
    public String toString(){
        return hostname + ":" + port + " - " + rawPayload ;
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public PacketIdentifier getPacketId() {
        return new PacketIdentifier(hostname, port, sequenceNumber);
    }
}
