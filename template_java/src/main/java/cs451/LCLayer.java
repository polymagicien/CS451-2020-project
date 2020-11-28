package cs451;

import java.util.HashMap;
import java.util.Map;
import java.util.List;


public class LCLayer implements Layer {

	// private Map<Host, Integer> vc;
	private Host me;
    private Layer urbLayer;
	private Layer upperLayer;
	
    private int[] vc;
    private Map<Host, List<Host>> dependency;

    LCLayer(List<Host> hosts, Host me) {
		this.me = me;
		this.urbLayer = new UrbLayer(hosts, me);
		this.urbLayer.deliverTo(this);

		this.vc = new int[hosts.size() + 1];


    }

	public void send(Host host, String message) {
		// TODO Auto-generated method stub
		
	}

	public void receive(Host host, String message) {
		// TODO Auto-generated method stub
		
	}

	public void deliverTo(Layer layer) {
        this.upperLayer = layer;
	}

	public void deliver(Host host, String message) {
        if (upperLayer != null) {
            upperLayer.receive(host, message);
        } else {
            System.out.println("FIFO : " + host + " - " + message);
        }
    }

	public void handleCrash(Host crashedHost) {
				
	}

	public String waitFinishBroadcasting(boolean retString) {
		return null;
	}

	private static int[] parseVC(String strVC) {
        String[] v = strVC.split(";");

        int[] vc = new int[v.length];
        for (int i = 0; i < v.length; i++) {
			int hostId = Integer.valueOf(v[i].split(":")[0]);
			int require = Integer.valueOf(v[i].split(":")[1]);
			vc[hostId] = require;
		}
        return vc;
    }

	private String getStringDependency(List<Host> dependencies) {
		String s = "";
		if (dependencies.size() == 0)
			return s;

        for (int i = 0; i < dependencies.size(); i++) {
            int hId = dependencies.get(i).getId();
            s += hId;
            s += ":";
            s += vc[hId]; 
            s += ";";
		}
		s = s.substring(0, s.length() - 1);
        return s;
    }
    
}
