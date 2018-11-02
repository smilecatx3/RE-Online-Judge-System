package reojs.system.util;


public interface Builder {
    /**
     * @return the exit status of the build tool. By convention, a nonzero status code
     *         indicates abnormal termination.
     * @throws Exception if any error occurred during the build process or is timed out.
     */
    int build() throws Exception;

    /**
     * @return the output message of the build tool.
     * @throws IllegalStateException if the method is invoked prior to the build process is executed.
     */
    String getBuildMessage();
}
