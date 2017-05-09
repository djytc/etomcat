package io.github.djytc.etomcat.common;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static io.github.djytc.etomcat.ETomcat.logger;

/**
 * User: alexkasko
 * Date: 5/9/17
 */
public class DeleteRecursiveVisitor extends SimpleFileVisitor<Path> {
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Files.delete(file);
        logger.debug("Deleted file: [" + file.toAbsolutePath() + "]");
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        Files.delete(dir);
        logger.debug("Deleted dir: [" + dir.toAbsolutePath() + "]");
        return FileVisitResult.CONTINUE;
    }
}
