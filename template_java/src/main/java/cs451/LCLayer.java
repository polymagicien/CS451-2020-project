package cs451;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import java.util.List;

public class LCLayer implements Layer {

	// private Map<Host, Integer> vc;
	private Host me;
	private Layer urbLayer;
	private Layer upperLayer;

	private List<Integer> vc;
	private Map<Host, List<Host>> dependency;
	private Map<Host, PriorityQueue<CausalMessage>> pending;
	private int num_sent = 0;
	private AtomicInteger numPending;

	private Set<Integer> broadcastSent;
	private List<String> log;

	LCLayer(List<Host> hosts, Host me, Map<Host, List<Host>> dependency) {
		this.me = me;
		this.urbLayer = new UrbLayer(hosts, me);
		this.urbLayer.deliverTo(this);
		this.dependency = dependency;

		this.vc = Collections.synchronizedList(new ArrayList<>(hosts.size() + 1));
		for (int i = 0; i < hosts.size() + 1; i++) {
			this.vc.add(0);
		}

		// Initialize pending
		this.pending = Collections.synchronizedMap(new HashMap<>());
		for (Host host : hosts) {
			pending.put(host, new PriorityQueue<CausalMessage>(new SortBySendingOrder()));
		}
		numPending = new AtomicInteger(0);

		this.broadcastSent = Collections.synchronizedSet(new HashSet<>());
		this.log = Collections.synchronizedList(new LinkedList<>());

		Thread t = new Thread(() -> {
			while (true) {
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

	// public void printPending() {
	// 	synchronized(pending){
	// 		CausalMessage m;
	// 		for (Host host : HostList.getAllHost()) {
	// 			PriorityQueue<CausalMessage> prio = pending.get(host);
	// 			System.out.println(host.getId() + "  ---------  ");
	// 			while((m = prio.poll()) != null) {
	// 				System.out.println(m);
	// 			}
	// 		}
	// 	}
	// }

	public void send(Host useless, String message) {
		String s = message + ";" + getStringDependency(dependency.get(me));
		urbLayer.send(null, s);
		num_sent++;

		// log
		broadcastSent.add(vc.get(me.getId()));
		log.add("b " + message + "\n");
	}

	public void receive(Host host, String payload) {
		String message = payload.split(";", 2)[0];
		String stringVc = payload.split(";", 2)[1];
		int[] vcm = parseVC(stringVc);

		CausalMessage m = new CausalMessage(host, message, vcm);
		synchronized (pending) {
			pending.get(host).add(m);
			numPending.incrementAndGet();
		}
	}

	public void deliverPending() {
		boolean oneWasDelivered = true;

		while (oneWasDelivered) {
			oneWasDelivered = false;

			synchronized (pending) {
				Iterator<Host> i = pending.keySet().iterator();
				while (i.hasNext()) {
					Host host = i.next();

					CausalMessage cm;
					int[] vcm;
					while ((cm = pending.get(host).peek()) != null && matchRequirements(cm.getVc())) {
						oneWasDelivered = true;
						numPending.decrementAndGet();
						vcm = cm.getVc();
						cm = pending.get(host).poll();
						int id = cm.getSource().getId();
						vc.set(id, vc.get(id) + 1);
						// log
						log.add("d " + id + " " + cm.getMessage() + "\n");
						if (cm.getSource().equals(me)) {
							broadcastSent.remove(vcm[me.getId()]);
						}
						deliver(cm.getSource(), cm.getMessage());
					}
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
		while (broadcastSent.size() > 0 || numPending.get() > 0) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// printPending();

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
