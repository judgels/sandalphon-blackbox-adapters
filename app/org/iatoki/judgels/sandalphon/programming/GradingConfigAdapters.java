package org.iatoki.judgels.sandalphon.programming;

import org.iatoki.judgels.sandalphon.programming.adapters.BatchWithSubtasksGradingConfigAdapter;
import org.iatoki.judgels.sandalphon.programming.adapters.InteractiveWithSubtasksGradingConfigAdapter;

public final class GradingConfigAdapters {
    private GradingConfigAdapters() {
        // prevent instantiation
    }

    public static GradingConfigAdapter fromGradingType(String gradingType) {
        switch (gradingType) {
            case "BatchWithSubtasks":
                return new BatchWithSubtasksGradingConfigAdapter();
            case "InteractiveWithSubtasks":
                return new InteractiveWithSubtasksGradingConfigAdapter();
            default:
                throw new IllegalArgumentException("Grading type " + gradingType + " unknown");
        }
    }
}
