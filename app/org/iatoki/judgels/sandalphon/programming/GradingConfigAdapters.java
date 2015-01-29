package org.iatoki.judgels.sandalphon.programming;

import org.iatoki.judgels.sandalphon.programming.adapters.BatchWithSubtasksGradingConfigAdapter;

public final class GradingConfigAdapters {
    private GradingConfigAdapters() {
        // prevent instantiation
    }

    public static GradingConfigAdapter fromGradingType(String gradingType) {
        switch (gradingType) {
            case "BatchWithSubtasks":
                return new BatchWithSubtasksGradingConfigAdapter();
            default:
                throw new IllegalArgumentException("Grading type " + gradingType + " unknown");
        }
    }
}
