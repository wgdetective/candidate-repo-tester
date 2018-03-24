package by.wgdetective.candidaterepotester.util;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author Wladimir Litvinov
 */
public class FindMainClassUtil {
    public static File findMainClass(final File projectDirectory, final String mainFileName) throws IOException {
        for (final File file : projectDirectory.listFiles()) {
            final boolean checkMainFile =
                    mainFileName == null || mainFileName.isEmpty() || file.getName().contains(mainFileName);
            if (file.getName().endsWith(".java")
                && checkMainFile) {
                if (containsMainMethod(file)) {
                    return file;
                }
            }
            if (file.isDirectory()) {
                final File res = findMainClass(file, mainFileName);
                if (res != null) {
                    return res;
                }
            }
        }
        return null;
    }

    public static boolean containsMainMethod(final File file) throws IOException {
        return IOUtils.toString(new FileReader(file)).contains("public static void main");
    }
}
