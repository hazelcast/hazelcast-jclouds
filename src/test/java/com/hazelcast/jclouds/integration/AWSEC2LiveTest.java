package com.hazelcast.jclouds.integration;

import com.hazelcast.test.HazelcastSerialClassRunner;
import com.hazelcast.test.annotation.SlowTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

@Category(SlowTest.class)
@RunWith(HazelcastSerialClassRunner.class)
public class AWSEC2LiveTest extends AbstractLiveTest {

    private static final String AWS_PROVIDER = "aws-ec2";
    private static final String AWS_IDENTITY = System.getProperty("test.aws-ec2.identity");
    private static final String AWS_CREDENTIAL = System.getProperty("test.aws-ec2.credential");

    private static LiveComputeServiceUtil builder = new LiveComputeServiceUtil(AWS_PROVIDER, AWS_IDENTITY, AWS_CREDENTIAL);

    @BeforeClass
    public static void provisionNodes() {
        builder.provisionNodes();
    }

    @AfterClass
    public static void destroyNodes() {
        builder.destroyNodes();
    }

    @Override
    protected Map<String, Comparable> getProperties() {
        Map<String, Comparable> properties = new HashMap<String, Comparable>();
        properties.put("provider", AWS_PROVIDER);
        properties.put("identity", AWS_IDENTITY);
        properties.put("credential", AWS_CREDENTIAL);
        return properties;
    }

    @Override
    protected String getRegion1() {
        return LiveComputeServiceUtil.AWS_REGION_1;
    }

    @Override
    protected String getRegion2() {
        return LiveComputeServiceUtil.AWS_REGION_2;
    }
}
