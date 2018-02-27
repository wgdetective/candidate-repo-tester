package by.wgdetective.candidaterepotester.loader;

import by.wgdetective.candidaterepotester.model.TestSuite;
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

    public List<TestSuite> load(final File testsPackage, final List<String> testNumbers) throws IOException {
        final List<TestSuite> result = new ArrayList<>();
        if (testsPackage != null) {
            for (final File file : testsPackage.listFiles((f) -> f.getPath().endsWith(".in"))) {
                final TestSuite testSuite = new TestSuite();
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
        result.sort(Comparator.comparing(TestSuite::getTestName));
        return result;
    }
}
