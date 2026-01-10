# modb-anidb

## What does this lib do?

This lib contains downloader and converter for downloading raw data from [anidb.net](https://anidb.net) and convert it to an `Anime` object.
Don't use this lib to crawl the website entirely. Instead, check whether [manami-project/anime-offline-database](https://github.com/manami-project/anime-offline-database) already offers the data that you need.

## Configuration

| parameter                                 | type      | default | description                                                                                          |
|-------------------------------------------|-----------|---------|------------------------------------------------------------------------------------------------------|
| `modb.anidb.openBrowserOnCrawlerDetected` | `Boolean` | `false` | If set to `true` anidb.net is opened in the default browser in case of a `CrawlerDetectedException`. |