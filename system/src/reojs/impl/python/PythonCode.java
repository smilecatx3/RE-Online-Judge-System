package reojs.impl.python;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import reojs.JudgeSystemException;
import reojs.Portable;
import reojs.SourceCode;


// TODO: future work
public class PythonCode extends SourceCode implements Portable {
    public PythonCode(String source) throws JudgeSystemException {
        super(source);
    }

    public PythonCode(Path zipFile) throws JudgeSystemException {
        super(zipFile);
    }

    @Override
    public String getEntryPoint() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<List<String>> getExecOptions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Path getExecutor() {
        throw new UnsupportedOperationException();
    }
}
