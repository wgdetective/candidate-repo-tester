package by.wgdetective.candidaterepotester.executor;

import by.wgdetective.candidaterepotester.model.TestSuite;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Wladimir Litvinov
 */
public abstract class AbstractExecutor {
    public static final String ENCODING = "UTF-8";
    public static final long INTERVAL = 500l;

    public boolean execute(final File file,
                           final List<TestSuite> tests,
                           final boolean argsModeOn,
                           final List<String> listOfIgnoringStringsInOutput,
                           final File resultsFile)
            throws IOException, InterruptedException {
        compile(file);
        try (final FileWriter fileWriter = new FileWriter(resultsFile, false)) {
            boolean success = false;
            for (TestSuite test : tests) {
                final Process process = run(file, test, argsModeOn);
                final boolean testResult = test(test, argsModeOn, process, listOfIgnoringStringsInOutput);
                if (testResult && !success) {
                    success = true;
                }
                fileWriter.append(test.getTestName() + " " + testResult).append("\n");
            }
            return success;
        }
    }

    protected abstract void compile(final File mainClassFile) throws IOException, InterruptedException;

    protected abstract Process run(final File mainClassFile,
                                   final TestSuite test,
                                   final boolean argsModeOn)
            throws IOException, InterruptedException;


    private boolean test(final TestSuite test,
                         final boolean argsModeOn,
                         final Process process,
                         final List<String> listOfIgnoringStringsInOutput)
            throws IOException, InterruptedException {
        final RunProgramThread thread = new RunProgramThread(process.getInputStream());
        thread.start();

        if (test.getArgs() != null && !argsModeOn) {
            IOUtils.write(test.getArgs() + "\n", process.getOutputStream(), ENCODING);
            process.getOutputStream().flush();

        }
        for (final String testLine : test.getParams()) {
            IOUtils.write(testLine + "\n", process.getOutputStream(), ENCODING);
            process.getOutputStream().flush();
        }

        Thread.sleep(INTERVAL);
        thread.cleanArray();
        Thread.sleep(INTERVAL);

        finishProgram(process);

        Thread.sleep(INTERVAL);
        final List<String> result = new ArrayList(thread.getLines());

        final boolean checkResult = process.exitValue() == 0 && check(test, result, listOfIgnoringStringsInOutput);
        System.out.println("\n" + test.getTestName() + " " + checkResult);
        if (!checkResult) {
            System.out.println("WRONG ANSWER");
            System.out.println("process.exitValue() = " + process.exitValue());
            System.out.println("Expected:");
            test.getExpected().forEach(System.out::println);
            System.out.println("But was:");
            result.forEach(System.out::println);

        }
        process.destroy();

        return checkResult;
    }

    private boolean check(final TestSuite test,
                          final List<String> preResult,
                          final List<String> listOfIgnoringStringsInOutput) {
        final List<String> result = new ArrayList<>();
        for (String s : preResult) {
            boolean contains = false;
            for (String s1 : listOfIgnoringStringsInOutput) {
                if (s.contains(s1)) {
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                result.add(s);
            }
        }

        final List<String> expected = test.getExpected();
        if (expected.size() == result.size()) {
            for (String s : expected) {
                if (!result.contains(s)) {
                    return false;
                }
            }
            return true;
        } else {
            if (expected.size() == 1 && result.size() == 0 && expected.get(0).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private void finishProgram(final Process process) throws IOException {
        IOUtils.write("\n", process.getOutputStream(), ENCODING);
        try {
            process.getOutputStream().flush();
        } catch (final IOException e) {
        }
        IOUtils.write("\n", process.getOutputStream(), ENCODING);
        try {
            process.getOutputStream().flush();
        } catch (final IOException e) {
        }
    }

}
