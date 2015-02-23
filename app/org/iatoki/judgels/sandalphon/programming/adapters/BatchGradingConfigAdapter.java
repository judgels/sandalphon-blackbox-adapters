package org.iatoki.judgels.sandalphon.programming.adapters;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.gabriel.GradingConfig;
import org.iatoki.judgels.gabriel.blackbox.TestGroup;
import org.iatoki.judgels.gabriel.blackbox.configs.BatchGradingConfig;
import org.iatoki.judgels.sandalphon.programming.Problem;
import org.iatoki.judgels.sandalphon.programming.forms.configs.BatchGradingConfigForm;
import org.iatoki.judgels.sandalphon.programming.views.html.configs.batchGradingConfigView;
import play.data.Form;
import play.twirl.api.Html;

import java.io.File;
import java.util.List;

public final class BatchGradingConfigAdapter extends SingleSourceFileWithoutSubtasksBlackBoxGradingConfigAdapter {
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
    public GradingConfig createConfigFromTokilib(List<File> testDataFiles) {
        return null;
    }

    @Override
    public Html renderUpdateGradingConfig(Form<?> form, Problem problem, List<File> testDataFiles, List<File> helperFiles) {
        @SuppressWarnings("unchecked")
        Form<BatchGradingConfigForm> castForm = (Form<BatchGradingConfigForm>) form;

        return batchGradingConfigView.render(castForm, problem, testDataFiles, helperFiles);
    }
}
