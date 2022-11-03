package org.entur.siri.server.repository;

import org.entur.siri.server.model.SiriDataType;
import org.entur.siri.server.model.Subscription;
import org.entur.siri.server.util.HttpHelper;
import org.entur.siri.server.util.SiriHelper;
import org.entur.siri21.util.SiriXml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.org.siri.siri21.Siri;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Repository
public class SubscriptionManager {
    private static final Logger LOG = LoggerFactory.getLogger(SubscriptionManager.class);
    private static final Integer MAX_FAILED_COUNTER = 5;

    private final Map<String, Subscription> subscriptions = new HashMap<>();
    private final Map<String, Integer> subscriptionFailCounter = new HashMap<>();
    private final Map<String, ScheduledExecutorService> heartbeatExecutors = new HashMap<>();

    private final SiriETRepository siriETRepository;
    private final SiriVMRepository siriVMRepository;
    private final SiriSXRepository siriSXRepository;

    private final HttpHelper httpHelper;

    public SubscriptionManager(
            @Autowired SiriSXRepository siriSXRepository,
            @Autowired SiriVMRepository siriVMRepository,
            @Autowired SiriETRepository siriETRepository,
            @Autowired HttpHelper httpHelper) {
        this.siriSXRepository = siriSXRepository;
        this.siriVMRepository = siriVMRepository;
        this.siriETRepository = siriETRepository;
        this.httpHelper = httpHelper;
    }

    public void pushSiriToSubscribers(Siri siri) {
        LOG.info("Pushing data to {} subscribers.", subscriptions.size());
        if (!siri.getServiceDelivery().getVehicleMonitoringDeliveries().isEmpty()) {
            for (Subscription subscription : subscriptions.values()) {
                if (subscription.getSubscriptionType() == SiriDataType.VM) {
                    try {
                        httpHelper.postData(subscription.getAddress(), SiriXml.toXml(siri));
                    } catch (Exception e) {
                        // Ignore
                    }
                }
            }
        }
        if (!siri.getServiceDelivery().getEstimatedTimetableDeliveries().isEmpty()) {
            for (Subscription subscription : subscriptions.values()) {
                if (subscription.getSubscriptionType() == SiriDataType.ET) {
                    try {
                        httpHelper.postData(subscription.getAddress(), SiriXml.toXml(siri));
                    }  catch (Exception e) {
                        // Ignore
                    }
                }
            }
        }
        if (!siri.getServiceDelivery().getSituationExchangeDeliveries().isEmpty()) {
            for (Subscription subscription : subscriptions.values()) {
                if (subscription.getSubscriptionType() == SiriDataType.SX) {
                    try {
                        httpHelper.postData(subscription.getAddress(), SiriXml.toXml(siri));
                    }  catch (Exception e) {
                        // Ignore
                    }
                }
            }
        }
    }

    public void addSubscription(Subscription subscription) {
        LOG.info("Adding subscription: {}", subscription);
        subscriptions.put(subscription.getSubscriptionId(), subscription);
        initHeartbeat(subscription);

        Siri initialDelivery = null;
        if (subscription.getSubscriptionType() == SiriDataType.SX) {
            initialDelivery = SiriHelper.createSiriSxServiceDelivery(siriSXRepository.getAll());
        } else if (subscription.getSubscriptionType() == SiriDataType.VM) {
            initialDelivery = SiriHelper.createSiriVmServiceDelivery(siriVMRepository.getAll());
        } else if (subscription.getSubscriptionType() == SiriDataType.ET) {
            initialDelivery = SiriHelper.createSiriEtServiceDelivery(siriETRepository.getAll());
        }

        try {
            httpHelper.postData(subscription.getAddress(), SiriXml.toXml(initialDelivery));
        } catch (Exception e) {
            LOG.warn("Initial delivery failed to address {}", subscription.getAddress());
            // Ignore
        }

        LOG.info("Added subscription: {}, now have {} subscriptions", subscription, subscriptions.size());
    }
    public void removeSubscription(String subscriptionId) {
        subscriptions.remove(subscriptionId);
        subscriptionFailCounter.remove(subscriptionId);
        stopHeartbeat(subscriptionId);
    }

    private void stopHeartbeat(String subscriptionId) {
        ScheduledExecutorService scheduledExecutorService = heartbeatExecutors.get(subscriptionId);
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdown();
        }
        heartbeatExecutors.remove(subscriptionId);
    }
    private void initHeartbeat(Subscription subscription) {
        ScheduledExecutorService heartbeatExecutorService = Executors.newSingleThreadScheduledExecutor();
        heartbeatExecutorService.scheduleAtFixedRate(
                () -> {
                    try {
                        if (hasFailed(subscription)) {
                            LOG.warn("Subscription has failed {} times, removing.", subscriptionFailCounter.get(subscription.getSubscriptionId()));
                            removeSubscription(subscription.getSubscriptionId());
                        } else {

                            LOG.info("Posting heartbeat to {}", subscription);

                            int responseCode = httpHelper.postHeartbeat(subscription.getAddress(), subscription.getSubscriptionId());
                            if (responseCode != 200) {
                                markFailed(subscription);
                            }

                            LOG.info("Posted heartbeat to {}", subscription);
                        }
                    } catch (Exception e) {
                        markFailed(subscription);
                        LOG.warn("Post heartbeat to {} failed with {}.", subscription, e.getMessage());
                    }
                },
                subscription.getHeartbeatInterval().getSeconds(),
                subscription.getHeartbeatInterval().getSeconds(),
                TimeUnit.SECONDS
        );

        LOG.info("Adding heartbeat for subscription {} every {} s", subscription, subscription.getHeartbeatInterval().getSeconds());
        if (heartbeatExecutors.containsKey(subscription.getSubscriptionId())) {
            heartbeatExecutors.get(subscription.getSubscriptionId()).shutdown();
            heartbeatExecutors.remove(subscription.getSubscriptionId());
        }
        heartbeatExecutors.put(subscription.getSubscriptionId(), heartbeatExecutorService);
    }

    private void markFailed(Subscription subscription) {
        Integer failedCounter = subscriptionFailCounter.getOrDefault(subscription.getSubscriptionId(), 0);
        subscriptionFailCounter.put(subscription.getSubscriptionId(), failedCounter+1);
    }

    private boolean hasFailed(Subscription subscription) {
        Integer failedCounter = subscriptionFailCounter.getOrDefault(subscription.getSubscriptionId(), 0);
        return failedCounter >= MAX_FAILED_COUNTER;
    }
}
