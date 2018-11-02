package reojs.system.impl.cpp;

import java.nio.file.Path;

import reojs.system.code.Compilable;
import reojs.system.code.Executable;
import reojs.system.JudgeSystemException;
import reojs.system.code.SourceCode;


// TODO: future work
public class CppSourceCode extends SourceCode implements Compilable {
    public CppSourceCode(String source) throws JudgeSystemException {
        super(source);
    }

    public CppSourceCode(Path zipFile) throws JudgeSystemException {
        super(zipFile);
    }

    @Override
    public Executable compile() throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getCompilerMessage() {
        throw new UnsupportedOperationException();
    }
}
