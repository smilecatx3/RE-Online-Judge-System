package reojs.system;


@FunctionalInterface
public interface ProgressListener {
    String STATUS_COMPILE = "Compiling";
    String STATUS_EXECUTE = "Executing";

    void onProgress(double progress, String status);
}
