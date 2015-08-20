package org.iatoki.judgels.sandalphon.blackbox.adapters.impls;

import org.iatoki.judgels.gabriel.blackbox.configs.SingleSourceFileBlackBoxGradingConfig;
import org.iatoki.judgels.sandalphon.blackbox.forms.SingleSourceFileBlackBoxGradingConfigForm;

import java.util.List;

public abstract class SingleSourceFileBlackBoxGradingConfigAdapter extends AbstractBlackBoxGradingConfigAdapter {

    protected final void fillSingleSourceFileBlackBoxGradingConfigFormPartsFromConfig(SingleSourceFileBlackBoxGradingConfigForm form, SingleSourceFileBlackBoxGradingConfig config) {
        fillAbstractBlackBoxGradingFormPartsFromConfig(form, config);
    }

    protected final List<Object> createSingleSourceFileBlackBoxGradingConfigPartsFromForm(SingleSourceFileBlackBoxGradingConfigForm form) {
        return createAbstractBlackBoxGradingConfigPartsFromForm(form);
    }
}
