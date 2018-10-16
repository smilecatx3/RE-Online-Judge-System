package reojs;


public interface Compilable {
    Executable compile() throws Exception;

    /**
     * @throws IllegalStateException if no build process is performed before.
     */
    String getCompilerMessage();
}
