# Simulación de juego de rol en Scala

[![🇬🇧 English Translation Available](https://img.shields.io/badge/🇬🇧-English%20translation%20available-blue)](README.es.md)

Éste fue el segundo trabajo práctico que hice en la materia de Técnicas Avanzadas de Programación en la Universidad Tecnológica Nacional, Facultad Regional Buenos Aires, primer cuatrimestre de 2024.

Aunque originalmente se trataba de un trabajo grupal para este punto del cuatrimestre mis compañeros ya se habían dado de baja de la materia. Lo trabajé con la supervisión de uno de los ayudantes.

Este proyecto me resultó muy disfrutable! Desde el principio me propuse escribirlo sin declarar una sola variable mutable. La implementación pasó por múltiples iteraciones, a través de las cuales pude ver cómo mi código se volvía cada vez más simple. También adopté la forma monádica de pensar de Scala, lo que me prepararía para adoptar el sistema de tipos de Rust sin muchas dificultades.

---

## TP Grupal - Funcional - 1C2024

### Introducción

Estamos modelando un juego de rol en el que héroes se agrupan en equipos para realizar distintas misiones. Nuestro objetivo es poder determinar el resultado obtenido al mandar a un equipo a realizar una misión, para evitar enviarlo a una muerte segura.

**IMPORTANTE:** Este trabajo práctico debe implementarse de manera que se apliquen los principios del paradigma híbrido objeto-funcional enseñados en clase. No alcanza con hacer que el código funcione en objetos, hay que aprovechar las herramientas funcionales, poder justificar las decisiones de diseño y elegir el modo y lugar para usar conceptos de un paradigma u otro.

Se tendrán en cuenta para la corrección los siguientes aspectos:

* Uso de Inmutabilidad vs. Mutabilidad
* Uso de Polimorfismo paramétrico (Pattern Matching) vs. Polimorfismo Ad-Hoc
* Aprovechamiento del polimorfismo entre objetos y funciones
* Uso adecuado de herramientas funcionales
* Cualidades de Software
* Diseño de interfaces y elección de tipos

---

## Descripción General del Dominio

### Héroes

Son los protagonistas del juego. Llevan un inventario y pueden o no desempeñar un trabajo. Las características principales de los héroes están representadas como una serie de valores numéricos que llamaremos Stats.

#### Stats

HP, fuerza, velocidad e inteligencia. Cada héroe posee un valor base innato para cada una de estas características que puede variar de persona en persona. Además, el trabajo y los ítems que cada individuo lleva equipado pueden afectar sus stats de diversas formas. Los stats nunca pueden tener valores negativos; si algo redujera un stat a un número menor a 1, el valor de ese stat debe considerarse 1 (esto sólo aplica a los stats finales).

#### Trabajo

Un trabajo es una especialización que algunos aventureros eligen desempeñar. El trabajo que un héroe elige afecta sus stats y le permite tener acceso a ítems y actividades especiales. Cada trabajo tiene también un stat principal, que impacta en el ejercicio del mismo. Si bien cada héroe puede tener un único trabajo asignado a la vez, este debe poder cambiarse en cualquier momento por cualquier otro (o ningún) trabajo.

Algunos ejemplos de trabajo son:

* **Guerrero:** +10 hp, +15 fuerza, -10 inteligencia. Stat principal: Fuerza.
* **Mago:** +20 inteligencia, -20 fuerza. Stat principal: Inteligencia.
* **Ladrón:** +10 velocidad, -5 hp. Stat principal: Velocidad.

#### Inventario

Para realizar sus misiones con éxito, los héroes se equipan con toda clase de herramientas y armaduras especiales que los protegen y ayudan. Cada individuo puede llevar:

* Un único sombrero o casco en su cabeza.
* Una armadura o vestido en el torso.
* Un arma o escudo en cada mano (algunas armas requieren ambas manos).
* Cualquier cantidad de talismanes.

Cada ítem puede tener sus propias restricciones para equiparlo y modifica los stats finales de quién lo lleve. Un héroe debe poder, en cualquier momento, equiparse con un ítem para el cual cumple las restricciones. Si un héroe se equipa con un ítem para una parte del cuerpo que ya tiene ocupada, el ítem anterior se descarta.

Algunos ejemplos de ítems son:

* **Casco Vikingo:** +10 hp, sólo lo pueden usar héroes con fuerza base > 30. (Cabeza)
* **Palito mágico:** +20 inteligencia, sólo lo pueden usar magos (o ladrones con más de 30 de inteligencia base). (Una mano)
* **Armadura Elegante-Sport:** +30 velocidad, -30 hp. (Armadura)
* **Arco Viejo:** +2 fuerza. (Dos manos)
* **Escudo Anti-Robo:** +20 hp. No pueden equiparlo los ladrones ni nadie con menos de 20 de fuerza base. (Una mano)
* **Talismán de Dedicación:** Todos los stats se incrementan 10% del valor del stat principal del trabajo.
* **Talismán del Minimalismo:** +50 hp. -10 hp por cada otro ítem equipado.
* **Vincha del búfalo de agua:** Si el héroe tiene más fuerza que inteligencia, +30 a la inteligencia; de lo contrario +10 a todos los stats menos la inteligencia. Sólo lo pueden equipar los héroes sin trabajo. (Sombrero)
* **Talismán maldito:** Todos los stats son 1.
* **Espada de la Vida:** Hace que la fuerza del héroe sea igual a su hp.

### Equipo

Ningún hombre es una isla. Los aventureros a menudo se agrupan en equipos para aumentar sus chances de tener éxito durante una misión. Un equipo es un grupo de héroes que trabajan juntos y comparten las ganancias de las misiones. Cada Equipo tiene un “pozo común” de oro que representa sus ganancias y un nombre de fantasía, como “Los Patos Salvajes”.

### Tareas y Misiones

Cómo no podía ser de otra forma, los aventureros tienen que ganarse la vida realizando misiones a cambio de tesoros. Las misiones se componen de un conjunto de tareas que deben llevarse a cabo para cumplirlas y una recompensa para el equipo que lo haga.

Las tareas pueden ser actividades de lo más variadas. Cada tarea debe ser realizada por un único héroe del equipo, el cual puede resultar afectado de alguna manera al realizarla.

Por ejemplo:

* **"Pelear contra monstruo"** reduce la vida de cualquier héroe con fuerza < 20 en 10hp.
* **"Forzar puerta"** no le hace nada a los magos ni a los ladrones, pero sube la fuerza de todos los demás en 1 y baja en 5 su hp.
* **"Robar talismán"** le agrega un talismán al héroe.

Sin embargo, no todas las tareas pueden ser hechas por cualquier equipo. De cada tarea se sabe también la “facilidad” con la que un héroe puede realizarla (ésta está representada por un número que, de ser positivo representa mayores chances, mientras que si es negativo indica mayor dificultad). Ojo! El cálculo de la facilidad puede variar de equipo en equipo. Algunos equipos simplemente no tienen lo que se necesita para que uno de sus miembros haga una tarea y, en esos casos, la facilidad no puede calcularse.

Por ejemplo:

* "Pelear contra monstruo" tiene una facilidad de 10 para cualquier héroe o 20 si el líder del equipo es un guerrero.
* "Forzar puerta" tiene facilidad igual a la inteligencia del héroe + 10 por cada ladrón en su equipo.
* "Robar talismán" tiene facilidad igual a la velocidad del héroe, pero no puede ser hecho por equipos cuyo líder no sea un ladrón.

### Recompensas

Las recompensas por llevar a cabo una misión pueden ser toda clase de cosas. Algunos ejemplos incluyen:

* Ganar oro para el pozo común
* Encontrar un ítem
* Incrementar los stats de los miembros del equipo que cumplan una condición
* Encontrar un nuevo héroe que se sume al equipo

---

Se pide implementar los siguientes casos de uso, acompañados de sus correspondientes tests y la documentación necesaria para explicar su diseño (la cual debe incluir, mínimo, un diagrama de clases):

### 1. Forjando un héroe

Modelar a los héroes, ítems y trabajos implementando todas las operaciones y validaciones que crean necesarias para manipularlos de forma consistente, de acuerdo a lo descrito anteriormente.

* Prevenir cualquier estado inválido.
* Elegir los tipos y representaciones más adecuados para presentar un modelo escalable y robusto basado en el paradigma híbrido objeto-funcional.
* Asegurarse de que un héroe pueda:
  - Obtener y alterar sus stats.
  - Equipar un ítem.
  - Cambiar de trabajo.

### 2. Hay equipo

Modelar los equipos de forma tal de que respeten la descripción dada previamente, proveyendo además las siguientes funcionalidades:

* **Mejor héroe según:** Dado un cuantificador de tipo `[Héroe => Int]` el equipo debe poder encontrar al miembro que obtenga el mayor valor para dicho cuantificador. Tener en cuenta que el equipo podría estar vacío.
* **Obtener ítem:** Cuando un equipo obtiene un ítem se lo da al héroe al que le produzca el mayor incremento en la main stat de su job. Si ninguno recibe nada positivo, se vende, incrementando el pozo común del equipo en una cantidad que depende del ítem.
* **Obtener miembro:** Permite que un nuevo héroe se una al equipo.
* **Reemplazar miembro:** Sustituye un héroe del equipo por otro.
* **Líder:** El líder de un equipo es el héroe con el mayor valor en su stat principal. En caso de que haya un empate, se considera que el equipo no tiene un líder claro.

### 3. Misiones

Modelar las misiones y permitir que los equipos de aventureros las realicen. Para esto, el equipo debe tratar de realizar cada tarea de la misión.

Cada tarea individual debe ser realizada por un único héroe (que debe ser aquel que tenga la mayor facilidad para realizarla). Al realizar una tarea los cambios que esta produce en el héroe deben aplicarse de inmediato (es decir, antes de pasar a la siguiente).

En caso de que ningún héroe pueda realizar una de las tareas la misión se considera Fallida. Todos los efectos de las tareas previamente realizadas se pierden y se debe informar el estado del equipo, junto con la tarea que no pudo ser resuelta.

En caso de éxito, se cobra la recompensa de la misión y se informa el estado final del equipo. Sólo se cobran las recompensas de las misiones realizadas con éxito.

### 4. La Taberna

Dado un tablón de anuncios con un conjunto de misiones, se pide:

* **Elegir Misión:** Elegir la mejor misión para un equipo, de acuerdo a un criterio `((Equipo,Equipo) => Boolean)` que, dados los estados resultantes de hacer que el equipo realice dos misiones retorna *true* si el resultado de la primera es mejor que el resultado de la segunda.

  Ejemplo: si el criterio fuese: `{(e1, e2) => e1.oro > e2.oro}` debería elegirse la misión que más oro le haría ganar al equipo en caso de realizarla.

  Elegir una misión para realizar no debe causar ningún cambio de estado en el equipo. Tener en cuenta que el equipo podría no ser capaz de realizar ninguna misión.

* **Entrenar:** Cuando un equipo entrena, intenta realizar todas las misiones, una por una, eligiendo la mejor misión para hacer a continuación. Cada misión se realiza luego de haber cobrado la recompensa de la anterior y el equipo no se detiene hasta haber finalizado todas las misiones o fallar una.
