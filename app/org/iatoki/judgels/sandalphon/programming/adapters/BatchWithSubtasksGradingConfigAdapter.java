package org.iatoki.judgels.sandalphon.programming.adapters;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.iatoki.judgels.gabriel.GradingConfig;
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

        form.sampleTestCaseInputs = Lists.transform(batchConfig.getTestData().get(0).getTestCases(), tc -> tc.getInput());
        form.sampleTestCaseOutputs = Lists.transform(batchConfig.getTestData().get(0).getTestCases(), tc -> tc.getOutput());

        int subtasksCount = Math.max(10, batchConfig.getSubtasks().size());

        ImmutableList.Builder<List<Integer>> sampleTestCaseSubtaskNumbers = ImmutableList.builder();

        for (TestCase sampleTestCase : batchConfig.getTestData().get(0).getTestCases()) {
            List<Integer> subtasks = Lists.newArrayList();

            Set<Integer> subtaskIds = sampleTestCase.getSubtaskIds();

            for (int id = 1; id <= subtasksCount; id++) {
                if (subtaskIds.contains(id)) {
                    subtasks.add(id);
                } else {
                    subtasks.add(null);
                }
            }
            sampleTestCaseSubtaskNumbers.add(subtasks);
        }

        form.sampleTestCaseSubtaskIds = sampleTestCaseSubtaskNumbers.build();

        ImmutableList.Builder<List<String>> testCasesInputs = ImmutableList.builder();
        ImmutableList.Builder<List<String>> testCaseOutputs = ImmutableList.builder();
        ImmutableList.Builder<List<Integer>> testGroupSubtasks = ImmutableList.builder();

        for (TestGroup testGroup : batchConfig.getTestData()) {
            if (testGroup.getId() == 0) {
                continue;
            }

            testCasesInputs.add(Lists.transform(testGroup.getTestCases(), tc -> tc.getInput()));
            testCaseOutputs.add(Lists.transform(testGroup.getTestCases(), tc -> tc.getOutput()));

            // unfortunately cannot be Guava's immutable list because it may contain nulls
            List<Integer> subtasks = Lists.newArrayList();

            if (!testGroup.getTestCases().isEmpty()) {
                Set<Integer> subtaskIds = testGroup.getTestCases().get(0).getSubtaskIds();

                for (int j = 0; j < subtasksCount; j++) {
                    if (subtaskIds.contains(j + 1)) {
                        subtasks.add(j + 1);
                    } else {
                        subtasks.add(null);
                    }
                }
            }
            testGroupSubtasks.add(subtasks);
        }

        form.testCaseInputs = testCasesInputs.build();
        form.testCaseOutputs = testCaseOutputs.build();
        form.testGroupSubtaskIds = testGroupSubtasks.build();

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
        BatchWithSubtasksGradingConfigForm batchForm = ((Form<BatchWithSubtasksGradingConfigForm>) form).get();

        config.timeLimitInMilliseconds = batchForm.timeLimit;
        config.memoryLimitInKilobytes = batchForm.memoryLimit;

        int totalSubtasksCount = 0;

        ImmutableList.Builder<TestCase> sampleTestCases = ImmutableList.builder();

        int sampleTestCasesCount = 0;
        if (batchForm.sampleTestCaseInputs != null) {
            sampleTestCasesCount = batchForm.sampleTestCaseInputs.size();
        }

        for (int i = 0; i < sampleTestCasesCount; i++) {
            List<Integer> subtasks;

            if (batchForm.sampleTestCaseSubtaskIds == null || i >= batchForm.sampleTestCaseSubtaskIds.size()) {
                subtasks = ImmutableList.of();
            } else {
                subtasks = batchForm.sampleTestCaseSubtaskIds.get(i);
            }

            Set<Integer> sampleTestCaseSubtasks = subtasks.stream()
                    .filter(s -> s != null)
                    .collect(Collectors.toSet());

            totalSubtasksCount = Math.max(totalSubtasksCount, sampleTestCaseSubtasks.stream().max(Integer::max).get());

            sampleTestCases.add(new TestCase(batchForm.sampleTestCaseInputs.get(i), batchForm.sampleTestCaseOutputs.get(i), sampleTestCaseSubtasks));
        }

        ImmutableList.Builder<TestGroup> testData = ImmutableList.builder();
        testData.add(new TestGroup(0, sampleTestCases.build()));

        int testDataGroupsCount = 0;
        if (batchForm.testCaseInputs != null) {
            testDataGroupsCount = batchForm.testCaseInputs.size();
        }

        for (int i = 0; i < testDataGroupsCount; i++) {
            if (batchForm.testGroupSubtaskIds != null && batchForm.testGroupSubtaskIds.get(i) != null) {
                totalSubtasksCount = Math.max(totalSubtasksCount, batchForm.testGroupSubtaskIds.get(i).size());
            }
        }

        for (int i = 0; i < testDataGroupsCount; i++) {

            ImmutableSet.Builder<Integer> subtaskIdsBuilder = ImmutableSet.builder();
            int subtasksCount = 0;
            if (batchForm.testGroupSubtaskIds != null && batchForm.testGroupSubtaskIds.get(i) != null) {
                subtasksCount = batchForm.testGroupSubtaskIds.get(i).size();
            }
            for (int j = 0; j < subtasksCount; j++) {
                if (batchForm.testGroupSubtaskIds.get(i).get(j) != null) {
                    subtaskIdsBuilder.add(j + 1);
                }
            }

            Set<Integer> subtaskIds = subtaskIdsBuilder.build();

            ImmutableList.Builder<TestCase> testCases = ImmutableList.builder();

            int testCasesCount = 0;
            if (batchForm.testCaseInputs.get(i) != null) {
                testCasesCount = batchForm.testCaseInputs.get(i).size();
            }

            for (int j = 0; j < testCasesCount; j++) {
                testCases.add(new TestCase(batchForm.testCaseInputs.get(i).get(j), batchForm.testCaseOutputs.get(i).get(j), subtaskIds));
            }


            testData.add(new TestGroup(i + 1, testCases.build()));
        }

        config.testData = testData.build();

        if (batchForm.subtaskPoints != null) {
            for (int i = 0; i < batchForm.subtaskPoints.size(); i++) {
                if (batchForm.subtaskPoints.get(i) != null) {
                    totalSubtasksCount = Math.max(totalSubtasksCount, i + 1);
                }
            }
        }

        ImmutableList.Builder<Integer> subtaskPoints = ImmutableList.builder();
        for (int i = 0; i < totalSubtasksCount; i++) {
            if (batchForm.subtaskPoints != null && i < batchForm.subtaskPoints.size() && batchForm.subtaskPoints.get(i) != null) {
                subtaskPoints.add(batchForm.subtaskPoints.get(i));
            } else {
                subtaskPoints.add(0);
            }
        }

        config.subtaskPoints = subtaskPoints.build();
        config.customScorer = batchForm.customScorer;

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
