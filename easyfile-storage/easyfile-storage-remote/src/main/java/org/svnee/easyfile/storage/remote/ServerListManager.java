package org.svnee.easyfile.storage.remote;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.svnee.easyfile.common.util.StringUtils;

/**
 * Server list manager.
 *
 * @author svnee
 */
@Slf4j
public class ServerListManager {

    private static final String HTTPS = "https://";

    private static final String HTTP = "http://";

    private final String serverAddrsStr;

    @Getter
    volatile List<String> serverUrls = new ArrayList<>();

    private volatile String currentServerAddr;

    public ServerListManager(RemoteBootstrapProperties bootstrapProperties) {
        serverAddrsStr = bootstrapProperties.getServerAddr();

        if (!StringUtils.isEmpty(serverAddrsStr)) {
            List<String> serverAddrList = new ArrayList<>();
            String[] serverAddrListArr = this.serverAddrsStr.split(",");

            for (String serverAddr : serverAddrListArr) {
                boolean whetherJoint = StringUtils.isNotBlank(serverAddr)
                        && !serverAddr.startsWith(HTTPS) && !serverAddr.startsWith(HTTP);
                if (whetherJoint) {
                    serverAddr = HTTP + serverAddr;
                }

                currentServerAddr = serverAddr;
                serverAddrList.add(serverAddr);
            }

            this.serverUrls = serverAddrList;
        }
    }

    public String getCurrentServerAddr() {
        if (StringUtils.isEmpty(currentServerAddr)) {
            Iterator<String> iterator = iterator();
            currentServerAddr = iterator.next();
        }
        return currentServerAddr;
    }

    Iterator<String> iterator() {
        return new ServerAddressIterator(serverUrls);
    }

    private static class ServerAddressIterator implements Iterator<String> {

        final List<RandomizedServerAddress> sorted;

        final Iterator<RandomizedServerAddress> iter;

        public ServerAddressIterator(List<String> source) {
            sorted = new ArrayList<>();
            for (String address : source) {
                sorted.add(new RandomizedServerAddress(address));
            }
            Collections.sort(sorted);
            iter = sorted.iterator();
        }

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public String next() {
            return null;
        }

        static class RandomizedServerAddress implements Comparable<RandomizedServerAddress> {

            static Random random = new Random();

            String serverIp;

            int priority = 0;

            int seed;

            public RandomizedServerAddress(String ip) {
                try {
                    this.serverIp = ip;
                    /*
                     change random scope from 32 to Integer.MAX_VALUE to fix load balance issue
                     */
                    this.seed = random.nextInt(Integer.MAX_VALUE);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public int compareTo(RandomizedServerAddress other) {
                if (this.priority != other.priority) {
                    return other.priority - this.priority;
                } else {
                    return other.seed - this.seed;
                }
            }
        }
    }

}
