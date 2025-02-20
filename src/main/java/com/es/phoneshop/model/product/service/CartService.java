package com.es.phoneshop.model.product.service;

import com.es.phoneshop.model.product.entity.Cart;
import com.es.phoneshop.model.product.exception.OutOfStockException;

import javax.servlet.http.HttpServletRequest;

public interface CartService {
    Cart getCart(HttpServletRequest request);

    void add(Cart cart, Long productId, int quantity, HttpServletRequest request) throws OutOfStockException;

    void update(Cart cart, Long productId, int quantity, HttpServletRequest request) throws OutOfStockException;

    void delete(Cart cart, Long productId, HttpServletRequest request);

    void reCalculateCart(Cart cartToRecalculate);

}
