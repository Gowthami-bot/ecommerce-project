package com.ecommerce;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import com.ecommerce.service.UserService;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.ecommerce.config.RootConfig;
import com.ecommerce.model.User;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.UserService;

@SpringJUnitConfig(RootConfig.class)
public class DatabaseConnectionTest {

    @Autowired
    private UserService userService;
    

   @Test
void testUserService() {

    User user = new User();

    user.setUsername("service_user_" + System.currentTimeMillis());
    user.setEmail("service_" + System.currentTimeMillis() + "@example.com");
    user.setPassword("test123");

    int rows = userService.createUser(user);

    System.out.println("Rows inserted: " + rows);

    assertEquals(1, rows);

    List<User> users = userService.getAllUsers();

    System.out.println("Users found: " + users.size());

    assertNotNull(users);
}
}