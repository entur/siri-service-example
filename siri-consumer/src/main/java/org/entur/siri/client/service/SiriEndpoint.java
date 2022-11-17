package org.entur.siri.client.service;

import org.entur.siri.client.model.Subscription;
import org.entur.siri.client.repository.SubscriptionRepository;
import org.entur.siri21.util.SiriXml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.org.siri.siri21.Siri;

import javax.xml.bind.JAXBException;

@RestController
public class SiriEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(SiriEndpoint.class);

    @Value("${verbose.xml:true}")
    private boolean verbose;

    private final SubscriptionRepository subscriptionRepository;

    public SiriEndpoint(@Autowired SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @PostMapping(value = "/subscription/{id}", consumes = "application/xml")
    public ResponseEntity<Object> handleSubscriptionRequest(@PathVariable String id, @RequestBody Siri siriRequest) throws JAXBException {

        if (!subscriptionRepository.exists(id)) {
            LOG.info("Received request to invalid subscription - ignoring");
            return ResponseEntity.notFound().build();
        }

        if (verbose) {
            LOG.info(SiriXml.toXml(siriRequest));
        }

        Subscription subscription = subscriptionRepository.get(id);

        if (siriRequest.getHeartbeatNotification() != null) {
            subscription.markHeartbeatReceived();
            LOG.info("Received HeartbeatNotification: {}", subscription);
        } else if (siriRequest.getServiceDelivery() != null) {
            subscription.markDataReceived();
            LOG.info("Received SIRI ServiceDelivery: {}", subscription);
        }




        return ResponseEntity.ok().build();
    }
}
