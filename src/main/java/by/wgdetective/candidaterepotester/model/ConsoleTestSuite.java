package by.wgdetective.candidaterepotester.model;

import lombok.Data;

import java.util.List;

/**
 * @author Wladimir Litvinov
 */
@Data
public class ConsoleTestSuite {
    private String testName;

    private String args;
    private List<String> params;
    private List<String> expected;
}
