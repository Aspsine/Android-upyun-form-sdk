package com.upyun.http.form.entity;

import java.io.File;

/**
 * Created by aspsine on 15/12/28.
 */
public class FileEntity {
    private String name;
    private File file;

    public FileEntity() {
    }

    public FileEntity(String name, File file) {
        this.name = name;
        this.file = file;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
