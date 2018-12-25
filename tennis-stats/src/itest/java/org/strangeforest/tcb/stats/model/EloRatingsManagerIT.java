package org.strangeforest.tcb.stats.model;

import java.util.*;
import javax.sql.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;
import org.springframework.test.context.*;
import org.springframework.test.context.junit.jupiter.*;
import org.strangeforest.tcb.stats.*;
import org.strangeforest.tcb.stats.model.elo.*;
import org.strangeforest.tcb.stats.model.elo.EloRatingsManager.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = DataSourceITConfig.class, initializers = ConfigFileApplicationContextInitializer.class)
class EloRatingsManagerIT {

	@Autowired private DataSource dataSource;

	@Test //@Disabled
	void tuneEloRatingsManager() throws InterruptedException {
		EloRatingsManager eloRatingsManager = new EloRatingsManager(dataSource);
		double maxValue = 0.0;
		PredictionResult maxResult = null;
		for (double tuningValue = 500.0; tuningValue <= 600.0; tuningValue += 10.0) {
//			EloCalculator.tuningValue = tuningValue;
			System.out.println("\nTuning value: " + tuningValue);
			Map<String, PredictionResult> results = eloRatingsManager.compute(false, false, null, 0);
			PredictionResult result = results.get("E");
			if (maxResult == null || result.getScore() > maxResult.getScore()) {
				System.out.println("***** New Best Result!");
				maxValue = tuningValue;
				maxResult = result;
			}
		}
		System.out.println("\nBest Result: " + maxValue);
		System.out.println(maxResult);
	}
}
