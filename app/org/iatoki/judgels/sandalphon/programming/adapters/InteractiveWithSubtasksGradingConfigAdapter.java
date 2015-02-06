package org.iatoki.judgels.sandalphon.programming.adapters;

import org.iatoki.judgels.gabriel.GradingConfig;
import org.iatoki.judgels.gabriel.blackbox.TestGroup;
import org.iatoki.judgels.gabriel.blackbox.configs.InteractiveWithSubtasksGradingConfig;
import org.iatoki.judgels.sandalphon.forms.configs.InteractiveWithSubtasksGradingConfigForm;
import org.iatoki.judgels.sandalphon.programming.Problem;
import org.iatoki.judgels.sandalphon.views.html.programming.configs.interactiveWithSubtasksGradingConfigView;
import play.data.Form;
import play.mvc.Http;
import play.twirl.api.Html;

import java.io.File;
import java.util.List;

public final class InteractiveWithSubtasksGradingConfigAdapter extends SingleSourceFileWithSubtasksGradingConfigAdapter {

    @Override
    public Form<?> createFormFromConfig(GradingConfig config) {
        InteractiveWithSubtasksGradingConfigForm form = new InteractiveWithSubtasksGradingConfigForm();
        InteractiveWithSubtasksGradingConfig castConfig = (InteractiveWithSubtasksGradingConfig) config;
        fillSingleSourceFileWithSubtasksGradingConfigFormPartsFromConfig(form, castConfig);

        if (castConfig.getCommunicator() == null) {
            form.communicator = "(None)";
        } else {
            form.communicator = castConfig.getCommunicator();
        }

        return Form.form(InteractiveWithSubtasksGradingConfigForm.class).fill(form);
    }

    @Override
    public GradingConfig createConfigFromForm(Form<?> form) {
        @SuppressWarnings("unchecked")
        Form<InteractiveWithSubtasksGradingConfigForm> castForm = (Form<InteractiveWithSubtasksGradingConfigForm>) form;
        InteractiveWithSubtasksGradingConfigForm formData = castForm.get();

        List<Object> parts = createSingleSourceFileWithSubtasksGradingConfigPartsFromForm(formData);

        int timeLimit = (int) parts.get(0);
        int memoryLimit = (int) parts.get(1);

        @SuppressWarnings("unchecked")
        List<TestGroup> testData = (List<TestGroup>) parts.get(2);

        @SuppressWarnings("unchecked")
        List<Integer> subtaskPoints = (List<Integer>) parts.get(3);

        String customScorer;
        if (formData.communicator.equals("(None)")) {
            customScorer = null;
        } else {
            customScorer = formData.communicator;
        }

        return new InteractiveWithSubtasksGradingConfig(timeLimit, memoryLimit, testData, subtaskPoints, customScorer);
    }

    @Override
    public Form<?> createFormFromRequest(Http.Request request) {
        return Form.form(InteractiveWithSubtasksGradingConfigForm.class).bindFromRequest(request);
    }

    @Override
    public Html renderUpdateGradingConfig(Form<?> form, Problem problem, List<File> testDataFiles, List<File> helperFiles) {
        @SuppressWarnings("unchecked")
        Form<InteractiveWithSubtasksGradingConfigForm> interactiveForm = (Form<InteractiveWithSubtasksGradingConfigForm>) form;

        return interactiveWithSubtasksGradingConfigView.render(interactiveForm, problem, testDataFiles, helperFiles);
    }
}
