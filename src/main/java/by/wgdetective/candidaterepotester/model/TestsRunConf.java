package by.wgdetective.candidaterepotester.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Wladimir Litvinov
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestsRunConf {
    private Boolean argsModeOn;
    private List<String> listOfIgnoringStringsInOutput;
    private List<String> testNumbers;
    private String projectDirectory;
    private String testsPackage;
    private String mainFileName;
    private Boolean ignorePom;
    private String exitCommand;
    private String startClasspathPackage;
}
