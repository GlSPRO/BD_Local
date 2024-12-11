package com.example.faaaaa;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1; // Код запроса для выбора изображения

    private EditText nameText, descriptionText; // Поля ввода для имени и описания товара
    private Button addButton, uploadImageButton, deleteButton; // Кнопки для добавления, загрузки изображения и удаления товара
    private ImageView itemImageView; // Изображение товара
    private ListView listView; // Список товаров

    private ArrayAdapter<String> adapter; // Адаптер для списка товаров
    private List<Item> clothingItems = new ArrayList<>(); // Список объектов товаров
    private String selectedImagePath; // Путь к выбранному изображению
    private String selectedItemId; // ID выбранного товара

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Установка макета активности

        Paper.init(this); // Инициализация библиотеки PaperDB

        // Проверка разрешений на чтение внешнего хранилища
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PICK_IMAGE_REQUEST);
        }

        // Инициализация элементов UI
        nameText = findViewById(R.id.nameText);
        descriptionText = findViewById(R.id.descriptionText);
        uploadImageButton = findViewById(R.id.uploadImageButton);
        itemImageView = findViewById(R.id.itemImageView);
        addButton = findViewById(R.id.addButton);
        deleteButton = findViewById(R.id.deleteButton);
        listView = findViewById(R.id.listView);

        // Настройка адаптера для списка товаров
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getClothingItemNames());
        listView.setAdapter(adapter);

        // Обработчик нажатия на кнопку загрузки изображения
        uploadImageButton.setOnClickListener(v -> openFileChooser());

        // Обработчик нажатия на кнопку добавления товара
        addButton.setOnClickListener(v -> {
            String name = nameText.getText().toString(); // Получаем имя товара из поля ввода
            String description = descriptionText.getText().toString(); // Получаем описание товара из поля ввода
            if (!name.isEmpty() && !description.isEmpty() && selectedImagePath != null) {
                // Создаем новый объект ClothindItem и сохраняем его в базе данных PaperDB
                Item item = new Item(name + "_" + System.currentTimeMillis(), name, description, selectedImagePath);
                Paper.book().write(item.getId(), item);
                updateClothingList(); // Обновляем список товаров
                clearInputs(); // Очищаем поля ввода
            } else {
                Toast.makeText(MainActivity.this, "Пожалуйста, заполните все поля и выберите изображение", Toast.LENGTH_SHORT).show();
            }
        });

        // Обработчик нажатия на элемент списка товаров
        listView.setOnItemClickListener((parent, view, position, id) -> {
            try {
                Item item = clothingItems.get(position); // Получаем выбранный товар из списка
                nameText.setText(item.getName()); // Устанавливаем имя товара в поле ввода
                descriptionText.setText(item.getDescription()); // Устанавливаем описание товара в поле ввода
                selectedImagePath = item.getImagePath(); // Сохраняем путь к изображению товара
                selectedItemId = item.getId(); // Сохраняем ID выбранного товара

                loadImage(selectedImagePath); // Загружаем изображение товара в ImageView
            } catch (IndexOutOfBoundsException e) {
                Toast.makeText(MainActivity.this, "Ошибка: товар не найден", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "Произошла ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Обработчик нажатия на кнопку удаления товара
        deleteButton.setOnClickListener(v -> {
            if (selectedItemId == null) {
                Toast.makeText(MainActivity.this, "Пожалуйста, сначала выберите товар", Toast.LENGTH_SHORT).show();
                return;
            }

            Paper.book().delete(selectedItemId); // Удаляем товар из базы данных PaperDB
            updateClothingList(); // Обновляем список товаров
            clearInputs(); // Очищаем поля ввода
            Toast.makeText(MainActivity.this, "Товар удален", Toast.LENGTH_SHORT).show();
        });

        updateClothingList(); // Обновляем список при запуске приложения
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            selectedImagePath = uri.toString();
            itemImageView.setImageURI(uri);
        } else {
            Toast.makeText(this, "Ошибка при выборе изображения", Toast.LENGTH_SHORT).show();
        }
    }

    private List<String> getClothingItemNames() {
        List<String> names = new ArrayList<>();
        clothingItems.clear();
        for (String key : Paper.book().getAllKeys()) {
            try {
                Item item = Paper.book().read(key);
                if (item != null) {
                    names.add(item.getName());
                    clothingItems.add(item);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Ошибка чтения товара: " + key, Toast.LENGTH_SHORT).show();
            }
        }
        return names;
    }

    private void loadImage(String imagePath) {
        Log.d("MainActivity", "Загружаем изображение из пути: " + imagePath);
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                Uri uri = Uri.parse(imagePath);
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                itemImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Ошибка загрузки изображения: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
//              Toast.makeText(MainActivity.this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            itemImageView.setImageResource(0);
            Toast.makeText(MainActivity.this, "Изображение не доступно", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateClothingList() {
        adapter.clear();
        adapter.addAll(getClothingItemNames());
        adapter.notifyDataSetChanged();
    }

    private void clearInputs() {
        nameText.setText("");
        descriptionText.setText("");
        selectedImagePath = null;
        selectedItemId = null;
        itemImageView.setImageResource(0);
    }
}