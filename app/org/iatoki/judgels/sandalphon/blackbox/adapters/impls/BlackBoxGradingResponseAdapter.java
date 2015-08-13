package org.iatoki.judgels.sandalphon.blackbox.adapters.impls;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import org.iatoki.judgels.gabriel.GradingResponse;
import org.iatoki.judgels.gabriel.blackbox.BlackBoxGradingResponse;
import org.iatoki.judgels.sandalphon.adapters.GradingResponseAdapter;

import java.util.Set;

public final class BlackBoxGradingResponseAdapter implements GradingResponseAdapter {

    @Override
    public Set<String> getSupportedGradingResponseNames() {
        return ImmutableSet.of("BlackBoxGradingResponse");
    }

    @Override
    public GradingResponse parseFromJson(String json) {
        return new Gson().fromJson(json, BlackBoxGradingResponse.class);
    }
}
