package org.iatoki.judgels.sandalphon.blackbox.adapters.impls;

import org.iatoki.judgels.sandalphon.adapters.SubmissionAdapter;

public final class SubmissionAdapters {

    private SubmissionAdapters() {
        // prevent instantiation
    }

    public static SubmissionAdapter fromGradingEngine(String engine) {
        switch (engine) {
            case "Batch":
            case "BatchWithSubtasks":
            case "Interactive":
            case "InteractiveWithSubtasks":
                return new BlackBoxSubmissionAdapter();
            default:
                throw new IllegalArgumentException("Grading engine " + engine + " unknown");
        }
    }
}
