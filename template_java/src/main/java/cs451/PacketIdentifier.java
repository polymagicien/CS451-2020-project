package cs451;

public class PacketIdentifier {
    private String hostname;
    private int port; 
    private int sequenceNumber;

    public PacketIdentifier(String hostname, int port, int sequenceNumber) {
        this.hostname = hostname;
        this.port = port;
        this.sequenceNumber = sequenceNumber;
    }

    @Override
    public String toString() {
        return "" + hostname + ":" + port + ":" + sequenceNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if(!(o instanceof PacketIdentifier))
            return false;
        PacketIdentifier other = (PacketIdentifier)o;
        return this.hostname.equals(other.hostname) && this.port == other.port && this.sequenceNumber == other.sequenceNumber;
    }

    @Override
    public int hashCode() {
        return hostname.hashCode() + port + sequenceNumber;
    }
}
