package cs451;

public class Constants {

    public static final int numThreadGroundLayer = 4;
    public static final String ACK = "**ACK**";
    public static final String PING = "**PING**";
    public static final int DELAY_RETRANSMIT = 90; // ms
    public static final int DELAY_PING = 100; // ms
	public static final int DELAY_FOR_CRASH = 4000; // ms

    public static final int ARG_LIMIT_NO_CONFIG = 10;
    public static final int ARG_LIMIT_CONFIG = 11;

    // indexes for id
    public static final int ID_KEY = 0;
    public static final int ID_VALUE = 1;

    // indexes for hosts
    public static final int HOSTS_KEY = 2;
    public static final int HOSTS_VALUE = 3;

    // indexes for barrier
    public static final int BARRIER_KEY = 4;
    public static final int BARRIER_VALUE = 5;

    // indexes for signal
    public static final int SIGNAL_KEY = 6;
    public static final int SIGNAL_VALUE = 7;

    // indexes for output
    public static final int OUTPUT_KEY = 8;
    public static final int OUTPUT_VALUE = 9;

    // indexes for config
    public static final int CONFIG_VALUE = 10;
}
