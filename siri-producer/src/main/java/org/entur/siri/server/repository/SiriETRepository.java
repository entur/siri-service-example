package org.entur.siri.server.repository;

import org.entur.siri.server.util.SiriHelper;
import org.springframework.stereotype.Repository;
import uk.org.siri.siri21.EstimatedVehicleJourney;
import uk.org.siri.siri21.Siri;

@Repository
public class SiriETRepository extends SiriRepository<EstimatedVehicleJourney> {

    @Override
    protected Siri createServiceDelivery() {
        return SiriHelper.createSiriEtServiceDelivery(getAll());
    }

    @Override
    protected String createKey(EstimatedVehicleJourney element) {
        return element.getDatedVehicleJourneyRef().getValue();
    }
}
