# SkinPatch

This mod patches the calls to the Mojang API, which have moved since 1.7, making vanilla calls invalid, and thus making any skin or cape request invalid. This mod transforms the calls to the new ones, allowing skins and capes to display again

## Building

Run the following command:
```
./gradlew build
```

The built jar should be present in ./build/libs

## Contributing

1. Run the following command:
```
./gradlew genSources
```
2. Depending on your IDE of choice, run one of the following commands:
```
./gradlew idea
./gradlew eclipse
./gradlew vscode
```
