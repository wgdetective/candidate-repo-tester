package by.wgdetective.candidaterepotester.executor.console;

import by.wgdetective.candidaterepotester.model.ConsoleTestSuite;

import java.io.File;
import java.io.IOException;

/**
 * @author Wladimir Litvinov
 */
public class MavenClassConsoleExecutor extends AbstractConsoleExecutor {

    @Override
    protected void compile(final File pomFile) throws IOException, InterruptedException {
        final String command = "mvn -f " + pomFile + " clean install";
        final Process compilationProcess = Runtime.getRuntime().exec(
                command);
        compilationProcess.waitFor();
    }

    @Override
    protected Process run(final File pomFile,
                          final ConsoleTestSuite test,
                          final boolean argsModeOn,
                          final String startClasspathPackage)
            throws IOException, InterruptedException {
        final String jarPath = findJar(pomFile);
        final StringBuilder command =
                new StringBuilder().append("java -jar ").append(jarPath);
        if (test.getArgs() != null && argsModeOn) {
            command.append(" ").append(test.getArgs());
        }
        return Runtime.getRuntime().exec(command.toString());
    }

    private String findJar(final File pomFile) {
        return pomFile.getParentFile().listFiles(f -> f.getName().equals("target"))[0]
                .listFiles(f -> f.getName().endsWith(".jar"))[0].getPath();
    }
}
