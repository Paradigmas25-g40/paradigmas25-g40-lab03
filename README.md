# SparkFeedFetcher - Laboratorio 3 Paradigmas

## Configuración del entorno y ejecución

Para correr el laboratorio necesitas tener instalado Java (recomendamos Java 17) y Apache Spark (nosotros usamos Spark 4.0.0). También hace falta la librería JSON en la carpeta `lib/`.

**¿Cómo se corre?**
1. Cloná el repo y asegurate de tener Spark descargado (no hace falta moverlo, solo tener la ruta bien puesta en el Makefile).
2. Ejecutá en la terminal:
   ```bash
   make
   ```
   Esto compila y ejecuta todo de una. Si querés solo compilar: `make build`. Si ya compilaste y solo querés correr: `make run`.

**¿Qué resultado deberías ver?**
- El programa va a leer el archivo `subscriptions.json`, descargar todos los feeds RSS, procesar los artículos y mostrar por pantalla una lista de entidades nombradas con la cantidad de veces que aparecen en todos los artículos de todos los feeds.
- Si algún feed no responde, simplemente lo saltea y sigue con el resto.

## Decisiones de diseño
- Decidimos usar la heurística simple para entidades nombradas porque es rápida y fácil de paralelizar. Si quisiéramos usar algo más complejo (como un modelo de ML), Spark igual nos serviría para no morir en el intento.
- El código está pensado para que si mañana hay que cambiar la heurística o agregar más feeds, no haya que reescribir todo.
- Usamos un Makefile para que no tengas que andar escribiendo comandos largos cada vez que quieras compilar o correr el programa.

## Conceptos importantes

1. **Describa el flujo de la aplicación**
   - Primero, el programa lee el archivo `subscriptions.json` y arma una lista con todas las URLs de los feeds RSS.
   - Después, reparte esas URLs entre los workers de Spark, que se encargan de descargar y parsear cada feed en paralelo.
   - Cada artículo que se encuentra se procesa también en paralelo para extraer las entidades nombradas.
   - Al final, Spark junta todas las entidades encontradas y cuenta cuántas veces aparece cada una, mostrando el resultado por pantalla.
   - Básicamente, todo lo que se puede hacer en paralelo, se hace en paralelo.

2. **¿Por qué se decide usar Apache Spark para este proyecto?**
   - Porque si tenés que procesar muchos feeds y muchos artículos, hacerlo de a uno sería lentísimo. Spark te permite repartir el trabajo y aprovechar todos los núcleos de tu compu (o incluso varias máquinas si tenés un clúster). Así, el programa escala mucho mejor y no se vuelve un dolor de cabeza si la cantidad de datos crece.

3. **Liste las principales ventajas y desventajas que encontró al utilizar Spark.**
   - **Ventajas:**
     - Es súper fácil paralelizar tareas, casi sin cambiar mucho el código.
     - Si mañana tenés un clúster, el mismo código corre igual.
     - Maneja bien los errores y no se cae todo si falla un feed.
   - **Desventajas:**
     - Hay que instalar Spark y tener cuidado con las versiones de Java.
     - El arranque inicial de Spark puede ser un poco lento para cosas chicas.
     - Si nunca usaste Spark, la curva de aprendizaje puede ser un poco empinada.

4. **¿Cómo se aplica el concepto de inversión de control en este laboratorio?**
   - Acá, en vez de controlar nosotros el orden y la ejecución de cada tarea, le delegamos a Spark el manejo de cómo y cuándo se ejecuta cada parte. Nosotros solo decimos "qué" queremos hacer (por ejemplo, procesar cada feed), y Spark decide "cómo" repartirlo entre los workers. Así, el control del flujo de ejecución ya no está 100% en nuestras manos.

5. **¿Considera que Spark requiere que el código original tenga una integración tight vs loose coupling?**
   - Spark se lleva mejor con código que tenga bajo acoplamiento (loose coupling), porque así es más fácil paralelizar partes independientes. Si todo está muy atado y mezclado, cuesta más dividir el trabajo. Por suerte, nuestro diseño ya venía bastante modular, así que fue fácil adaptarlo.

6. **¿El uso de Spark afectó la estructura de su código original?**
   - No tuvimos que cambiar mucho. Solo agregamos la parte de Spark para paralelizar, pero las clases y métodos principales quedaron igual. Lo que sí, ahora el flujo principal usa RDDs y funciones de Spark en vez de bucles for tradicionales. Pero la lógica de parsear feeds, extraer entidades, etc., sigue igual que antes.
