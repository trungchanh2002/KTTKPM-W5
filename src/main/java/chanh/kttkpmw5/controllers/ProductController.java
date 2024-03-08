package chanh.kttkpmw5.controllers;


import chanh.kttkpmw5.models.Product;
import chanh.kttkpmw5.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.Jedis;

import java.util.List;

@RestController
public class ProductController {

    @Autowired
    private ProductRepository productRepository;
    private Jedis jedis = new Jedis();

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/products")
    private List<Product> getList() {
        List<Product> list = productRepository.findAll();
        return list;
    }

    @PostMapping("/products")
    private Product addAllProduct(@RequestBody Product product) {
        product = productRepository.save(product); // Save product to database
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        hashOps.put("products", String.valueOf(product.getId()), product.getName()); // Store name
        hashOps.put("quantities", String.valueOf(product.getId()), String.valueOf(product.getQuantity()));
        hashOps.put("status", String.valueOf(product.getId()), String.valueOf(product.getStatus())); // Store status
        System.out.println("saved in cache");
        return product;
    }

    @GetMapping("/products/{id}")
    public Product getProductById(@PathVariable(value = "id") int id) {
        String key = String.valueOf(id);
        if (jedis.exists(key)) {
            Product productCash = new Product();
            productCash.setId(id);
            String userName = jedis.get(key);
            productCash.setName(userName);
            Product productFromDB = productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User_id " + id + " not found"));
            productCash.setQuantity(productFromDB.getQuantity());
            productCash.setStatus((productFromDB.getStatus()));
            return productCash;
        } else {
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User_id " + id + " not found"));
            jedis.setex(key, 3600, product.getName());
            System.out.println("Saved in cache");
            return product;
        }
    }


    @DeleteMapping("/products/{id}")
    private void deleteProduct(@PathVariable(value = "id") int id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
        productRepository.delete(product);
        jedis.del(String.valueOf(product.getId()));
        System.out.println("Delete in cache");
    }

//    @PostMapping("/products")
//    private Product addProduct(@RequestBody Product product) {
//        jedis.set(String.valueOf(product.getId()), product.getName());
//        System.out.println("saved in cache");
//        return productRepository.save(product);
//    }

    @PutMapping("products/{id}")
    public Product updateProduct(@PathVariable(value = "id") int id, @RequestBody Product product) {
        Product productUpdate = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
        productUpdate.setName(product.getName());
        jedis.set(String.valueOf(product.getId()), product.getName());
        return productRepository.save(productUpdate);
    }


}
