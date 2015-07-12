package org.iatoki.judgels.sandalphon.adapters.impls;

import org.iatoki.judgels.sandalphon.adapters.GradingConfigAdapter;

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
