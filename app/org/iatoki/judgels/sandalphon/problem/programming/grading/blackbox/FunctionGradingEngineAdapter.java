package org.iatoki.judgels.sandalphon.problem.programming.grading.blackbox;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.io.FilenameUtils;
import org.iatoki.judgels.FileInfo;
import org.iatoki.judgels.gabriel.GradingConfig;
import org.iatoki.judgels.gabriel.blackbox.TestCase;
import org.iatoki.judgels.gabriel.blackbox.TestGroup;
import org.iatoki.judgels.gabriel.blackbox.configs.FunctionGradingConfig;
import org.iatoki.judgels.sandalphon.problem.programming.grading.ConfigurableWithAutoPopulation;
import org.iatoki.judgels.sandalphon.problem.programming.grading.blackbox.html.interactiveGradingConfigView;
import play.api.mvc.Call;
import play.data.Form;
import play.twirl.api.Html;

import java.util.List;

public final class FunctionGradingEngineAdapter extends SingleSourceFileWithoutSubtasksBlackBoxGradingEngineAdapter implements ConfigurableWithAutoPopulation {

    @Override
    public Form<?> createFormFromConfig(GradingConfig config) {
        FunctionGradingConfigForm form = new FunctionGradingConfigForm();
        FunctionGradingConfig castConfig = (FunctionGradingConfig) config;
        fillSingleSourceFileWithoutSubtasksBlackBoxGradingConfigFormPartsFromConfig(form, castConfig);

        if (castConfig.getMainSourceFile() == null) {
            form.mainSourceFile = "(none)";
        } else {
            form.mainSourceFile = castConfig.getMainSourceFile();
        }

        return Form.form(FunctionGradingConfigForm.class).fill(form);
    }

    @Override
    public Form<?> createEmptyForm() {
        return Form.form(FunctionGradingConfigForm.class);
    }

    @Override
    public GradingConfig createConfigFromForm(Form<?> form) {
        @SuppressWarnings("unchecked")
        Form<FunctionGradingConfigForm> castForm = (Form<FunctionGradingConfigForm>) form;
        FunctionGradingConfigForm formData = castForm.get();

        List<Object> parts = createSingleSourceFileWithoutSubtasksBlackBoxGradingConfigPartsFromForm(formData);

        int timeLimit = (int) parts.get(0);
        int memoryLimit = (int) parts.get(1);

        @SuppressWarnings("unchecked")
        List<TestGroup> testData = (List<TestGroup>) parts.get(2);

        String mainSourceFile;
        if (formData.mainSourceFile.equals("(none)")) {
            mainSourceFile = null;
        } else {
            mainSourceFile = formData.mainSourceFile;
        }

        String customScorer;
        if (formData.customScorer.equals("(none)")) {
            customScorer = null;
        } else {
            customScorer = formData.customScorer;
        }

        return new FunctionGradingConfig(timeLimit, memoryLimit, testData, mainSourceFile, customScorer);
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

        FunctionGradingConfig castConfig = (FunctionGradingConfig) config;
        return new FunctionGradingConfig(castConfig.getTimeLimitInMilliseconds(), castConfig.getMemoryLimitInKilobytes(), testData, castConfig.getMainSourceFile(), castConfig.getCustomScorer());
    }

    @Override
    public Html renderUpdateGradingConfig(Form<?> form, Call postUpdateGradingConfigCall, List<FileInfo> testDataFiles, List<FileInfo> helperFiles) {
        @SuppressWarnings("unchecked")
        Form<FunctionGradingConfigForm> castForm = (Form<FunctionGradingConfigForm>) form;

        return functionGradingConfigView.render(castForm, postUpdateGradingConfigCall, testDataFiles, helperFiles);
    }

    private boolean isTestCase(String in) {
        String inExtension = FilenameUtils.getExtension(in);

        return inExtension.equals("in");
    }
}
