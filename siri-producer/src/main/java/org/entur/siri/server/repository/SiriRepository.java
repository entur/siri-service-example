package org.entur.siri.server.repository;

import org.entur.siri.server.util.HttpHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.siri.siri21.Siri;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class SiriRepository<T> {
    private static Logger LOG = LoggerFactory.getLogger(HttpHelper.class);
    protected Map<String, T> siriData = new HashMap<>();

    public Collection<T> getAll(){
        return siriData.values();
    }

    public void add(T element, SubscriptionManager subscriptionManager) {
        LOG.info("Adding SIRI-data: {}", element);
        siriData.put(createKey(element), element);
        subscriptionManager.pushSiriToSubscribers(createServiceDelivery());
    }

    protected abstract Siri createServiceDelivery();

    protected abstract String createKey(T element);
}
