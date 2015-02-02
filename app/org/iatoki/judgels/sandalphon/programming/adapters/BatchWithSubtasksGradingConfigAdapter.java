package org.iatoki.judgels.sandalphon.programming.adapters;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.iatoki.judgels.gabriel.GradingConfig;
import org.iatoki.judgels.gabriel.blackbox.SampleTestCase;
import org.iatoki.judgels.gabriel.blackbox.Subtask;
import org.iatoki.judgels.gabriel.blackbox.TestCase;
import org.iatoki.judgels.gabriel.blackbox.TestGroup;
import org.iatoki.judgels.gabriel.graders.BatchWithSubtasksGradingConfig;
import org.iatoki.judgels.sandalphon.forms.configs.BatchWithSubtasksGradingConfigForm;
import org.iatoki.judgels.sandalphon.programming.GradingConfigAdapter;
import org.iatoki.judgels.sandalphon.programming.Problem;
import org.iatoki.judgels.sandalphon.views.html.programming.configs.batchWithSubtasksGradingConfigView;
import play.data.Form;
import play.mvc.Http;
import play.twirl.api.Html;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class BatchWithSubtasksGradingConfigAdapter implements GradingConfigAdapter {
    @Override
    public Form<?> createFormFromConfig(GradingConfig config) {
        BatchWithSubtasksGradingConfig batchConfig = (BatchWithSubtasksGradingConfig) config;
        BatchWithSubtasksGradingConfigForm form = new BatchWithSubtasksGradingConfigForm();

        form.timeLimit = batchConfig.getTimeLimitInMilliseconds();
        form.memoryLimit = batchConfig.getMemoryLimitInKilobytes();

        form.sampleTestCaseInputs = Lists.transform(batchConfig.getSampleTestData(), tc -> tc.getInput());
        form.sampleTestCaseOutputs = Lists.transform(batchConfig.getSampleTestData(), tc -> tc.getOutput());

        int subtasksCount = Math.max(10, batchConfig.getSubtasks().size());

        ImmutableList.Builder<List<Integer>> sampleTestCaseSubtaskNumbers = ImmutableList.builder();

        for (SampleTestCase testCase : batchConfig.getSampleTestData()) {
            List<Integer> subtasks = Lists.newArrayList();

            Set<Integer> subtaskNumbers = testCase.getSubtaskNumbers();

            for (int i = 0; i < subtasksCount; i++) {
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

        for (TestGroup testGroup : batchConfig.getTestData()) {
            testCasesInputs.add(Lists.transform(testGroup.getTestCases(), tc -> tc.getInput()));
            testCaseOutputs.add(Lists.transform(testGroup.getTestCases(), tc -> tc.getOutput()));

            // unfortunately cannot be Guava's immutable list because it may contain nulls
            List<Integer> subtasks = Lists.newArrayList();

            Set<Integer> subtaskNumbers = testGroup.getSubtaskNumbers();

            for (int i = 0; i < subtasksCount; i++) {
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

        List<Integer> subtaskPoints = Lists.newArrayList();
        for (Subtask subtask : batchConfig.getSubtasks()) {
            subtaskPoints.add(subtask.getPoints());
        }
        for (int i = batchConfig.getSubtasks().size(); i < subtasksCount; i++) {
            subtaskPoints.add(null);
        }
        form.subtaskPoints = subtaskPoints;

        form.customScorer = batchConfig.getCustomScorer();

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
    public GradingConfig createConfigFromForm(Form<?> form) {
        BatchWithSubtasksGradingConfig config = new BatchWithSubtasksGradingConfig();

        @SuppressWarnings("unchecked")
        BatchWithSubtasksGradingConfigForm data = ((Form<BatchWithSubtasksGradingConfigForm>) form).get();

        config.timeLimitInMilliseconds = data.timeLimit;
        config.memoryLimitInKilobytes = data.memoryLimit;

        ImmutableList.Builder<SampleTestCase> sampleTestData = ImmutableList.builder();

        int totalSubtasksCount = 0;

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

            totalSubtasksCount = Math.max(totalSubtasksCount, sampleTestCaseSubtasks.stream().max(Integer::max).get());

            sampleTestData.add(new SampleTestCase(data.sampleTestCaseInputs.get(i), data.sampleTestCaseOutputs.get(i), sampleTestCaseSubtasks));
        }

        config.sampleTestData = sampleTestData.build();

        ImmutableList.Builder<TestGroup> testData = ImmutableList.builder();

        int testGroupsCount = 0;
        if (data.testCaseInputs != null) {
            testGroupsCount = data.testCaseInputs.size();
        }

        for (int i = 0; i < testGroupsCount; i++) {
            if (data.testGroupSubtaskNumbers != null && data.testGroupSubtaskNumbers.get(i) != null) {
                totalSubtasksCount = Math.max(totalSubtasksCount, data.testGroupSubtaskNumbers.get(i).size());
            }
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
            int subtasksCount = 0;
            if (data.testGroupSubtaskNumbers != null && data.testGroupSubtaskNumbers.get(i) != null) {
                subtasksCount = data.testGroupSubtaskNumbers.get(i).size();
            }
            for (int j = 0; j < subtasksCount; j++) {
                if (data.testGroupSubtaskNumbers.get(i).get(j) != null) {
                    subtaskNumbers.add(j);
                }
            }

            testData.add(new TestGroup(testCases.build(), subtaskNumbers.build()));
        }

        config.testData = testData.build();

        if (data.subtaskPoints != null) {
            totalSubtasksCount = Math.max(totalSubtasksCount, data.subtaskPoints.size());
        }

        ImmutableList.Builder<Integer> subtaskPoints = ImmutableList.builder();
        for (int i = 0; i < totalSubtasksCount; i++) {
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
    public Html renderUpdateGradingConfig(Form<?> form, Problem problem, List<File> testDataFiles, List<File> helperFiles) {
        @SuppressWarnings("unchecked")
        Form<BatchWithSubtasksGradingConfigForm> batchForm = (Form<BatchWithSubtasksGradingConfigForm>) form;

        return batchWithSubtasksGradingConfigView.render(batchForm, problem, testDataFiles, helperFiles);
    }
}
