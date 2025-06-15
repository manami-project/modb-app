# modb-serde

## What does this lib do?

This lib can serialize and deserialize (serde) both the anime dataset file as well as the files for the dead entries.
 
## Usage

### Serialization

Create an instance of the respective `JsonSerializer`:

`JsonSerializer` comes with two implementations:
* `DatasetJsonSerializer`
* `DeadEntriesJsonSerializer`

It allows you to create the JSON which is used in "anime-offline-database" project and supports both "pretty-print" as well as "minified" JSON.

A different interface is `JsonLinesSerializer` with a single implementation:
* `JsonLinesSerializer`

This will create a [JSON lines](https://jsonlines.org) where the first line is meta data followed by one serialized `Anime` per line.

### Deserialization

There are different implementations available. Use the class that fits your needs. Dead entries files are not available
in [JSON lines](https://jsonlines.org) format.

| name                                          | required data type | deserializes to |
|-----------------------------------------------|--------------------|-----------------|
| `AnimeFromJsonInputStreamDeserializer`        | JSON               | `Flow<Anime>`   |
| `AnimeFromJsonLinesInputStreamDeserializer`   | JSON lines         | `Flow<Anime>`   |
| `DatasetFromJsonInputStreamDeserializer`      | JSON               | `Dataset`       |
| `DatasetFromJsonLinesInputStreamDeserializer` | JSON lines         | `Dataset`       |
| `DeadEntriesFromInputStreamDeserializer`      | JSON               | `Dataset`       |

All of the above `Deserialzer` implementations require an `InputStream` as source.

# Deserialize files and HTTP responses

Wrap the instances above in either `FromRegularFileDeserializer` for deserialing files or `FromUrlDeserializer` if you want
to download a file and directly deserialze it. Both implementations can handle raw text files for JSON as well as Zstandard compressed files.

**Example:**

```kotlin
val animeDatasetDeserializer = FromRegularFileDeserializer<Flow<Anime>>(deserializer = AnimeFromJsonLinesInputStreamDeserializer.instance)
val anime: Flow<Anime> = animeDatasetDeserializer.deserialize(Path("anime.zst"))


val deadEntriesDeserializer = FromUrlDeserializer<DeadEntries>(deserializer = DeadEntriesFromInputStreamDeserializer.instance)
val deadEntries: DeadEntries = deadEntriesDeserializer.deserialize(URI("http://localhost/dead-entries.json").toURL())
```