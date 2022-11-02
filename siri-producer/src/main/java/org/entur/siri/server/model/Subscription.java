package org.entur.siri.server.model;

import uk.org.siri.siri21.RequestorRef;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.StringJoiner;
import java.util.concurrent.ScheduledExecutorService;

public class Subscription {

    private ZonedDateTime requestTimestamp;
    private final SiriDataType subscriptionType;
    private final String address;
    private final String subscriptionId;
    private Duration heartbeatInterval;
    private String requestorRef;

    public Subscription(
            ZonedDateTime requestTimestamp,
            SiriDataType subscriptionType,
            String address,
            String subscriptionId,
            Duration heartbeatInterval,
            RequestorRef requestorRef
    ) {
        this.requestTimestamp = requestTimestamp;
        this.subscriptionType = subscriptionType;
        this.address = address;
        this.subscriptionId = subscriptionId;
        this.heartbeatInterval = heartbeatInterval;
        this.requestorRef = requestorRef.getValue();
    }

    public ZonedDateTime getRequestTimestamp() {
        return requestTimestamp;
    }

    public SiriDataType getSubscriptionType() {
        return subscriptionType;
    }

    public String getAddress() {
        return address;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public Duration getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public String getRequestorRef() {
        return requestorRef;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Subscription.class.getSimpleName() + "[", "]")
                .add("subscriptionType=" + subscriptionType)
                .add("subscriptionId='" + subscriptionId + "'")
                .toString();
    }
}
