package chanh.kttkpmw5.repositories;

import chanh.kttkpmw5.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
}
