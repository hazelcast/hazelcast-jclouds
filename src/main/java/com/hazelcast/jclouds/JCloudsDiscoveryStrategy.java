/*
* Copyright 2020 Hazelcast Inc.
*
* Licensed under the Hazelcast Community License (the "License"); you may not use
* this file except in compliance with the License. You may obtain a copy of the
* License at
*
* http://hazelcast.com/hazelcast-community-license
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
* WARRANTIES OF ANY KIND, either express or implied. See the License for the
* specific language governing permissions and limitations under the License.
*/

package com.hazelcast.jclouds;

import com.hazelcast.config.InvalidConfigurationException;
import com.hazelcast.core.HazelcastException;
import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;
import com.hazelcast.nio.Address;
import com.hazelcast.spi.discovery.AbstractDiscoveryStrategy;
import com.hazelcast.spi.discovery.DiscoveryNode;
import com.hazelcast.spi.discovery.DiscoveryStrategy;
import com.hazelcast.spi.discovery.SimpleDiscoveryNode;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.domain.Location;
import org.jclouds.domain.LocationScope;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.hazelcast.spi.partitiongroup.PartitionGroupMetaData.PARTITION_GROUP_HOST;
import static com.hazelcast.spi.partitiongroup.PartitionGroupMetaData.PARTITION_GROUP_ZONE;

/**
 * JClouds implementation of {@link DiscoveryStrategy}
 */
public class JCloudsDiscoveryStrategy extends AbstractDiscoveryStrategy {

    private static final ILogger LOGGER = Logger.getLogger(JCloudsDiscoveryStrategy.class);
    private final ComputeServiceBuilder computeServiceBuilder;
    private final Map<String, Object> memberMetaData = new HashMap<String, Object>();

    /**
     * Instantiates a new JCloudsDiscoveryStrategy
     *
     * @param properties the properties
     */
    public JCloudsDiscoveryStrategy(Map<String, Comparable> properties) {
        super(LOGGER, properties);
        this.computeServiceBuilder = new ComputeServiceBuilder(properties);
    }

    protected JCloudsDiscoveryStrategy(ComputeServiceBuilder computeServiceBuilder) {
        super(LOGGER, new HashMap<String, Comparable>());
        this.computeServiceBuilder = computeServiceBuilder;
    }

    @Override
    public void start() {
        this.computeServiceBuilder.build();
    }

    @Override
    public Iterable<DiscoveryNode> discoverNodes() {
        List<DiscoveryNode> discoveryNodes = new ArrayList<DiscoveryNode>();
        try {
            Iterable<? extends NodeMetadata> nodes = computeServiceBuilder.getFilteredNodes();
            for (NodeMetadata metadata : nodes) {
                if (metadata.getStatus() != NodeMetadata.Status.RUNNING) {
                    continue;
                }
                discoveryNodes.add(buildDiscoveredNode(metadata));
            }
            if (discoveryNodes.isEmpty()) {
                LOGGER.warning("No running nodes discovered in configured cloud provider.");
            } else {
                StringBuilder sb = new StringBuilder("Discovered the following nodes with public IPS:\n");
                for (DiscoveryNode node : discoveryNodes) {
                    sb.append("    ").append(node.getPublicAddress().toString()).append("\n");
                }
                LOGGER.finest(sb.toString());
            }
        } catch (Exception e) {
            throw new HazelcastException("Failed to get registered addresses", e);
        }
        return discoveryNodes;
    }

    @Override
    public void destroy() {
        computeServiceBuilder.destroy();
    }

    @Override
    public Map<String, Object> discoverLocalMetadata() {
        if (memberMetaData.size() == 0) {
            discoverNodes();
        }
        return memberMetaData;
    }

    private DiscoveryNode buildDiscoveredNode(NodeMetadata metadata) {
        Address privateAddressInstance = null;
        if (!metadata.getPrivateAddresses().isEmpty()) {
            InetAddress privateAddress = mapAddress(metadata.getPrivateAddresses().iterator().next());
            privateAddressInstance = new Address(privateAddress, computeServiceBuilder.getServicePort());
            if (privateAddress.getHostAddress().equals(getLocalHostAddress())) {
                fetchMemberMetaData(metadata);
            }
        }

        Address publicAddressInstance = null;
        if (!metadata.getPublicAddresses().isEmpty()) {
            InetAddress publicAddress = mapAddress(metadata.getPublicAddresses().iterator().next());
            publicAddressInstance = new Address(publicAddress, computeServiceBuilder.getServicePort());
            if (publicAddress.getHostAddress().equals(getLocalHostAddress())) {
                fetchMemberMetaData(metadata);
            }
        }

        return new SimpleDiscoveryNode(privateAddressInstance, publicAddressInstance);
    }

    private InetAddress mapAddress(String address) {
        if (address == null) {
            return null;
        }
        try {
            return InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            throw new InvalidConfigurationException("Address '" + address + "' could not be resolved");
        }
    }

    private void fetchMemberMetaData(NodeMetadata metadata) {
        Location location = metadata.getLocation();
        while (location != null) {
            String id = location.getId();
            if (location.getScope().equals(LocationScope.ZONE)) {
                if (id != null) {
                    memberMetaData.put(PARTITION_GROUP_ZONE, id);
                }
            }
            location = location.getParent();
        }
        memberMetaData.put(PARTITION_GROUP_HOST, metadata.getHostname());
    }

    public String getLocalHostAddress() {
        try {
            InetAddress candidateAddress = null;
            // Iterate all NICs (network interface cards)...
            for (Enumeration ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements(); ) {
                NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
                for (Enumeration inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements(); ) {
                    InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
                    if (!inetAddr.isLoopbackAddress()) {
                        if (inetAddr.isSiteLocalAddress()) {
                            return inetAddr.getHostAddress();
                        } else if (candidateAddress == null) {
                            candidateAddress = inetAddr;
                        }
                    }
                }
            }
            if (candidateAddress != null) {
                return candidateAddress.getHostAddress();
            }
            InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
            if (jdkSuppliedAddress == null) {
                throw new UnknownHostException("The JDK InetAddress.getLocalHost() method unexpectedly returned null.");
            }
            return jdkSuppliedAddress.getHostAddress();
        } catch (Exception e) {
            LOGGER.warning("Failed to determine Host address: " + e);
            return null;
        }
    }
}
