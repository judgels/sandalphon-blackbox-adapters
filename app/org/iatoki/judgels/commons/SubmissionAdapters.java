package org.iatoki.judgels.commons;

import org.iatoki.judgels.commons.adapters.BlackBoxSubmissionAdapter;

public final class SubmissionAdapters {
    private SubmissionAdapters() {
        // prevent instantiation
    }

    public static SubmissionAdapter fromGradingType(String type) {
        switch (type) {
            case "BatchWithSubtasks":
                return new BlackBoxSubmissionAdapter();
            default:
                throw new IllegalArgumentException("Grading type " + type + " unknown");
        }
    }
}
