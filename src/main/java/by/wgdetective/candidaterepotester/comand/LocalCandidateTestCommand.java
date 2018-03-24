package by.wgdetective.candidaterepotester.comand;

import by.wgdetective.candidaterepotester.executor.file.MainClassFileExecutor;
import by.wgdetective.candidaterepotester.loader.TestsPackageLoader;
import by.wgdetective.candidaterepotester.model.FileTestSuite;
import by.wgdetective.candidaterepotester.util.FindMainClassUtil;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author Wladimir Litvinov
 */
@ShellComponent
public class LocalCandidateTestCommand {


    private final MainClassFileExecutor mainClassFileExecutor = new MainClassFileExecutor();
    private final TestsPackageLoader testsPackageLoader = new TestsPackageLoader();

    private String lastRunTestSuite;

    //run-local-pack -d /Users/wgdetective/sources/candidates/round_3/gr_1 -m 1 -p /Users/wgdetective/sources/candidate-repo-tester/testSuites/mart/task_1
    //run-local -d /Users/wgdetective/sources/candidates/round_3/gr_1/Кирилин/ -m 1 -p /Users/wgdetective/sources/candidate-repo-tester/testSuites/mart/task_1
    @ShellMethod("run-local-pack")
    public String runLocalPack(@ShellOption(value = {"-t", "--testNumbers"},
            defaultValue = "") final List<String> testNumbers,
                               @ShellOption(value = {"-p", "--testsPackage"}, defaultValue = "") File testsPackage,
                               @ShellOption(value = {"-m", "--mainFileName"},
                                       defaultValue = "") final String mainFileName,
                               @ShellOption(value = {"-d", "--projectsDirectory"},
                                       defaultValue = "") final File projectsDirectory)
            throws IOException, InterruptedException {
        if ((testsPackage == null || testsPackage.getPath().isEmpty()) && lastRunTestSuite != null) {
            System.out.println("lastRunTestSuite = " + lastRunTestSuite);
            testsPackage = new File(lastRunTestSuite);
        }
        if (projectsDirectory.getPath().isEmpty()) {
            throw new NullPointerException("Empty projectDirectory");
        }

        for (final File file : projectsDirectory.listFiles()) {
            try {
                if (file.isDirectory()) {
                    System.out.println("Testing " + file.getPath());
                    runLocal(testNumbers, testsPackage, mainFileName, file);
                }
            } catch (final Exception e) {
                //todo change exception
                e.printStackTrace();
            }
        }


        return "";
    }

    @ShellMethod("run-local")
    public String runLocal(@ShellOption(value = {"-t", "--testNumbers"},
            defaultValue = "") final List<String> testNumbers,
                           @ShellOption(value = {"-p", "--testsPackage"}, defaultValue = "") File testsPackage,
                           @ShellOption(value = {"-m", "--mainFileName"}, defaultValue = "") final String mainFileName,
                           @ShellOption(value = {"-d", "--projectDirectory"},
                                   defaultValue = "") final File projectDirectory)
            throws IOException, InterruptedException {
        if ((testsPackage == null || testsPackage.getPath().isEmpty()) && lastRunTestSuite != null) {
            System.out.println("lastRunTestSuite = " + lastRunTestSuite);
            testsPackage = new File(lastRunTestSuite);
        }
        if (projectDirectory.getPath().isEmpty()) {
            throw new NullPointerException("Empty projectDirectory");
        }
        doRun(projectDirectory, mainFileName, testsPackage, testNumbers);

        return "";
    }

    private void doRun(final File projectDirectory,
                       final String mainFileName,
                       final File testsPackage,
                       final List<String> testNumbers) throws IOException, InterruptedException {
        final File mainClassFile = FindMainClassUtil.findMainClass(projectDirectory, mainFileName);
        if (mainClassFile == null) {
            throw new NullPointerException("Main class is not found");
        }
        final List<FileTestSuite> tests = testsPackageLoader.loadFile(testsPackage, testNumbers);
        mainClassFileExecutor.execute(mainClassFile, tests);
    }
}
