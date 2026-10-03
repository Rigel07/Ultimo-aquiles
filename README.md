# 🗿 Achilles — mod para Minecraft Forge 1.20.1

Estatuas de un antiguo guerrero (Aquiles) repartidas por el mundo, con una **zona sagrada** a su alrededor,
y un **mini Aquiles** adorable y torpe que intenta ser el héroe de la leyenda.

## Las estatuas

- **Aparecen solas** en el Overworld: una por región de 16×16 chunks, siempre en la zona central de la región.
  Así dos estatuas quedan separadas **más de ~150 bloques**, y como el radio máximo es 58 bloques, **las zonas nunca chocan**,
  suban los niveles que suban. Cada estatua se genera sobre una plataforma de ladrillos con pilares rotos y 1-2 mini Aquiles de guardia.
- **Indestructibles**: ni jugadores (ni en creativo), ni explosiones, ni Wither/Dragón, ni pistones.
- **Zona de inmunidad** (radio base 8 bloques):
  - Los mobs hostiles no pueden aparecer dentro, son empujados fuera al acercarse y los que se cuelan se desvanecen. Los jefes solo son repelidos.
  - Los jugadores dentro no reciben daño (salvo vacío y `/kill`) y recuperan vida cada segundo.
- **Propiedad**: el primer jugador que interactúa (clic derecho) con una estatua sin dueño la reclama y gana **1 punto**.
  Mientras no tiene dueño, protege y cura a todo el mundo. Cuando tiene dueño, **solo entran él y los jugadores a los que dé permiso**;
  el resto es expulsado del área. Los administradores en creativo (permiso 2) pueden entrar.
- **Mejoras** (clic derecho como propietario → pantalla): cada punto se invierte en **Radio** o en **Curación**, hasta nivel 100 en cada una, por estatua.
  Los puntos son del jugador y se reparten entre todas sus estatuas.

| | Nivel 0 | Nivel 100 |
|---|---|---|
| Radio | 8 bloques | 58 bloques (+0,5 por nivel) |
| Curación | 0,5 vida/s | 10,5 vida/s (+0,1 por nivel) |

Extras de curación: nivel 50+ limpia efectos negativos; nivel 100 también sacia el hambre.
Los números están en `data/StatueStats.java`.

En la pantalla de la estatua también se dan o quitan permisos escribiendo el nombre de un jugador (hasta 20).

## El mini Aquiles

Guerrero diminuto con casco de cresta roja, peto de bronce, grebas, escudo, espadita y capa.
Practica con la espada, **se tropieza y se queda tumbado**, y se cree el héroe: ataca monstruos (menos creepers)… aunque pega con cosquillas.
Sonidos propios (¡piii!, ¡eek!, tropiezo, clang de juguete…) sintetizados en `tools/generate_assets.py`.
Aparece en llanuras, bosques, sabana y prados, y cerca de las estatuas. Huevo generador en la pestaña creativa.

## Compilar

Requisitos: **JDK 17**, y para generar assets **Python 3 + Pillow + numpy + ffmpeg** (los assets ya vienen generados en el repo;
solo se vuelven a generar si borras `src/main/resources/assets`).

1. Copia `gradlew`, `gradlew.bat` y `gradle/wrapper/gradle-wrapper.jar` del
   [MDK oficial de Forge 1.20.1](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.20.1.html), o usa
   el workflow de GitHub Actions incluido (`.github/workflows/build.yml`), que compila el jar sin wrapper.
2. `./gradlew build` → jar en `build/libs/`. Para probar: `./gradlew runClient`.

## Administradores

La Estatua de Aquiles está en la pestaña creativa para colocarla a mano (2 de alto, mira hacia ti al colocarla).

## Licencia

MIT.
