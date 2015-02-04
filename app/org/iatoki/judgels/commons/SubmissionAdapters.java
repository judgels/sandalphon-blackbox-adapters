package org.iatoki.judgels.commons;

import org.iatoki.judgels.commons.adapters.BlackBoxSubmissionAdapter;

public final class SubmissionAdapters {
    private SubmissionAdapters() {
        // prevent instantiation
    }

    public static SubmissionAdapter fromGradingEngine(String engine) {
        switch (engine) {
            case "BatchWithSubtasks":
                return new BlackBoxSubmissionAdapter();
            default:
                throw new IllegalArgumentException("Grading engine " + engine + " unknown");
        }
    }
}
