package cs451;

import java.util.HashMap;
import java.util.Map;
import java.util.List;


public class LCLayer implements Layer {

    private Map<Host, Integer> VC;

    LCLayer(List<Host> hosts, Host me) {
        VC = new HashMap<>();

        for(Host host : hosts) {
            VC.put(host, 0);
        }


    }

	public void send(Host host, String message) {
		// TODO Auto-generated method stub
		
	}

	public void receive(Host host, String message) {
		// TODO Auto-generated method stub
		
	}

	public void deliverTo(Layer layer) {
		// TODO Auto-generated method stub
		
	}

	public void handleCrash(Host crashedHost) {
		// TODO Auto-generated method stub
		
	}

	public String waitFinishBroadcasting(boolean retString) {
		// TODO Auto-generated method stub
		return null;
	}
    
}
