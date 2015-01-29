package org.iatoki.judgels.sandalphon.programming.adapters;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import org.iatoki.judgels.gabriel.blackbox.BlackBoxGradingConfig;
import org.iatoki.judgels.gabriel.blackbox.SampleTestCase;
import org.iatoki.judgels.gabriel.blackbox.TestCase;
import org.iatoki.judgels.gabriel.blackbox.TestGroup;
import org.iatoki.judgels.gabriel.graders.BatchWithSubtasksGradingConfig;
import org.iatoki.judgels.sandalphon.forms.grading.BatchWithSubtasksGradingConfigForm;
import org.iatoki.judgels.sandalphon.programming.GradingConfigAdapter;
import org.iatoki.judgels.sandalphon.programming.Problem;
import org.iatoki.judgels.sandalphon.views.html.programming.grading.batchWithSubtasksGradingConfigView;
import play.data.Form;
import play.mvc.Http;
import play.twirl.api.Html;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class BatchWithSubtasksGradingConfigAdapter implements GradingConfigAdapter {
    @Override
    public Form<?> createFormFromConfigJson(String gradingConfigJson) {
        BatchWithSubtasksGradingConfig config = new Gson().fromJson(gradingConfigJson, BatchWithSubtasksGradingConfig.class);
        BatchWithSubtasksGradingConfigForm form = new BatchWithSubtasksGradingConfigForm();

        form.timeLimit = config.getTimeLimitInMilliseconds();
        form.memoryLimit = config.getMemoryLimitInKilobytes();

        form.sampleTestCaseInputs = Lists.transform(config.getSampleTestData(), tc -> tc.getInput());
        form.sampleTestCaseOutputs = Lists.transform(config.getSampleTestData(), tc -> tc.getOutput());

        ImmutableList.Builder<List<Integer>> sampleTestCaseSubtaskNumbers = ImmutableList.builder();

        for (SampleTestCase testCase : config.getSampleTestData()) {
            List<Integer> subtasks = Lists.newArrayList();

            Set<Integer> subtaskNumbers = testCase.getSubtaskNumbers();

            for (int i = 0; i < config.getSubtasks().size(); i++) {
                if (subtaskNumbers.contains(i)) {
                    subtasks.add(i);
                } else {
                    subtasks.add(null);
                }
            }
            sampleTestCaseSubtaskNumbers.add(subtasks);
        }

        form.sampleTestCaseSubtaskNumbers = sampleTestCaseSubtaskNumbers.build();

        ImmutableList.Builder<List<String>> testCasesInputs = ImmutableList.builder();
        ImmutableList.Builder<List<String>> testCaseOutputs = ImmutableList.builder();
        ImmutableList.Builder<List<Integer>> testGroupSubtasks = ImmutableList.builder();

        for (TestGroup testGroup : config.getTestData()) {
            testCasesInputs.add(Lists.transform(testGroup.getTestCases(), tc -> tc.getInput()));
            testCaseOutputs.add(Lists.transform(testGroup.getTestCases(), tc -> tc.getOutput()));

            // unfortunately cannot be Guava's immutable list because it may contain nulls
            List<Integer> subtasks = Lists.newArrayList();

            Set<Integer> subtaskNumbers = testGroup.getSubtaskNumbers();

            for (int i = 0; i < config.getSubtasks().size(); i++) {
                if (subtaskNumbers.contains(i)) {
                    subtasks.add(i);
                } else {
                    subtasks.add(null);
                }
            }
            testGroupSubtasks.add(subtasks);
        }

        form.testCaseInputs = testCasesInputs.build();
        form.testCaseOutputs = testCaseOutputs.build();
        form.testGroupSubtaskNumbers = testGroupSubtasks.build();

        form.subtaskPoints = Lists.transform(config.getSubtasks(), s -> s.getPoints());

        form.customScorer = config.getCustomScorer();

        if (form.customScorer == null) {
            form.customScorer = "(None)";
        }

        return Form.form(BatchWithSubtasksGradingConfigForm.class).fill(form);
    }

    @Override
    public Form<?> createFormFromRequest(Http.Request request) {
        return Form.form(BatchWithSubtasksGradingConfigForm.class).bindFromRequest(request);
    }

    @Override
    public BlackBoxGradingConfig createConfigFromForm(Form<?> form) {
        BatchWithSubtasksGradingConfig config = new BatchWithSubtasksGradingConfig();

        @SuppressWarnings("unchecked")
        BatchWithSubtasksGradingConfigForm data = ((Form<BatchWithSubtasksGradingConfigForm>) form).get();

        config.timeLimitInMilliseconds = data.timeLimit;
        config.memoryLimitInKilobytes = data.memoryLimit;

        ImmutableList.Builder<SampleTestCase> sampleTestData = ImmutableList.builder();

        int sampleTestCasesCount = 0;
        if (data.sampleTestCaseInputs != null) {
            sampleTestCasesCount = data.sampleTestCaseInputs.size();
        }

        for (int i = 0; i < sampleTestCasesCount; i++) {
            List<Integer> subtasks;

            if (data.sampleTestCaseSubtaskNumbers == null || i >= data.sampleTestCaseSubtaskNumbers.size()) {
                subtasks = ImmutableList.of();
            } else {
                subtasks = data.sampleTestCaseSubtaskNumbers.get(i);
            }

            Set<Integer> sampleTestCaseSubtasks = subtasks.stream()
                    .filter(s -> s != null)
                    .collect(Collectors.toSet());

            sampleTestData.add(new SampleTestCase(data.sampleTestCaseInputs.get(i), data.sampleTestCaseOutputs.get(i), sampleTestCaseSubtasks));
        }

        config.sampleTestData = sampleTestData.build();

        ImmutableList.Builder<TestGroup> testData = ImmutableList.builder();

        int testGroupsCount = 0;
        if (data.testCaseInputs != null) {
            testGroupsCount = data.testCaseInputs.size();
        }

        int subtasksCount;
        if (data.testGroupSubtaskNumbers == null) {
            subtasksCount = 10;
        } else {
            subtasksCount = Math.max(data.testGroupSubtaskNumbers.size(), 10);
        }

        for (int i = 0; i < testGroupsCount; i++) {
            ImmutableList.Builder<TestCase> testCases = ImmutableList.builder();

            int testCasesCount = 0;
            if (data.testCaseInputs.get(i) != null) {
                testCasesCount = data.testCaseInputs.get(i).size();
            }

            for (int j = 0; j < testCasesCount; j++) {
                testCases.add(new TestCase(data.testCaseInputs.get(i).get(j), data.testCaseOutputs.get(i).get(j)));
            }

            ImmutableSet.Builder<Integer> subtaskNumbers = ImmutableSet.builder();
            for (int j = 0; j < subtasksCount; j++) {
                if (data.testGroupSubtaskNumbers != null && j < data.testGroupSubtaskNumbers.get(i).size() && data.testGroupSubtaskNumbers.get(i).get(j) != null) {
                    subtaskNumbers.add(j);
                }
            }

            testData.add(new TestGroup(testCases.build(), subtaskNumbers.build()));
        }

        config.testData = testData.build();

        ImmutableList.Builder<Integer> subtaskPoints = ImmutableList.builder();
        for (int i = 0; i < subtasksCount; i++) {
            if (data.subtaskPoints != null && i < data.subtaskPoints.size()) {
                subtaskPoints.add(data.subtaskPoints.get(i));
            } else {
                subtaskPoints.add(0);
            }
        }

        config.subtaskPoints = subtaskPoints.build();
        config.customScorer = data.customScorer;

        if (config.customScorer.equals("(None)")) {
            config.customScorer = null;
        }

        return config;
    }

    @Override
    public Html renderForm(Form<?> form, Problem problem, List<File> testDataFiles, List<File> helperFiles) {
        @SuppressWarnings("unchecked")
        Form<BatchWithSubtasksGradingConfigForm> batchForm = (Form<BatchWithSubtasksGradingConfigForm>) form;

        return batchWithSubtasksGradingConfigView.render(batchForm, problem, testDataFiles, helperFiles);
    }
}
