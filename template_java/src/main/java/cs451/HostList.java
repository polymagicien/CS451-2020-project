package cs451;

import java.util.LinkedList;
import java.util.List;

public class HostList {
    static List<Host> declaredHosts;

    public static void populate(List<Host> declaredHost) {
        HostList.declaredHosts = new LinkedList<>();
        HostList.declaredHosts.addAll(declaredHost);
    }

    public static Host getHost(String ipAddress, int port) {
        for (Host host : declaredHosts) {
            if (host.getIp().equals(ipAddress) && host.getPort() == port)
                return host;
        }
        return null;
    }

    public static Host getHost(int id) {
        for (Host host : declaredHosts) {
            if (host.getId() == id)
                return host;
        }
        return null;
    }
}
