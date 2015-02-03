package org.iatoki.judgels.commons.adapters;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.iatoki.judgels.commons.SubmissionAdapter;
import org.iatoki.judgels.commons.views.html.adapters.blackBoxViewSubmissionView;
import org.iatoki.judgels.commons.views.html.adapters.blackBoxViewStatementView;
import org.iatoki.judgels.gabriel.GradingConfig;
import org.iatoki.judgels.gabriel.GradingRequest;
import org.iatoki.judgels.gabriel.GradingSource;
import org.iatoki.judgels.gabriel.Verdict;
import org.iatoki.judgels.gabriel.blackbox.BlackBoxGradingConfig;
import org.iatoki.judgels.gabriel.blackbox.BlackBoxGradingRequest;
import org.iatoki.judgels.gabriel.blackbox.BlackBoxGradingResultDetails;
import org.iatoki.judgels.gabriel.blackbox.BlackBoxGradingSource;
import org.iatoki.judgels.gabriel.blackbox.SourceFile;
import play.api.mvc.Call;
import play.mvc.Http;
import play.twirl.api.Html;

import java.io.IOException;

public final class BlackBoxSubmissionAdapter implements SubmissionAdapter {

    @Override
    public Html renderViewStatement(Call targetCall, String statement, GradingConfig config) {
        BlackBoxGradingConfig blackBoxConfig = (BlackBoxGradingConfig) config;
        return blackBoxViewStatementView.render(targetCall, statement, blackBoxConfig);
    }

    @Override
    public Html renderViewSubmission(long submissionId, Verdict verdict, int score, String detailsAsJson, GradingConfig config) {
        BlackBoxGradingResultDetails details = new Gson().fromJson(detailsAsJson, BlackBoxGradingResultDetails.class);
        BlackBoxGradingConfig blackBoxConfig = (BlackBoxGradingConfig) config;
        return blackBoxViewSubmissionView.render(submissionId, verdict, score, details, blackBoxConfig);
    }

    @Override
    public GradingSource createGradingSource(GradingConfig config, Http.MultipartFormData body) {
        BlackBoxGradingConfig blackBoxConfig = (BlackBoxGradingConfig) config;

        ImmutableMap.Builder<String, SourceFile> sourceFiles = ImmutableMap.builder();

        for (String key : blackBoxConfig.getRequiredSourceFileKeys()) {
            Http.MultipartFormData.FilePart file = body.getFile(key);

            if (file == null) {
                throw new RuntimeException("file can't be null");
            }

            try {
                String content = FileUtils.readFileToString(file.getFile());
                sourceFiles.put(key, new SourceFile(file.getFilename(), content));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return new BlackBoxGradingSource("Cpp", sourceFiles.build());
    }

    @Override
    public GradingRequest createGradingRequest(String submissionJid, String problemJid, long problemLastUpdate, String gradingType, GradingSource gradingSource) {
        return new BlackBoxGradingRequest(submissionJid, problemJid, problemLastUpdate, gradingType, (BlackBoxGradingSource) gradingSource);
    }
}
