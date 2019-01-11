package org.springframework.util;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.net.ServerSocketFactory;

//Socket工具类
public class SocketUtils {

    //端口最小值
    public static final int PORT_RANGE_MIN = 1024;

    //端口最大值
    public static final int PORT_RANGE_MAX = 65535;

    private static final Random random = new Random(System.currentTimeMillis());

    //构造器
    public SocketUtils() {}

    //寻找可用的TCP端口
    public static int findAvailableTcpPort() {
        return findAvailableTcpPort(PORT_RANGE_MIN);
    }

    //寻找可用的TCP端口
    public static int findAvailableTcpPort(int minPort) {
        return findAvailableTcpPort(minPort, PORT_RANGE_MAX);
    }

    //寻找可用的TCP端口
    public static int findAvailableTcpPort(int minPort, int maxPort) {
        return SocketType.TCP.findAvailablePort(minPort, maxPort);
    }

    //寻找可用的TCP端口
    public static SortedSet<Integer> findAvailableTcpPorts(int numRequested) {
        return findAvailableTcpPorts(numRequested, PORT_RANGE_MIN, PORT_RANGE_MAX);
    }

    //寻找可用的TCP端口
    public static SortedSet<Integer> findAvailableTcpPorts(int numRequested, int minPort, int maxPort) {
        return SocketType.TCP.findAvailablePorts(numRequested, minPort, maxPort);
    }

    //寻找可用的UDP端口
    public static int findAvailableUdpPort() {
        return findAvailableUdpPort(PORT_RANGE_MIN);
    }

    //寻找可用的UDP端口
    public static int findAvailableUdpPort(int minPort) {
        return findAvailableUdpPort(minPort, PORT_RANGE_MAX);
    }

    //寻找可用的UDP端口
    public static int findAvailableUdpPort(int minPort, int maxPort) {
        return SocketType.UDP.findAvailablePort(minPort, maxPort);
    }

    //寻找可用的UDP端口
    public static SortedSet<Integer> findAvailableUdpPorts(int numRequested) {
        return findAvailableUdpPorts(numRequested, PORT_RANGE_MIN, PORT_RANGE_MAX);
    }

    //寻找可用的UDP端口
    public static SortedSet<Integer> findAvailableUdpPorts(int numRequested, int minPort, int maxPort) {
        return SocketType.UDP.findAvailablePorts(numRequested, minPort, maxPort);
    }

    private enum SocketType {

        TCP {
            @Override
            protected boolean isPortAvailable(int port) {
                try {
                    ServerSocket serverSocket = ServerSocketFactory.getDefault().createServerSocket(
                            port, 1, InetAddress.getByName("localhost"));
                    serverSocket.close();
                    return true;
                } catch (Exception ex) {
                    return false;
                }
            }
        },

        UDP {
            @Override
            protected boolean isPortAvailable(int port) {
                try {
                    DatagramSocket socket = new DatagramSocket(port, InetAddress.getByName("localhost"));
                    socket.close();
                    return true;
                } catch (Exception ex) {
                    return false;
                }
            }
        };

        /**
         * Determine if the specified port for this {@code SocketType} is
         * currently available on {@code localhost}.
         */
        protected abstract boolean isPortAvailable(int port);

        /**
         * Find a pseudo-random port number within the range
         * [{@code minPort}, {@code maxPort}].
         *
         * @param minPort the minimum port number
         * @param maxPort the maximum port number
         * @return a random port number within the specified range
         */
        private int findRandomPort(int minPort, int maxPort) {
            int portRange = maxPort - minPort;
            return minPort + random.nextInt(portRange + 1);
        }

        /**
         * Find an available port for this {@code SocketType}, randomly selected
         * from the range [{@code minPort}, {@code maxPort}].
         *
         * @param minPort the minimum port number
         * @param maxPort the maximum port number
         * @return an available port number for this socket type
         * @throws IllegalStateException if no available port could be found
         */
        int findAvailablePort(int minPort, int maxPort) {
            Assert.isTrue(minPort > 0, "'minPort' must be greater than 0");
            Assert.isTrue(maxPort >= minPort, "'maxPort' must be greater than or equals 'minPort'");
            Assert.isTrue(maxPort <= PORT_RANGE_MAX, "'maxPort' must be less than or equal to " + PORT_RANGE_MAX);

            int portRange = maxPort - minPort;
            int candidatePort;
            int searchCounter = 0;
            do {
                if (++searchCounter > portRange) {
                    throw new IllegalStateException(String.format(
                            "Could not find an available %s port in the range [%d, %d] after %d attempts",
                            name(), minPort, maxPort, searchCounter));
                }
                candidatePort = findRandomPort(minPort, maxPort);
            }
            while (!isPortAvailable(candidatePort));

            return candidatePort;
        }

        //寻找可用的端口
        SortedSet<Integer> findAvailablePorts(int numRequested, int minPort, int maxPort) {
            Assert.isTrue(minPort > 0, "'minPort' must be greater than 0");
            Assert.isTrue(maxPort > minPort, "'maxPort' must be greater than 'minPort'");
            Assert.isTrue(maxPort <= PORT_RANGE_MAX, "'maxPort' must be less than or equal to " + PORT_RANGE_MAX);
            Assert.isTrue(numRequested > 0, "'numRequested' must be greater than 0");
            Assert.isTrue((maxPort - minPort) >= numRequested, "'numRequested' must not be greater than 'maxPort' - 'minPort'");

            SortedSet<Integer> availablePorts = new TreeSet<Integer>();
            int attemptCount = 0;
            while ((++attemptCount <= numRequested + 100) && availablePorts.size() < numRequested) {
                availablePorts.add(findAvailablePort(minPort, maxPort));
            }

            if (availablePorts.size() != numRequested) {
                throw new IllegalStateException(String.format(
                        "Could not find %d available %s ports in the range [%d, %d]",
                        numRequested, name(), minPort, maxPort));
            }

            return availablePorts;
        }
    }

}
