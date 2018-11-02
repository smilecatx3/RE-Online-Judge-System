package reojs.system.impl.python;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import reojs.system.JudgeSystemException;
import reojs.system.code.Portable;
import reojs.system.code.SourceCode;


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
