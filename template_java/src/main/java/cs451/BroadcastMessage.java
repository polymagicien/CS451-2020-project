package cs451;

public class BroadcastMessage {
    private Host host;
    private String message;

    public BroadcastMessage(Host host, String message) {
        this.host = host;
        this.message = message;
    }

    public Host getHost(){
        return this.host;
    }

    public String getMessage(){
        return this.message;
    }

    @Override
    public int hashCode() {
        return this.host.hashCode() + 11*this.message.hashCode();
    }

    @Override
    public String toString() {
        return "" + this.host + "-" + this.message;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if(!(o instanceof Host))
            return false;
        BroadcastMessage other = (BroadcastMessage)o;
        return this.host.equals(other.host) && this.message.equals(other.message);
    }
    
}
