package com.familyfood.application.mapper;

import com.familyfood.application.dto.shopping.ShoppingItemResponse;
import com.familyfood.application.dto.shopping.ShoppingListResponse;
import com.familyfood.domain.model.ShoppingItem;
import com.familyfood.domain.model.ShoppingList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ShoppingListMapper Tests")
class ShoppingListMapperTest {

    private ShoppingListMapper mapper;

    private UUID listId;
    private UUID familyGroupId;
    private UUID itemId1;
    private UUID itemId2;

    @BeforeEach
    void setUp() {
        mapper = new ShoppingListMapperImpl();

        listId = UUID.randomUUID();
        familyGroupId = UUID.randomUUID();
        itemId1 = UUID.randomUUID();
        itemId2 = UUID.randomUUID();
    }

    @Nested
    @DisplayName("toResponse(ShoppingList)")
    class ToResponseTests {

        @Test
        @DisplayName("debe mapear todos los campos de ShoppingList a ShoppingListResponse")
        void debeMapearShoppingListAResponse() {
            ShoppingList list = ShoppingList.builder()
                    .id(listId)
                    .familyGroupId(familyGroupId)
                    .year(2026)
                    .weekNumber(25)
                    .items(List.of())
                    .build();

            ShoppingListResponse response = mapper.toResponse(list);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(listId);
            assertThat(response.familyGroupId()).isEqualTo(familyGroupId);
            assertThat(response.year()).isEqualTo(2026);
            assertThat(response.weekNumber()).isEqualTo(25);
            assertThat(response.semana()).isNull(); // ignorado por MapStruct
            assertThat(response.categorias()).isNull(); // ignorado por MapStruct
            assertThat(response.items()).isNull(); // ignorado por MapStruct
        }
    }

    @Nested
    @DisplayName("toItemResponse(ShoppingItem)")
    class ToItemResponseTests {

        @Test
        @DisplayName("debe mapear todos los campos de ShoppingItem a ShoppingItemResponse")
        void debeMapearShoppingItemAResponse() {
            ShoppingItem item = ShoppingItem.builder()
                    .id(itemId1)
                    .shoppingListId(listId)
                    .ingrediente("Leche")
                    .cantidad(1.5)
                    .unidad("litros")
                    .categoria("LACTEOS")
                    .comprado(false)
                    .esManual(false)
                    .build();

            ShoppingItemResponse response = mapper.toItemResponse(item);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(itemId1);
            assertThat(response.ingrediente()).isEqualTo("Leche");
            assertThat(response.cantidad()).isEqualTo(1.5);
            assertThat(response.unidad()).isEqualTo("litros");
            assertThat(response.categoria()).isEqualTo("LACTEOS");
            assertThat(response.comprado()).isFalse();
            assertThat(response.esManual()).isFalse();
            assertThat(response.recetasOrigen()).isNull(); // ignorado por MapStruct
        }

        @Test
        @DisplayName("debe mapear item con comprado=true")
        void debeMapearItemComprado() {
            ShoppingItem item = ShoppingItem.builder()
                    .id(itemId1)
                    .shoppingListId(listId)
                    .ingrediente("Pan")
                    .cantidad(1.0)
                    .unidad("barra")
                    .categoria("PANADERIA")
                    .comprado(true)
                    .esManual(true)
                    .build();

            ShoppingItemResponse response = mapper.toItemResponse(item);

            assertThat(response.comprado()).isTrue();
            assertThat(response.esManual()).isTrue();
        }
    }

    @Nested
    @DisplayName("toItemResponseList(List<ShoppingItem>)")
    class ToItemResponseListTests {

        @Test
        @DisplayName("debe mapear lista de ShoppingItem a lista de ShoppingItemResponse")
        void debeMapearListaDeItemsAListaResponse() {
            List<ShoppingItem> items = List.of(
                    ShoppingItem.builder()
                            .id(itemId1)
                            .shoppingListId(listId)
                            .ingrediente("Pan")
                            .cantidad(1.0)
                            .unidad("barra")
                            .categoria("PANADERIA")
                            .build(),
                    ShoppingItem.builder()
                            .id(itemId2)
                            .shoppingListId(listId)
                            .ingrediente("Leche")
                            .cantidad(2.0)
                            .unidad("litros")
                            .categoria("LACTEOS")
                            .build()
            );

            List<ShoppingItemResponse> responses = mapper.toItemResponseList(items);

            assertThat(responses).hasSize(2);
            assertThat(responses).extracting(ShoppingItemResponse::ingrediente)
                    .containsExactly("Pan", "Leche");
            assertThat(responses).extracting(ShoppingItemResponse::cantidad)
                    .containsExactly(1.0, 2.0);
        }

        @Test
        @DisplayName("debe devolver lista vacía para lista vacía")
        void debeDevolverListaVacia() {
            List<ShoppingItemResponse> responses = mapper.toItemResponseList(List.of());

            assertThat(responses).isEmpty();
        }
    }
}
