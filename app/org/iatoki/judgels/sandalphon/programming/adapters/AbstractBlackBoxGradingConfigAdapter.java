package org.iatoki.judgels.sandalphon.programming.adapters;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.iatoki.judgels.gabriel.blackbox.TestCase;
import org.iatoki.judgels.gabriel.blackbox.TestGroup;
import org.iatoki.judgels.gabriel.blackbox.configs.AbstractBlackBoxGradingConfig;
import org.iatoki.judgels.sandalphon.forms.configs.AbstractBlackBoxGradingConfigForm;
import org.iatoki.judgels.sandalphon.programming.GradingConfigAdapter;

import java.util.List;

public abstract class AbstractBlackBoxGradingConfigAdapter implements GradingConfigAdapter {

    protected final void fillAbstractBlackBoxGradingFormPartsFromConfig(AbstractBlackBoxGradingConfigForm form, AbstractBlackBoxGradingConfig config) {
        form.timeLimit = config.getTimeLimitInMilliseconds();
        form.memoryLimit = config.getMemoryLimitInKilobytes();

        ImmutableList.Builder<List<String>> testCasesInputs = ImmutableList.builder();
        ImmutableList.Builder<List<String>> testCaseOutputs = ImmutableList.builder();

        for (TestGroup testGroup : config.getTestData()) {
            if (testGroup.getId() == 0) {
                form.sampleTestCaseInputs = Lists.transform(testGroup.getTestCases(), tc -> tc.getInput());
                form.sampleTestCaseOutputs = Lists.transform(testGroup.getTestCases(), tc -> tc.getOutput());
            } else {
                testCasesInputs.add(Lists.transform(testGroup.getTestCases(), tc -> tc.getInput()));
                testCaseOutputs.add(Lists.transform(testGroup.getTestCases(), tc -> tc.getOutput()));
            }
        }

        form.testCaseInputs = testCasesInputs.build();
        form.testCaseOutputs = testCaseOutputs.build();
    }

    protected final List<Object> createAbstractBlackBoxGradingConfigPartsFromForm(AbstractBlackBoxGradingConfigForm form) {
        int timeLimit = form.timeLimit;
        int memoryLimit = form.memoryLimit;

        ImmutableList.Builder<TestCase> sampleTestCases = ImmutableList.builder();

        int sampleTestCasesCount = 0;
        if (form.sampleTestCaseInputs != null) {
            sampleTestCasesCount = form.sampleTestCaseInputs.size();
        }

        for (int i = 0; i < sampleTestCasesCount; i++) {
            sampleTestCases.add(new TestCase(form.sampleTestCaseInputs.get(i), form.sampleTestCaseOutputs.get(i), null /* placeholder */));
        }

        ImmutableList.Builder<TestGroup> testData = ImmutableList.builder();
        testData.add(new TestGroup(0, sampleTestCases.build()));

        int testDataGroupsCount = 0;
        if (form.testCaseInputs != null) {
            testDataGroupsCount = form.testCaseInputs.size();
        }

        for (int i = 0; i < testDataGroupsCount; i++) {
            ImmutableList.Builder<TestCase> testCases = ImmutableList.builder();

            int testCasesCount = 0;
            if (form.testCaseInputs.get(i) != null) {
                testCasesCount = form.testCaseInputs.get(i).size();
            }

            for (int j = 0; j < testCasesCount; j++) {
                testCases.add(new TestCase(form.testCaseInputs.get(i).get(j), form.testCaseOutputs.get(i).get(j), null /* placeholder */));
            }


            testData.add(new TestGroup(-1 /* placeholder */, testCases.build()));
        }

        return ImmutableList.of(timeLimit, memoryLimit, testData.build());
    }
}
