package com.hazelcast.jclouds.integration;

import com.google.common.collect.Iterators;
import com.hazelcast.jclouds.JCloudsDiscoveryStrategy;
import org.junit.Test;

import java.util.Map;

import static com.hazelcast.jclouds.integration.LiveComputeServiceUtil.GROUP_NAME_1;
import static com.hazelcast.jclouds.integration.LiveComputeServiceUtil.GROUP_NAME_2;
import static com.hazelcast.jclouds.integration.LiveComputeServiceUtil.TAG_1;
import static com.hazelcast.jclouds.integration.LiveComputeServiceUtil.TAG_2;
import static com.hazelcast.jclouds.integration.LiveComputeServiceUtil.TAG_3;
import static org.junit.Assert.assertEquals;

public abstract class AbstractLiveTest {

    protected abstract Map<String, Comparable> getProperties();

    protected abstract String getRegion1();

    protected abstract String getRegion2();

    @Test
    public void test_DiscoveryStrategyFiltersNodesByGroup() {
        Map<String, Comparable> properties1 = getProperties();
        properties1.put("group", GROUP_NAME_1);
        JCloudsDiscoveryStrategy strategy1 = new JCloudsDiscoveryStrategy(properties1);
        strategy1.start();

        Map<String, Comparable> properties2 = getProperties();
        properties2.put("group", GROUP_NAME_2);
        JCloudsDiscoveryStrategy strategy2 = new JCloudsDiscoveryStrategy(properties2);
        strategy2.start();

        assertEquals(3, Iterators.size(strategy1.discoverNodes().iterator()));
        assertEquals(2, Iterators.size(strategy2.discoverNodes().iterator()));
    }

    @Test
    public void test_DiscoveryStrategyFiltersNodesByRegion() {

        Map<String, Comparable> properties1 = getProperties();
        properties1.put("group", GROUP_NAME_1);
        properties1.put("regions", getRegion1());
        JCloudsDiscoveryStrategy strategy1 = new JCloudsDiscoveryStrategy(properties1);
        strategy1.start();

        Map<String, Comparable> properties2 = getProperties();
        properties2.put("group", GROUP_NAME_2);
        properties2.put("regions", getRegion1() + "," + getRegion2());
        JCloudsDiscoveryStrategy strategy2 = new JCloudsDiscoveryStrategy(properties2);
        strategy2.start();

        assertEquals(2, Iterators.size(strategy1.discoverNodes().iterator()));
        assertEquals(2, Iterators.size(strategy2.discoverNodes().iterator()));
    }

    @Test
    public void test_DiscoveryStrategyFiltersNodesByTags() {
        Map<String, Comparable> properties1 = getProperties();
        properties1.put("tag-keys", TAG_1.getKey());
        properties1.put("tag-values", TAG_1.getValue());
        JCloudsDiscoveryStrategy strategy1 = new JCloudsDiscoveryStrategy(properties1);
        strategy1.start();

        Map<String, Comparable> properties2 = getProperties();
        properties2.put("tag-keys", TAG_2.getKey() + "," + TAG_3.getKey());
        properties2.put("tag-values", TAG_2.getValue() + "," + TAG_3.getValue());
        JCloudsDiscoveryStrategy strategy2 = new JCloudsDiscoveryStrategy(properties2);
        strategy2.start();

        assertEquals(3, Iterators.size(strategy1.discoverNodes().iterator()));
        assertEquals(2, Iterators.size(strategy2.discoverNodes().iterator()));
    }
}
