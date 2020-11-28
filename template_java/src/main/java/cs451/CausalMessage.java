package cs451;

import java.util.Arrays;

public class CausalMessage {

    private String message;
    private Host source;
    private int[] vc;

    public CausalMessage(Host source, String message, int[] vc) {
        this.source = source;
        this.message = message;
        this.vc = vc;
    }

    public int[] getVc() {
        return vc;
    }

    public String getMessage() {
        return message;
    }

    public Host getSource() {
        return source;
    }

    @Override
    public String toString() {
        return source.getId() + "-" + message + "-" + Arrays.toString(vc);
    }

    @Override
    public int hashCode() {
        return this.source.hashCode() + 7 * this.message.hashCode() + 29 * this.vc.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (!(o instanceof CausalMessage))
            return false;
        CausalMessage other = (CausalMessage) o;
        return this.source.equals(other.source) && this.message.equals(other.message)
                && Arrays.equals(this.vc, other.vc);
    }
}
