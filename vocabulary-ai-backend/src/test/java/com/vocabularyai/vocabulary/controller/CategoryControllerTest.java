package com.vocabularyai.vocabulary.controller;

import com.vocabularyai.vocabulary.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("CategoryController Tests")
public class CategoryControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private CategoryService categoryService;
    
    @BeforeEach
    public void setUp() {
    }
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should get all categories")
    public void testGetAllCategories_Success() throws Exception {
        mockMvc.perform(get("/api/v1/vocabulary/categories"))
            .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should get category by ID")
    public void testGetCategory_Success() throws Exception {
        mockMvc.perform(get("/api/v1/vocabulary/categories/1"))
            .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("Should require authentication for categories")
    public void testGetCategories_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/vocabulary/categories"))
            .andExpect(status().isUnauthorized());
    }
}
