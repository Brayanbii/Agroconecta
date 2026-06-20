# TODO - Fix tabla en blanco Thymeleaf (campesino_pedidos)

## Estado actual
- [x] Analizado `CampesinoController.misPedidos()`
- [x] Analizado `DetalleOrden`
- [x] Analizado `campesino_pedidos.html`
- [x] Plan aprobado por usuario

## Pasos de implementación
- [x] Cambiar `DetalleOrden` de `@Transient Map<String,Object> repartidorInfo` a `@Transient String repartidorInfoJson`
- [x] Actualizar `CampesinoController.misPedidos()` para serializar info de repartidor con `ObjectMapper` y guardar en `repartidorInfoJson`
- [x] Reescribir bloque `LISTO_PARA_RECOGER` en `campesino_pedidos.html` usando `th:data-repartidor`
- [x] Eliminar acceso a mapa en Thymeleaf (`v.repartidorInfo['...']`)
- [x] Agregar null-check estricto para fecha (`#temporals.format`) y total (`#numbers.formatDecimal`)
- [x] Actualizar JS `openDriverModal(orderId)` para leer `data-repartidor`, parsear JSON seguro y poblar modal
- [ ] Compilar proyecto para validar que no hay errores

## Verificación final
- [ ] Confirmar que ya no hay crash silencioso en `th:each`
- [ ] Confirmar que carga el `<script>` y se ejecuta filtro inicial
- [ ] Confirmar que tabla y/o mensajes vacíos se muestran correctamente
