package org.iatoki.judgels.commons.adapters;

import org.apache.commons.io.FileUtils;
import org.iatoki.judgels.commons.SubmissionAdapter;
import org.iatoki.judgels.commons.views.html.adapters.blackBoxViewStatementView;
import org.iatoki.judgels.gabriel.GradingConfig;
import org.iatoki.judgels.gabriel.GradingRequest;
import org.iatoki.judgels.gabriel.blackbox.BlackBoxGradingConfig;
import org.iatoki.judgels.gabriel.blackbox.SourceFile;
import play.api.mvc.Call;
import play.mvc.Http;
import play.twirl.api.Html;

import java.io.File;
import java.io.IOException;

public final class BlackBoxSubmissionAdapter implements SubmissionAdapter {

    @Override
    public Html renderViewStatement(Call targetCall, String statement, GradingConfig config) {
        BlackBoxGradingConfig blackBoxConfig = (BlackBoxGradingConfig) config;
        return blackBoxViewStatementView.render(targetCall, statement, blackBoxConfig);
    }

    @Override
    public GradingRequest createGradingRequest(GradingConfig config, Http.MultipartFormData body) {
        BlackBoxGradingConfig blackBoxConfig = (BlackBoxGradingConfig) config;

        for (String key : blackBoxConfig.getRequiredSourceFileKeys()) {
            Http.MultipartFormData.FilePart file = body.getFile(key);

            if (file == null) {
                throw new IllegalStateException("file can't be null");
            }

            File sourceFile = file.getFile();

            try {
                byte[] content = FileUtils.readFileToByteArray(sourceFile);

            } catch (IOException e) {

            }
        }

        return null;
    }
}
