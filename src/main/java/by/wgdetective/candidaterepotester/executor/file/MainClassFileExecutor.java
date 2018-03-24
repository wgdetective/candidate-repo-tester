package by.wgdetective.candidaterepotester.executor.file;

import by.wgdetective.candidaterepotester.model.FileTestSuite;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * @author Wladimir Litvinov
 */
public class MainClassFileExecutor {
    public void execute(final File mainClassFile, final List<FileTestSuite> tests)
            throws IOException, InterruptedException {
        compile(mainClassFile);

        final File projectDirectoryFile = mainClassFile.getParentFile();
        final String mainClassPath = getMainClassWithPackagePath(mainClassFile, projectDirectoryFile.getPath());
        final String command = String.format("java -cp %s %s", projectDirectoryFile.getPath(), mainClassPath);
        final FileWriter fileWriter =
                new FileWriter(new File(projectDirectoryFile.getPath() + "/TestResults.txt"), false);
        for (final FileTestSuite test : tests) {
            final File inputFile = new File(projectDirectoryFile.getPath() + "/input.txt");
            inputFile.delete();
            FileUtils.copyFile(test.getInputFile(), inputFile);

            final Process exec = Runtime.getRuntime().exec(command, null, projectDirectoryFile);

            long time = 0;
            while (exec.isAlive()) {
                Thread.sleep(100);
                time += 100;
                if (time > 1500) {
                    throw new RuntimeException("Too long");
                }
            }

            final File outputFile = new File(projectDirectoryFile.getPath() + "/output.txt");
            final String expected = IOUtils.toString(new FileReader(test.getOutputFile()));
            final String result = IOUtils.toString(new FileReader(outputFile));
            appendResult(fileWriter, test, expected, result);
            printToConsole(test, expected, result);

            inputFile.delete();
            outputFile.delete();
        }

    }

    private void appendResult(final FileWriter fileWriter,
                              final FileTestSuite test,
                              final String expected,
                              final String result) throws IOException {
        fileWriter.append("\n").append(test.getName()).append("\n");
        if (!check(expected, result)) {
            fileWriter.append("expected = ").append(expected).append("\n");
            fileWriter.append("result = ").append(result).append("\n");
        } else {
            fileWriter.append("OK" + "\n");
        }
        fileWriter.flush();
    }

    private boolean check(final String expected, final String result) {
        return expected.replaceAll("[\\s]+$", "").equals(result.replaceAll("[\\s]+$", ""));
    }

    private void printToConsole(final FileTestSuite test,
                                final String expected,
                                final String result) throws IOException {
        System.out.println("\n" + test.getName());
        if (!check(expected, result)) {
            System.out.println("expected = " + expected);
            System.out.println("result = " + result);
        } else {
            System.out.println("OK");
        }
    }

    private void compile(final File mainClassFile) throws IOException, InterruptedException {
        final File classFile = new File(mainClassFile.getPath().replace(".java", ".class"));
        if (classFile.exists()) {
            classFile.delete();
        }
        final Process compilationProcess = Runtime.getRuntime().exec("javac " + mainClassFile.getPath());
        compilationProcess.waitFor();
    }

    private String getMainClassWithPackagePath(final File mainClassFile, final String startClasspathPackage) {
        final String cp = startClasspathPackage + "/";
        final String v1 = mainClassFile.getPath().replace(".java", "");
        return v1.substring(v1.indexOf(cp) + cp.length());
    }
}
