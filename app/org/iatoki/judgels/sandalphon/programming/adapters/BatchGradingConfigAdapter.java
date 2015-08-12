package org.iatoki.judgels.sandalphon.programming.adapters;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.io.FilenameUtils;
import org.iatoki.judgels.gabriel.GradingConfig;
import org.iatoki.judgels.gabriel.blackbox.TestCase;
import org.iatoki.judgels.gabriel.blackbox.TestGroup;
import org.iatoki.judgels.gabriel.blackbox.configs.BatchGradingConfig;
import org.iatoki.judgels.sandalphon.commons.programming.ProgrammingProblem;
import org.iatoki.judgels.sandalphon.forms.programming.configs.BatchGradingConfigForm;
import org.iatoki.judgels.sandalphon.views.html.programming.configs.batchGradingConfigView;
import play.data.Form;
import play.twirl.api.Html;

import java.io.File;
import java.util.List;

public final class BatchGradingConfigAdapter extends SingleSourceFileWithoutSubtasksBlackBoxGradingConfigAdapter implements ConfigurableWithAutoPopulation {
    @Override
    public Form<?> createFormFromConfig(GradingConfig config) {
        BatchGradingConfigForm form = new BatchGradingConfigForm();
        BatchGradingConfig castConfig = (BatchGradingConfig) config;
        fillSingleSourceFileWithoutSubtasksBlackBoxGradingConfigFormPartsFromConfig(form, castConfig);

        if (castConfig.getCustomScorer() == null) {
            form.customScorer = "(none)";
        } else {
            form.customScorer = castConfig.getCustomScorer();
        }

        return Form.form(BatchGradingConfigForm.class).fill(form);
    }

    @Override
    public Form<?> createEmptyForm() {
        return Form.form(BatchGradingConfigForm.class);
    }

    @Override
    public GradingConfig createConfigFromForm(Form<?> form) {
        @SuppressWarnings("unchecked")
        Form<BatchGradingConfigForm> castForm = (Form<BatchGradingConfigForm>) form;
        BatchGradingConfigForm formData = castForm.get();

        List<Object> parts = createSingleSourceFileWithoutSubtasksBlackBoxGradingConfigPartsFromForm(formData);

        int timeLimit = (int) parts.get(0);
        int memoryLimit = (int) parts.get(1);

        @SuppressWarnings("unchecked")
        List<TestGroup> testData = (List<TestGroup>) parts.get(2);

        String customScorer;
        if (formData.customScorer.equals("(none)")) {
            customScorer = null;
        } else {
            customScorer = formData.customScorer;
        }

        return new BatchGradingConfig(timeLimit, memoryLimit, testData, customScorer);
    }

    @Override
    public GradingConfig updateConfigWithAutoPopulation(GradingConfig config, List<File> testDataFiles) {
        ImmutableList.Builder<TestCase> testCases = ImmutableList.builder();

        for (int i = 0; i + 1 < testDataFiles.size(); i++) {
            String in = testDataFiles.get(i).getName();
            String out = testDataFiles.get(i + 1).getName();
            if (isTestCasePair(in, out)) {
                testCases.add(new TestCase(in, out, ImmutableSet.of(-1)));
                i++;
            }
        }

        List<TestGroup> testData = ImmutableList.of(new TestGroup(0, ImmutableList.of()), new TestGroup(-1, testCases.build()));

        BatchGradingConfig castConfig = (BatchGradingConfig) config;
        return new BatchGradingConfig(castConfig.getTimeLimitInMilliseconds(), castConfig.getMemoryLimitInKilobytes(), testData, castConfig.getCustomScorer());
    }

    @Override
    public Html renderUpdateGradingConfig(Form<?> form, ProgrammingProblem problem, List<File> testDataFiles, List<File> helperFiles) {
        @SuppressWarnings("unchecked")
        Form<BatchGradingConfigForm> castForm = (Form<BatchGradingConfigForm>) form;

        return batchGradingConfigView.render(castForm, problem, testDataFiles, helperFiles);
    }

    private boolean isTestCasePair(String in, String out) {
        String inBaseName = FilenameUtils.getBaseName(in);
        String inExtension = FilenameUtils.getExtension(in);

        String outBaseName = FilenameUtils.getBaseName(out);
        String outExtension = FilenameUtils.getExtension(out);

        return inBaseName.equals(outBaseName) && inExtension.equals("in") && outExtension.equals("out");
    }
}
