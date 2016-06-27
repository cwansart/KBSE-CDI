package de.ksw.kbse.di;

import java.io.File;

public class ClassInfo {

    private final String name;
    private final String path;
    private final File file;

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public File getFile() {
        return file;
    }

    public ClassInfo(String name, String path, File file) {
        this.name = name;
        this.path = path;
        this.file = file;
    }
}
