---
**REMARK**

Dear community members,

Thanks for your interest in **hazelcast-jclouds**! This project has become a Hazelcast Community project.

Hazelcast Inc. gives this project to the developers community in the hope you can benefit from it. It comes without any maintenance guarantee by the original developers but their goodwill (and time!). We encourage you to use this project however you see fit, including any type of contribution in the form of a pull request or an issue. 

Feel free to visit our Slack Community for any help and feedback.

---

# Table of Contents

* [Supported Versions](#supported-versions)
* [Discovering Members with jclouds](#discovering-members-with-jclouds)
* [Configuring Dependencies for jclouds via Maven](#configuring-dependencies-for-jclouds-via-maven)
* [Configuring IAM Roles for AWS](#configuring-iam-roles-for-aws)
* [Discovering Members on Different Regions](#discovering-members-on-different-regions)
* [Using jclouds With ZONE_AWARE Partition Group](#using-jclouds-with-zone_aware-partition-group)

# Supported Versions

hazelcast-jclouds 3.7+ is compatible with hazelcast 3.7+

# Discovering Members with jclouds

Hazelcast members and native clients support jclouds&reg; for discovery. It is useful when you do not want to provide or you cannot provide the list of possible IP addresses on various cloud providers. 

To configure your cluster to use jclouds auto-discovery, follow these steps:

- Add the *hazelcast-jclouds.jar* dependency to your project. Note that this is also bundled inside *hazelcast-all.jar*. The Hazelcast jclouds module depends on jclouds; please make sure the necessary JARs for your provider are present on the classpath.
- Disable the multicast and TCP/IP join mechanisms. To do this, set the `enabled` attributes of the `multicast` and `tcp-ip` elements to `false` in your `hazelcast.xml` configuration file
- Set the `enabled` attribute of the `hazelcast.discovery.enabled` property to `true`.
- Within the `discovery-providers` element, provide your credentials (access and secret key), your region, etc.

The following is an example declarative configuration.

```xml
 ...
  <properties>
    <property name="hazelcast.discovery.enabled">true</property>
  </properties>
   ....
 <join>
    <multicast enabled="false">
    </multicast>
    <tcp-ip enabled="false">
    </tcp-ip>
    <discovery-strategies>
        <discovery-strategy class="com.hazelcast.jclouds.JCloudsDiscoveryStrategy" enabled="true">
          <properties>
           <property name="provider">google-compute-engine</property>
           <property name="identity">GCE_IDENTITY</property>
           <property name="credential">GCE_CREDENTIAL</property>
          </properties>
        </discovery-strategy>
    </discovery-strategies>
</join>
...
```
As stated in the first paragraph of this section, Hazelcast native clients also support jclouds for discovery. It means you can also configure your `hazelcast-client.xml` configuration file to include the <discovery-strategies> element in the same way as with `hazelcast.xml`.

The table below lists the jclouds configuration properties with their descriptions.

Property Name | Type | Description
:--------------|:------|:------------
`provider`|String|String value that is used to identify ComputeService provider. For example, "google-compute-engine" is used for Google Cloud services. See the <a href="https://jclouds.apache.org/reference/providers/#compute " target="_blank">full provider list here</a>.
`identity`|String|Cloud Provider identity, can be thought of as a user name for cloud services.
`credential`|String|Cloud Provider credential, can be thought of as a password for cloud services.
`endpoint`|String|Defines the endpoint for a gneric API such as OpenStack or CloudStack (optional).
`zones`|String|Defines zone for a cloud service (optional). Can be used with comma separated values for multiple values.
`regions`|String|Defines region for a cloud service (optional). Can be used with comma separated values for multiple values.
`tag-keys`|String|Filters cloud instances with tags (optional). Can be used with comma separated values for multiple values.
`tag-values`|String|Filters cloud instances with tags (optional) Can be used with comma separated values for multiple values.
`group`|String|Filters instance groups (optional). When used with AWS it maps to security group.
`hz-port`|Int|Port which the hazelcast instance service uses on the cluster member. Default value is 5701. (optional)
`role-name*`|String|Used for IAM role support specific to AWS (optional, but if defined, no identity or credential should be defined in the configuration).
`credentialPath*`|String|Used for cloud providers which require an extra JSON or P12 key file. This denotes the path of that file. Only tested with Google Compute Engine. (Required if Google Compute Engine is used.)

# Configuring Dependencies for jclouds via Maven

jclouds depends on many libraries internally and `hazelcast-jclouds.jar` does not contain any of them. If you want to use jclouds, we recommend that you use its dependency management tool. The following is a simple maven dependency configuration that uses the
maven assembly plugin to create an uber JAR with the necessary jclouds properties.

```xml

<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>group-id</groupId>
    <artifactId>artifact-id </artifactId>
    <version>version</version>
    <name>compute-basics</name>

    <properties>
        <jclouds.version>latest-version</jclouds.version>
        <hazelcast.version>latest-version</hazelcast.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>com.hazelcast</groupId>
            <artifactId>hazelcast</artifactId>
            <version>${hazelcast.version}</version>
        </dependency>
        <dependency>
            <groupId>com.hazelcast</groupId>
            <artifactId>hazelcast-jclouds</artifactId>
            <version>${hazelcast.version}</version>
        </dependency>
        <dependency>
            <groupId>org.apache.jclouds</groupId>
            <artifactId>jclouds-compute</artifactId>
            <version>${jclouds.version}</version>
        </dependency>
        <dependency>
            <groupId>org.apache.jclouds</groupId>
            <artifactId>jclouds-allcompute</artifactId>
            <version>${jclouds.version}</version>
        </dependency>
        <dependency>
            <groupId>org.apache.jclouds.labs</groupId>
            <artifactId>google-compute-engine</artifactId>
            <version>${jclouds.version}</version>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            ...
            <plugin>
                <artifactId>maven-assembly-plugin</artifactId>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>single</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <descriptorRefs>
                        <descriptorRef>jar-with-dependencies</descriptorRef>
                    </descriptorRefs>
                </configuration>
            </plugin>
            ...
        </plugins>
    </build>
</project>
```


# Configuring IAM Roles for AWS

IAM roles are used to make secure requests from your clients. You can provide the name of your IAM role that you created previously on your AWS console to the jclouds configuration. IAM roles only work in AWS and when a role name is provided, the other credentials' properties should be empty.

```xml
 ...
  <properties>
    <property name="hazelcast.discovery.enabled">true</property>
  </properties>
   ....
 <join>
    <multicast enabled="false">
    </multicast>
    <tcp-ip enabled="false">
    </tcp-ip>
    <discovery-providers>
        <discovery-provider class="com.hazelcast.jclouds.JCloudsDiscoveryStrategy" enabled="true">
          <properties>
                  <property name="provider">aws-ec2</property>
                  <property name="role-name">i-am-role-for-member</property>
                  <property name="credential">AWS_CREDENTIAL</property>
          </properties>
        </discovery-provider>
    </discovery-providers>
</join>
...
```

Note that, for AWS EC2, you can also configure your cluster using the <aws> element as described in [Discovering Members within EC2 Cloud](https://github.com/hazelcast/hazelcast-aws/blob/master/README.md).


# Discovering Members on Different Regions

You can define multiple regions in your jclouds configuration. By default, Hazelcast Discovery SPI uses private IP addresses for member connections. If you want the members to find each other over a different region, you must set the system property `hazelcast.discovery.public.ip.enabled` to `true`. In this way, the members on different regions can connect to each other by using public IPs.

```xml
 ...
  <properties>
    <property name="hazelcast.discovery.enabled">true</property>
    <property name="hazelcast.discovery.public.ip.enabled">true</property>
  </properties>
   ....
 <join>
    <multicast enabled="false">
    </multicast>
    <tcp-ip enabled="false">
    </tcp-ip>
    <discovery-providers>
        <discovery-provider class="com.hazelcast.jclouds.JCloudsDiscoveryStrategy" enabled="true">
          <properties>
           <property name="provider">aws-ec2</property>
           <property name="identity">AWS_IDENTITY</property>
           <property name="credential">AWS_CREDENTIAL</property>
          </properties>
        </discovery-provider>
    </discovery-providers>
</join>
...
```

# Using jclouds With ZONE_AWARE Partition Group

When you use jclouds as discovery provider, you can configure Hazelcast Partition Group configuration with jclouds.
For more information please read: http://docs.hazelcast.org/docs/3.7/manual/html-single/index.html#partition-group-configuration

```xml
...
<partition-group enabled="true" group-type="ZONE_AWARE" />
...
```
