package com.es.phoneshop.web;

import com.es.phoneshop.model.product.dao.ProductDao;
import com.es.phoneshop.model.product.dao.impl.ArrayListProductDao;
import com.es.phoneshop.model.product.entity.Product;
import com.es.phoneshop.model.product.enums.SortField;
import com.es.phoneshop.model.product.enums.SortOrder;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AdvancedSearchPageServlet extends HttpServlet {
    private ProductDao productDao;

    private final String DESCRIPTION_PARAM = "description";
    private final String MINIMUM_PRICE_PARAM = "minimumPrice";
    private final String MAXIMUM_PRICE_PARAM = "maximumPrice";
    private final String MINIMUM_PRICE_ATTR = "minPrice";
    private final String MAXIMUM_PRICE_ATTR = "maxPrice";
    private final String ERRORS_ATTR = "errorsMap";

    private final String PRODUCT_LIST_ATTR = "productList";
    private final String ERROR_NOT_A_NUMBER = "Not a number!";
    private final String ALL_WORDS_CASE = "allWords";
    private final String ANY_WORD_CASE = "anyWord";

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        productDao = ArrayListProductDao.getInstance();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/pages/advancedSearch.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String description = request.getParameter(DESCRIPTION_PARAM);
        String minPrice = request.getParameter(MINIMUM_PRICE_PARAM);
        String maxPrice = request.getParameter(MAXIMUM_PRICE_PARAM);

        Map<String, String> possibleErrorsMap = new HashMap<>();

        long minimumPrice = 0, maximumPrice = 0;

        if (description.isEmpty() && minPrice.isEmpty() && maxPrice.isEmpty()) {
            request.setAttribute(PRODUCT_LIST_ATTR, productDao.findProducts("", SortField.DESCRIPTION, SortOrder.ASC, false));
            doGet(request, response);
        } else {
            if (!minPrice.isEmpty()) {
                try {
                    Long.parseLong(minPrice);
                    minimumPrice = Long.parseLong(minPrice);
                    ;
                } catch (NumberFormatException nfe) {
                    possibleErrorsMap.put(MINIMUM_PRICE_ATTR, ERROR_NOT_A_NUMBER);
                }
            } else {
                minimumPrice = 0;
            }
            if (!maxPrice.isEmpty()) {
                try {
                    Long.parseLong(maxPrice);
                    maximumPrice = Long.parseLong(maxPrice);
                    ;
                } catch (NumberFormatException nfe) {
                    possibleErrorsMap.put(MAXIMUM_PRICE_ATTR, ERROR_NOT_A_NUMBER);
                }
            } else {
                maximumPrice = Long.MAX_VALUE;
            }
            if (possibleErrorsMap.isEmpty()) {
                switch (request.getParameter("select")) {
                    case ALL_WORDS_CASE: {
                        List<Product> foundProducts = filterPrice(description, minimumPrice, maximumPrice, true);
                        request.setAttribute(PRODUCT_LIST_ATTR, foundProducts);
                        break;
                    }
                    case ANY_WORD_CASE: {
                        List<Product> foundProducts = filterPrice(description, minimumPrice, maximumPrice, false);
                        request.setAttribute(PRODUCT_LIST_ATTR, foundProducts);
                    }
                }
            }
            request.setAttribute(ERRORS_ATTR, possibleErrorsMap);
            doGet(request, response);
        }
    }

    private List<Product> filterPrice(String description, long minimumPrice, long maximumPrice, boolean lazySearch) {
        List<Product> foundProducts = productDao.findProducts(
                description,
                SortField.DESCRIPTION,
                SortOrder.ASC,
                lazySearch);
        foundProducts.stream().
                filter(product -> product.getPrice().longValue() > minimumPrice).
                filter(product -> product.getPrice().longValue() < maximumPrice).
                collect(Collectors.toList());
        foundProducts.removeIf(product -> product.getPrice().longValue() < minimumPrice);
        foundProducts.removeIf(product -> product.getPrice().longValue() > maximumPrice);
        return foundProducts;
    }
}
