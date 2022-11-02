package org.entur.siri.client.service;

import org.entur.siri.client.model.SiriDataType;
import org.entur.siri.client.model.Subscription;
import org.entur.siri.client.repository.SubscriptionRepository;
import org.entur.siri.client.util.HttpHelper;
import org.entur.siri.client.util.SiriHelper;
import org.entur.siri21.util.SiriXml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.org.siri.siri21.Siri;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class SubscriptionService {

    private static final long MONITOR_FREQUENCY_SECONDS = 10;
    private static Logger LOG = LoggerFactory.getLogger(HttpHelper.class);
    @Value("${siri.server.endpoint:}")
    String siriEndpoint;
    @Value("${siri.subscription.enabled.sx:false}")
    boolean sxSubscriptionEnabled;
    @Value("${siri.subscription.enabled.vm:false}")
    boolean vmSubscriptionEnabled;
    @Value("${siri.subscription.enabled.et:false}")
    boolean etSubscriptionEnabled;

    @Autowired
    SubscriptionRepository subscriptionRepository;
    ScheduledExecutorService monitorService = Executors.newSingleThreadScheduledExecutor();

    @PostConstruct
    private void initSubscriptionMonitor() {
        monitorService.scheduleAtFixedRate(() -> checkSubscriptionStatus(),
                MONITOR_FREQUENCY_SECONDS,
                MONITOR_FREQUENCY_SECONDS,
                TimeUnit.SECONDS);
    }

    private void checkSubscriptionStatus() {
        try {
            Collection<Subscription> subscriptions = subscriptionRepository.getAll();
            for (Subscription subscription : subscriptions) {
                if (!subscription.isHealthy()) {
                    terminateSubscription(subscription);
                    startSubscription(subscription);
                }
            }
        } catch (Exception e) {
            LOG.warn("Unable to check subscriptionstatus: {}",e.getMessage());
        }
    }

    @PostConstruct
    private void initSubscriptions() {
        if (sxSubscriptionEnabled) {
            Subscription subscription = new Subscription(
                    UUID.randomUUID().toString(),
                    SiriDataType.SX,
                    30,
                    UUID.randomUUID().toString());

            subscriptionRepository.add(subscription);
        }
        if (etSubscriptionEnabled) {
            Subscription subscription = new Subscription(
                    UUID.randomUUID().toString(),
                    SiriDataType.ET,
                    30,
                    UUID.randomUUID().toString());

            subscriptionRepository.add(subscription);
        }
        if (vmSubscriptionEnabled) {
            Subscription subscription = new Subscription(
                    UUID.randomUUID().toString(),
                    SiriDataType.VM,
                    30,
                    UUID.randomUUID().toString());

            subscriptionRepository.add(subscription);
        }
    }

    private void startSubscription(Subscription subscription) throws IOException, JAXBException {
        Siri subscriptionRequest = SiriHelper.createSubscriptionRequest(subscription);
        int responseCode = HttpHelper.postData(siriEndpoint, SiriXml.toXml(subscriptionRequest));
        if (responseCode == 200) {
            subscription.markStarted();
            LOG.info("Initialized subscription: {}", subscription);
        } else {
            LOG.info("Initializing subscription failed with code: {}", responseCode);
        }
    }

    private void terminateSubscription(Subscription subscription) throws IOException, JAXBException {
        Siri subscriptionRequest = SiriHelper.createTerminateSubscriptionRequest(subscription);
        int responseCode = HttpHelper.postData(siriEndpoint, SiriXml.toXml(subscriptionRequest));
        if (responseCode == 200) {
            subscriptionRepository.add(subscription);
            LOG.info("Terminate subscription: {}", subscription);
        } else {
            LOG.info("Terminate subscription failed with code: {}", responseCode);
        }
    }

}
