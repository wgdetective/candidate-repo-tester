package by.wgdetective.candidaterepotester.executor;

import by.wgdetective.candidaterepotester.model.TestSuite;

import java.io.File;
import java.io.IOException;

/**
 * @author Wladimir Litvinov
 */
public class MainClassExecutor extends AbstractExecutor {

    @Override
    protected void compile(final File mainClassFile) throws IOException, InterruptedException {
        final Process compilationProcess = Runtime.getRuntime().exec("javac " + mainClassFile.getPath());
        compilationProcess.waitFor();
    }

    @Override
    protected Process run(final File mainClassFile,
                          final TestSuite test,
                          final boolean argsModeOn)
            throws IOException, InterruptedException {
        final String projectDirectory = getProjectDirectory(mainClassFile);
        final String mainClassPath = getMainClassWithPackagePath(mainClassFile);
        final StringBuilder command =
                new StringBuilder().append("java -cp ").append(projectDirectory).append(" ").append(mainClassPath);
        if (test.getArgs() != null && argsModeOn) {
            command.append(" ").append(test.getArgs());
        }
        return Runtime.getRuntime().exec(command.toString());
    }

    protected String getProjectDirectory(final File mainClassFile) {
        return mainClassFile.getPath().substring(0, mainClassFile.getPath().indexOf("src/") + 4);
    }

    protected String getMainClassWithPackagePath(final File mainClassFile) {
        final String v1 = mainClassFile.getPath().replace(".java", "");
        return v1.substring(v1.indexOf("src/") + 4);
    }

}
