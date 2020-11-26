package cs451;

import java.io.StringReader;
import java.util.Arrays;

public class CausalMessage {

    private String message;
    private Host source;
    private int[] VC;

    public CausalMessage(Host source, String payload) {
        this.source = source;
        this.parsePayload(payload);
    }

    public CausalMessage(Host source, String message, int[] VC) {
        this.source = source;
        this.message = message;
        this.VC = VC;
    }

    /**
     * Parse the payload of the form "message; VC[0], VC[1], ..."
     * 
     * @param payload
     */
    private void parsePayload(String payload) {
    }

    @Override
    public int hashCode() {
        return this.source.hashCode() + 7*this.message.hashCode() + 29*this.VC.hashCode()
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (!(o instanceof CausalMessage))
            return false;
        CausalMessage other = (CausalMessage) o;
        return this.source.equals(other.source) && this.message.equals(other.message)
                && Arrays.equals(this.VC, other.VC);
    }
}
