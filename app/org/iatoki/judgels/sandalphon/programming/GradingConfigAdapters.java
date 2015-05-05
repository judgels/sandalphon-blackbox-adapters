package org.iatoki.judgels.sandalphon.programming;

import org.iatoki.judgels.sandalphon.programming.adapters.BatchGradingConfigAdapter;
import org.iatoki.judgels.sandalphon.programming.adapters.BatchWithSubtasksGradingConfigAdapter;
import org.iatoki.judgels.sandalphon.programming.adapters.InteractiveGradingConfigAdapter;
import org.iatoki.judgels.sandalphon.programming.adapters.InteractiveWithSubtasksGradingConfigAdapter;

public final class GradingConfigAdapters {
    private GradingConfigAdapters() {
        // prevent instantiation
    }

    public static GradingConfigAdapter fromGradingType(String gradingType) {
        switch (gradingType) {
            case "Batch":
                return new BatchGradingConfigAdapter();
            case "BatchWithSubtasks":
                return new BatchWithSubtasksGradingConfigAdapter();
            case "Interactive":
                return new InteractiveGradingConfigAdapter();
            case "InteractiveWithSubtasks":
                return new InteractiveWithSubtasksGradingConfigAdapter();
            default:
                throw new IllegalArgumentException("Grading type " + gradingType + " unknown");
        }
    }
}
