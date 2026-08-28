# SoarClient-Fork

SoarClient-Fork is an unofficial SoarClient fork for Minecraft 1.21.11. See the
[original SoarClient repository](https://github.com/Soar-Client/SoarClient) for the
upstream project.

Using or modifying this client means accepting the terms of the [MIT License](LICENSE).
If you publish a video or a modified build, please link back to this repository.

## Requirements

- Java 21
- Git

## Build

Clone the repository, then run a clean Gradle build:

```bash
git clone https://github.com/xiaoyuyuaa/SoarClient-fork.git
cd SoarClient-fork
chmod +x gradlew
./gradlew clean build
```

On Windows, use `gradlew.bat clean build` instead. The distributable mod is written
to `build/libs/soarclient-fork-<version>.jar`; the similarly named `-sources.jar`
is only for source browsing.

## Launch a development client

Run the Fabric development client from the repository:

```bash
./gradlew runClient
```

On Windows, use `gradlew.bat runClient`. The first launch downloads Minecraft and
Fabric dependencies. Runtime files, logs, saves, and options are stored in `run/`.
Stop the client normally from Minecraft or press Ctrl+C in the Gradle terminal.

To launch the release JAR in a normal Minecraft installation:

1. Install Fabric Loader 0.19.3 for Minecraft 1.21.11.
2. Put `soarclient-fork-<version>.jar` and Fabric API 0.141.6+1.21.11 in the
   installation's `mods` directory.
3. Start the Minecraft 1.21.11 Fabric profile with Java 21.

ViaFabricPlus and the client's native runtime libraries are bundled in the mod JAR.

## Release

1. Update `mod_version` in `gradle.properties`.
2. Run `./gradlew clean build`, then `./gradlew runClient` and verify the title screen.
3. Merge the change to `main`.
4. Create and publish a GitHub release targeting `main`, using the same version as
   the tag (for example, `8.0.0`).

The release workflow builds with Java 21, uploads the build artifacts, attaches the
distributable JAR to the GitHub release, and updates the configured website repository
when `TARGET_REPO_TOKEN` is available.
