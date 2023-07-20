package com.badrul.ecommercepoc.controller;

import com.badrul.ecommercepoc.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomePageController {

    private final ProductService productService;

    @GetMapping
    public String getHomePage(Model model) {

        model.addAttribute("homePageProducts", productService.getAllProducts());

        return "index";
    }

}
