package by.wgdetective.candidaterepotester.executor;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Wladimir Litvinov
 */
public class RunProgramThread extends Thread {
    private final InputStream inputStream;
    private final List<String> lines;

    public RunProgramThread(final InputStream inputStream) {
        super();
        this.inputStream = inputStream;
        this.lines = new ArrayList<>();
    }

    @Override
    public void run() {
        try {
            final LineIterator lineIterator = IOUtils.lineIterator(inputStream, AbstractExecutor.ENCODING);
            while (lineIterator.hasNext()) {
                addLineToArray(lineIterator.nextLine());
            }
            lineIterator.close();
        } catch (final Exception e) {
            // e.printStackTrace();
        }
    }

    private synchronized void addLineToArray(final String line) {
        //System.out.println(line);
        lines.add(line);
    }

    public synchronized void cleanArray() {
        lines.clear();
    }

    public List<String> getLines() {
        return lines;
    }
}
