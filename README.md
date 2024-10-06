[![Tests](https://github.com/manami-project/modb-app/actions/workflows/tests.yml/badge.svg)](https://github.com/manami-project/modb-app/actions/workflows/tests.yml) [![codecov](https://codecov.io/gh/manami-project/modb-app/graph/badge.svg?token=66LR8JA8KE)](https://codecov.io/gh/manami-project/modb-app)
# modb-app

## Documentation

* Downloading
  * [Download Control State (DCS)](docs/dcs.md)
* Merging
  * [Merging](docs/merging.md)
  * [Merge locks](docs/merge_locks.md) 
  * [Reviewed isolated entries](docs/reviewed-isolated-entries.md)

## Requirements

* JDK/JVM 21 (LTS) or higher
* Linux/Unix system supporting `ifconfig`
* ipv6 based internet connection with SLAAC enabled

## Getting started

### Setup configuration

Create a [configuration file](https://github.com/manami-project/modb-core#configuration-management).
Set all the properties from the "Configuration" section down below which don't offer a default value.

### Optional: Logback configuration

Optionally you can create a [logback configuration](https://logback.qos.ch/manual/configuration.html) to override the default setup.

### Start using IDE

Run `main()` in `io/github/manamiproject/modb/app/App.kt` of the `app` module with the following VM parameter:
* `-Djava.net.preferIPv6Addresses=true`
* `-Djava.net.preferIPv4Stack=false`

### Start using *.jar file

Run `java -Djava.net.preferIPv6Addresses=true -Djava.net.preferIPv4Stack=false -jar modb-app.jar`

## Configuration

| parameter                                | type     | default                                                                     | description                                                                                                                                                                                               |
|------------------------------------------|----------|-----------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `modb.app.downloadsDirectory`            | `String` | -                                                                           | Root directory in which the raw files and converted files are stored.                                                                                                                                     |
| `modb.app.outputDirectory`               | `String` | -                                                                           | Target output directory. Normally this should be the directory in which you cloned the [anime-offline-database](https://github.com/manami-project/anime-offline-database)                                 |
| `modb.app.downloadControlStateDirectory` | `String` | -                                                                           | Root directory of download control state files.                                                                                                                                                           |
| `modb.app.logFileDirectory`              | `String` | A directory called `logs` within the working directory of the current week. | Defines the directory in which the logs saved.                                                                                                                                                            |
| `modb.app.keepDownloadDirectories`       | `Long`   | `1`                                                                         | Number of download directories to keep. Download directories contain both raw data and conv files (intermediate format). Default is `1` which means that only the most recent download directory is kept. |