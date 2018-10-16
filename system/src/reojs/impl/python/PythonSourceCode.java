package reojs.impl.python;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import reojs.JudgeSystemException;
import reojs.Portable;
import reojs.SourceCode;


// TODO: future work
public class PythonSourceCode extends SourceCode implements Portable {
    public PythonSourceCode(String source) throws JudgeSystemException {
        super(source);
    }

    public PythonSourceCode(Path zipFile) throws JudgeSystemException {
        super(zipFile);
    }

    @Override
    public String getEntryPoint() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<List<String>> getExecutorOptions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Path getExecutor() {
        throw new UnsupportedOperationException();
    }
}
