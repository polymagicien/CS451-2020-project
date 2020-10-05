package cs451;

public interface Layer {

    public void send(Host host, String message);
    public void receive(Host host, String message);
    public void deliverTo(Layer layer);

    public void handleCrash(Host crashedHost);

}
