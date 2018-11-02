package reojs.system.util;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;


public class Sconscript {
    private static final Log log = LogFactory.getLog(Sconscript.class);
    private Map<String, String> constructionVariables = new HashMap<>();
    private String buildCommand;


    public Sconscript compiler(String name, Path path) {
        if (Files.notExists(path)) {
            throw new UncheckedIOException(new FileNotFoundException(path.toString()));
        }
        String p = FilenameUtils.separatorsToUnix(path.toString());
        constructionVariables.put(name, String.format("File('%s')", p));
        return this;
    }

    public Sconscript flags(String name, String... flags) {
        StringBuilder flagsString = new StringBuilder("[");
        for (var x : flags) {
            flagsString.append("'").append(x).append("', ");
        }
        flagsString.append("]");
        constructionVariables.put(name, flagsString.toString());
        return this;
    }

    public Sconscript builder(String name, String target, String source) {
        var t = FilenameUtils.separatorsToUnix(target);
        var s = FilenameUtils.separatorsToUnix(source);
        buildCommand = String.format("%s('%s', '%s')", name, t, s);
        return this;
    }

    public void write(Path dir) throws IOException {
        Path file = Paths.get(dir.toString(), "SConstruct");
        Files.write(file, this.toString().getBytes());
        log.trace(String.format("The sconscript has been written to '%s'.", file));
    }

    @Override
    public String toString() {
        StringBuilder script = new StringBuilder("env = Environment(");
        for (var entry : constructionVariables.entrySet()) {
            script.append(entry.getKey()).append("=").append(entry.getValue()).append(",");
        }
        script.append(")").append(System.lineSeparator());
        script.append("env.").append(buildCommand);
        return script.toString();
    }
}
