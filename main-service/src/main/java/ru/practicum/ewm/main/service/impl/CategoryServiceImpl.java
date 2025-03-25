package ru.practicum.ewm.main.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.main.dto.CategoryDto;
import ru.practicum.ewm.main.dto.NewCategoryDto;
import ru.practicum.ewm.main.exception.ConflictException;
import ru.practicum.ewm.main.exception.NotFoundException;
import ru.practicum.ewm.main.mapper.CategoryMapper;
import ru.practicum.ewm.main.model.Category;
import ru.practicum.ewm.main.model.Event;
import ru.practicum.ewm.main.repository.CategoryRepository;
import ru.practicum.ewm.main.repository.EventRepository;
import ru.practicum.ewm.main.service.CategoryService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;


    @Override
    @Transactional
    public CategoryDto addCategory(NewCategoryDto dto) {
        if (categoryRepository.existsByName(dto.getName())) {
            throw new ConflictException("Category with name already exists");
        }

        Category category = Category.builder()
                .name(dto.getName())
                .build();

        return CategoryMapper.toCategoryDto(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long catId, CategoryDto dto) {
        Category category = getCategoryById(catId);

        String newName = dto.getName();

        if (!category.getName().equals(newName) && categoryRepository.existsByNameAndIdNot(newName, catId)) {
            throw new ConflictException("Category with name already exists");
        }
        category.setName(newName);

        return CategoryMapper.toCategoryDto(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deleteCategory(Long catId) {
        getCategoryById(catId);

        List<Event> eventsByCategory = eventRepository.findAllByCategoryId(catId);

        if (eventsByCategory.isEmpty()) {
            categoryRepository.deleteById(catId);
        } else {
            throw new ConflictException("The category is not empty");
        }
    }

    @Override
    public List<CategoryDto> getAll(int from, int size) {
        List<Category> categories = categoryRepository
                .findAll(PageRequest.of(from / size, size))
                .getContent();

        return CategoryMapper.toCategoryDtoList(categories);
    }

    private Category getCategoryById(Long catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория с id: " + catId + " не найдена!"));
    }


    //!!!!!!!!
    //ЭТОТ МЕТОД НАДО использовать только для контроллера!
    //ОН НЕ ДОЛЖЕН КАСАТЬСЯ ДРУГОГО СЕРВИСА И ПРЕОБРАЗОВАНИЯ В ДТО ДОЛЖНЫ ПРОИСХОДИТ В МАППЕРЕ
    //!!!!!!!!
    @Override
    public CategoryDto getById(Long catId) {
        return categoryRepository.findById(catId)
                .map(CategoryMapper::toCategoryDto)
                .orElseThrow(() -> new NotFoundException("Категория с id: " + catId + " не найдена!"));
    }
}
