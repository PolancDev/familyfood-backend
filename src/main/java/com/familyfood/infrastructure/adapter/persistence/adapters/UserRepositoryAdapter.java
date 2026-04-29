package com.familyfood.infrastructure.adapter.persistence.adapters;

import com.familyfood.application.mapper.UserMapper;
import com.familyfood.application.port.repository.UserRepository;
import com.familyfood.domain.model.User;
import com.familyfood.infrastructure.adapter.persistence.entities.UserEntity;
import com.familyfood.infrastructure.adapter.persistence.repository.SpringDataUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UserRepositoryAdapter implements UserRepository {

    private final SpringDataUserRepository repository;
    private final UserMapper userMapper;

    @Override
    public User save(User user) {
        log.info("============ Estoy en save repository ============");
        log.info("Usuario que me llega: {}", user);

        UserEntity entity;
        if (user.getId() != null) {
            log.info("Usuario con ID existente -> UPDATE");
            entity = userMapper.toEntityForUpdate(user);
        } else {
            log.info("Usuario sin ID -> CREATE");
            entity = userMapper.toEntityForCreate(user);
        }

        log.info("Usuario entity: {}", entity.getId());
        return userMapper.toDomain(repository.save(entity));
    }

    @Override
    public Optional<User> findById(UUID id) {
        return repository.findById(id)
                .map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email)
                .map(userMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public long count() {
        return repository.count();
    }
}