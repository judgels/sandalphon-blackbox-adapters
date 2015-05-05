package org.iatoki.judgels.sandalphon.programming.adapters;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.io.FilenameUtils;
import org.iatoki.judgels.commons.FileInfo;
import org.iatoki.judgels.gabriel.GradingConfig;
import org.iatoki.judgels.gabriel.blackbox.TestCase;
import org.iatoki.judgels.gabriel.blackbox.TestGroup;
import org.iatoki.judgels.gabriel.blackbox.configs.InteractiveGradingConfig;
import org.iatoki.judgels.sandalphon.Problem;
import org.iatoki.judgels.sandalphon.forms.programming.configs.InteractiveGradingConfigForm;
import org.iatoki.judgels.sandalphon.views.html.programming.grading.interactiveGradingConfigView;
import play.data.Form;
import play.twirl.api.Html;

import java.util.List;

public final class InteractiveGradingConfigAdapter extends SingleSourceFileWithoutSubtasksBlackBoxGradingConfigAdapter implements ConfigurableWithAutoPopulation {
    @Override
    public Form<?> createFormFromConfig(GradingConfig config) {
        InteractiveGradingConfigForm form = new InteractiveGradingConfigForm();
        InteractiveGradingConfig castConfig = (InteractiveGradingConfig) config;
        fillSingleSourceFileWithoutSubtasksBlackBoxGradingConfigFormPartsFromConfig(form, castConfig);

        if (castConfig.getCommunicator() == null) {
            form.communicator = "(none)";
        } else {
            form.communicator = castConfig.getCommunicator();
        }

        return Form.form(InteractiveGradingConfigForm.class).fill(form);
    }

    @Override
    public Form<?> createEmptyForm() {
        return Form.form(InteractiveGradingConfigForm.class);
    }

    @Override
    public GradingConfig createConfigFromForm(Form<?> form) {
        @SuppressWarnings("unchecked")
        Form<InteractiveGradingConfigForm> castForm = (Form<InteractiveGradingConfigForm>) form;
        InteractiveGradingConfigForm formData = castForm.get();

        List<Object> parts = createSingleSourceFileWithoutSubtasksBlackBoxGradingConfigPartsFromForm(formData);

        int timeLimit = (int) parts.get(0);
        int memoryLimit = (int) parts.get(1);

        @SuppressWarnings("unchecked")
        List<TestGroup> testData = (List<TestGroup>) parts.get(2);

        String communicator;
        if (formData.communicator.equals("(none)")) {
            communicator = null;
        } else {
            communicator = formData.communicator;
        }

        return new InteractiveGradingConfig(timeLimit, memoryLimit, testData, communicator);
    }

    @Override
    public GradingConfig updateConfigWithAutoPopulation(GradingConfig config, List<FileInfo> testDataFiles) {
        ImmutableList.Builder<TestCase> testCases = ImmutableList.builder();

        for (int i = 0; i + 1 < testDataFiles.size(); i++) {
            String in = testDataFiles.get(i).getName();
            if (isTestCase(in)) {
                testCases.add(new TestCase(in, null, ImmutableSet.of(-1)));
            }
        }

        List<TestGroup> testData = ImmutableList.of(new TestGroup(0, ImmutableList.of()), new TestGroup(-1, testCases.build()));

        InteractiveGradingConfig castConfig = (InteractiveGradingConfig) config;
        return new InteractiveGradingConfig(castConfig.getTimeLimitInMilliseconds(), castConfig.getMemoryLimitInKilobytes(), testData, castConfig.getCommunicator());
    }

    @Override
    public Html renderUpdateGradingConfig(Form<?> form, Problem problem, List<FileInfo> testDataFiles, List<FileInfo> helperFiles) {
        @SuppressWarnings("unchecked")
        Form<InteractiveGradingConfigForm> castForm = (Form<InteractiveGradingConfigForm>) form;

        return interactiveGradingConfigView.render(castForm, problem, testDataFiles, helperFiles);
    }

    private boolean isTestCase(String in) {
        String inExtension = FilenameUtils.getExtension(in);

        return inExtension.equals("in");
    }
}
