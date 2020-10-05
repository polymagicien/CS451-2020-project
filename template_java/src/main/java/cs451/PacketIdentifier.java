package cs451;

public class PacketIdentifier {
    private Host host; 
    private int sequenceNumber;

    public PacketIdentifier(Host host, int sequenceNumber) {
        this.host = host;
        this.sequenceNumber = sequenceNumber;
    }

    public Host getDest() {
        return this.host;
    }

    @Override
    public String toString() {
        return "" + host + "(" + sequenceNumber + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if(!(o instanceof PacketIdentifier))
            return false;
        PacketIdentifier other = (PacketIdentifier)o;
        return this.host.equals(other.host) && this.sequenceNumber == other.sequenceNumber;
    }

    @Override
    public int hashCode() {
        return host.getIp().hashCode() + host.getPort() + sequenceNumber;
    }
}
