package cs451;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import java.util.List;

// TODO : add logs for later printing

public class LCLayer implements Layer {

	// private Map<Host, Integer> vc;
	private Host me;
	private Layer urbLayer;
	private Layer upperLayer;

	private List<Integer> vc;
	private Map<Host, List<Host>> dependency;
	private Set<CausalMessage> pending;
	private int num_sent = 0;

	private Set<Integer> broadcastSent;
	private List<String> log;

	LCLayer(List<Host> hosts, Host me, Map<Host, List<Host>> dependency) {
		this.me = me;
		this.urbLayer = new UrbLayer(hosts, me);
		this.urbLayer.deliverTo(this);
		this.dependency = dependency;

		System.out.println("" + hosts.size());
		this.vc = Collections.synchronizedList(new ArrayList<>(hosts.size() + 1));
		for (int i = 0; i < hosts.size() + 1; i++) {
			this.vc.add(0);
		}

		this.pending = Collections.synchronizedSet(new HashSet<>());

		this.broadcastSent = Collections.synchronizedSet(new HashSet<>());
		this.log = Collections.synchronizedList(new LinkedList<>());

		Thread t = new Thread(() -> {
			while(true) {
				deliverPending();
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		t.start();
	}

	public void send(Host useless, String message) {
		String s = message + ";" + getStringDependency(dependency.get(me));
		urbLayer.send(null, s);
		num_sent++;

		System.out.println("SEND " + s);
		// log
		broadcastSent.add(vc.get(me.getId()));
		log.add("b " + message + "\n");
	}

	public void receive(Host host, String payload) {
		String message = payload.split(";", 2)[0];
		String stringVc = payload.split(";", 2)[1];
		int[] vcm = parseVC(stringVc);

		CausalMessage m = new CausalMessage(host, message, vcm);
		System.out.println("RCV " + m);
		// System.out.println("VC " + vc.toString());
		synchronized (pending) {
			pending.add(m);
		}
	}

	public void deliverPending() {
		boolean oneWasDelivered = true;
		LinkedList<CausalMessage> toRemove = new LinkedList<>();

		while (oneWasDelivered) {
			oneWasDelivered = false;


			synchronized (pending) {
				Iterator<CausalMessage> i = pending.iterator(); // Must be in the synchronized block
				while (i.hasNext()) {
					CausalMessage causalMessage = i.next();
					int[] vcm = causalMessage.getVc();
					if (matchRequirements(vcm)) {
						oneWasDelivered = true;
						int id = causalMessage.getSource().getId();
						vc.set(id, id + 1);
						toRemove.add(causalMessage);

						// log
						log.add("d " + id + " " + causalMessage.getMessage() + "\n");
						if (causalMessage.getSource().equals(me)) {
							broadcastSent.remove(vcm[me.getId()]);
						}

						deliver(causalMessage.getSource(), causalMessage.getMessage());
					}
				}

				pending.removeAll(toRemove);
				toRemove.clear();
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
		while (broadcastSent.size() > 0) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		if (retString) {
			return log.stream().collect(Collectors.joining(""));
		}
		return "";

	}

	private int[] parseVC(String strVC) {
		String[] strVcList = strVC.split(";");

		int[] v = new int[vc.size()];
		for (int i = 0; i < strVcList.length; i++) {
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
			int value = vc.get(hId);
			if (hId == me.getId())
				value = num_sent;
			
			s += hId;
			s += ":";
			s += value;
			s += ";";
		}
		s = s.substring(0, s.length() - 1);
		return s;
	}

	private boolean matchRequirements(int[] vcm) {
		for (int i = 1; i < vcm.length; i++) {
			if (vcm[i] > vc.get(i))
				return false;
		}
		return true;
	}

}
