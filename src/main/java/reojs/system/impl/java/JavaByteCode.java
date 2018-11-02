package reojs.system.impl.java;

import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import reojs.system.code.Portable;
import reojs.system.core.JudgeSystem;


public class JavaByteCode implements Portable {
    private static final Log log = LogFactory.getLog(JavaByteCode.class);
    private JavaSourceCode source;


    JavaByteCode(JavaSourceCode x) {
        source = x;
    }

    @Override
    public Path getExecutor() {
        return Paths.get(JudgeSystem.getConfig().getString("java.jdk_path"),
                         SystemUtils.IS_OS_WINDOWS ? "java.exe" : "java");
    }

    @Override
    public String getEntryPoint() {
        if (source.getFile().isPresent()) {
            String className = source.getFile().get().getFileName().toString().replace(".java", "");
            if (source.getPackageName().isPresent()) {
                className = source.getPackageName().get() + "." + className;
            }
            return className;
        }

        try {
            assert source.getDirectory().isPresent();
            var dirname = source.getDirectory().get().toString();
            var manifest = Paths.get(dirname, "META-INF", "MANIFEST.MF");
            var lines = Files.readAllLines(manifest, StandardCharsets.UTF_8);
            for (var line : lines) {
                var matcher = Pattern.compile("Main-Class:\\s*(.+)").matcher(line.trim());
                if (matcher.matches()) {
                    return matcher.group(1);
                }
            }
            log.debug("Manifest content: " + lines); // The main class cannot be found
        } catch (IOException e) {
            log.error("Failed to read the manifest file.", e);
        }

        return null;
    }

    @Override
    public Optional<List<String>> getExecOptions() {
        var options = List.of("-Dfile.encoding=UTF-8", "-Duser.language=en",
                "-Djava.security.manager",
                "-cp", Paths.get(source.getWorkingDir().toString(), "out").toString());
        return Optional.of(options);
    }
}
