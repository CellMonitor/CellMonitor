package com.example.joe.cellmonitor.models;



public class Sections {

    private String name , image;
    private long time ;
    public Sections(){

    }

    public Sections(String name, String image, long time) {
        this.name = name;
        this.image = image;
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
