package tools;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.TERMINATE;

public class Finder extends SimpleFileVisitor<Path> {

    private final PathMatcher matcher;
    private List<String> filePaths = new ArrayList<>();
    private FileVisitResult RESULT = TERMINATE;

    public Finder(String pattern, Boolean fullTraversal) {
        matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
        if (fullTraversal) {
            RESULT = CONTINUE;
        }
    }

    public List<String> getFilePaths() {
        return this.filePaths;
    }

    // Compares the glob pattern against
    // the file or directory name.
    private boolean find(Path file) {
        Path name = file.getFileName();
        Boolean match = false;
        if (name != null && matcher.matches(name)) {
            match = true;
            filePaths.add(file.toString());
            System.out.println("File has been found at " + file.toString());
        }
        return match;
    }

    // Invoke the pattern matching
    // method on each file.
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        if (find(file)) {
            return RESULT;
        } else {
            return CONTINUE;
        }
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        exc.printStackTrace();
        return CONTINUE;
    }
}