package org.iatoki.judgels.sandalphon.forms.grading;

import play.data.validation.Constraints;

import java.util.List;

public final class SubtaskBatchGradingForm {
    @Constraints.Required
    public int timeLimit;

    @Constraints.Required
    public int memoryLimit;

    public List<List<String>> testCasesIn;

    public List<List<String>> testCasesOut;

    public List<List<Integer>> testSetsSubtasks;
}
