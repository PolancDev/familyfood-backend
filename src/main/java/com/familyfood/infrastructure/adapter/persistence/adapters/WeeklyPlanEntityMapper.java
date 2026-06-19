package com.familyfood.infrastructure.adapter.persistence.adapters;

import com.familyfood.domain.model.PlanDay;
import com.familyfood.domain.model.WeeklyPlan;
import com.familyfood.infrastructure.adapter.persistence.entities.PlanDayEntity;
import com.familyfood.infrastructure.adapter.persistence.entities.WeeklyPlanEntity;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = ComponentModel.SPRING)
public interface WeeklyPlanEntityMapper {

    WeeklyPlan toDomain(WeeklyPlanEntity entity);

    List<WeeklyPlan> toDomainList(List<WeeklyPlanEntity> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dias", ignore = true)
    WeeklyPlanEntity toEntityForCreate(WeeklyPlan domain);

    WeeklyPlanEntity toEntityForUpdate(WeeklyPlan domain);

    @Named("toPlanDayDomain")
    @Mapping(target = "weeklyPlanId", ignore = true)
    @Mapping(target = "recetaNombre", ignore = true)
    @Mapping(target = "recetaTiempoMinutos", ignore = true)
    @Mapping(target = "alergenosAdvertencia", ignore = true)
    PlanDay toPlanDayDomain(PlanDayEntity entity);

    @IterableMapping(qualifiedByName = "toPlanDayDomain")
    List<PlanDay> toPlanDayDomainList(List<PlanDayEntity> entities);

    @Named("toPlanDayEntityForCreate")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "weeklyPlan", ignore = true)
    PlanDayEntity toPlanDayEntityForCreate(PlanDay domain);

    @Named("toPlanDayEntityForUpdate")
    @Mapping(target = "weeklyPlan", ignore = true)
    PlanDayEntity toPlanDayEntityForUpdate(PlanDay domain);

    @IterableMapping(qualifiedByName = "toPlanDayEntityForCreate")
    List<PlanDayEntity> toPlanDayEntityList(List<PlanDay> days);
}
