package com.example.faaaaa;

public class Item {
    private String id;
    private String name;
    private String description;
    private String imagePath;
    public Item(String id, String name, String description, String imagePath){
        this.id = id;
        this.name = name;
        this.description = description;
        this.imagePath = imagePath;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getImagePath() {
        return imagePath;
    }
}
