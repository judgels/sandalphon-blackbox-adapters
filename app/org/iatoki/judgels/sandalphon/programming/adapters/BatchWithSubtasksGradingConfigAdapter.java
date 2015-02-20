package org.iatoki.judgels.sandalphon.programming.adapters;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.iatoki.judgels.gabriel.GradingConfig;
import org.iatoki.judgels.gabriel.blackbox.Subtask;
import org.iatoki.judgels.gabriel.blackbox.TestCase;
import org.iatoki.judgels.gabriel.blackbox.TestGroup;
import org.iatoki.judgels.gabriel.blackbox.configs.BatchWithSubtasksGradingConfig;
import org.iatoki.judgels.sandalphon.programming.forms.configs.BatchWithSubtasksGradingConfigForm;
import org.iatoki.judgels.sandalphon.programming.Problem;
import org.iatoki.judgels.sandalphon.programming.views.html.configs.batchWithSubtasksGradingConfigView;
import play.data.Form;
import play.twirl.api.Html;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public final class BatchWithSubtasksGradingConfigAdapter extends SingleSourceFileWithSubtasksGradingConfigAdapter {

    @Override
    public Form<?> createFormFromConfig(GradingConfig config) {
        BatchWithSubtasksGradingConfigForm form = new BatchWithSubtasksGradingConfigForm();
        BatchWithSubtasksGradingConfig castConfig = (BatchWithSubtasksGradingConfig) config;
        fillSingleSourceFileWithSubtasksGradingConfigFormPartsFromConfig(form, castConfig);

        if (castConfig.getCustomScorer() == null) {
            form.customScorer = "(none)";
        } else {
            form.customScorer = castConfig.getCustomScorer();
        }

        return Form.form(BatchWithSubtasksGradingConfigForm.class).fill(form);
    }

    @Override
    public GradingConfig createConfigFromForm(Form<?> form) {
        @SuppressWarnings("unchecked")
        Form<BatchWithSubtasksGradingConfigForm> castForm = (Form<BatchWithSubtasksGradingConfigForm>) form;
        BatchWithSubtasksGradingConfigForm formData = castForm.get();

        List<Object> parts = createSingleSourceFileWithSubtasksGradingConfigPartsFromForm(formData);

        int timeLimit = (int) parts.get(0);
        int memoryLimit = (int) parts.get(1);

        @SuppressWarnings("unchecked")
        List<TestGroup> testData = (List<TestGroup>) parts.get(2);

        @SuppressWarnings("unchecked")
        List<Integer> subtaskPoints = (List<Integer>) parts.get(3);

        String customScorer;
        if (formData.customScorer.equals("(none)")) {
            customScorer = null;
        } else {
            customScorer = formData.customScorer;
        }

        return new BatchWithSubtasksGradingConfig(timeLimit, memoryLimit, testData, subtaskPoints, customScorer);
    }

    @Override
    public GradingConfig createConfigFromTokilib(List<File> testDataFiles) {
        Set<String> filenames = Sets.newHashSet(Lists.transform(testDataFiles, f -> f.getName()));
        Set<String> filenamesNoExt = Sets.newHashSet();
        for (String filename : filenames) {
            String[] parts = filename.split("\\.");
            if (parts.length != 2) {
                continue;
            }

            filenamesNoExt.add(parts[0]);
        }

        List<TokilibFile> tokilibFiles = Lists.newArrayList();

        for (String filename : filenamesNoExt) {
            if (!filenames.contains(filename + ".in") || !filenames.contains(filename + ".out")) {
                continue;
            }

            String[] parts = filename.split("_");

            if (parts.length != 3) {
                continue;
            }

            try {
                String name = parts[0];
                int batchNo = Integer.parseInt(parts[1]);
                int tcNo = Integer.parseInt(parts[2]);

                tokilibFiles.add(new TokilibFile(name, batchNo, tcNo));
            } catch (NumberFormatException e) {

            }
        }

        Collections.sort(tokilibFiles);

        int maxBatchNo = 0;
        for (TokilibFile file : tokilibFiles) {
            maxBatchNo = Math.max(maxBatchNo, file.batchNo);
        }

        List<TestGroup> testData = Lists.newArrayList();
        for (int i = 0; i <= maxBatchNo; i++) {
            testData.add(new TestGroup(i, Lists.newArrayList()));
        }

        for (TokilibFile file : tokilibFiles) {
            String name = file.filename;
            int batchNo = file.batchNo;
            int tcNo = file.tcNo;

            String filename = name + "_" + batchNo + "_" + tcNo;
            Set<Integer> subtaskIds = Sets.newHashSet();

            if (batchNo == 0) {
                subtaskIds.add(0);
            } else {
                for (int i = batchNo; i <= maxBatchNo; i++) {
                    subtaskIds.add(i);
                }
            }

            TestCase testCase = new TestCase(filename + ".in", filename + ".out", subtaskIds);

            testData.get(file.batchNo).getTestCases().add(testCase);
        }

        List<Integer> subtaskPoints = Lists.newArrayList();
        for (int i = 1; i <= maxBatchNo; i++) {
            subtaskPoints.add(0);
        }

        return new BatchWithSubtasksGradingConfig(2000, 65536, testData, subtaskPoints, null);
    }

    @Override
    public Form<?> createEmptyForm() {
        return Form.form(BatchWithSubtasksGradingConfigForm.class);
    }

    @Override
    public Html renderUpdateGradingConfig(Form<?> form, Problem problem, List<File> testDataFiles, List<File> helperFiles) {
        @SuppressWarnings("unchecked")
        Form<BatchWithSubtasksGradingConfigForm> batchForm = (Form<BatchWithSubtasksGradingConfigForm>) form;

        return batchWithSubtasksGradingConfigView.render(batchForm, problem, testDataFiles, helperFiles);
    }
}

class TokilibFile implements Comparable<TokilibFile> {
    public String filename;
    public int batchNo;
    public int tcNo;

    public TokilibFile(String filename, int batchNo, int tcNo) {
        this.filename = filename;
        this.batchNo = batchNo;
        this.tcNo = tcNo;
    }

    @Override
    public int compareTo(TokilibFile o) {
        if (!filename.equals(o.filename)) {
            return filename.compareTo(o.filename);
        }

        if (batchNo != o.batchNo) {
            return batchNo - o.batchNo;
        }

        if (tcNo != o.tcNo) {
            return tcNo - o.tcNo;
        }

        return 0;
    }
}
