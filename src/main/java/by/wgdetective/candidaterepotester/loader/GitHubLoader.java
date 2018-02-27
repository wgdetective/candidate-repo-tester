package by.wgdetective.candidaterepotester.loader;

import java.io.IOException;

/**
 * @author Wladimir Litvinov
 */
public class GitHubLoader {

    public void load(final String gitHubLink, final String directoryPath) throws IOException {
        Runtime.getRuntime()
                .exec(String.format("git clone %s %s", gitHubLink, directoryPath));

    }
}
