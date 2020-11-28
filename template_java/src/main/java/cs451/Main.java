package cs451;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;



public class Main {

    public static class Config {
        public static int numBroadcasts = 10;
        public static Map<Host, List<Host>> dependency = null;

        public static void setConfig(int numBroadcasts, Map<Host, List<Host>> dependency){
            Config.numBroadcasts = numBroadcasts;
            Config.dependency = dependency;
        }
    }

    private static Layer applicationLayer;
    private static String outputFile = "default.txt";

    private static void handleSignal() {
        //immediately stop network packet processing
        System.out.println("Immediately stopping network packet processing.");
        //write/flush output file if necessary
        System.out.println("Writing output.");
        try {
            FileWriter myWriter = new FileWriter(outputFile, false);
            myWriter.write(applicationLayer.waitFinishBroadcasting(true));
            myWriter.close();
            
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
    
    private static void initSignalHandlers() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                handleSignal();
            }
        });
    }
    
    private static void parseConfig(Parser parser) {
        HashMap<Host, List<Host>> dependency = new HashMap<>();

        try (FileReader reader = new FileReader(parser.config());
            BufferedReader br = new BufferedReader(reader)) {

            // read line by line
            String line;
            line = br.readLine();
            int numMessages = Integer.valueOf(line);

            int processId = 1;
            while ((line = br.readLine()) != null) {
                if ("".equals(line)) 
                    continue;

                Host h = HostList.getHost(processId);
                List<Host> l = new ArrayList<>();
                String[] lineList = line.split(" ");
                for (String s : lineList) {
                    l.add(HostList.getHost(Integer.valueOf(s)));
                }
                processId++;
                dependency.put(h, l);
            }

        Config.setConfig(numMessages, dependency);
        } catch (IOException e) {
            System.err.format("IOException: %s%n", e);
        }
    }
    
    public static void talk(Layer layer, Host destHost) {
        Scanner scanner = new Scanner(System.in);
        String data;
        while ( !(data = scanner.nextLine()).equals("") ) {
            layer.send(null, data);
        }
        scanner.close();
    }
    
    public static void main(String[] args) throws InterruptedException {
        Parser parser = new Parser(args);
        parser.parse();
        
        initSignalHandlers();
        
        // example
        long pid = ProcessHandle.current().pid();
        System.out.println("My PID is " + pid + ".");
        System.out.println("Use 'kill -SIGINT " + pid + " ' or 'kill -SIGTERM " + pid + " ' to stop processing packets.");

        System.out.println("My id is " + parser.myId() + ".");
        System.out.println("List of hosts is:");
        for (Host host: parser.hosts()) {
            System.out.println(host.getId() + ", " + host.getIp() + ", " + host.getPort());
        }
        HostList.populate(parser.hosts());

        System.out.println("Barrier: " + parser.barrierIp() + ":" + parser.barrierPort());
        System.out.println("Signal: " + parser.signalIp() + ":" + parser.signalPort());
        System.out.println("Output: " + parser.output());
        // if config is defined; always check before parser.config()
        if (parser.hasConfig()) {
            System.out.println("Config: " + parser.config());
            parseConfig(parser);
        } else {
            System.err.println("No config file provided");
            return;
        }

        outputFile = parser.output();
        Coordinator coordinator = new Coordinator(parser.myId(), parser.barrierIp(), parser.barrierPort(), parser.signalIp(), parser.signalPort());

        System.out.println("Waiting for all processes for finish initialization");
            coordinator.waitOnBarrier();

        System.out.println("Broadcasting messages...");
        // Retrieve own port for initialisation
        Host me = HostList.getHost(parser.myId());
        
        applicationLayer = new ApplicationLayer(parser.hosts(), me, Config.dependency);
        GroundLayer.start(me.getPort());
        PingLayer.start(parser.hosts(), me);

        for (int i = 1; i <= Config.numBroadcasts; i++) {
            applicationLayer.send(null, ""+i);
        }
        applicationLayer.waitFinishBroadcasting(false);

        System.out.println("Signaling end of broadcasting messages");
        coordinator.finishedBroadcasting();

        while (true) {
            // Sleep for 1 hour
            Thread.sleep(60 * 60 * 1000);
        }
    }
}
