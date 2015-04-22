package org.iatoki.judgels.gabriel.commons;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.iatoki.judgels.gabriel.commons.views.html.blackBoxViewSubmissionView;
import org.iatoki.judgels.gabriel.commons.views.html.blackBoxViewStatementView;
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
import org.iatoki.judgels.sandalphon.commons.views.html.statementLanguageSelectionLayout;
import play.mvc.Http;
import play.twirl.api.Html;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class BlackBoxSubmissionAdapter implements SubmissionAdapter {

    private static final long MAX_SUBMISSION_FILE_LENGTH = 300 * 1024; // 300 KB

    @Override
    public Html renderViewStatement(String postSubmitUri, String name, String statement, GradingConfig config, String engine, Set<String> allowedGradingLanguageNames, String reasonNotAllowedToSubmit) {
        BlackBoxGradingConfig blackBoxConfig = (BlackBoxGradingConfig) config;
        return blackBoxViewStatementView.render(postSubmitUri, name, statement, blackBoxConfig, engine, allowedGradingLanguageNames, reasonNotAllowedToSubmit);
    }

    @Override
    public Html renderStatementLanguageSelection(String switchLanguageUri, Set<String> allowedStatementLanguages, String currentStatementLanguage, Html statement) {
        return statementLanguageSelectionLayout.render(switchLanguageUri, allowedStatementLanguages, currentStatementLanguage, statement);
    }

    @Override
    public Html renderViewSubmission(Submission submission, GradingSource source, String authorName, String problemAlias, String problemName, String gradingLanguageName, String contestName) {
        BlackBoxGradingResultDetails details = new Gson().fromJson(submission.getLatestDetails(), BlackBoxGradingResultDetails.class);
        return blackBoxViewSubmissionView.render(submission, details, ((BlackBoxGradingSource) source).getSourceFiles(), authorName, problemAlias, problemName, gradingLanguageName, contestName);
    }

    @Override
    public GradingSource createGradingSourceFromNewSubmission(Http.MultipartFormData body) throws SubmissionException {
        String language = body.asFormUrlEncoded().get("language")[0];
        String sourceFileFieldKeysUnparsed = body.asFormUrlEncoded().get("sourceFileFieldKeys")[0];

        if (language == null || sourceFileFieldKeysUnparsed == null) {
            return new BlackBoxGradingSource(ImmutableMap.of());
        }

        List<String> sourceFileFieldKeys = Arrays.asList(sourceFileFieldKeysUnparsed.split(","));

        List<Http.MultipartFormData.FilePart> fileParts = body.getFiles();
        Map<String, String> formFilenames = fileParts.stream().collect(Collectors.toMap(e -> e.getKey(), e -> e.getFilename()));
        Map<String, File> files = fileParts.stream().collect(Collectors.toMap(e -> e.getKey(), e -> e.getFile()));

        ImmutableMap.Builder<String, String> formFileContents = ImmutableMap.builder();

        for (Map.Entry<String, File> entry : files.entrySet()) {
            String key = entry.getKey();
            File file = entry.getValue();

            if (file.length() > MAX_SUBMISSION_FILE_LENGTH) {
                throw new SubmissionException("Source file must not exceed 300KB");
            }

            String content;
            try {
                content = FileUtils.readFileToString(file);
            } catch (IOException e) {
                throw new SubmissionException(e.getMessage());
            }

            formFileContents.put(key, content);
        }

        return createBlackBoxGradingSourceFromNewSubmission(language, sourceFileFieldKeys, formFilenames, formFileContents.build());
    }

    public GradingSource createBlackBoxGradingSourceFromNewSubmission(String language, List<String> sourceFileFieldKeys, Map<String, String> formFilenames, Map<String, String> formFileContents) throws SubmissionException {
        GradingLanguage gradingLanguage = GradingLanguageRegistry.getInstance().getLanguage(language);

        ImmutableMap.Builder<String, SourceFile> sourceFiles = ImmutableMap.builder();

        for (String fieldKey : sourceFileFieldKeys) {
            if (!formFilenames.containsKey(fieldKey)) {
                throw new SubmissionException("You must submit a source file for '" + fieldKey + "'");
            }

            String filename = formFilenames.get(fieldKey);
            String fileContent = formFileContents.get(fieldKey);

            String verification = gradingLanguage.verifyFile(filename, fileContent);

            if (verification != null) {
                throw new SubmissionException(verification);
            }

            sourceFiles.put(fieldKey, new SourceFile(filename, fileContent));
        }

        return new BlackBoxGradingSource(sourceFiles.build());
    }

    @Override
    public GradingSource createGradingSourceFromPastSubmission(File submissionBaseDir, String submissionJid) {
        File submissionDir = new File(submissionBaseDir, submissionJid);

        ImmutableMap.Builder<String, SourceFile> sourceFiles = ImmutableMap.builder();

        for (File fieldKey : submissionDir.listFiles()) {

            File[] sourceFilesInDir = fieldKey.listFiles();
            if (sourceFilesInDir == null) {
                continue;
            }

            File sourceFile = sourceFilesInDir[0];

            try {
                String name = sourceFile.getName();
                String content = FileUtils.readFileToString(sourceFile);
                sourceFiles.put(fieldKey.getName(), new SourceFile(name, content));
            } catch (NullPointerException | IOException e) {
                throw new RuntimeException(e);
            }
        }

        return new BlackBoxGradingSource(sourceFiles.build());
    }

    @Override
    public GradingRequest createGradingRequest(String gradingJid, String problemJid, String gradingEngine, String gradingLanguage, GradingSource gradingSource) {
        return new BlackBoxGradingRequest(gradingJid, problemJid, gradingEngine, gradingLanguage, (BlackBoxGradingSource) gradingSource);
    }

    @Override
    public void storeSubmissionFiles(File submissionBaseDir, String submissionJid, GradingSource source) {
        File submissionDir = new File(submissionBaseDir, submissionJid);

        try {
            FileUtils.forceMkdir(submissionDir);

            for (Map.Entry<String, SourceFile> entry : ((BlackBoxGradingSource) source).getSourceFiles().entrySet()) {
                File sourceFileDir = new File(submissionDir, entry.getKey());
                FileUtils.forceMkdir(sourceFileDir);
                File sourceFile = new File(sourceFileDir, entry.getValue().getName());
                FileUtils.writeStringToFile(sourceFile, entry.getValue().getContent());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
