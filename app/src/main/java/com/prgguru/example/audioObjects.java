package com.prgguru.example;

/**
 * Created by axll on 12/12/2015.
 */
public class audioObjects {
    private String filename;
    private String fileLenght;

    public audioObjects(String filename, String fileLenght) {
        this.filename = filename;
        this.fileLenght = fileLenght;
    }

    public void setFilename(String filename) { this.filename = filename;    }

    public void setFileLenght(String fileLenght) {
        this.fileLenght = fileLenght;
    }

    public String getFilename() { return filename; }

    public String getFileLenght() { return fileLenght; }
}
