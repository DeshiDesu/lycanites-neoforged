# Lycanites Mobs Neoforged

## Status

Lycanites Mobs Neoforged is an unofficial continuation and fork of Lycanites Mobs for NeoForge 1.21.1. It is not endorsed by the original creator or the previous port maintainer.

Upstream project information remains available at [lycanitesmobs.com](https://lycanitesmobs.com).

## Credits

- Lycanite — original Lycanites Mobs creator
- feetnuggets — NeoForge 1.21.1 port
- deshidesu — current fork maintainer

## Compatibility identity

This fork deliberately retains the compatibility-critical identities used by existing worlds and modpacks:

- mod ID `lycanitesmobs`
- registry and resource namespace `lycanitesmobs`
- Java package root `com.lycanitesmobs`
- configuration path `config/lycanitesmobs`

Existing entity, item, block, sound, effect, menu, recipe, tag, and data identifiers are also retained.

## Main fork change

The primary fork change optimizes structure-based spawn location searches while preserving their existing acceptance semantics. It:

- fixes the block/chunk range mismatch in structure lookup
- derives the locator radius in chunks using ceiling conversion
- supports an optional `structureSearchRadiusChunks` override
- supports `per_id` lookup mode
- supports combined `nearest_any` lookup mode
- deterministically deduplicates configured structures
- keeps `structureRange` as the final block-distance acceptance range

Example configuration:

```json
{
  "type": "structure",
  "structureRange": 256,
  "structureSearchRadiusChunks": 16,
  "structureSearchMode": "nearest_any",
  "structureIds": [
    "example:village_one",
    "example:village_two"
  ]
}
```

## Building

Java 21 and the included Gradle wrapper are required. Runtime packaging also requires a known-good upstream `lycanitesmobs-0.0.1.jar`. Supply its absolute path through the `upstreamLycanitesJar` property; local binary inputs belong outside the repository or in the ignored `local-artifacts` directory.

Run the tests on Windows:

```powershell
.\gradlew.bat clean test --console=plain
```

Build the canonical surgical runtime JAR:

```powershell
.\gradlew.bat neoforgedJar "-PupstreamLycanitesJar=C:\path\to\lycanitesmobs-0.0.1.jar" --console=plain
```

Verify complete unrelated-entry parity and runtime invariants:

```powershell
.\gradlew.bat verifyNeoforgedJar "-PupstreamLycanitesJar=C:\path\to\lycanitesmobs-0.0.1.jar" --console=plain
```

The only supported runtime artifact is:

```text
build/libs/lycanitesmobs-neoforged-0.0.1.jar
```

The ordinary hybrid `jar` task is disabled. `assemble` and `build` route through `neoforgedJar`, so release-style builds require the same explicit upstream JAR property. The `test` task does not launch Minecraft and does not require that binary input.

## Installation

- Replace the upstream JAR with `lycanitesmobs-neoforged-0.0.1.jar`.
- Never install both JARs together; both declare mod ID `lycanitesmobs`.
- Retain existing `config/lycanitesmobs` data when upgrading this fork.
- Old external configuration from incompatible Lycanites versions may need selective regeneration; preserve a backup and regenerate only incompatible files.

## Known build limitation

This repository is a hybrid of legacy source resources and precompiled classes. A full source-tree archive can select stale resources, including an obsolete creature class reference. The supported release task therefore starts from a verified upstream JAR, preserves every unrelated entry byte-for-byte by uncompressed SHA-256, and overlays only the optimized structure-location classes and deliberate NeoForge metadata update.

## License

This fork preserves the repository's existing [LICENSE](LICENSE) and upstream attribution. It does not change or replace those terms.
