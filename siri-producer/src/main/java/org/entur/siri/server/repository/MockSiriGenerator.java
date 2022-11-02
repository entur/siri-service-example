package org.entur.siri.server.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.org.siri.siri21.AffectedLineStructure;
import uk.org.siri.siri21.AffectsScopeStructure;
import uk.org.siri.siri21.CallStatusEnumeration;
import uk.org.siri.siri21.DatedVehicleJourneyRef;
import uk.org.siri.siri21.DefaultedTextStructure;
import uk.org.siri.siri21.DirectionRefStructure;
import uk.org.siri.siri21.EstimatedCall;
import uk.org.siri.siri21.EstimatedVehicleJourney;
import uk.org.siri.siri21.HalfOpenTimestampOutputRangeStructure;
import uk.org.siri.siri21.LineRef;
import uk.org.siri.siri21.LocationStructure;
import uk.org.siri.siri21.NaturalLanguageStringStructure;
import uk.org.siri.siri21.PtSituationElement;
import uk.org.siri.siri21.SituationNumber;
import uk.org.siri.siri21.SituationSourceStructure;
import uk.org.siri.siri21.SituationSourceTypeEnumeration;
import uk.org.siri.siri21.StopPointRefStructure;
import uk.org.siri.siri21.VehicleActivityStructure;
import uk.org.siri.siri21.VehicleJourneyRef;
import uk.org.siri.siri21.VehicleModesEnumeration;
import uk.org.siri.siri21.WorkflowStatusEnumeration;

import javax.annotation.PostConstruct;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class MockSiriGenerator {

    private static final int SX_GENERATE_FREQUENCY_SECONDS = 300;
    private static final int ET_GENERATE_FREQUENCY_SECONDS = 30;
    private static final int VM_GENERATE_FREQUENCY_SECONDS = 3;

    @Autowired
    private SiriETRepository siriETRepository;
    @Autowired
    private SiriVMRepository siriVMRepository;
    @Autowired
    private SiriSXRepository siriSXRepository;
    @Autowired
    private SubscriptionManager subscriptionManager;

    ScheduledExecutorService mockExecutorService = Executors.newSingleThreadScheduledExecutor();

    @PostConstruct
    private void initMockDataGenerator() {
        ZonedDateTime startTime = ZonedDateTime.now();
        mockExecutorService.scheduleAtFixedRate(() -> siriETRepository.add(createMockEstimatedVehicleData(startTime), subscriptionManager),
                        0,
                        ET_GENERATE_FREQUENCY_SECONDS,
                        TimeUnit.SECONDS);
        mockExecutorService.scheduleAtFixedRate(() -> siriVMRepository.add(createMockVehicleMonitoringData(), subscriptionManager),
                        0,
                        VM_GENERATE_FREQUENCY_SECONDS,
                        TimeUnit.SECONDS);
        mockExecutorService.scheduleAtFixedRate(() -> siriSXRepository.add(createMockPtSituationData(startTime), subscriptionManager),
                        0,
                        SX_GENERATE_FREQUENCY_SECONDS,
                        TimeUnit.SECONDS);
    }

    private PtSituationElement createMockPtSituationData(ZonedDateTime creationTime) {
        PtSituationElement sx = new PtSituationElement();

        sx.setCreationTime(creationTime);

        SituationNumber situationNumber = new SituationNumber();
        situationNumber.setValue("TST:SituationNumber:1234");
        sx.setSituationNumber(situationNumber);

        SituationSourceStructure source = new SituationSourceStructure();
        source.setSourceType(SituationSourceTypeEnumeration.DIRECT_REPORT);
        sx.setSource(source);

        sx.setProgress(WorkflowStatusEnumeration.OPEN);

        HalfOpenTimestampOutputRangeStructure validity = new HalfOpenTimestampOutputRangeStructure();
        validity.setStartTime(creationTime);
        validity.setEndTime(creationTime.plusDays(10));
        sx.getValidityPeriods().add(validity);

        sx.setUnknownReason(null);

        sx.getDescriptions().add(createText("Description-text - EN", "EN"));
        sx.getDescriptions().add(createText("Description-text - NO", "NO"));
        sx.getSummaries().add(createText("Summary-text - EN", "EN"));
        sx.getSummaries().add(createText("Summary-text - NO", "NO"));

        AffectsScopeStructure affects = new AffectsScopeStructure();
        AffectsScopeStructure.Networks networks = new AffectsScopeStructure.Networks();
        AffectsScopeStructure.Networks.AffectedNetwork affectedNetwork = new AffectsScopeStructure.Networks.AffectedNetwork();
        AffectedLineStructure affectedLine = new AffectedLineStructure();

        LineRef lineRef = new LineRef();
        lineRef.setValue("TST:Line:1234");
        affectedLine.setLineRef(lineRef);

        affectedNetwork.getAffectedLines().add(affectedLine);
        networks.getAffectedNetworks().add(affectedNetwork);
        affects.setNetworks(networks);
        sx.setAffects(affects);

        return sx;
    }

    private DefaultedTextStructure createText(String value, String lang) {
        DefaultedTextStructure text = new DefaultedTextStructure();
        text.setValue(value);
        if (lang != null) {
            text.setLang(lang);
        }

        return text;
    }

    private VehicleActivityStructure createMockVehicleMonitoringData() {
        VehicleActivityStructure vm = new VehicleActivityStructure();
        vm.setRecordedAtTime(ZonedDateTime.now());
        vm.setValidUntilTime(ZonedDateTime.now().plusMinutes(5));

        VehicleActivityStructure.MonitoredVehicleJourney monitoredVehicleJourney = new VehicleActivityStructure.MonitoredVehicleJourney();

        LineRef lineRef = new LineRef();
        lineRef.setValue("TST:Line:1234");
        monitoredVehicleJourney.setLineRef(lineRef);

        VehicleJourneyRef vehicleJourneyRef = new VehicleJourneyRef();
        vehicleJourneyRef.setValue("TST:DatedServiceJourney:1234");
        monitoredVehicleJourney.setVehicleJourneyRef(vehicleJourneyRef);

        monitoredVehicleJourney.setMonitored(true);

        LocationStructure location = new LocationStructure();
        location.setLatitude(BigDecimal.valueOf(59.9071088433266));
        location.setLongitude(BigDecimal.valueOf(10.746674593538));
        monitoredVehicleJourney.setVehicleLocation(location);

        Duration delay = null;
        try {
            delay = DatatypeFactory.newInstance().newDuration("PT14S");
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }
        monitoredVehicleJourney.setDelay(delay);

        vm.setMonitoredVehicleJourney(monitoredVehicleJourney);
        return vm;
    }

    private EstimatedVehicleJourney createMockEstimatedVehicleData(ZonedDateTime startTime) {
        EstimatedVehicleJourney et = new EstimatedVehicleJourney();

        LineRef lineRef = new LineRef();
        lineRef.setValue("TST:Line:1234");
        et.setLineRef(lineRef);

        DirectionRefStructure dirRef = new DirectionRefStructure();
        dirRef.setValue("0");
        et.setDirectionRef(dirRef);

        et.getVehicleModes().add(VehicleModesEnumeration.BUS);

        et.setMonitored(true);

        et.setEstimatedCalls(createEstimatedCalls(10, startTime));

        DatedVehicleJourneyRef dsjRef = new DatedVehicleJourneyRef();
        dsjRef.setValue("TST:DatedServiceJourney:1234");
        et.setDatedVehicleJourneyRef(dsjRef);

        return et;
    }

    private EstimatedVehicleJourney.EstimatedCalls createEstimatedCalls(int numberOfCalls, ZonedDateTime startTime) {
        EstimatedVehicleJourney.EstimatedCalls estimatedCalls = new EstimatedVehicleJourney.EstimatedCalls();
        for (int i = 0; i < numberOfCalls; i++) {
            EstimatedCall call = new EstimatedCall();
                StopPointRefStructure stopPointRef = new StopPointRefStructure();
                stopPointRef.setValue("NSR:Quay:1000"+i);
            call.setStopPointRef(stopPointRef);
            call.setOrder(BigInteger.valueOf(i));
            call.getStopPointNames().add(createStringStructure("Quay " + i));
            if (i > 0) {
                call.setAimedArrivalTime(startTime.plusMinutes(i));
                call.setExpectedArrivalTime(startTime.plusMinutes(i));
                call.setArrivalStatus(CallStatusEnumeration.ON_TIME);
            }

            if (i < numberOfCalls-1) {
                call.setAimedDepartureTime(startTime.plusMinutes(i));
                call.setExpectedDepartureTime(startTime.plusMinutes(i));
                call.setDepartureStatus(CallStatusEnumeration.ON_TIME);
            }

            estimatedCalls.getEstimatedCalls().add(call);
        }

        return estimatedCalls;
    }

    private NaturalLanguageStringStructure createStringStructure(String name) {
        NaturalLanguageStringStructure s = new NaturalLanguageStringStructure();
        s.setValue(name);
        return s;
    }

}
