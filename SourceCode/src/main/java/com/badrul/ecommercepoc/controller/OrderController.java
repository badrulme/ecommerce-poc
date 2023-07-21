package com.badrul.ecommercepoc.controller;

import com.badrul.ecommercepoc.enums.OrderFrom;
import com.badrul.ecommercepoc.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService service;

    @GetMapping
    public String view(Model model) {

        model.addAttribute("orders", service.getAllOrders());

        return "order";
    }
}
