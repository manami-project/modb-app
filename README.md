[![Tests](https://github.com/manami-project/modb-app/actions/workflows/tests.yml/badge.svg)](https://github.com/manami-project/modb-app/actions/workflows/tests.yml) [![codecov](https://codecov.io/gh/manami-project/modb-app/graph/badge.svg?token=66LR8JA8KE)](https://codecov.io/gh/manami-project/modb-app)
# modb-app

_[modb](https://github.com/manami-project?tab=repositories&q=modb&type=source)_ stands for _**M**anami **O**ffline **D**ata**B**ase_. The applications and libraries of this repository are used to create the [manami-project/anime-offline-database](https://github.com/manami-project/anime-offline-database).

* **analyzer:** Allows to review the entries of the dataset and create merge locks.
* **anidb:** Config, downloader and converter for anidb.net
* **anilist:** Config, downloader and converter for anilist.co
* **anime-planet:** Config, downloader and converter for anime-planet.com
* **anisearch:** Config, downloader and converter for anisearch.com
* **app:** The application that runs the crawlers, merges anime and updates the repository.
* **core:** Core functionality used by all other modules.
* **kitsu:** Config, downloader and converter for kitsu.app
* **lib:** A library that drives drives the applications "app" and "analyzer".
* **livechart:** Config, downloader and converter for livechart.me
* **myanimelist:** Config, downloader and converter for myanimelist.net
* **notify:** Config, downloader and converter for notify.moe
* **serde:** Serialization and deserialization of the finalized dataset files.
* **test:** All essential dependencies as well as some convenience functions and classes for creating tests.

## Documentation

* Downloading
  * [Download Control State (DCS)](docs/dcs.md)
  * [Data lifecycle](docs/data-lifecycle.md)
* Merging
  * [Merging](docs/merging.md)
  * [Merge locks](docs/merge-locks.md) 
  * [Reviewed isolated entries](docs/reviewed-isolated-entries.md)
* Terminology
  * [Terminology](docs/terminology.md)

## Requirements

* JDK/JVM 21 (LTS) or higher
* Linux/Unix system supporting `ifconfig`
* ipv6 based internet connection with SLAAC enabled

## Getting started

Setup is identical for app and analyzer.

### Setup configuration

Create a [configuration file](core/README.md#configuration-management).
Set all the properties from the "Configuration" section down below which don't offer a default value.

### Optional: Logback configuration

Optionally you can create a [logback configuration](https://logback.qos.ch/manual/configuration.html) to override the default setup.

### Start using IDE

Run `main()` in `io/github/manamiproject/modb/app/App.kt` of the `app` module or `io/github/manamiproject/modb/analyzer/Analyzer.kt` of the `analyzer` module with the following VM parameter:
* `-Djava.net.preferIPv6Addresses=true`
* `-Djava.net.preferIPv4Stack=false`

### Start using *.jar file

Run
* either `java -Djava.net.preferIPv6Addresses=true -Djava.net.preferIPv4Stack=false -jar modb-app.jar`
* or `java -Djava.net.preferIPv6Addresses=true -Djava.net.preferIPv4Stack=false -jar modb-analyzer.jar`

## Configuration

For more configuration options see the `README.md` files of the respective modules.

| parameter                                | type     | default                                                                     | description                                                                                                                                                                                               |
|------------------------------------------|----------|-----------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `modb.app.downloadsDirectory`            | `String` | -                                                                           | Root directory in which the raw files and converted files are stored.                                                                                                                                     |
| `modb.app.outputDirectory`               | `String` | -                                                                           | Target output directory. Normally this should be the directory in which you cloned the [anime-offline-database](https://github.com/manami-project/anime-offline-database)                                 |
| `modb.app.downloadControlStateDirectory` | `String` | -                                                                           | Root directory of download control state files.                                                                                                                                                           |
| `modb.app.logFileDirectory`              | `String` | A directory called `logs` within the working directory of the current week. | Defines the directory in which the logs saved.                                                                                                                                                            |
| `modb.app.keepDownloadDirectories`       | `Long`   | `1`                                                                         | Number of download directories to keep. Download directories contain both raw data and conv files (intermediate format). Default is `1` which means that only the most recent download directory is kept. |