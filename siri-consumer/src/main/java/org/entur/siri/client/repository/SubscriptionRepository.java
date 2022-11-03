package org.entur.siri.client.repository;

import org.entur.siri.client.model.Subscription;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
public class SubscriptionRepository {

    private final Map<String, Subscription> subscriptionMap = new HashMap<>();

    public void add(Subscription subscription) {
        subscriptionMap.put(subscription.subscriptionId, subscription);
    }
    public Subscription get(String subscriptionId) {
        return subscriptionMap.get(subscriptionId);
    }

    public boolean exists(String subscriptionId) {
        return subscriptionMap.containsKey(subscriptionId);
    }

    public Collection<Subscription> getAll() {
        return subscriptionMap.values();
    }
}
