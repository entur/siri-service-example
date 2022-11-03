package org.entur.siri.client.util;

import org.entur.siri.client.model.SiriDataType;
import org.entur.siri.client.model.Subscription;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.org.siri.siri21.EstimatedTimetableRequestStructure;
import uk.org.siri.siri21.EstimatedTimetableSubscriptionStructure;
import uk.org.siri.siri21.MessageQualifierStructure;
import uk.org.siri.siri21.RequestorRef;
import uk.org.siri.siri21.Siri;
import uk.org.siri.siri21.SituationExchangeRequestStructure;
import uk.org.siri.siri21.SituationExchangeSubscriptionStructure;
import uk.org.siri.siri21.SubscriptionContextStructure;
import uk.org.siri.siri21.SubscriptionQualifierStructure;
import uk.org.siri.siri21.SubscriptionRequest;
import uk.org.siri.siri21.TerminateSubscriptionRequestStructure;
import uk.org.siri.siri21.VehicleMonitoringRequestStructure;
import uk.org.siri.siri21.VehicleMonitoringSubscriptionStructure;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.time.ZonedDateTime;
import java.util.UUID;

@Component
public class SiriHelper {

    private  final String SIRI_VERSION = "2.1";
    private final String baseUrl;

    public SiriHelper(@Value("${siri.client.endpoint.baseurl:}") String baseUrl) {
        this.baseUrl  = baseUrl;
    }

    public  Siri createSubscriptionRequest(Subscription subscription) {
        Siri siri = createSiriObject();

        SubscriptionRequest request = null;

        if (subscription.dataType == SiriDataType.SX) {
            request = createSituationExchangeSubscriptionRequest(subscription);
        }
        if (subscription.dataType == SiriDataType.VM) {
            request = createVehicleMonitoringSubscriptionRequest(subscription);
        }
        if (subscription.dataType == SiriDataType.ET) {
            request = createEstimatedTimetableSubscriptionRequest(subscription);
        }
        siri.setSubscriptionRequest(request);

        return siri;
    }


    public  Siri createTerminateSubscriptionRequest(Subscription subscription) {
        Siri siri = createSiriObject();
        TerminateSubscriptionRequestStructure terminationReq = new TerminateSubscriptionRequestStructure();

        terminationReq.setRequestTimestamp(ZonedDateTime.now());
        terminationReq.getSubscriptionReves().add(createSubscriptionIdentifier(subscription.subscriptionId));
        terminationReq.setRequestorRef(createRequestorRef(subscription.requestorRef));
        terminationReq.setMessageIdentifier(createMessageIdentifier(UUID.randomUUID().toString()));

        siri.setTerminateSubscriptionRequest(terminationReq);
        return siri;
    }

    private  SubscriptionRequest createSituationExchangeSubscriptionRequest(Subscription subscription) {
        SubscriptionRequest request = createSubscriptionRequest(createAddress(subscription.subscriptionId), subscription.getHeartbeatInterval(), subscription.requestorRef);

        SituationExchangeRequestStructure sxRequest = new SituationExchangeRequestStructure();
        sxRequest.setRequestTimestamp(ZonedDateTime.now());
        sxRequest.setVersion(SIRI_VERSION);
        sxRequest.setMessageIdentifier(createMessageIdentifier(UUID.randomUUID().toString()));

        SituationExchangeSubscriptionStructure sxSubscriptionReq = new SituationExchangeSubscriptionStructure();
        sxSubscriptionReq.setSituationExchangeRequest(sxRequest);
        sxSubscriptionReq.setSubscriptionIdentifier(createSubscriptionIdentifier(subscription.subscriptionId));
        sxSubscriptionReq.setInitialTerminationTime(ZonedDateTime.now().plusDays(365));
        sxSubscriptionReq.setSubscriberRef(request.getRequestorRef());

        request.getSituationExchangeSubscriptionRequests().add(sxSubscriptionReq);

        return request;
    }

    private  SubscriptionRequest createVehicleMonitoringSubscriptionRequest(Subscription subscription) {
        SubscriptionRequest request = createSubscriptionRequest(
                createAddress(subscription.subscriptionId),
                subscription.getHeartbeatInterval(),
                subscription.requestorRef);

        VehicleMonitoringRequestStructure vmRequest = new VehicleMonitoringRequestStructure();
        vmRequest.setRequestTimestamp(ZonedDateTime.now());
        vmRequest.setVersion(SIRI_VERSION);

        VehicleMonitoringSubscriptionStructure vmSubscriptionReq = new VehicleMonitoringSubscriptionStructure();
        vmSubscriptionReq.setVehicleMonitoringRequest(vmRequest);
        vmSubscriptionReq.setSubscriptionIdentifier(createSubscriptionIdentifier(subscription.subscriptionId));
        vmSubscriptionReq.setInitialTerminationTime(ZonedDateTime.now().plusDays(365));
        vmSubscriptionReq.setSubscriberRef(request.getRequestorRef());

        request.getVehicleMonitoringSubscriptionRequests().add(vmSubscriptionReq);

        return request;
    }

    private  SubscriptionRequest createSubscriptionRequest(String address, String heartbeatInterval, String requestorRef) {
        SubscriptionRequest request = new SubscriptionRequest();

        request.setRequestorRef(createRequestorRef(requestorRef));
        request.setMessageIdentifier(createMessageIdentifier(UUID.randomUUID().toString()));
        request.setConsumerAddress(address);
        request.setRequestTimestamp(ZonedDateTime.now());

        SubscriptionContextStructure ctx = new SubscriptionContextStructure();
        ctx.setHeartbeatInterval(createDuration(heartbeatInterval));

        request.setSubscriptionContext(ctx);

        return request;
    }

    private  SubscriptionQualifierStructure createSubscriptionIdentifier(String subscriptionId) {
        SubscriptionQualifierStructure subId = new SubscriptionQualifierStructure();
        subId.setValue(subscriptionId);
        return subId;
    }


    private  SubscriptionRequest createEstimatedTimetableSubscriptionRequest(Subscription subscription) {
        SubscriptionRequest request = createSubscriptionRequest(
                createAddress(subscription.subscriptionId),
                subscription.getHeartbeatInterval(),
                subscription.requestorRef);

        EstimatedTimetableRequestStructure etRequest = new EstimatedTimetableRequestStructure();
        etRequest.setRequestTimestamp(ZonedDateTime.now());
        etRequest.setVersion(SIRI_VERSION);


        EstimatedTimetableSubscriptionStructure etSubscriptionReq = new EstimatedTimetableSubscriptionStructure();
        etSubscriptionReq.setEstimatedTimetableRequest(etRequest);
        etSubscriptionReq.setSubscriptionIdentifier(createSubscriptionIdentifier(subscription.subscriptionId));
        etSubscriptionReq.setInitialTerminationTime(ZonedDateTime.now().plusDays(365));
        etSubscriptionReq.setSubscriberRef(request.getRequestorRef());

        request.getEstimatedTimetableSubscriptionRequests().add(etSubscriptionReq);

        return request;
    }

    private  String createAddress(String subscriptionId) {
        return baseUrl + subscriptionId;
    }

    private  MessageQualifierStructure createMessageIdentifier(String id) {
        MessageQualifierStructure messageQualifierStructure = new MessageQualifierStructure();
        messageQualifierStructure.setValue(id);
        return messageQualifierStructure;
    }

    private  javax.xml.datatype.Duration createDuration(String durationStr) {
        try {
            return DatatypeFactory.newInstance().newDuration(durationStr);
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    private  RequestorRef createRequestorRef(String requestorRef) {
        RequestorRef ref = new RequestorRef();
        ref.setValue(requestorRef);
        return ref;
    }

    private  Siri createSiriObject() {
        Siri siri = new Siri();
        siri.setVersion("2.1");
        return siri;
    }
}
