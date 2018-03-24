package by.wgdetective.candidaterepotester.model;

import lombok.Data;

import java.io.File;
import java.util.List;

/**
 * @author Wladimir Litvinov
 */
@Data
public class FileTestSuite {
    private String name;

    private File inputFile;
    private File outputFile;
}
