package org.skriptlang.skript.lang.experiment;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ExperimentDataTest {

    @Test
    void singularRequirementPassesWhenExperimentEnabled() {
        Experiment feature = Experiment.constant("my-feature", LifeCycle.EXPERIMENTAL);
        ExperimentData data = ExperimentData.createSingularData(feature);
        ExperimentSet enabled = new ExperimentSet();
        enabled.add(feature);

        assertTrue(data.checkRequirements(enabled));
    }

    @Test
    void singularRequirementFailsWhenExperimentMissing() {
        Experiment feature = Experiment.constant("my-feature", LifeCycle.EXPERIMENTAL);
        ExperimentData data = ExperimentData.createSingularData(feature);

        assertFalse(data.checkRequirements(new ExperimentSet()));
    }
}
