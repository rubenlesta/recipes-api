# Recipes API

Servicio REST que busca por nombre una receta en Spoonacular y devuelve la primera coincidencia, con su nombre, tiempo de preparación, porciones, ingredientes, instrucciones e imagen.

## Requisitos

- Java 21.
- Maven o el wrapper incluido (`./mvnw`).
- Git.
- Docker con Docker Compose, opcional.
- Una clave gratuita de API de Spoonacular: <https://spoonacular.com/food-api>.

## Instalación

```bash
git clone https://github.com/rubenlesta/recipes-api.git
cd recipes-api
```

## Configuración de la clave

La aplicación obtiene la clave de la variable de entorno `SPOONACULAR_API_KEY`.

El archivo `.env` no está en el repositorio y está ignorado por Git. Docker Compose lo requiere: si no existe, `docker compose up` falla con un error de archivo no encontrado. Créalo antes de iniciar el contenedor:

```bash
cp .env.example .env
```

Edita `.env` y sustituye el valor de ejemplo por tu clave real, sin comillas ni espacios:

```text
SPOONACULAR_API_KEY=tu_clave_real
```

Spring Boot no carga `.env` por sí solo. Para ejecutar la aplicación localmente, carga sus variables en la terminal:

```bash
set -a; source .env; set +a
```

No subas nunca la clave al repositorio.

## Ejecución local

Con la variable cargada en la terminal:

```bash
./mvnw spring-boot:run
```

La aplicación escucha en el puerto `8080`.

## Ejecución con Docker

Con `.env` creado:

```bash
docker compose up --build -d
docker compose logs -f app
docker compose down
```

El contenedor ejecuta la aplicación con un usuario sin privilegios. Docker Compose inyecta la clave al iniciar el contenedor; no se guarda en la imagen.

## Uso de la API

```http
GET /api/recipes?name=<nombre>
```

Ejemplo:

```bash
curl -s "http://localhost:8080/api/recipes?name=pasta"
```

La respuesta contiene los campos `name`, `preparationTimeMinutes`, `servings`, `ingredients`, `instructions` e `imageUrl`. Este ejemplo resume la respuesta de `Ratatouille Pasta` del fixture de Spoonacular incluido en los tests:

```json
{
  "name": "Ratatouille Pasta",
  "preparationTimeMinutes": 45,
  "servings": 4,
  "ingredients": [
    "1 large Yellow Onion, diced",
    "Olive oil for sauteing",
    "Sea salt and fresh pepper",
    "4 cloves Garlic, minced",
    "3 cups Zucchini, peeled, thinly sliced(⅛ of an inch), cut into bite size pieces",
    "3 cups Eggplant, peeled, thinly sliced(⅛ of an inch), cut into bite size pieces",
    "1 teaspoon Italian Seasoning",
    "14 ounces can of Artichokes, rinsed, chopped into bite size pieces",
    "14 ounces can Diced Tomatoes",
    "8 ounces Spaghetti (like Barila Whole Grain)",
    "Extra Virgin Olive oil, for serving",
    "Fresh grated Parmesan or Romano, for serving"
  ],
  "instructions": [
    "Saute the yellow onion in a large skillet or saute pan with enough Olive oil to coat the bottom of the pan, over medium-low heat until tender and translucent. Season with salt and pepper.",
    "Add garlic, saute until fragrant.",
    "Add another tablespoon of Olive oil and the zucchini, eggplant and Italian seasoning. Continue sauteing until the zucchini and eggplant start to break down and become limp and tender.",
    "Add the artichokes and tomatoes. Season with salt and pepper. Bring to a simmer for ten - fifteen minutes. Taste and re-season if needed.",
    "Meanwhile cook the spaghetti to al dente according to package directions.",
    "Toss with pasta and serve with a drizzle of extra-virgin Olive oil and Parmesan cheese."
  ],
  "imageUrl": "https://img.spoonacular.com/recipes/657933-312x231.jpg"
}
```

## Errores

Todos los errores usan `ProblemDetail` con el tipo de contenido `application/problem+json`.

| Código | Causa |
|---|---|
| 400 | Falta el parámetro `name` o está en blanco. |
| 404 | Spoonacular no devuelve resultados para el nombre buscado. |
| 502 | Spoonacular falla o no responde. |
| 500 | Se produce un error interno inesperado. |

Ejemplo de cuerpo para una búsqueda sin resultados de `xyz`:

```json
{
  "type": "about:blank",
  "title": "Receta no encontrada",
  "status": 404,
  "detail": "No se encontró ninguna receta para: xyz"
}
```

## Documentación Swagger

- Interfaz Swagger UI: <http://localhost:8080/swagger-ui.html>
- Especificación OpenAPI: <http://localhost:8080/v3/api-docs>

## Caché y cuota de Spoonacular

La caché usa Caffeine en memoria. Normaliza la clave de búsqueda pasando el nombre a minúsculas y eliminando espacios sobrantes, guarda hasta 100 entradas y cada entrada vive 10 minutos. Los errores y las búsquedas sin resultados no se cachean.

Spoonacular mide su cuota en puntos diarios. La caché ayuda a reducir el consumo de esos puntos; consulta el panel de Spoonacular para conocer el límite vigente.

## Tests

```bash
./mvnw -B test
```

La suite contiene 33 tests. Cubre el cliente con un servidor HTTP simulado, el servicio, el controlador, el manejo de errores, la caché y la documentación OpenAPI. No se realizan llamadas reales a Spoonacular.

## Estructura del proyecto

```text
src/main/java/com/aldaba/recipes/
├── client/      Cliente de Spoonacular y DTO de su respuesta.
├── config/      Configuración de caché, HTTP y OpenAPI.
├── controller/  Endpoint HTTP para buscar recetas.
├── dto/         DTO de respuesta de la API.
├── exception/   Excepciones y manejador global de errores.
└── service/     Búsqueda, transformación y caché de recetas.
src/main/resources/  Configuración de Spring Boot.
src/test/java/       Tests del cliente, servicio, controlador, errores y OpenAPI.
src/test/resources/  Respuesta de Spoonacular usada como fixture.
```

## Decisiones técnicas

- Arquitectura en capas: `controller`, `service` y `client`.
- El DTO de respuesta de la API está separado del DTO que mapea el JSON externo de Spoonacular.
- Una única llamada al endpoint `complexSearch` solicita la primera coincidencia con `addRecipeInformation`, `fillIngredients` y `addRecipeInstructions`.
- La clave se envía en la cabecera `x-api-key`, nunca en la URL.
- Los errores devuelven mensajes genéricos al cliente; los detalles del proveedor y los errores internos se registran en los logs.
- El flujo de ramas usa `main`, `develop` y ramas `feature/*`; los commits siguen Conventional Commits.

## Limitaciones y mejoras futuras

- La caché es local a cada instancia.
- Una clave de API vacía o una cuota agotada de Spoonacular también produce un `502`.
- El servicio no incluye healthcheck ni métricas.
