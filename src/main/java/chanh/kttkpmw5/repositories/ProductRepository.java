package chanh.kttkpmw5.repositories;


import chanh.kttkpmw5.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Integer> {
}
