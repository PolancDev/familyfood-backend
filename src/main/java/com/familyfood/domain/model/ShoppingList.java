package com.familyfood.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShoppingList {
    private UUID id;
    private UUID familyGroupId;
    private int year;
    private int weekNumber;

    @Builder.Default
    private List<ShoppingItem> items = new ArrayList<>();

    @Builder.Default
    private Long version = 0L;

    public static ShoppingList create(UUID familyGroupId, int year, int weekNumber) {
        return ShoppingList.builder()
                .id(UUID.randomUUID())
                .familyGroupId(familyGroupId)
                .year(year)
                .weekNumber(weekNumber)
                .items(new ArrayList<>())
                .build();
    }
}
