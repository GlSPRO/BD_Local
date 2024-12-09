package com.example.faaaaa;

public class ClothindItem {
    private String id; // Уникальный идентификатор
    private String name; // Название товара
    private String description; // Описание товара
    private String imagePath; // Путь к изображению
    public ClothindItem(String id, String name, String description, String imagePath){
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
