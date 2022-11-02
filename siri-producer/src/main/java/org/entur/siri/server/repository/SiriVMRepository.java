package org.entur.siri.server.repository;

import org.entur.siri.server.util.SiriHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.org.siri.siri21.Siri;
import uk.org.siri.siri21.VehicleActivityStructure;

@Repository
public class SiriVMRepository extends SiriRepository<VehicleActivityStructure> {

    @Override
    protected Siri createServiceDelivery() {
        return SiriHelper.createSiriVmServiceDelivery(getAll());
    }
    @Override
    protected String createKey(VehicleActivityStructure element) {
        return element.getMonitoredVehicleJourney().getVehicleJourneyRef().getValue();
    }
}
