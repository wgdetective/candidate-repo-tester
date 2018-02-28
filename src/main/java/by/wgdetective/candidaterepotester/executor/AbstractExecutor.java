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
                           final File resultsFile,
                           final String exitCommand,
                           final String startClasspathPackage)
            throws IOException, InterruptedException {
        compile(file);
        try (final FileWriter fileWriter = new FileWriter(resultsFile, false)) {
            boolean success = false;
            for (TestSuite test : tests) {
                final Process process = run(file, test, argsModeOn, startClasspathPackage);
                final boolean testResult = test(test, argsModeOn, process, listOfIgnoringStringsInOutput, exitCommand);
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
                                   final boolean argsModeOn, final String startClasspathPackage)
            throws IOException, InterruptedException;


    private boolean test(final TestSuite test,
                         final boolean argsModeOn,
                         final Process process,
                         final List<String> listOfIgnoringStringsInOutput,
                         final String exitCommand)
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

        finishProgram(process, exitCommand);

        Thread.sleep(INTERVAL);
        final List<String> preResult = new ArrayList(thread.getLines());

        int processExitValue;
        try {
            processExitValue = process.exitValue();
        } catch (final IllegalThreadStateException e) {
            if (preResult.size() > 0) {
                processExitValue = 0;
            } else {
                processExitValue = -1;
            }
        }
        final List<String> result = filterResult(preResult, listOfIgnoringStringsInOutput);
        final boolean checkResult = processExitValue == 0 && check(test, result);
        System.out.println("\n" + test.getTestName() + " " + checkResult);
        if (!checkResult) {
            System.out.println("WRONG ANSWER");
            System.out.println("process.exitValue() = " + processExitValue);
            System.out.println("Expected:");
            test.getExpected().forEach(System.out::println);
            System.out.println("But was:");
            result.forEach(System.out::println);

        }
        try {
            process.destroy();
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return checkResult;
    }

    private List<String> filterResult(final List<String> preResult, final List<String> listOfIgnoringStringsInOutput) {
        final List<String> result = new ArrayList<>();
        for (String s : preResult) {
            boolean contains = false;
            for (String s1 : listOfIgnoringStringsInOutput) {
                if (s.equals(s1)) {
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                result.add(s);
            }
        }
        return result;
    }

    private boolean check(final TestSuite test,
                          final List<String> result) {

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

    private void finishProgram(final Process process, final String exitCommand) throws IOException {
        if (exitCommand != null && !exitCommand.isEmpty()) {
            IOUtils.write(exitCommand, process.getOutputStream(), ENCODING);
            try {
                process.getOutputStream().flush();
            } catch (final IOException e) {
            }
        }
        IOUtils.write("\n", process.getOutputStream(), ENCODING);
        try {
            process.getOutputStream().flush();
        } catch (final IOException e) {
        }
    }

}
