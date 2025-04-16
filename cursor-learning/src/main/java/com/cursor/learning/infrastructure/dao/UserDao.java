package com.cursor.learning.infrastructure.dao;

import com.cursor.learning.domain.entity.User;
import com.cursor.learning.domain.repository.UserRepository;
import com.cursor.learning.infrastructure.mapper.UserMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class UserDao implements UserRepository {
    private final UserMapper userMapper;

    @Autowired
    public UserDao(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            // 新增用户
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.insert(user);
        } else {
            // 更新用户
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.update(user);
        }
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        return userMapper.findById(id);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userMapper.findByEmail(email);
    }

    @Override
    public List<User> findAll() {
        return userMapper.findAll();
    }

    @Override
    public void update(User user) {
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.update(user);
    }

    @Override
    public void deleteById(Long id) {
        userMapper.deleteById(id);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userMapper.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userMapper.existsByEmail(email);
    }
} 