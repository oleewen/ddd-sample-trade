package com.cursor.learning.domain.repository;

import com.cursor.learning.domain.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.annotation.Rollback;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @Transactional
    public void testSaveAndFindUser() {
        // 创建新用户
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setPhone("1234567890");

        // 保存用户
        User savedUser = userRepository.save(user);

        // 通过ID查找用户
        Optional<User> foundUser = userRepository.findById(savedUser.getId());
        
        // 验证用户是否找到并且属性正确
        assertTrue(foundUser.isPresent());
        assertEquals("testuser", foundUser.get().getUsername());
        assertEquals("test@example.com", foundUser.get().getEmail());
        assertEquals("1234567890", foundUser.get().getPhone());
        assertNotNull(foundUser.get().getCreatedAt());
        assertNotNull(foundUser.get().getUpdatedAt());
    }

    @Test
    @Transactional
    public void testFindByUsername() {
        // 创建并保存用户
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setPhone("1234567890");
        userRepository.save(user);

        // 通过用户名查找
        Optional<User> foundUser = userRepository.findByUsername("testuser");
        
        // 验证用户是否找到
        assertTrue(foundUser.isPresent());
        assertEquals("test@example.com", foundUser.get().getEmail());
    }

    @Test
    @Transactional
    public void testFindByEmail() {
        // 创建并保存用户
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setPhone("1234567890");
        userRepository.save(user);

        // 通过邮箱查找
        Optional<User> foundUser = userRepository.findByEmail("test@example.com");
        
        // 验证用户是否找到
        assertTrue(foundUser.isPresent());
        assertEquals("testuser", foundUser.get().getUsername());
    }

    @Test
    @Transactional
    public void testUpdateUser() {
        // 创建并保存用户
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setPhone("1234567890");
        User savedUser = userRepository.save(user);

        // 更新用户信息
        savedUser.setEmail("newemail@example.com");
        savedUser.setPhone("9876543210");
        userRepository.update(savedUser);

        // 查找更新后的用户
        Optional<User> foundUser = userRepository.findById(savedUser.getId());
        
        // 验证更新是否成功
        assertTrue(foundUser.isPresent());
        assertEquals("newemail@example.com", foundUser.get().getEmail());
        assertEquals("9876543210", foundUser.get().getPhone());
        assertNotNull(foundUser.get().getUpdatedAt());
    }

    @Test
    @Transactional
    public void testDeleteUser() {
        // 创建并保存用户
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setPhone("1234567890");
        User savedUser = userRepository.save(user);

        // 删除用户
        userRepository.deleteById(savedUser.getId());

        // 尝试查找已删除的用户
        Optional<User> foundUser = userRepository.findById(savedUser.getId());
        
        // 验证用户是否已被删除
        assertFalse(foundUser.isPresent());
    }

    @Test
    @Transactional
    public void testExistsBy() {
        // 创建并保存用户
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setPhone("1234567890");
        userRepository.save(user);

        // 验证存在性检查
        assertTrue(userRepository.existsByUsername("testuser"));
        assertTrue(userRepository.existsByEmail("test@example.com"));
        assertFalse(userRepository.existsByUsername("nonexistent"));
        assertFalse(userRepository.existsByEmail("nonexistent@example.com"));
    }

    @Test
    @Transactional
    public void listAllUsers() {
        // 查询所有用户
        List<User> users = userRepository.findAll();
        
        // 打印用户信息
        System.out.println("\n当前数据库中的用户信息：");
        for (User user : users) {
            System.out.println("ID: " + user.getId());
            System.out.println("用户名: " + user.getUsername());
            System.out.println("邮箱: " + user.getEmail());
            System.out.println("电话: " + user.getPhone());
            System.out.println("创建时间: " + user.getCreatedAt());
            System.out.println("更新时间: " + user.getUpdatedAt());
            System.out.println("------------------------");
        }
        
        // 打印用户总数
        System.out.println("数据库中共有 " + users.size() + " 个用户");
    }

    @Test
    @Transactional
    @Rollback(false)
    public void createPersistentUsers() {
        // 使用时间戳创建唯一的用户名
        String timestamp = String.valueOf(System.currentTimeMillis());
        
        // 创建测试用户
        User user1 = new User();
        user1.setUsername("persistent_user1_" + timestamp);
        user1.setEmail("persistent1_" + timestamp + "@example.com");
        user1.setPassword("password123");
        user1.setPhone("1234567890");
        userRepository.save(user1);

        User user2 = new User();
        user2.setUsername("persistent_user2_" + timestamp);
        user2.setEmail("persistent2_" + timestamp + "@example.com");
        user2.setPassword("password123");
        user2.setPhone("0987654321");
        userRepository.save(user2);

        // 查询并打印所有用户
        List<User> users = userRepository.findAll();
        System.out.println("\n当前数据库中的用户信息：");
        for (User user : users) {
            System.out.println("ID: " + user.getId());
            System.out.println("用户名: " + user.getUsername());
            System.out.println("邮箱: " + user.getEmail());
            System.out.println("电话: " + user.getPhone());
            System.out.println("创建时间: " + user.getCreatedAt());
            System.out.println("更新时间: " + user.getUpdatedAt());
            System.out.println("------------------------");
        }
        System.out.println("数据库中共有 " + users.size() + " 个用户");
    }
} 