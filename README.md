# BlackLine Deobfuscator

[![Coded with Claude Opus 5.5](https://img.shields.io/badge/Coded_with-Claude_Opus_5.5-F97316?style=flat-square&logo=anthropic&logoColor=white)](https://claude.ai/)

Деобфускатор классов из jar, обфусцированных, о боже BlackLine. Написан клод попусом 5.5 за 14 минут.

## Требования

- JDK 17+;
- Gradle 8+;

## Сборка

```powershell
gradle clean build
```

Готовый файл:

```text
build/libs/blackline-deobfuscator-1.0.0.jar
```

Если Gradle не установлен:

```powershell
powershell -ExecutionPolicy Bypass -File .\build.ps1
```

## Использование

```powershell
java -Xmx4G -jar build/libs/blackline-deobfuscator-1.0.0.jar input.jar output.jar
```

Если `output.jar` не указать, рядом с исходным файлом появится `<имя>-deobfuscated.jar`.

## Пример

```powershell
java -Xmx4G -jar build/libs/blackline-deobfuscator-1.0.0.jar `
    ..\outt\blackLine.jar `
    build\blackLine-deobfuscated.jar
```

После обработки:

```powershell
java -Xmx4G -jar build\blackLine-deobfuscated.jar
```

## Ограничения
Исходные случайно переименованные имена классов и методов автоматически не восстанавливаются.
