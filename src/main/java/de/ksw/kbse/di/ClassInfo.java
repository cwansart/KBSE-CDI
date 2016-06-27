package de.ksw.kbse.di;

import java.io.File;

public class ClassInfo {

    private final String name;
    private final String path;
    private final File file;
    private String alias = "";

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public File getFile() {
        return file;
    }

    public String getAlias() {
        return alias;
    }

    public ClassInfo(String name, String path, File file) {
        this.name = name;
        this.path = path;
        this.file = file;
    }

    public ClassInfo(String name, String path, File file, String alias) {
        this(name, path, file);
        this.alias = alias;
    }
}
