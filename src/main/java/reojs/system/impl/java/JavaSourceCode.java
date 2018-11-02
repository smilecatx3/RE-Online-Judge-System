package reojs.system.impl.java;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.Problem;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.TypeDeclaration;

import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import reojs.system.code.Compilable;
import reojs.system.code.Executable;
import reojs.system.JudgeFailure;
import reojs.system.JudgeSystemException;
import reojs.system.code.SourceCode;
import reojs.system.code.SourceFileChecker;
import reojs.system.core.JudgeSystem;
import reojs.system.util.Builder;
import reojs.system.util.SConsBuilder;
import reojs.system.util.Sconscript;


public class JavaSourceCode extends SourceCode implements Compilable {
    private static final Log log = LogFactory.getLog(JavaSourceCode.class);
    private Builder builder = createBuilder();
    private String packageName;


    public JavaSourceCode(String text) throws JudgeSystemException {
        super(text);
    }

    @Override
    protected Path createFile(String text) throws JudgeSystemException {
        CompilationUnit cu = parse(text);

        Path srcDir = Paths.get(workingDir.toString(), "src");
        if (cu.getPackageDeclaration().isPresent()) {
            packageName = cu.getPackageDeclaration().get().getName().asString();
            String[] dirs = packageName.split("\\.");
            srcDir = Paths.get(srcDir.toString(), dirs);
        }
        Path sourceFile = Paths.get(srcDir.toString(), findPublicClassName(cu)+".java");

        try {
            Files.createDirectories(srcDir);
            Files.createFile(sourceFile);
            return Files.write(sourceFile, text.getBytes());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new JudgeSystemException("Failed to create a file from source text.");
        }
    }

    private CompilationUnit parse(String text) {
        try {
            return JavaParser.parse(text);
        } catch (ParseProblemException e) {
            String message = e.getProblems().stream()
                                            .map(Problem::getVerboseMessage)
                                            .collect(Collectors.joining("\n\n"));
            throw new JudgeFailure(JudgeFailure.COMPILE_ERROR, message);
        }
    }

    private String findPublicClassName(CompilationUnit cu) {
        for (var node : cu.getTypes()) {
            var type = (TypeDeclaration) node;
            if (type.getModifiers().contains(Modifier.PUBLIC)) {
                return type.getName().asString();
            }
        }
        throw new JudgeFailure(JudgeFailure.INVALID_SOURCE_FILE,
                               "Failed to determine the public class name.");
    }

    public JavaSourceCode(Path zipFile) throws JudgeSystemException {
        super(zipFile, new SourceFileChecker() {
            @Override
            public Boolean apply(Path path) {
                Path srcDir = Paths.get(path.toString(), "src");
                Path manifest = Paths.get(path.toString(), "META-INF", "MANIFEST.MF");
                return Files.exists(srcDir) && Files.exists(manifest);
            }

            @Override
            public String getHintMessage() {
                return "The submitted file should be \"" + "USER_ID.zip" +
                        "\" with the following content structure: \n\n" +
                        "USER_ID.zip \n" +
                        "|- src/ \n" +
                        "|- META-INF/ \n" +
                        "|  |- MANIFEST.MF \n";
            }
        });
    }

    public JavaSourceCode(Path zipFile, SourceFileChecker checker)
                         throws JudgeSystemException {
        super(zipFile, checker);
    }

    @Override
    public Executable compile() throws Exception {
        int exitStatus = builder.build();
        String message = builder.getBuildMessage();
        log.trace(String.format("exit status: %d; build message: %s", exitStatus, message));
        if (exitStatus != 0) {
            throw new JudgeFailure(JudgeFailure.COMPILE_ERROR, getCompilerMessage());
        }
        return new JavaByteCode(this);
    }

    @Override
    public String getCompilerMessage() {
        return Arrays.stream(builder.getBuildMessage().replace("\r", "").split("\n"))
                     .filter(line -> !line.startsWith("scons"))
                     .filter(line -> !line.startsWith("javac"))
                     .collect(Collectors.joining("\n"));
    }

    Optional<String> getPackageName() {
        return Optional.ofNullable(packageName);
    }

    private Builder createBuilder() {
        var javac = Paths.get(JudgeSystem.getConfig().getString("java.jdk_path"),
                              SystemUtils.IS_OS_WINDOWS ? "javac.exe" : "javac");
        var sconscript = new Sconscript().compiler("JAVAC", javac)
                                         .flags("JAVACFLAGS", "-encoding", "utf8")
                                         .builder("Java", "out", "src");
        return new SConsBuilder(sconscript, getWorkingDir());
    }
}
