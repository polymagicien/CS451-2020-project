package cs451;

import java.util.Comparator;

class SortBySequenceNumber implements Comparator<PacketParser> 
{ 
    // Used for sorting in ascending order of 
    // roll name 
    public int compare(PacketParser a, PacketParser b) 
    { 
        return (int)(a.getSequenceNumber() - b.getSequenceNumber()); 
    } 
} 


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

    @Override
    public int hashCode() {
        return host.hashCode() + 7*(int)sequenceNumber + 29*data.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if(!(o instanceof PacketParser))
            return false;
        PacketParser other = (PacketParser)o;
        return this.host.equals(other.host) && this.data.equals(other.data) && this.sequenceNumber == other.sequenceNumber;
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
