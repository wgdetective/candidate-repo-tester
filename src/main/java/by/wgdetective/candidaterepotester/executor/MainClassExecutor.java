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
        final File classFile = new File(mainClassFile.getPath().replace(".java", ".class"));
        if (classFile.exists()) {
            classFile.delete();
        }
        final Process compilationProcess = Runtime.getRuntime().exec("javac " + mainClassFile.getPath());
        compilationProcess.waitFor();
    }

    @Override
    protected Process run(final File mainClassFile,
                          final TestSuite test,
                          final boolean argsModeOn,
                          final String startClasspathPackage)
            throws IOException, InterruptedException {
        final String projectDirectory = getProjectDirectory(mainClassFile, startClasspathPackage);
        final String mainClassPath = getMainClassWithPackagePath(mainClassFile, startClasspathPackage);
        final StringBuilder command =
                new StringBuilder().append("java -cp ").append(projectDirectory).append(" ").append(mainClassPath);
        if (test.getArgs() != null && argsModeOn) {
            command.append(" ").append(test.getArgs());
        }
        return Runtime.getRuntime().exec(command.toString());
    }

    protected String getProjectDirectory(final File mainClassFile, final String startClasspathPackage) {
        final String cp = startClasspathPackage + "/";
        return mainClassFile.getPath().substring(0, mainClassFile.getPath().indexOf(cp) + cp.length());
    }

    protected String getMainClassWithPackagePath(final File mainClassFile, final String startClasspathPackage) {
        final String cp = startClasspathPackage + "/";
        final String v1 = mainClassFile.getPath().replace(".java", "");
        return v1.substring(v1.indexOf(cp) + cp.length());
    }

}
