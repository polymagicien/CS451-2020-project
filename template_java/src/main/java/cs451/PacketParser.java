package cs451;

public class PacketParser {
    private Host host;
    private String rawPayload;

    private long sequenceNumber;
    private String data;

    public PacketParser(Host host, String payload) {
        this.host = host;
        this.rawPayload = payload;
        if (!payload.contains(";")){
            System.out.println("Wrong packet format");
            sequenceNumber = -1;
            data = payload;
        }
        else {
            String arrPayload[] = payload.split(";", 2);
            sequenceNumber = Long.parseLong(arrPayload[0]);
            data = arrPayload[1];
        }
    }

    @Override
    public String toString(){
        return   "" + host + " - " + rawPayload ;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public PacketIdentifier getPacketId() {
        return new PacketIdentifier(host, sequenceNumber);
    }
}
