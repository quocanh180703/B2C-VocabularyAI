package com.vocabularyai.vocabulary.service;

import com.vocabularyai.vocabulary.repository.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
@DisplayName("CategoryService Tests")
public class CategoryServiceTest {
    
    @MockBean
    private CategoryRepository categoryRepository;
}
