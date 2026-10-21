# The Stalker — mod de terror para Minecraft (Forge 1.20.1)

Una entidad alta y delgada que te acecha, con animaciones propias (GeckoLib):
idle, caminar, correr, quedarse mirándote (stare) y un ataque-susto.

## Comportamiento
1. **Observación**: aparece de noche o en zonas oscuras, detrás de ti (~26 bloques) y te sigue a distancia. Si te acercas, desaparece.
2. **Agresivo** (tras ~60 s): se acerca cada vez que NO lo miras (corre si está lejos).
3. **Si lo miras**, se congela y suena un latido. Si lo miras ~7 s seguidos, se esfuma.
4. **Si te alcanza**: grito, 3 corazones de daño, ceguera y oscuridad.
5. Si lo golpeas, se esfuma (y puede volver más tarde). No se puede matar a golpes.
6. Huevo generador en la pestaña de creativo "Spawn Eggs" para probarlo.

Todos los números están como constantes al inicio de `StalkGoal.java` y `StalkerSpawner.java`.

## Requisitos para jugarlo
- Minecraft **1.20.1** con **Forge 47.x**
- **GeckoLib 4.4.x** para Forge 1.20.1 (CurseForge / Modrinth) en la carpeta `mods`

## Cómo obtener el .jar (elige una)

### Opción A — GitHub (no instalas nada)
1. Crea un repositorio en GitHub y sube el contenido de esta carpeta.
2. Ve a la pestaña **Actions** → se ejecuta "Build mod" (o dale a *Run workflow*).
3. Al terminar, descarga el artefacto **stalker-mod** (contiene el .jar).

### Opción B — En tu PC
1. Instala **JDK 17**.
2. Descarga el **Forge MDK 1.20.1-47.3.0** (files.minecraftforge.net) y copia de él a esta carpeta
   las carpetas `gradle/` y los archivos `gradlew` y `gradlew.bat`.
3. Ejecuta `./gradlew build` (Windows: `gradlew.bat build`).
4. El .jar queda en `build/libs/stalker-1.0.0.jar`.

Para probarlo en desarrollo: `./gradlew runClient`.

## Estructura
- `entity/StalkerEntity.java` — entidad + animaciones (GeckoLib)
- `entity/StalkGoal.java` — IA de acecho
- `StalkerSpawner.java` — aparición nocturna
- `assets/stalker/geo|animations|textures` — modelo, animaciones y textura (editables con Blockbench)
