package com.labijie.infra.logging;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class Tools {

    public static String getServerIp() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback() || networkInterface.isVirtual() || networkInterface.isPointToPoint()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (address == null || address instanceof Inet6Address) {
                        continue;
                    }
                    return address.getHostAddress();
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }
}
