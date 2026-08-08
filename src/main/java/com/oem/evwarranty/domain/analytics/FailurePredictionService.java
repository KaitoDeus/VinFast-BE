package com.oem.evwarranty.domain.analytics;

import com.oem.evwarranty.domain.vehicle.Vehicle;


import com.oem.evwarranty.domain.analytics.PredictionResult;
import java.util.List;

/**
 * Service interface for vehicle failure prediction using historical data.
 */
public interface FailurePredictionService {

    /**
     * Predict potential failures for a given vehicle based on its history and fleet
     * trends.
     * 
     * @param vehicleId the ID of the vehicle
     * @return a list of predicted failures and their risks
     */
    List<PredictionResult> predictFailures(Long vehicleId);
}


