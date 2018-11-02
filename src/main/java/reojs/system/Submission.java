package reojs.system;

import org.json.JSONObject;

import java.nio.file.Paths;
import java.util.function.Function;
import java.util.regex.Pattern;

import reojs.system.code.SourceCode;
import reojs.system.util.SourceCodeFactory;


public class Submission {
    private String problemId;
    private String userId;
    private SourceCode sourceCode;


    /**
     * The json data should contain the following records:
     * "problem_id": string, "student_id": string,
     * "source": json object {"language": string, "text": string, "file_path": string}.
     * Either "source.text" or "source.file_path" should be provided but not both.
     */
    public Submission(JSONObject data) throws JudgeSystemException {
        validate(data);

        problemId = data.getString("problem_id");
        userId = data.getString("user_id");

        var source = data.getJSONObject("source");
        if (source.has("text")) {
            sourceCode = SourceCodeFactory.getSourceCode(
                    source.getString("language"), source.getString("text"));
        } else {
            sourceCode = SourceCodeFactory.getSourceCode(
                    source.getString("language"), Paths.get(source.getString("file")));
        }
    }

    private void validate(JSONObject data) {
        var source = data.getJSONObject("source");
        boolean isValid = data.has("problem_id") && data.has("user_id") &&
                          data.has("source") && (source.has("text") ^ source.has("file"));
        if (!isValid) {
            throw new IllegalArgumentException("Illegal submission data");
        }

        Pattern pattern = Pattern.compile("\\p{Alnum}+");
        Function<String, Boolean> matcher = key -> pattern.matcher(data.getString(key)).matches();
        if (!matcher.apply("problem_id") || !matcher.apply("user_id")) {
            throw new IllegalArgumentException("Format error");
        }
    }

    public String getProblemId() {
        return problemId;
    }

    public String getUserId() {
        return userId;
    }

    public SourceCode getSourceCode() {
        return sourceCode;
    }
}
