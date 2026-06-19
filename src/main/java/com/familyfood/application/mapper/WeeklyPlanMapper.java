package com.familyfood.application.mapper;

import com.familyfood.application.dto.plan.PlanDayResponse;
import com.familyfood.application.dto.plan.SaveWeeklyPlanRequest;
import com.familyfood.application.dto.plan.WeeklyPlanResponse;
import com.familyfood.domain.model.PlanDay;
import com.familyfood.domain.model.WeeklyPlan;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;

import java.util.List;

@Mapper(componentModel = ComponentModel.SPRING)
public interface WeeklyPlanMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "familyGroupId", ignore = true)
    @Mapping(target = "startDate", ignore = true)
    @Mapping(target = "endDate", ignore = true)
    @Mapping(source = "dias", target = "dias")
    WeeklyPlan toDomain(SaveWeeklyPlanRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "weeklyPlanId", ignore = true)
    @Mapping(target = "recetaNombre", ignore = true)
    @Mapping(target = "recetaTiempoMinutos", ignore = true)
    @Mapping(target = "alergenosAdvertencia", ignore = true)
    @Mapping(target = "version", ignore = true)
    PlanDay toPlanDayDomain(SaveWeeklyPlanRequest.PlanDayRequest request);

    List<PlanDay> toPlanDayDomainList(List<SaveWeeklyPlanRequest.PlanDayRequest> requests);

    WeeklyPlanResponse toResponse(WeeklyPlan plan);

    List<PlanDayResponse> toPlanDayResponseList(List<PlanDay> days);

    @Mapping(target = "receta", ignore = true)
    @Mapping(target = "sobrasOrigenRecetaId", ignore = true)
    @Mapping(target = "sobrasOrigenRecetaNombre", ignore = true)
    PlanDayResponse toPlanDayResponse(PlanDay day);
}
