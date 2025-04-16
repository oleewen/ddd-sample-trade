package com.cursor.learning.infrastructure.mapper;

import com.cursor.learning.domain.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

@Mapper
public interface UserMapper {
    @Insert("INSERT INTO users(username, password, email, phone, created_at, updated_at) " +
            "VALUES(#{username}, #{password}, #{email}, #{phone}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(User user);

    @Select("SELECT * FROM users WHERE id = #{id}")
    Optional<User> findById(Long id);

    @Select("SELECT * FROM users WHERE username = #{username}")
    Optional<User> findByUsername(String username);

    @Select("SELECT * FROM users WHERE email = #{email}")
    Optional<User> findByEmail(String email);

    @Select("SELECT * FROM users")
    List<User> findAll();

    @Update("UPDATE users SET username = #{username}, password = #{password}, " +
            "email = #{email}, phone = #{phone}, updated_at = #{updatedAt} " +
            "WHERE id = #{id}")
    void update(User user);

    @Delete("DELETE FROM users WHERE id = #{id}")
    void deleteById(Long id);

    @Select("SELECT COUNT(*) > 0 FROM users WHERE username = #{username}")
    boolean existsByUsername(String username);

    @Select("SELECT COUNT(*) > 0 FROM users WHERE email = #{email}")
    boolean existsByEmail(String email);
} 