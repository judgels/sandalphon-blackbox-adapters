package org.iatoki.judgels.sandalphon.forms.grading;

import play.data.validation.Constraints;

import java.util.List;

public final class BatchGradingConfigForm {
    public int timeLimit;

    public int memoryLimit;

    public List<List<String>> testCaseInputs;

    public List<List<String>> testCaseOutputs;

    public List<List<Integer>> testSetSubtasks;

    public List<Double> subtaskPoints;

    public List<String> subtaskParams;
}
