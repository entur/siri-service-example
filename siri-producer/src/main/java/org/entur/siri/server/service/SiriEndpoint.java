package org.entur.siri.server.service;

import org.entur.siri.server.model.Subscription;
import org.entur.siri.server.repository.SiriETRepository;
import org.entur.siri.server.repository.SiriSXRepository;
import org.entur.siri.server.repository.SiriVMRepository;
import org.entur.siri.server.repository.SubscriptionManager;
import org.entur.siri.server.util.SiriHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.org.siri.siri21.EstimatedTimetableRequestStructure;
import uk.org.siri.siri21.EstimatedVehicleJourney;
import uk.org.siri.siri21.RequestorRef;
import uk.org.siri.siri21.ServiceRequest;
import uk.org.siri.siri21.Siri;
import uk.org.siri.siri21.SituationExchangeRequestStructure;
import uk.org.siri.siri21.SubscriptionRequest;
import uk.org.siri.siri21.TerminateSubscriptionRequestStructure;
import uk.org.siri.siri21.VehicleMonitoringRequestStructure;

import javax.xml.datatype.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RestController
public class SiriEndpoint {

    private final SubscriptionManager subscriptionManager;

    private final SiriETRepository siriETRepository;

    private final SiriVMRepository siriVMRepository;

    private final SiriSXRepository siriSXRepository;

    public SiriEndpoint(
            @Autowired SubscriptionManager subscriptionManager,
            @Autowired SiriSXRepository siriSXRepository,
            @Autowired SiriVMRepository siriVMRepository,
            @Autowired SiriETRepository siriETRepository) {
        this.subscriptionManager = subscriptionManager;
        this.siriSXRepository = siriSXRepository;
        this.siriVMRepository = siriVMRepository;
        this.siriETRepository = siriETRepository;
    }

    @PostMapping(value = "/subscribe", produces = "application/xml", consumes = "application/xml")
    public Siri handleSubscriptionRequest(@RequestBody Siri siriRequest) {
        SubscriptionRequest subscriptionRequest = siriRequest.getSubscriptionRequest();
        if (subscriptionRequest == null && siriRequest.getTerminateSubscriptionRequest() != null) {
            return handleTerminateSubscriptionRequest(siriRequest);
        }

        String address = subscriptionRequest.getConsumerAddress();
        if (address == null) { // Fallback to Address
            address = subscriptionRequest.getAddress();
        }
        RequestorRef requestorRef = subscriptionRequest.getRequestorRef();
        Duration heartbeatInterval = subscriptionRequest.getSubscriptionContext().getHeartbeatInterval();
        Subscription subscription = new Subscription(
                siriRequest.getSubscriptionRequest().getRequestTimestamp(),
                SiriHelper.resolveSiriDataType(siriRequest.getSubscriptionRequest()),
                address,
                SiriHelper.resolveSubscriptionId(siriRequest.getSubscriptionRequest()),
                java.time.Duration.parse(heartbeatInterval.toString()),
                requestorRef
                );
        subscriptionManager.addSubscription(subscription);
        return SiriHelper.createSubscriptionResponse(subscription.getSubscriptionId());
    }

    @PostMapping(value = "/unsubscribe", produces = "application/xml", consumes = "application/xml")
    public Siri handleTerminateSubscriptionRequest(@RequestBody Siri siriRequest) {
        TerminateSubscriptionRequestStructure terminateSubscriptionRequest = siriRequest.getTerminateSubscriptionRequest();
        subscriptionManager.removeSubscription(terminateSubscriptionRequest.getSubscriptionReves().get(0).getValue());
        return SiriHelper.createTerminateSubscriptionResponse(terminateSubscriptionRequest);
    }


    @PostMapping(value = "/service", produces = "application/xml", consumes = "application/xml")
    public Siri handleServiceRequest(@RequestBody Siri siriRequest) {
        ServiceRequest serviceRequest = siriRequest.getServiceRequest();

        List<SituationExchangeRequestStructure> situationExchangeRequests = serviceRequest.getSituationExchangeRequests();
        // TODO: Handle request for SX

        List<VehicleMonitoringRequestStructure> vehicleMonitoringRequests = serviceRequest.getVehicleMonitoringRequests();
        // TODO: Handle request for VM

        List<EstimatedTimetableRequestStructure> estimatedTimetableRequests = serviceRequest.getEstimatedTimetableRequests();
        // TODO: Handle request for ET

        Collection<EstimatedVehicleJourney> siriEtElements = new ArrayList<>();
        return SiriHelper.createSiriEtServiceDelivery(siriEtElements);
    }
}
