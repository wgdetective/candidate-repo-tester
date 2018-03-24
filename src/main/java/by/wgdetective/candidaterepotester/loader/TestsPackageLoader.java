package by.wgdetective.candidaterepotester.loader;

import by.wgdetective.candidaterepotester.model.ConsoleTestSuite;
import by.wgdetective.candidaterepotester.model.FileTestSuite;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

/**
 * @author Wladimir Litvinov
 */
public class TestsPackageLoader {

    public List<ConsoleTestSuite> loadConsole(final File testsPackage, final List<String> testNumbers) throws IOException {
        final List<ConsoleTestSuite> result = new ArrayList<>();
        if (testsPackage != null) {
            for (final File file : testsPackage.listFiles((f) -> f.getPath().endsWith(".in"))) {
                final ConsoleTestSuite testSuite = new ConsoleTestSuite();
                testSuite.setTestName(file.getName());
                try (final Scanner scanner = new Scanner(file)) {
                    final String args = scanner.nextLine();
                    if (!args.isEmpty()) {
                        testSuite.setArgs(args);
                    }
                    final List<String> lines = new ArrayList<>();
                    while (scanner.hasNextLine()) {
                        final String line = scanner.nextLine();
                        if (!line.isEmpty()) {
                            lines.add(line);
                        }
                    }
                    testSuite.setParams(lines);
                }
                final String outFilePath = file.getPath().replace("in", "out");
                final String outLines = IOUtils.toString(new FileReader(outFilePath));
                testSuite.setExpected(Arrays.asList(outLines.split("\n")));
                result.add(testSuite);
            }
        }
        result.sort(Comparator.comparing(ConsoleTestSuite::getTestName));
        return result;
    }

    public List<FileTestSuite> loadFile(final File testsPackage, final List<String> testNumbers) throws IOException {
        final List<FileTestSuite> result = new ArrayList<>();
        if (testsPackage != null) {
            for (final File file : testsPackage.listFiles((f) -> f.getPath().endsWith(".in"))) {
                final FileTestSuite testSuite = new FileTestSuite();
                testSuite.setName(file.getName());
                testSuite.setInputFile(file);
                final File outFile = new File(file.getPath().replace("in", "out"));
                if (outFile.exists()) {
                    testSuite.setOutputFile(outFile);
                    result.add(testSuite);
                } else {
                    throw new NullPointerException("No test out file found " + outFile.getPath());
                }
            }
        }
        result.sort(Comparator.comparing(FileTestSuite::getName));
        return result;
    }
}
