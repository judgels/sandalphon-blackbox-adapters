package org.iatoki.judgels.sandalphon.programming.adapters;

import org.iatoki.judgels.gabriel.GradingConfig;
import org.iatoki.judgels.gabriel.blackbox.TestGroup;
import org.iatoki.judgels.gabriel.blackbox.configs.BatchWithSubtasksGradingConfig;
import org.iatoki.judgels.sandalphon.forms.configs.BatchWithSubtasksGradingConfigForm;
import org.iatoki.judgels.sandalphon.programming.Problem;
import org.iatoki.judgels.sandalphon.views.html.programming.configs.batchWithSubtasksGradingConfigView;
import play.data.Form;
import play.mvc.Http;
import play.twirl.api.Html;

import java.io.File;
import java.util.List;

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
    public Form<?> createFormFromRequest(Http.Request request) {
        return Form.form(BatchWithSubtasksGradingConfigForm.class).bindFromRequest(request);
    }

    @Override
    public Html renderUpdateGradingConfig(Form<?> form, Problem problem, List<File> testDataFiles, List<File> helperFiles) {
        @SuppressWarnings("unchecked")
        Form<BatchWithSubtasksGradingConfigForm> batchForm = (Form<BatchWithSubtasksGradingConfigForm>) form;

        return batchWithSubtasksGradingConfigView.render(batchForm, problem, testDataFiles, helperFiles);
    }
}
