package ru.practicum.ewm.main.mapper;

import ru.practicum.ewm.main.dto.CategoryDto;
import ru.practicum.ewm.main.model.Category;

public class CategoryMapper {

    public static CategoryDto toDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    public static Category toEntity(CategoryDto dto) {
        return Category.builder()
                .id(dto.getId())
                .name(dto.getName())
                .build();
    }
}