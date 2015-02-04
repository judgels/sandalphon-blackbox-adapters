package org.iatoki.judgels.commons.adapters;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.iatoki.judgels.commons.Submission;
import org.iatoki.judgels.commons.SubmissionAdapter;
import org.iatoki.judgels.commons.SubmissionException;
import org.iatoki.judgels.commons.views.html.adapters.blackBoxViewSubmissionView;
import org.iatoki.judgels.commons.views.html.adapters.blackBoxViewStatementView;
import org.iatoki.judgels.gabriel.GradingConfig;
import org.iatoki.judgels.gabriel.GradingRequest;
import org.iatoki.judgels.gabriel.GradingSource;
import org.iatoki.judgels.gabriel.GradingLanguage;
import org.iatoki.judgels.gabriel.GradingLanguageRegistry;
import org.iatoki.judgels.gabriel.blackbox.BlackBoxGradingConfig;
import org.iatoki.judgels.gabriel.blackbox.BlackBoxGradingRequest;
import org.iatoki.judgels.gabriel.blackbox.BlackBoxGradingResultDetails;
import org.iatoki.judgels.gabriel.blackbox.BlackBoxGradingSource;
import org.iatoki.judgels.gabriel.blackbox.SourceFile;
import play.api.mvc.Call;
import play.mvc.Http;
import play.twirl.api.Html;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public final class BlackBoxSubmissionAdapter implements SubmissionAdapter {

    @Override
    public Html renderViewStatement(Call targetCall, String name, String statement, GradingConfig config) {
        BlackBoxGradingConfig blackBoxConfig = (BlackBoxGradingConfig) config;
        return blackBoxViewStatementView.render(targetCall, name, statement, blackBoxConfig);
    }

    @Override
    public Html renderViewSubmission(Submission submission, GradingSource source) {
        BlackBoxGradingResultDetails details = new Gson().fromJson(submission.getDetails(), BlackBoxGradingResultDetails.class);
        return blackBoxViewSubmissionView.render(submission, details, ((BlackBoxGradingSource) source).getSourceFiles());
    }

    @Override
    public GradingSource createGradingSourceFromNewSubmission(GradingConfig config, Http.MultipartFormData body) {
        BlackBoxGradingConfig blackBoxConfig = (BlackBoxGradingConfig) config;

        String gradingLanguage = body.asFormUrlEncoded().get("language")[0];
        GradingLanguage language = GradingLanguageRegistry.getInstance().getLanguage(gradingLanguage);

        ImmutableMap.Builder<String, SourceFile> sourceFiles = ImmutableMap.builder();

        for (Map.Entry<String, String> entry : blackBoxConfig.getSourceFileFields().entrySet()) {
            Http.MultipartFormData.FilePart file = body.getFile(entry.getKey());

            if (file == null) {
                throw new SubmissionException("You must submit a file for '" + entry.getValue() + "'.");
            }

            try {
                String filename = file.getFilename();
                String content = FileUtils.readFileToString(file.getFile());

                String verification = language.verifyFile(filename, content);

                if (verification != null) {
                    throw new SubmissionException(verification);
                }

                sourceFiles.put(entry.getKey(), new SourceFile(file.getFilename(), content));
            } catch (IOException e) {
                throw new SubmissionException(e.getMessage());
            }
        }

        return new BlackBoxGradingSource(sourceFiles.build());
    }

    @Override
    public GradingSource createGradingSourceFromPastSubmission(GradingConfig config, File submissionBaseDir, String submissionJid) {
        File submissionDir = new File(submissionBaseDir, submissionJid);
        BlackBoxGradingConfig blackBoxConfig = (BlackBoxGradingConfig) config;

        ImmutableMap.Builder<String, SourceFile> sourceFiles = ImmutableMap.builder();

        for (Map.Entry<String, String> entry : blackBoxConfig.getSourceFileFields().entrySet()) {
            File sourceFileDir = new File(submissionDir, entry.getKey());
            try {
                File sourceFile = sourceFileDir.listFiles()[0];
                String name = sourceFile.getName();
                String content = FileUtils.readFileToString(sourceFile);
                sourceFiles.put(entry.getKey(), new SourceFile(name, content));
            } catch (NullPointerException | IOException e) {
                throw new RuntimeException(e);
            }
        }

        return new BlackBoxGradingSource(sourceFiles.build());
    }

    @Override
    public GradingRequest createGradingRequest(String submissionJid, String problemJid, long problemLastUpdate, String gradingEngine, String gradingLanguage, GradingSource gradingSource) {
        return new BlackBoxGradingRequest(submissionJid, problemJid, problemLastUpdate, gradingEngine, gradingLanguage, (BlackBoxGradingSource) gradingSource);
    }

    @Override
    public void storeSubmissionFiles(File submissionBaseDir, String submissionJid, GradingSource source) {
        File submissionDir = new File(submissionBaseDir, submissionJid);

        for (Map.Entry<String, SourceFile> entry : ((BlackBoxGradingSource) source).getSourceFiles().entrySet()) {
            File sourceFileDir = new File(submissionDir, entry.getKey());
            try {
                FileUtils.forceMkdir(sourceFileDir);
                File sourceFile = new File(sourceFileDir, entry.getValue().getName());
                FileUtils.writeStringToFile(sourceFile, entry.getValue().getContent());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
