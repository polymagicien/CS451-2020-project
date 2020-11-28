package cs451;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.List;

// TODO : add logs for later printing

public class LCLayer implements Layer {

	// private Map<Host, Integer> vc;
	private Host me;
    private Layer urbLayer;
	private Layer upperLayer;
	
    private int[] vc;
	private Map<Host, List<Host>> dependency;
	private Set<CausalMessage> pending;

	private Set<BroadcastMessage> broadcastSent;
	private List<String> log;

    LCLayer(List<Host> hosts, Host me, Map<Host, List<Host>> dependency) {
		this.me = me;
		this.urbLayer = new UrbLayer(hosts, me);
		this.urbLayer.deliverTo(this);
		this.dependency = dependency;

		this.vc = new int[hosts.size() + 1];
		this.pending = new HashSet<>();

		
        this.broadcastSent = Collections.synchronizedSet(new HashSet<>());
        this.log = Collections.synchronizedList(new LinkedList<>());
    }

	public void send(Host useless, String message) {
		String s = message + ";" + getStringDependency(dependency.get(me));
		vc[me.getId()] ++;
		urbLayer.send(null, s);
	}

	public void receive(Host host, String payload) {
		String message = payload.split(";", 2)[0];
		String stringVc = payload.split(";", 2)[1];
		int[] vcm = parseVC(stringVc);
		CausalMessage m = new CausalMessage(host, message, vcm);
		pending.add(m);

		deliverPending();
	}

	public void deliverPending() {
		boolean oneWasDelivered = true;

		while (oneWasDelivered) {
			oneWasDelivered = false;
			for (CausalMessage causalMessage : pending) {
				if (matchRequirements(causalMessage.getVc())) {
					vc[causalMessage.getSource().getId()] += 1;
					pending.remove(causalMessage);
					oneWasDelivered = true;
					deliver(causalMessage.getSource(), causalMessage.getMessage());
					break;
				}
			}
		}

	}

	public void deliverTo(Layer layer) {
        this.upperLayer = layer;
	}

	public void deliver(Host host, String message) {
        if (upperLayer != null) {
            upperLayer.receive(host, message);
        } else {
            System.out.println("LCausal : " + host + " - " + message);
        }
    }

	public void handleCrash(Host crashedHost) {
        this.urbLayer.handleCrash(crashedHost);
	}

	public String waitFinishBroadcasting(boolean retString) {
		// TODO : implement waitFinishBroadcasting
		return null;
	}

	private int[] parseVC(String strVC) {
        String[] strVcList = strVC.split(";");

        int[] v = new int[vc.length];
        for (int i = 0; i < v.length; i++) {
			int hostId = Integer.valueOf(strVcList[i].split(":")[0]);
			int require = Integer.valueOf(strVcList[i].split(":")[1]);
			v[hostId] = require;
		}
        return v;
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
	
	private boolean matchRequirements(int[] vcm) {
		for (int i = 1; i < vcm.length; i++) {
			if (vcm[i] > vc[i])
				return false;
		}
		return true;
	}
    
}
