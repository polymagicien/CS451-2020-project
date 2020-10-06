package cs451;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PingLayer {
    static private Set<Host> pingReceived = Collections.synchronizedSet(new HashSet<>()); // Updated with ping received in period
    static private HashMap<Host, TimerTask> hostToTask = new HashMap<>(); // Link host to task used to send ping to it
    static private Set<Host> correctProcesses = Collections.synchronizedSet(new HashSet<>()); // Correct Processes 
    static private Set<Host> declaredProcesses = Collections.synchronizedSet(new HashSet<>());
    static private Timer timer = new Timer();  // Send pings

    static Layer notifiedLayer = null;

    public static void start(List<Host> hosts) {
        // Initialize correctProcesses
        correctProcesses.addAll(hosts);
        declaredProcesses.addAll(hosts);

        for (Host host : hosts)
            System.out.println(host.getIp() + ":" + host.getPort());

        // Schedule ping sending
        for (Host host : correctProcesses) {
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    GroundLayer.send(host, Constants.PING);
                }
            };
            timer.scheduleAtFixedRate(task, 0, Constants.DELAY_PING);
            hostToTask.put(host, task);
        }


        Thread thread = new Thread(() -> {
            checkForCrash();
        });
        thread.start();
    }

    public static void setNotifiedLayer(Layer layer){
        notifiedLayer = layer;
    }

    public static void checkForCrash() {
        while (true) {
            try {
                Thread.sleep(Constants.DELAY_FOR_CRASH);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // System.out.println("Rain Check");

            Set<Host> crashedProcesses = new HashSet<>();
            synchronized(correctProcesses){
                crashedProcesses.addAll(correctProcesses);
                crashedProcesses.removeAll(pingReceived);
                correctProcesses.removeAll(crashedProcesses);
            }
;
            for (Host host : crashedProcesses) 
                handleCrash(host);

            pingReceived.clear();
        }
    }

    public static void handlePing(String sourceHostname, int sourcePort){
        Host host = new Host();
        host.populate("-1", sourceHostname, String.valueOf(sourcePort));
        pingReceived.add(host);
        // System.out.println("Ping from " + sourceHostname + ":" + sourcePort);

    }

    public static void handleCrash(Host host){
        System.out.println("Crash report : " + host);
        hostToTask.get(host).cancel();  // Stop sending ping to it

        if (notifiedLayer != null)
            notifiedLayer.handleCrash(host);
    }

    public static Set<Host> getCorrectProcesses(){
        return correctProcesses;
    }
    
}
