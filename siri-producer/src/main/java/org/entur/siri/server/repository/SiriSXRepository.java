package org.entur.siri.server.repository;

import org.entur.siri.server.util.SiriHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.org.siri.siri21.EstimatedVehicleJourney;
import uk.org.siri.siri21.PtSituationElement;
import uk.org.siri.siri21.Siri;

@Repository
public class SiriSXRepository extends SiriRepository<PtSituationElement> {

    @Override
    protected Siri createServiceDelivery() {
        return SiriHelper.createSiriSxServiceDelivery(getAll());
    }
    @Override
    protected String createKey(PtSituationElement element) {
        return element.getSituationNumber().getValue();
    }
}
