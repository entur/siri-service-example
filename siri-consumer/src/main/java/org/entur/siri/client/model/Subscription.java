package org.entur.siri.client.model;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.StringJoiner;

public class Subscription {

    public final String subscriptionId;
    public final SiriDataType dataType;
    public final int heartbeatIntervalSeconds;
    public final String requestorRef;
    private int heartbeatCounter;
    private int dataCounter;

    private ZonedDateTime lastReceivedHeartbeat;
    private ZonedDateTime lastReceivedData;

    public Subscription(String subscriptionId, SiriDataType dataType, int heartbeatIntervalSeconds, String requestorRef) {
        this.subscriptionId = subscriptionId;
        this.dataType = dataType;
        this.heartbeatIntervalSeconds = heartbeatIntervalSeconds;
        this.requestorRef = requestorRef;
    }

    public String getHeartbeatInterval() {
        return Duration.ofSeconds(heartbeatIntervalSeconds).toString();
    }

    public void markHeartbeatReceived() {
        heartbeatCounter++;
        lastReceivedHeartbeat = ZonedDateTime.now();
    }
    public void markDataReceived() {
        dataCounter++;
        lastReceivedData = ZonedDateTime.now();
    }

    public void markStarted() {
        lastReceivedHeartbeat = ZonedDateTime.now();
        lastReceivedData = ZonedDateTime.now();
    }

    /**
     * Returns false if data/heartbeats has NOT been received in graceperiod
     */
    public boolean isHealthy() {
        // Defining graceperiod as 5 x HeartbeatInterval
        int gracePeriodSeconds = heartbeatIntervalSeconds * 5;
        ZonedDateTime zonedDateTime = ZonedDateTime.now().minusSeconds(gracePeriodSeconds);

        return (lastReceivedData != null && lastReceivedData.isAfter(zonedDateTime)) &&
                (lastReceivedHeartbeat != null && lastReceivedHeartbeat.isAfter(zonedDateTime));
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Subscription.class.getSimpleName() + "[", "]")
                .add("subscriptionId='" + subscriptionId + "'")
                .add("dataType=" + dataType)
                .add("heartbeatCounter=" + heartbeatCounter)
                .add("dataCounter=" + dataCounter)
                .toString();
    }
}
