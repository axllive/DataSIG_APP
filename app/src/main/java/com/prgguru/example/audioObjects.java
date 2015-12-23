package com.prgguru.example;

/**
 * Created by axll on 12/12/2015.
 */
public class audioObjects {
    private String filename;
    private String fileLenght;
    private String creationDate;

    public audioObjects(String filename, String fileLenght, String creationDate) {
        this.filename = filename;
        this.fileLenght = fileLenght;
        this.creationDate = creationDate;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setFileLenght(String fileLenght) {
        this.fileLenght = fileLenght;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getFilename() {
        return filename;
    }

    public String getFileLenght() {
        return fileLenght;
    }

    public String getCreationDate() {
        return creationDate;
    }
}
