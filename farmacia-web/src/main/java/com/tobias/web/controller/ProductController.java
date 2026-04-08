package com.tobias.web.controller;

import com.tobias.dao.ProductDao;
import com.tobias.model.Product;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductController {

    private final ProductDao productDao = new ProductDao();

    @GetMapping
    public List<Product> findAll(@RequestParam(required = false) String filtro) throws Exception {
        return productDao.findAll(filtro);
    }

    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<Product> findByCodigoBarra(@PathVariable String codigo) throws Exception {
        Product product = productDao.findByCodigoBarra(codigo);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(product);
    }

    @PostMapping
    public Product create(@RequestBody Product product) throws Exception {
        return productDao.insert(product);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Integer id, @RequestBody Product product) throws Exception {
        product.setId(id);
        productDao.update(product);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) throws Exception {
        productDao.delete(id);
        return ResponseEntity.ok().build();
    }
}
