# NeoForge 26.1.2.78 Port Design

## Goal

Port Camera Inertia from Forge for Minecraft 1.20.1 to NeoForge 26.1.2.78 for Minecraft 26.1.2 with minimal behavior changes.

## Scope

- Replace the ForgeGradle build with the official NeoForge 26.1.2 ModDevGradle layout.
- Target Minecraft `26.1.2`, NeoForge `26.1.2.78`, Java `25`, and a Gradle wrapper compatible with Java 25.
- Preserve the current mod id, package layout, resources, translations, shaders, config screens, and camera behavior wherever the target API still supports them.
- Move mod metadata from `META-INF/mods.toml` to NeoForge's `META-INF/neoforge.mods.toml` generation flow.
- Fix Java compile errors caused by package changes, event API changes, method renames, resource location changes, and rendering API changes.

## Approach

Use a thin in-place migration instead of recreating the project. The build files will be aligned with the official `MDK-26.1.2-ModDevGradle` template, then source imports and APIs will be updated based on compiler errors. This keeps the diff focused and avoids changing gameplay logic during the loader/version port.

## Testing

The repository has no dedicated automated tests. The verification target is `./gradlew.bat build`, which must compile the Java sources, process resources, and produce a NeoForge-compatible JAR. If runtime-only mixin target changes cannot be fully proven by compilation, they will be noted separately.
