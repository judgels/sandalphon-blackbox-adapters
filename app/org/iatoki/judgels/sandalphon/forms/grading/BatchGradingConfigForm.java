package org.iatoki.judgels.sandalphon.forms.grading;

import java.util.List;

public final class BatchGradingConfigForm {
    public int timeLimit;

    public int memoryLimit;

    public List<List<String>> testCaseInputs;

    public List<List<String>> testCaseOutputs;

    public List<List<Integer>> testSetSubtasks;

    public List<Integer> subtaskPoints;

    public List<String> subtaskParams;

    public String scoringExecutorFilename;
}
