package com.cansuiremkanli.libmanage.controller;

import com.cansuiremkanli.libmanage.service.BookService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/book")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @Tag(name = "Get All Books", description = "Gets All Books From Database")
    @GetMapping
    public String getAllBooks(){
        return "All Books";
    }
}
