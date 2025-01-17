# modb-serde

## What does this lib do?

This lib can serialize and deserialize (serde) both the anime dataset file as well as the files for the dead entries.
 
## Usage

Create an instance of the respective `JsonSerializer`:

```kotlin
// deserialize a the JSON String of the anime dataset file
val animeListDeserializer = AnimeListJsonStringDeserializer()

// deserialize a the JSON String of a dead entries file
val deadEntriesDeserializer = DeadEntriesJsonStringDeserializer()
```

Wrap the instance above in a `ExternalResourceJsonDeserializer` to be able to deserialize a `URL` or a `Path`

```kotlin
val animeDatasetFileDeserializer = DefaultExternalResourceJsonDeserializer<Dataset>(deserializer = AnimeListJsonStringDeserializer())

val deadEntriesFileDeserializer = DefaultExternalResourceJsonDeserializer<DeadEntries>(deserializer = DeadEntriesJsonStringDeserializer())
```

Now you can either deserialize the anime dataset file or a dead entries file by using a `URL` or a `Path`.
The `DefaultExternalResourceJsonDeserializer` can also handle zipped files, but the zip file must only contain a single JSON file.

*Example:*

```kotlin
val deserializer = DefaultExternalResourceJsonDeserializer<Dataset>(deserializer = AnimeListJsonStringDeserializer())
val dataset: Dataset = deserializer.deserialize(URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/anime-offline-database.json").toURL())
val allAnime = dataset.data
```