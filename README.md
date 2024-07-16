[![Tests](https://github.com/manami-project/modb-app/actions/workflows/tests.yml/badge.svg)](https://github.com/manami-project/modb-app/actions/workflows/tests.yml) [![codecov](https://codecov.io/gh/manami-project/modb-app/graph/badge.svg?token=66LR8JA8KE)](https://codecov.io/gh/manami-project/modb-app)
# modb-app

## Configuration

| parameter                                | type     | default                                                                     | description                                                           |
|------------------------------------------|----------|-----------------------------------------------------------------------------|-----------------------------------------------------------------------|
| `modb.app.downloadsDirectory`            | `String` | -                                                                           | Root directory in which the raw files and converted files are stored. |
| `modb.app.outputDirectory`               | `String` | -                                                                           | Target output directory.                                              |
| `modb.app.downloadControlStateDirectory` | `String` | -                                                                           | Root directory of download control state files.                       |
| `modb.app.logFileDirectory`              | `String` | A directory called `logs` within the working directory of the current week. | Defines the directory in which the logs saved.                        |