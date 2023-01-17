# Ornithe Example Mod

## Setup

1. Edit build.gradle, settings.gradle and mod.json to suit your needs.
    * The "mixins" object can be removed from mod.json if you do not need to use mixins.
    * Please replace all occurences of "modid" with your own mod ID - sometimes, a different string may also suffice.
2. Run the following command:
```
./gradlew genSources
```
3. Depending on your IDE of choice, run one of the following commands:
```
./gradlew idea
./gradlew eclipse
./gradlew vscode
```

## License

This template is available under the CC0 license. Feel free to learn from it and incorporate it in your own projects.
