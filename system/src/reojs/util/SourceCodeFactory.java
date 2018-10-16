package reojs.util;

import java.nio.file.Path;
import java.util.Objects;

import reojs.JudgeSystemException;
import reojs.SourceCode;
import reojs.impl.java.JavaSourceCode;


public class SourceCodeFactory {
    public static SourceCode getSourceCode(String language, String code)
                             throws JudgeSystemException {
        return getSourceCode(language, code, null);
    }

    public static SourceCode getSourceCode(String language, Path zipFile)
                             throws JudgeSystemException {
        return getSourceCode(language, null, zipFile);
    }

    private static SourceCode getSourceCode(String language, String code, Path zipFile)
                              throws JudgeSystemException {
        switch (language) {
            case "java":
                return Objects.nonNull(code) ? new JavaSourceCode(code) :
                                               new JavaSourceCode(zipFile);
            default:
                throw new IllegalArgumentException("Not supported language: " + language);
        }
    }
}
