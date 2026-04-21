package org.lewap.photogallery.util;

import java.nio.file.Path;
import java.nio.file.Paths;

public class FilePathParser {

    private final Path path;

    public FilePathParser (String in) {
        path = Paths.get(in);
    }

    public String getPath() {
        Path parent = path.getParent();
        return (parent != null) ? parent.toString() : "";
    }

    public String getFileNameWithExtension() {
        return path.getFileName().toString();
    }

    public String getFileNameWithoutExtension() {
        String fileNameWithExtension = getFileNameWithExtension();
        String fileName = fileNameWithExtension;
        int lastDotIndex = fileNameWithExtension.lastIndexOf('.');

        if (lastDotIndex > 0 && lastDotIndex < fileNameWithExtension.length() - 1) {
            fileName = fileNameWithExtension.substring(0, lastDotIndex);
        }

        return fileName;

    }

    public String getFileExtension() {
        String fileNameWithExtension = getFileNameWithExtension();
        int lastDotIndex = fileNameWithExtension.lastIndexOf('.');
        String extension = "";
        if (lastDotIndex > 0 && lastDotIndex < fileNameWithExtension.length() - 1) {
            extension = fileNameWithExtension.substring(lastDotIndex);
        }
        return extension;
    }

}