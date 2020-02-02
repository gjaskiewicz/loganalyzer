# Log Analyzer for Credit Suisse
Log analyzer

# Development
Project is developed with Eclipse with Gradle plugin. In order to work on it import it into Eclipse.

# Building
In order to build invoke gradle wrapper.
```
./gradlew jar
```
As output there is produced runnable jar.

# Running
In order to invoke log analyzer run.
```
  java -jar build/lib/logalert.jar [FILE]
```

To get help on command line args invoke:
```
  java -jar build/lib/logalert.jar -h
```
