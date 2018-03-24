package by.wgdetective.candidaterepotester.comand;

import by.wgdetective.candidaterepotester.executor.console.MainClassConsoleExecutor;
import by.wgdetective.candidaterepotester.executor.console.MavenClassConsoleExecutor;
import by.wgdetective.candidaterepotester.loader.GitHubLoader;
import by.wgdetective.candidaterepotester.loader.TestsPackageLoader;
import by.wgdetective.candidaterepotester.model.ConsoleTestSuite;
import by.wgdetective.candidaterepotester.model.TestsRunConf;
import by.wgdetective.candidaterepotester.util.FindMainClassUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.IOUtils;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Wladimir Litvinov
 */
@ShellComponent
public class CandidateRepoTestCommand {
    public static final String SOURCES_CANDIDATES = "/Users/wgdetective/sources/candidates/";
    public static final String TESTS_RUN_CONF_FILE_NAME = "/testsRun.conf";
    public static final String TESTS_RUN_RESULT_FILE_NAME = "/testsRunResult.txt";

    private final GitHubLoader gitHubLoader = new GitHubLoader();
    private final MainClassConsoleExecutor mainClassExecutor = new MainClassConsoleExecutor();
    private final MavenClassConsoleExecutor mavenClassExecutor = new MavenClassConsoleExecutor();
    private final TestsPackageLoader testsPackageLoader = new TestsPackageLoader();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private String lastLoaded;
    private String lastRunTestSuite;

    @ShellMethod("loadConsole")
    public String load(final String gitHubLink)
            throws IOException {
        final String dirName = gitHubLink.replace("https://github.com/", "")
                .replace(".git", "")
                .replace("/", "_");
        final String fullDirName = SOURCES_CANDIDATES + dirName;
        gitHubLoader.load(gitHubLink, fullDirName);

        lastLoaded = fullDirName;

        return fullDirName;
    }

    //run --project-directory /Users/wgdetective/sources/candidates/tajbe99_Task1 --tests-package /Users/wgdetective/sources/candidate-repo-tester/testSuites/task_1
    //run --project-directory /Users/wgdetective/sources/candidates/alyaromin_TextFilter --tests-package /Users/wgdetective/sources/candidate-repo-tester/testSuites/task_1 -a -i Вывод:
    //run --project-directory /Users/wgdetective/sources/candidates/IlyaBeetle_temp-repository/task-1-words -p /Users/wgdetective/sources/candidate-repo-tester/testSuites/task_1 -a
    //run --project-directory /Users/wgdetective/sources/candidates/DmitrySamsonov_gpSolutionsTask2_ -p /Users/wgdetective/sources/candidate-repo-tester/testSuites/task_1 -m Text -a --ignorePom  -c java -i Output\ :#space,Enter\ strings\ :#space,#space,#empty
    //
    @ShellMethod("run")
    public String run(@ShellOption(value = {"-a", "--argsModeOn"}, defaultValue = "false") final Boolean argsModeOn,
                      @ShellOption(value = {"-i",
                                            "--listOfIgnoringStringsInOutput"},
                              defaultValue = "") final List<String> _listOfIgnoringStringsInOutput,
                      @ShellOption(value = {"-t", "--testNumbers"}, defaultValue = "") final List<String> testNumbers,
                      @ShellOption(value = {"-p", "--testsPackage"}, defaultValue = "") File testsPackage,
                      @ShellOption(value = {"-m", "--mainFileName"}, defaultValue = "") final String mainFileName,
                      @ShellOption(value = {"-ip", "--ignorePom"}, defaultValue = "false") final Boolean ignorePom,
                      @ShellOption(value = {"-e", "--exitCommand"}, defaultValue = "") final String exitCommand,
                      @ShellOption(value = {"-c", "--startClasspathPackage"},
                              defaultValue = "src") final String startClasspathPackage,
                      @ShellOption(value = {"-d", "--projectDirectory"}, defaultValue = "") File projectDirectory)
            throws IOException, InterruptedException {
        if ((projectDirectory == null || projectDirectory.getPath().isEmpty()) && lastLoaded != null) {
            System.out.println("lastLoaded = " + lastLoaded);
            projectDirectory = new File(lastLoaded);
        }
        if ((testsPackage == null || testsPackage.getPath().isEmpty()) && lastRunTestSuite != null) {
            System.out.println("lastRunTestSuite = " + lastRunTestSuite);
            testsPackage = new File(lastRunTestSuite);
        }
        if (projectDirectory.getPath().isEmpty()) {
            throw new NullPointerException("Empty projectDirectory");
        }
        //TODO refactor and check testsPackage
        final List<String> listOfIgnoringStringsInOutput = new ArrayList<>();
        for (String s : _listOfIgnoringStringsInOutput) {
            listOfIgnoringStringsInOutput.add(s.replace("#space", " ").replace("#empty", ""));
        }
        if (testsPackage.getPath().isEmpty()) {
            final File confFile = new File(projectDirectory.getPath() + TESTS_RUN_CONF_FILE_NAME);
            if (!confFile.exists()) {
                throw new FileNotFoundException("File not found:" + confFile.getPath());
            }
            final TestsRunConf testsRunConf =
                    gson.fromJson(IOUtils.toString(new FileReader(confFile)), TestsRunConf.class);
            //TODO refactor
            doRun(testsRunConf.getArgsModeOn(), testsRunConf.getListOfIgnoringStringsInOutput(),
                  testsRunConf.getTestNumbers(), new File(testsRunConf.getProjectDirectory()),
                  new File(testsRunConf.getTestsPackage()), testsRunConf.getMainFileName(),
                  testsRunConf.getIgnorePom(), testsRunConf.getExitCommand(),
                  testsRunConf.getStartClasspathPackage());
        } else {
            doRun(argsModeOn, listOfIgnoringStringsInOutput, testNumbers, projectDirectory, testsPackage,
                  mainFileName,
                  ignorePom, exitCommand, startClasspathPackage);
        }
        lastRunTestSuite = testsPackage.getPath();
        return "";
    }

    private void doRun(final Boolean argsModeOn,
                       final List<String> listOfIgnoringStringsInOutput,
                       final List<String> testNumbers,
                       final File projectDirectory,
                       final File testsPackage,
                       final String mainFileName,
                       final boolean ignorePom,
                       final String exitCommand,
                       final String startClasspathPackage)
            throws IOException, InterruptedException {
        final File mainClassFile = FindMainClassUtil.findMainClass(projectDirectory, mainFileName);
        if (mainClassFile == null) {
            throw new NullPointerException("Main class is not found");
        }
        final File pomFile = findPomFile(projectDirectory);

        final List<ConsoleTestSuite> tests = testsPackageLoader.loadConsole(testsPackage, testNumbers);
        final File resultsFile = new File(projectDirectory + TESTS_RUN_RESULT_FILE_NAME);

        final boolean success;
        if (pomFile != null && !ignorePom) {
            success = mavenClassExecutor
                    .execute(pomFile, tests, Boolean.valueOf(argsModeOn), listOfIgnoringStringsInOutput, resultsFile,
                             exitCommand, startClasspathPackage);
        } else {
            success = mainClassExecutor
                    .execute(mainClassFile, tests, Boolean.valueOf(argsModeOn), listOfIgnoringStringsInOutput,
                             resultsFile, exitCommand, startClasspathPackage);

        }

        if (success) {
            //TODO refactor
            saveConf(argsModeOn, listOfIgnoringStringsInOutput, testNumbers, projectDirectory, testsPackage,
                     mainFileName, ignorePom, exitCommand, startClasspathPackage);
        }
    }

    private File findPomFile(final File projectDirectory) {
        for (final File file : projectDirectory.listFiles()) {
            if (file.getName().equals("pom.xml")) {
                return file;
            } else if (file.isDirectory()) {
                if (!file.getName().startsWith(".")) {
                    final File result = findPomFile(file);
                    if (result != null) {
                        return result;
                    }
                }
            }
        }
        return null;
    }

    private void saveConf(final Boolean argsModeOn,
                          final List<String> listOfIgnoringStringsInOutput,
                          final List<String> testNumbers,
                          final File projectDirectory,
                          final File testsPackage,
                          final String mainFileName,
                          final boolean ignorePom,
                          final String exitCommand,
                          final String startClasspathPackage)
            throws IOException {
        final TestsRunConf testsRunConf =
                new TestsRunConf(argsModeOn, listOfIgnoringStringsInOutput, testNumbers, projectDirectory.getPath(),
                                 testsPackage.getPath(), mainFileName, ignorePom, exitCommand, startClasspathPackage);
        try (final FileWriter fileWriter = new FileWriter(projectDirectory.getPath() + TESTS_RUN_CONF_FILE_NAME,
                                                          false)) {
            fileWriter.append(gson.toJson(testsRunConf));
            fileWriter.flush();
        }
    }
}
