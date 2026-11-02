# Tienda Virtual (JavaFX)

Tienda virtual de escritorio construida con JavaFX 17 y Maven, con
persistencia en JSON plano. Proyecto educativo con capas estrictas:
`ui (JavaFX) → servicio → persistencia → modelo`.

## Ejecutar

Requisitos: **Java 17** y **Maven 3.9**.

### Opción recomendada (sin advertencias)

```bash
mvn clean javafx:run
```

### Desde IntelliJ IDEA

- Usa la configuración compartida **"Tienda Virtual (javafx:run)"** (carpeta
  `.run/`), que lanza la app por Maven con JavaFX en el module path.
- También puedes ejecutar la clase `com.tienda.Principal` directamente: es un
  lanzador plano (`Application.launch(AppTienda.class, args)`) que funciona
  con JavaFX en el classpath. En ese modo JavaFX imprime una advertencia
  cosmética ("Unsupported JavaFX configuration: classes were loaded from
  'unnamed module'"); la aplicación funciona igual.
- Ejecutar `AppTienda` directamente desde el IDE **no funciona**: la JVM
  exige los componentes de JavaFX en el module path y responde
  "faltan los componentes de JavaFX runtime".

## Pruebas

```bash
mvn test
```

Los tests usan `data/test-*.json` y limpian sus archivos al terminar.

## Cuentas demo (datos sembrados al primer arranque)

| Usuario  | Clave     | Rol           |
|----------|-----------|---------------|
| admin    | admin123  | ADMINISTRADOR |
| cliente  | cli123    | CLIENTE       |
| vendedor | ven123    | VENDEDOR      |

## Estructura

```
src/main/java/com/tienda/
├── modelo/        POJOs puros (Usuario, Producto, Categoria, ...)
├── persistencia/  Repositorios sobre JSON (Gson)
├── servicio/      Lógica de negocio (auth, productos, categorías)
├── util/          Hash de claves, IDs, paginación
├── estructuras/   PilaNavegacion (y otras estructuras didácticas)
└── ui/            Vistas JavaFX programáticas (sin FXML)
```

Los datos viven en `data/*.json` (excluidos del repositorio); las imágenes
de producto irán a `assets/images/`.
