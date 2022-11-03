package org.entur.siri.server.util;

import org.entur.siri.server.model.SiriDataType;
import uk.org.siri.siri21.EstimatedTimetableDeliveryStructure;
import uk.org.siri.siri21.EstimatedTimetableSubscriptionStructure;
import uk.org.siri.siri21.EstimatedVehicleJourney;
import uk.org.siri.siri21.EstimatedVersionFrameStructure;
import uk.org.siri.siri21.HeartbeatNotificationStructure;
import uk.org.siri.siri21.MessageRefStructure;
import uk.org.siri.siri21.PtSituationElement;
import uk.org.siri.siri21.RequestorRef;
import uk.org.siri.siri21.ResponseStatus;
import uk.org.siri.siri21.ServiceDelivery;
import uk.org.siri.siri21.Siri;
import uk.org.siri.siri21.SituationExchangeDeliveryStructure;
import uk.org.siri.siri21.SituationExchangeSubscriptionStructure;
import uk.org.siri.siri21.SubscriptionRefStructure;
import uk.org.siri.siri21.SubscriptionRequest;
import uk.org.siri.siri21.SubscriptionResponseStructure;
import uk.org.siri.siri21.TerminateSubscriptionRequestStructure;
import uk.org.siri.siri21.TerminateSubscriptionResponseStructure;
import uk.org.siri.siri21.TerminationResponseStatusStructure;
import uk.org.siri.siri21.VehicleActivityStructure;
import uk.org.siri.siri21.VehicleMonitoringDeliveryStructure;
import uk.org.siri.siri21.VehicleMonitoringSubscriptionStructure;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.UUID;

public class SiriHelper {


    //Simple indication of when server was started
    private static final Instant serverStartTime = Instant.now();

    public static Siri createSiriSxServiceDelivery(Collection<PtSituationElement> elements) {
        Siri siri = createSiriServiceDelivery();
        siri.getServiceDelivery()
                .getSituationExchangeDeliveries()
                .add(new SituationExchangeDeliveryStructure());


        siri.getServiceDelivery()
                .getSituationExchangeDeliveries()
                .get(0)
                .setSituations(new SituationExchangeDeliveryStructure.Situations());

        siri.getServiceDelivery()
                .getSituationExchangeDeliveries()
                .get(0)
                .getSituations()
                .getPtSituationElements()
                .addAll(elements)
        ;

        return siri;
    }

    public static Siri createSiriVmServiceDelivery(Collection<VehicleActivityStructure> elements) {
        Siri siri = createSiriServiceDelivery();
        siri.getServiceDelivery()
                .getVehicleMonitoringDeliveries()
                .add(new VehicleMonitoringDeliveryStructure());

        siri.getServiceDelivery()
                .getVehicleMonitoringDeliveries()
                .get(0).getVehicleActivities()
                .addAll(elements)
        ;

        return siri;
    }

    public static Siri createSiriEtServiceDelivery(Collection<EstimatedVehicleJourney> elements) {
        Siri siri = createSiriServiceDelivery();
        siri.getServiceDelivery()
                .getEstimatedTimetableDeliveries()
                .add(new EstimatedTimetableDeliveryStructure());

        siri.getServiceDelivery()
                .getEstimatedTimetableDeliveries()
                        .get(0).getEstimatedJourneyVersionFrames()
                        .add(new EstimatedVersionFrameStructure());

        siri.getServiceDelivery()
                .getEstimatedTimetableDeliveries()
                .get(0).getEstimatedJourneyVersionFrames()
                .get(0).getEstimatedVehicleJourneies()
                .addAll(elements);


        return siri;
    }

    private static Siri createSiriServiceDelivery() {
        Siri siri = createSiriObject();
        ServiceDelivery serviceDelivery = new ServiceDelivery();

        siri.setServiceDelivery(serviceDelivery);
        return siri;
    }

    public static Siri createHeartbeatNotification(String requestorRef) {
        Siri siri = createSiriObject();
        HeartbeatNotificationStructure heartbeat = new HeartbeatNotificationStructure();
        heartbeat.setStatus(true);
        heartbeat.setServiceStartedTime(serverStartTime.atZone(ZoneId.systemDefault()));
        heartbeat.setRequestTimestamp(ZonedDateTime.now());
        heartbeat.setProducerRef(createRequestorRef(requestorRef));
        siri.setHeartbeatNotification(heartbeat);
        return siri;

    }

    private static RequestorRef createRequestorRef(String requestorRef) {
        RequestorRef ref = new RequestorRef();
        ref.setValue(requestorRef);
        return ref;
    }

    private static Siri createSiriObject() {
        Siri siri = new Siri();
        siri.setVersion("2.1");
        return siri;
    }

    public static String resolveSubscriptionId(SubscriptionRequest subscriptionRequest) {
        if (!subscriptionRequest.getSituationExchangeSubscriptionRequests().isEmpty()) {

            SituationExchangeSubscriptionStructure situationExchangeSubscriptionStructure = subscriptionRequest.
                    getSituationExchangeSubscriptionRequests().get(0);

            return situationExchangeSubscriptionStructure.getSubscriptionIdentifier().getValue();

        } else if (!subscriptionRequest.getVehicleMonitoringSubscriptionRequests().isEmpty()) {

            VehicleMonitoringSubscriptionStructure vehicleMonitoringSubscriptionStructure =
                    subscriptionRequest.getVehicleMonitoringSubscriptionRequests().get(0);

            return vehicleMonitoringSubscriptionStructure.getSubscriptionIdentifier().getValue();

        } else if (!subscriptionRequest.getEstimatedTimetableSubscriptionRequests().isEmpty()) {

            EstimatedTimetableSubscriptionStructure estimatedTimetableSubscriptionStructure =
                    subscriptionRequest.getEstimatedTimetableSubscriptionRequests().get(0);

            return estimatedTimetableSubscriptionStructure.getSubscriptionIdentifier().getValue();
        }
        return null;
    }

    public static SiriDataType resolveSiriDataType(SubscriptionRequest subscriptionRequest) {
        if (!subscriptionRequest.getSituationExchangeSubscriptionRequests().isEmpty()) {
            return SiriDataType.SX;
        }
        if (!subscriptionRequest.getVehicleMonitoringSubscriptionRequests().isEmpty()) {
            return SiriDataType.VM;
        }
        if (!subscriptionRequest.getEstimatedTimetableSubscriptionRequests().isEmpty()) {
            return SiriDataType.ET;
        }
        return null;
    }

    public static Siri createSubscriptionResponse(String subscriptionRef) {
        Siri siri = createSiriObject();
        SubscriptionResponseStructure response = new SubscriptionResponseStructure();
        response.setServiceStartedTime(serverStartTime.atZone(ZoneId.systemDefault()));
        response.setRequestMessageRef(createMessageRef());
        response.setResponderRef(createRequestorRef(subscriptionRef));
        response.setResponseTimestamp(ZonedDateTime.now());


        ResponseStatus responseStatus = new ResponseStatus();
        responseStatus.setResponseTimestamp(ZonedDateTime.now());
        responseStatus.setRequestMessageRef(createMessageRef());
        responseStatus.setSubscriptionRef(createSubscriptionRef(subscriptionRef));

        responseStatus.setStatus(true);

        response.getResponseStatuses().add(responseStatus);

        siri.setSubscriptionResponse(response);
        return siri;
    }

    public static Siri createTerminateSubscriptionResponse(TerminateSubscriptionRequestStructure terminateSubscriptionRequest) {
        Siri siri = createSiriObject();

        TerminateSubscriptionResponseStructure response = new TerminateSubscriptionResponseStructure();
        TerminationResponseStatusStructure status = new TerminationResponseStatusStructure();
        status.setResponseTimestamp(ZonedDateTime.now());
        status.setSubscriptionRef(createSubscriptionRef(terminateSubscriptionRequest.getSubscriptionReves().get(0).getValue()));
        status.setStatus(true);

        response.getTerminationResponseStatuses().add(status);
        siri.setTerminateSubscriptionResponse(response);
        return siri;
    }

    private static MessageRefStructure createMessageRef() {
        MessageRefStructure ref = new MessageRefStructure();
        ref.setValue(UUID.randomUUID().toString());
        return ref;
    }

    private static SubscriptionRefStructure createSubscriptionRef(String subscriptionRef) {
        SubscriptionRefStructure ref = new SubscriptionRefStructure();
        ref.setValue(subscriptionRef);
        return ref;
    }
}
