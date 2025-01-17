# modb-core

_[modb](https://github.com/manami-project?tab=repositories&q=modb&type=source)_ stands for _**M**anami **O**ffline **D**ata**B**ase_. Repositories prefixed with this acronym are used to create the [manami-project/anime-offline-database](https://github.com/manami-project/anime-offline-database).

## What does this lib do?

This lib is the base for every specific meta data provider module. It contains the API for downloaders and converters, defines the anime model and provides basic functionality.

## Features

This lib contains the following features.

### Interfaces

* Interfaces for `MetaDataProviderConfig`, `Downloader`, `AnimeConverter`, `PathAnimeConverter`, `DataExtractor`, `PathDataExtractor`, `HttpClient` which define the standard API.

### Converter
 
* Default implementation for `PathAnimeConverter`. Takes an `AnimeConverter` as delegate and can convert files and directories.

### DataExtractor

* Default implementation for `PathDataExtractor`. Takes an `DataExtractor` as delegate and can convert files and directories.

### HttpClient

* Leightweight interface for creating HTTP calls including helpers for creating headers and setting user agents

#### DefaultHttpClient

* Implementation of `HttpClient`
* A client for HTTP requests based on [okhttp](https://github.com/square/okhttp)
* Offers retry behavior which can be individualized

### Configuration management

Configurations can either be set:
* by adding a `config.toml` file to the classpath
* by adding a `config.toml` file to the same directory
* setting `modb.core.config.location` environment variable containing the path to a local `config.toml` file
* setting `modb.core.config.location` system property containing the path to a local `config.toml` file
* by setting environment variables with the given keys
* by setting system properties with the given keys

Evaluation priority is as follows (higher number means higher priority):
1. classpath
2. same directory
3. file via environment variable
4. file via system property
5. property set by environment variable
6. property set by system property

Configuration properties can be injected by using delegated properties such as `StringPropertyDelegate`.

### Logger

* Delegate for [SLF4J](https://github.com/qos-ch/slf4j) logger
```kotlin
companion object {
    private val log by LoggerDelegate()
}
```
* Const `LOG_LEVEL_CONFIG_PROPERTY_NAME` for property name which lets you set the global log level

### Models
* For `Anime` and all underlying subtypes
    * Typealias for `Episodes`
    * Typealias for `Title`
    * Typealias for `Tag`
    * Enum `Anime.Type`
    * Enum `Anime.Status`
    * Type `AnimeSeason`
        * Typealias for `Year`
        * Type for `AnimeSeason.Season`
    * Type `Duration`
        * Typealias for `Seconds`
        * Enum `TimeUnit`
* Const `EMTPY` for empty `String`
* Const `LOCK_FILE_SUFFIX`  
* Const `YEAR_OF_THE_FIRST_ANIME` for year of the first anime
* Typealias for `RegularFile` as more specific version of `Path`
* Typealias for `Directory` as more specific version of `Path`
* Typealias for `FileSuffix`

### Json (de)serialization

* Object class for serializing/deserializing objects to/from JSON

### Extension and utility functions

| function                                | description                                                                                                                                    |
|-----------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------|
| `loadResource`                          | Conveniently load a file from `src/main/resources`                                                                                             |
| `resourceFileExists`                    | Checks if a file exists in `src/main/resources`                                                                                                |
| `random`                                | Pick a a random number from a given interval                                                                                                   |
| `excludeFromTestContext`                | Won't execute the code within during test execution                                                                                            |
| `Collection<T>.pickRandom()`            | Picks a random element of a `Collection`                                                                                                       |
| `Int.toAnimeId`                         | Converts an `Int` to an `AnimeId`                                                                                                              |
| `List<T>.createShuffledList()`          | Creates a new list from the given `List` and randomizes the order of elements                                                                  |
| `List<T>.containsExactlyInTheSameOrder` | Checks if a list contains the same elements in the same order as another list                                                                  |
| `OutputStream.write`                    | Writes a `String` to the `OutputStream` and also flushes the stream                                                                            |
| `Path.changeSuffix`                     | Changes the file suffix                                                                                                                        |
| `Path.regularFileExists`                | Checks if a given `Path` exists and is a file                                                                                                  |
| `Path.directoryExists`                  | Checks if a given `Path` exists and is a directory                                                                                             |
| `Path.readFile`                         | Read the content of a file into a `String`                                                                                                     |
| `Path.copyTo`                           | Copy file to file, directory to directory or a file into a directory                                                                           |
| `Path.fileName`                         | Filename as `String`                                                                                                                           |
| `Path.fileSuffix`                       | Returns the file suffix as `String`                                                                                                            |
| `Path.listRegularFiles`                 | Returns a list of files. Optionally with additional glob filter.                                                                               |
| `Path.createZipOf`                      | Creates a zip file containing one or multiple files.                                                                                           |
| `ByteArray.writeToFile`                 | Write `ByteArray` to file and optionally write a lock file as indications for other processes that the file is being written                   |
| `String.writeToFile`                    | Write `String` to file and optionally write a lock file as indications for other processes that the file is being written                      |
| `String.remove`                         | Remove sequence from a `String`                                                                                                                |
| `String.normalizeWhitespaces`           | Replaces multiple consecutive whitespaces with a single one. Replaces different kinds of whitespace with the default one and trims the string. |
| `String.normalize`                      | Replaces tabs, carriage return and line feed with whitespaces and additionally does the same as `String.normalizeWhitespaces`.                 |
| `String.eitherNullOrBlank`              | Returns true for `null`, empty strings and strings consisting only of non-visible characters.                                                  |
| `String.neitherNullNorBlank`            | Opposite of `String.eitherNullOrBlank`                                                                                                         |

## Configuration

| parameter                                        | type           | default | description                                                                                  |
|--------------------------------------------------|----------------|---------|----------------------------------------------------------------------------------------------|
| `modb.core.logging.logLevel`                     | `String`       | `INFO`  | Can be one of: `OFF`, `ERROR`, `WARN`, `INFO`, `DEBUG`, `TRACE`                              |
| `modb.core.httpclient.useragents.firefoxDesktop` | `List<String>` | -       | List of [user agents](https://www.whatismybrowser.com/guides/the-latest-user-agent/firefox). |
| `modb.core.httpclient.useragents.firefoxMobile`  | `List<String>` | -       | List of [user agents](https://www.whatismybrowser.com/guides/the-latest-user-agent/firefox). |
| `modb.core.httpclient.useragents.chromeDesktop`  | `List<String>` | -       | List of [user agents](https://www.whatismybrowser.com/guides/the-latest-user-agent/chrome).  |
| `modb.core.httpclient.useragents.chromeMobile`   | `List<String>` | -       | List of [user agents](https://www.whatismybrowser.com/guides/the-latest-user-agent/chrome).  |