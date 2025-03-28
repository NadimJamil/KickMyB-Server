package org.kickmyb.server;

import java.net.InetAddress;

public class ConnectionChecker {
    public boolean internetOn(){
        try {
            InetAddress address = InetAddress.getByName("8.8.8.8");
            return address.isReachable(5000);
        } catch (Exception e) {
            return false;
        }
    }
}
