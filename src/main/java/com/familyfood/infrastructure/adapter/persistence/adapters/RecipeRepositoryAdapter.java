package com.familyfood.infrastructure.adapter.persistence.adapters;

import com.familyfood.application.port.repository.RecipeRepository;
import com.familyfood.domain.model.Recipe;
import com.familyfood.infrastructure.adapter.persistence.entities.RecipeEntity;
import com.familyfood.infrastructure.adapter.persistence.repository.SpringDataRecipeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Slf4j
public class RecipeRepositoryAdapter implements RecipeRepository {

    private final SpringDataRecipeRepository repository;
    private final RecipeEntityMapper entityMapper;

    @Override
    public Recipe save(Recipe recipe) {
        RecipeEntity entity;
        if (recipe.getId() != null) {
            log.info("Receta con ID existente -> UPDATE");
            entity = entityMapper.toEntityForUpdate(recipe);
        } else {
            log.info("Receta sin ID -> CREATE");
            entity = entityMapper.toEntityForCreate(recipe);
        }
        return entityMapper.toDomain(repository.save(entity));
    }

    @Override
    public Optional<Recipe> findById(UUID id) {
        return repository.findById(id)
                .map(entityMapper::toDomain);
    }

    @Override
    public List<Recipe> findAll() {
        return entityMapper.toDomainList(repository.findAll());
    }

    @Override
    public List<Recipe> findByUserId(UUID userId) {
        return entityMapper.toDomainList(
                repository.findByUserIdOrderByNombreAsc(userId));
    }

    @Override
    public List<Recipe> findByFavoritaTrueAndUserId(UUID userId) {
        return entityMapper.toDomainList(
                repository.findByFavoritaTrueAndUserIdOrderByNombreAsc(userId));
    }

    @Override
    public List<Recipe> findByEtiquetasContainsAndUserId(String etiqueta, UUID userId) {
        return entityMapper.toDomainList(
                repository.findByEtiquetasContainsAndUserIdOrderByNombreAsc(etiqueta, userId));
    }

    @Override
    public List<Recipe> findByNombreContainingIgnoreCaseAndUserId(String busqueda, UUID userId) {
        return entityMapper.toDomainList(
                repository.findByNombreContainingIgnoreCaseAndUserIdOrderByNombreAsc(busqueda, userId));
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }
}
