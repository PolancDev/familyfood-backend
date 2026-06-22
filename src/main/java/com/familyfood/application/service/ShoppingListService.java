package com.familyfood.application.service;

import com.familyfood.application.dto.shopping.*;
import com.familyfood.application.mapper.ShoppingListMapper;
import com.familyfood.application.port.repository.FamilyMemberRepository;
import com.familyfood.application.port.repository.RecipeRepository;
import com.familyfood.application.port.repository.ShoppingListRepository;
import com.familyfood.application.port.repository.WeeklyPlanRepository;
import com.familyfood.domain.enums.*;
import com.familyfood.domain.exception.*;
import com.familyfood.domain.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public class ShoppingListService {

    private final ShoppingListRepository shoppingListRepository;
    private final WeeklyPlanRepository weeklyPlanRepository;
    private final RecipeRepository recipeRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final ShoppingListMapper shoppingListMapper;

    /**
     * Genera la lista de compra a partir del plan semanal.
     * Solo ADMIN puede ejecutarlo.
     */
    @Transactional
    public ShoppingListResponse generateShoppingList(UUID familyGroupId, GenerateShoppingListRequest request, UUID userId) {
        validateAdmin(userId, familyGroupId);

        // 1. Obtener WeeklyPlan
        WeeklyPlan weeklyPlan = weeklyPlanRepository.findByFamilyGroupAndWeek(
                        familyGroupId, request.year(), request.weekNumber())
                .orElseThrow(() -> new WeeklyPlanNotFoundException(
                        "No existe un plan semanal para la semana " + request.weekNumber()
                                + " del año " + request.year() + ". Crea el plan primero."));

        // 2. Inicializar mapa para agrupar ingredientes
        Map<String, IngredientAggregation> aggregatedMap = new LinkedHashMap<>();

        // 3. Para cada PlanDay con estado=NORMAL y recetaId != null
        for (PlanDay day : weeklyPlan.getDias()) {
            if (day.getEstado() != EstadoDia.NORMAL || day.getRecetaId() == null) {
                continue;
            }

            Recipe recipe = recipeRepository.findById(day.getRecetaId()).orElse(null);
            if (recipe == null || recipe.getIngredientes() == null) {
                continue;
            }

            double factor = (double) request.raciones() / (recipe.getRaciones() != null && recipe.getRaciones() > 0
                    ? recipe.getRaciones() : 1);

            for (RecipeIngredient ingredient : recipe.getIngredientes()) {
                if (ingredient.getNombre() == null || ingredient.getNombre().isBlank()) continue;

                String nombreOrig = ingredient.getNombre().trim();
                String unidad = ingredient.getUnidad() != null ? ingredient.getUnidad().trim() : "unidad";
                String key = nombreOrig.toLowerCase() + "|" + unidad.toLowerCase();
                double cantidadTotal = ingredient.getCantidad() * factor;

                if (aggregatedMap.containsKey(key)) {
                    IngredientAggregation agg = aggregatedMap.get(key);
                    agg.cantidad += cantidadTotal;
                    agg.recetasOrigen.add(recipe.getId());
                } else {
                    aggregatedMap.put(key, new IngredientAggregation(
                            nombreOrig, unidad, cantidadTotal, categorizar(nombreOrig),
                            new ArrayList<>(List.of(recipe.getId())), false));
                }
            }
        }

        // 4. Si incluirBasicos → añadir items básicos predefinidos
        if (request.incluirBasicos()) {
            String[] basicos = {"Pan", "Huevos", "Aceite de oliva", "Sal", "Pimienta", "Ajo", "Cebolla"};
            for (String basico : basicos) {
                String key = basico.toLowerCase() + "|unidad";
                if (!containsKeyIgnoreUnit(aggregatedMap, basico.toLowerCase())) {
                    aggregatedMap.put(key, new IngredientAggregation(
                            basico, "unidad", 1.0, categorizar(basico),
                            new ArrayList<>(), false));
                }
            }
        }

        // 5. Crear ShoppingList (o reemplazar existente)
        shoppingListRepository.findByFamilyGroupAndWeek(familyGroupId, request.year(), request.weekNumber())
                .ifPresent(existing -> {
                    log.info("Reemplazando lista de compra existente: {}", existing.getId());
                    shoppingListRepository.delete(existing);
                });

        ShoppingList newList = ShoppingList.create(familyGroupId, request.year(), request.weekNumber());

        List<ShoppingItem> items = new ArrayList<>();
        for (Map.Entry<String, IngredientAggregation> entry : aggregatedMap.entrySet()) {
            IngredientAggregation agg = entry.getValue();
            ShoppingItem item = ShoppingItem.builder()
                    .id(UUID.randomUUID())
                    .shoppingListId(newList.getId())
                    .ingrediente(agg.ingrediente)
                    .cantidad(Math.round(agg.cantidad * 100.0) / 100.0)
                    .unidad(agg.unidad)
                    .categoria(agg.categoria)
                    .comprado(false)
                    .esManual(agg.esManual)
                    .build();
            items.add(item);
        }

        newList.setItems(items);
        ShoppingList saved = shoppingListRepository.save(newList);

        log.info("Lista de compra generada: familia={}, año={}, semana={}, items={}",
                familyGroupId, request.year(), request.weekNumber(), items.size());

        return buildResponse(saved, aggregatedMap);
    }

    /**
     * Obtiene la lista de compra para una semana. NO auto-crea, devuelve 404 si no existe.
     */
    @Transactional(readOnly = true)
    public ShoppingListResponse getShoppingList(UUID familyGroupId, int year, int weekNumber, UUID userId) {
        validateUserInFamily(userId, familyGroupId);

        ShoppingList list = shoppingListRepository.findByFamilyGroupAndWeek(familyGroupId, year, weekNumber)
                .orElseThrow(() -> new ShoppingListNotFoundException(
                        "No se encontró la lista de compra para la semana solicitada"));

        Map<String, IngredientAggregation> aggregations = rebuildAggregations(list, familyGroupId, year, weekNumber);

        return buildResponse(list, aggregations);
    }

    /**
     * Añade un item manual a la lista.
     */
    @Transactional
    public ShoppingItemResponse addItem(UUID familyGroupId, int year, int weekNumber,
                                         AddItemRequest request, UUID userId) {
        validateUserInFamily(userId, familyGroupId);

        ShoppingList list = shoppingListRepository.findByFamilyGroupAndWeek(familyGroupId, year, weekNumber)
                .orElseThrow(() -> new ShoppingListNotFoundException(
                        "No se encontró la lista de compra para la semana solicitada"));

        ShoppingItem newItem = ShoppingItem.crearManual(
                list.getId(), request.ingrediente(), request.cantidad(), request.unidad(), request.categoria());

        list.getItems().add(newItem);
        shoppingListRepository.save(list);

        log.info("Item manual añadido a lista de compra: {}, usuario={}", newItem.getIngrediente(), userId);

        return shoppingListMapper.toItemResponse(newItem);
    }

    /**
     * Actualiza un item (marcar comprado, modificar cantidad/unidad).
     */
    @Transactional
    public ShoppingItemResponse updateItem(UUID itemId, UpdateItemRequest request, UUID userId) {
        ShoppingItem item = shoppingListRepository.findItemById(itemId)
                .orElseThrow(() -> new ShoppingListNotFoundException(
                        "No se encontró el item de compra solicitado"));

        ShoppingList list = shoppingListRepository.findListByItemId(itemId)
                .orElseThrow(() -> new ShoppingListNotFoundException(
                        "No se encontró la lista de compra asociada"));

        validateUserInFamily(userId, list.getFamilyGroupId());

        if (request.comprado() != null) {
            if (request.comprado()) {
                item.marcarComprado();
            } else {
                item.desmarcarComprado();
            }
        }
        if (request.cantidad() != null) {
            item.setCantidad(request.cantidad());
        }
        if (request.unidad() != null) {
            item.setUnidad(request.unidad());
        }

        shoppingListRepository.updateItem(item);

        log.info("Item actualizado: id={}, comprado={}", itemId, item.isComprado());

        return shoppingListMapper.toItemResponse(item);
    }

    /**
     * Elimina un item. Solo ADMIN.
     */
    @Transactional
    public void deleteItem(UUID itemId, UUID userId) {
        ShoppingList list = shoppingListRepository.findListByItemId(itemId)
                .orElseThrow(() -> new ShoppingListNotFoundException(
                        "No se encontró el item de compra solicitado"));

        validateAdmin(userId, list.getFamilyGroupId());

        shoppingListRepository.deleteItem(itemId);

        log.info("Item eliminado de lista de compra: id={}, usuario={}", itemId, userId);
    }

    /**
     * Exporta la lista de compra en formato texto plano.
     */
    @Transactional(readOnly = true)
    public String exportShoppingList(UUID familyGroupId, int year, int weekNumber, UUID userId) {
        validateUserInFamily(userId, familyGroupId);

        ShoppingList list = shoppingListRepository.findByFamilyGroupAndWeek(familyGroupId, year, weekNumber)
                .orElseThrow(() -> new ShoppingListNotFoundException(
                        "No se encontró la lista de compra para la semana solicitada"));

        Map<String, List<ShoppingItem>> itemsByCategory = list.getItems().stream()
                .collect(Collectors.groupingBy(
                        i -> i.getCategoria() != null ? i.getCategoria() : CategoriaCompra.OTROS,
                        LinkedHashMap::new,
                        Collectors.toList()));

        StringBuilder sb = new StringBuilder();
        sb.append("LISTA DE COMPRA — Semana ").append(year).append("-W").append(weekNumber).append("\n");
        sb.append("=".repeat(50)).append("\n\n");

        List<String> orderedCategories = CategoriaCompra.categoriasPredefinidas();
        for (String cat : orderedCategories) {
            List<ShoppingItem> catItems = itemsByCategory.get(cat);
            if (catItems != null && !catItems.isEmpty()) {
                sb.append("**").append(cat).append("**\n");
                for (ShoppingItem item : catItems) {
                    String check = item.isComprado() ? "[x]" : "[ ]";
                    sb.append(check).append(" ")
                            .append(item.getIngrediente()).append(" — ")
                            .append(item.getCantidad()).append(" ").append(item.getUnidad());
                    if (item.isEsManual()) {
                        sb.append(" (manual)");
                    }
                    sb.append("\n");
                }
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    // ========== CATEGORIZACIÓN ==========

    /**
     * Categoriza un ingrediente según su nombre usando palabras clave.
     */
    public String categorizar(String ingrediente) {
        if (ingrediente == null || ingrediente.isBlank()) {
            return CategoriaCompra.OTROS;
        }

        String nombre = ingrediente.toLowerCase().trim();

        // CONSERVAS — detectar antes por contexto de lata/bote/conserva
        if (contieneAlguna(nombre, "lata", "bote", "conserva", "enlatado", "encurtido")) {
            return CategoriaCompra.CONSERVAS;
        }

        // Aceitunas, alcaparras, pepinillos (siempre conservas)
        if (contieneAlguna(nombre, "aceituna", "alcaparra", "pepinillo")) {
            return CategoriaCompra.CONSERVAS;
        }

        // CONGELADOS (antes que otras categorías)
        if (contieneAlguna(nombre, "congelado", "congelada", "congelados", "ultracongelado")) {
            return CategoriaCompra.CONGELADOS;
        }

        // LACTEOS
        if (contieneAlguna(nombre, "leche", "queso", "yogur", "mantequilla", "nata", "crema",
                "requesón", "mozzarella", "parmesano", "ricotta", "kefir", "cuajada", "helado")) {
            return CategoriaCompra.LACTEOS;
        }

        // PANADERIA
        if (contieneAlguna(nombre, "pan", "baguette", "barra", "chapata", "molde", "bollería",
                "croissant", "napolitana", "ensaimada", "magdalena", "bizcocho", "tostada",
                "biscote", "pico", "colín", "rosquilla", "donut", "panecillo", "hogaza")) {
            return CategoriaCompra.PANADERIA;
        }

        // PASTA
        if (contieneAlguna(nombre, "pasta", "macarrón", "espagueti", "tallarín", "fideo",
                "ravioli", "tortellini", "lasaña", "canelón", "penne", "fusilli", "rigatoni",
                "ñoqui", "cuscús", "fettuccine", "tagliatelle", "linguine", "pappardelle")) {
            return CategoriaCompra.PASTA;
        }

        // ARROCES
        if (contieneAlguna(nombre, "arroz", "basmati", "integral", "bomba", "jazmín",
                "arborio", "carnaroli", "vaporizado", "salvaje", "sushi", "thai", "risotto")) {
            return CategoriaCompra.ARROCES;
        }

        // LEGUMBRES
        if (contieneAlguna(nombre, "lenteja", "alubia", "garbanzo", "judía", "haba",
                "soja", "guisante", "cacahuete", "altramuz", "azuki", "frijol",
                "habichuela", "pocha", "fabada")) {
            return CategoriaCompra.LEGUMBRES;
        }

        // CARNE
        if (contieneAlguna(nombre, "pollo", "ternera", "cerdo", "cordero", "chuleta",
                "filete", "solomillo", "costilla", "lomo", "jamón", "panceta", "beicon",
                "salchicha", "chorizo", "morcillo", "hamburguesa", "pavo", "conejo",
                "codorniz", "butifarra")) {
            return CategoriaCompra.CARNE;
        }

        // PESCADERIA
        if (contieneAlguna(nombre, "merluza", "gambas", "langostino", "salmón", "bacalao",
                "boquerón", "trucha", "lubina", "dorada", "rodaballo", "rape", "pulpo",
                "calamar", "sepia", "almeja", "navaja", "percebe", "cangrejo", "bogavante",
                "atún", "sardina", "berberecho", "mejillón")) {
            return CategoriaCompra.PESCADERIA;
        }

        // FRUTERIA
        if (contieneAlguna(nombre, "manzana", "lechuga", "tomate", "cebolla", "pimiento",
                "zanahoria", "patata", "plátano", "naranja", "pera", "uva", "fresa",
                "sandía", "melón", "kiwi", "limón", "aguacate", "brócoli", "coliflor",
                "espinaca", "acelga", "calabacín", "berenjena", "pepino", "apio", "puerro",
                "rábano", "remolacha", "champiñón", "seta", "alcachofa", "judía verde",
                "maíz", "cilantro", "perejil", "albahaca", "hierbabuena", "menta")) {
            return CategoriaCompra.FRUTERIA;
        }

        return CategoriaCompra.OTROS;
    }

    // ========== MÉTODOS PRIVADOS ==========

    private boolean contieneAlguna(String texto, String... palabras) {
        for (String palabra : palabras) {
            if (texto.contains(palabra)) return true;
        }
        return false;
    }

    private boolean containsKeyIgnoreUnit(Map<String, IngredientAggregation> map, String nombreLower) {
        return map.keySet().stream().anyMatch(k -> k.startsWith(nombreLower + "|"));
    }

    private void validateUserInFamily(UUID userId, UUID familyGroupId) {
        if (!familyMemberRepository.existsByUserIdAndFamilyGroupId(userId, familyGroupId)) {
            throw new UnauthorizedException("No perteneces a este grupo familiar");
        }
    }

    private void validateAdmin(UUID userId, UUID familyGroupId) {
        FamilyMember member = familyMemberRepository.findByUserIdAndFamilyGroupId(userId, familyGroupId)
                .orElseThrow(() -> new UnauthorizedException("No eres miembro de este grupo familiar"));

        if (member.getRole() != FamilyRole.ADMIN) {
            throw new UnauthorizedException("Solo el administrador puede gestionar la lista de compra");
        }
    }

    /**
     * Reconstruye las agregaciones desde los items guardados para el response.
     */
    private Map<String, IngredientAggregation> rebuildAggregations(ShoppingList list,
                                                                    UUID familyGroupId, int year, int weekNumber) {
        Map<String, IngredientAggregation> aggregations = new LinkedHashMap<>();

        WeeklyPlan weeklyPlan = weeklyPlanRepository.findByFamilyGroupAndWeek(familyGroupId, year, weekNumber)
                .orElse(null);

        Map<String, Set<UUID>> recetasPorIngrediente = new HashMap<>();
        if (weeklyPlan != null) {
            for (PlanDay day : weeklyPlan.getDias()) {
                if (day.getEstado() != EstadoDia.NORMAL || day.getRecetaId() == null) continue;
                Recipe recipe = recipeRepository.findById(day.getRecetaId()).orElse(null);
                if (recipe == null || recipe.getIngredientes() == null) continue;
                for (RecipeIngredient ing : recipe.getIngredientes()) {
                    if (ing.getNombre() == null) continue;
                    String nombreLower = ing.getNombre().trim().toLowerCase();
                    recetasPorIngrediente.computeIfAbsent(nombreLower, k -> new HashSet<>())
                            .add(recipe.getId());
                }
            }
        }

        for (ShoppingItem item : list.getItems()) {
            String nombreLower = item.getIngrediente().toLowerCase().trim();
            List<UUID> recetasIds = new ArrayList<>(
                    recetasPorIngrediente.getOrDefault(nombreLower, new HashSet<>()));

            aggregations.put(nombreLower + "|" + item.getUnidad().toLowerCase(),
                    new IngredientAggregation(
                            item.getIngrediente(), item.getUnidad(), item.getCantidad(),
                            item.getCategoria(), recetasIds, item.isEsManual()));
        }

        return aggregations;
    }

    private ShoppingListResponse buildResponse(ShoppingList list,
                                                Map<String, IngredientAggregation> aggregations) {
        List<ShoppingItemResponse> itemResponses = new ArrayList<>();

        for (ShoppingItem item : list.getItems()) {
            String key = item.getIngrediente().toLowerCase().trim()
                    + "|" + item.getUnidad().toLowerCase().trim();
            IngredientAggregation agg = aggregations.get(key);
            List<UUID> recetasOrigen = agg != null ? agg.recetasOrigen : new ArrayList<>();

            itemResponses.add(new ShoppingItemResponse(
                    item.getId(),
                    item.getIngrediente(),
                    item.getCantidad(),
                    item.getUnidad(),
                    item.getCategoria(),
                    item.isComprado(),
                    item.isEsManual(),
                    recetasOrigen
            ));
        }

        List<String> categorias = itemResponses.stream()
                .map(ShoppingItemResponse::categoria)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        String semana = list.getYear() + "-W" + String.format("%02d", list.getWeekNumber());

        return new ShoppingListResponse(
                list.getId(),
                list.getFamilyGroupId(),
                list.getYear(),
                list.getWeekNumber(),
                semana,
                categorias,
                itemResponses
        );
    }

    // ========== INNER CLASS ==========

    @lombok.AllArgsConstructor
    private static class IngredientAggregation {
        private final String ingrediente;
        private final String unidad;
        private double cantidad;
        private final String categoria;
        private final List<UUID> recetasOrigen;
        private final boolean esManual;
    }
}
