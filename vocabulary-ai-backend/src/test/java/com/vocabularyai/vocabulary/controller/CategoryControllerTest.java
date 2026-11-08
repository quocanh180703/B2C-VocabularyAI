package com.vocabularyai.vocabulary.controller;

import com.vocabularyai.vocabulary.dto.CategoryResponse;
import com.vocabularyai.vocabulary.service.CategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("CategoryController Tests")
public class CategoryControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private CategoryService categoryService;
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should get all categories")
    public void testGetAllCategories_Success() throws Exception {
        when(categoryService.getAllCategories(any(Pageable.class)))
            .thenReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 20), 0));

        mockMvc.perform(get("/categories"))
            .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should get category by ID")
    public void testGetCategory_Success() throws Exception {
        CategoryResponse response = new CategoryResponse();
        when(categoryService.getCategoryById(anyLong())).thenReturn(response);

        mockMvc.perform(get("/categories/1"))
            .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("Should require authentication for categories")
    public void testGetCategories_Unauthenticated() throws Exception {
        mockMvc.perform(get("/categories"))
            .andExpect(status().isUnauthorized());
    }
}
