# Download Control State (DCS)

+ Anime are not downloaded once, but checked for updates as well
+ Updates for [anime-offline-database](https://github.com/manami-project/anime-offline-database) are created every week
+ Creating a weekly update must be possible within a day
+ Ongoing and upcoming anime have a higher tendency to change and therefore will be updated every week
+ Changes for finished anime are less likely
+ Downloading anime with frequent or recent changes more and other anime less reduces the number of downloads per week
+ This also reduces load on the meta data providers
+ Every anime is updated at least once per quarter

## What is DCS and how does it work?

Download Control State (DCS) is a meta data provider specific tracker for each anime.
It basically tracks each anime on each meta data provider over time. Based on the changes it orchestrates which
anime already known in the dataset need to be downloaded again for updates. It is important to know that anime are not
downloaded once. They are re-downloaded and checked for updates.

In the beginning it was possible to run the application once a week. It was possible to download all anime within a day.
An average week contained a low two digit number of new anime at max.
When MAL experienced consecutive weeks of updates with hundreds of new anime, scaling became an issue faster than expected.
The requirement was always to be able to start and finish the process within a day.
DCS has been introduced to be able to scale and meet that requirement. It tracks each anime entry on each meta data provider
individually for changes. It is only responsible for anime which are already known in the dataset and schedules when
to download them again.
Anime which are ongoing or upcoming are expected to change frequently. Therefore they are updated every week until they
finished. If they have finished it depends on the frequency of changes when they will be downloaded again.
If an anime is neither ongoing nor upcoming and has no changes for the first time, it is scheduled for the next download somewhere between 2-4 weeks from now.
The exact number of weeks is picked randomly to further distribute the number of downloads.
If an anime, which is neither is not ongoing nor upcoming, hasn't changed repeatedly, it will be downloaded in the number of weeks without changes up to this point.

Let's assume that with this weeks update an anime had no changes for the past four weeks, then it will be downloaded again 
in four weeks. This way the span between downloads for each anime will increase and reduces the number of anime to download.
However, there is a limited to this. The maximum number of weeks this can add up to is 12 weeks. The reason is that 
every anime must be downloaded from each meta data provider at least once per quarter.

**Example lifecycle:**

* **2019-01** Newly added as `UPCOMING`
* **...** Updated every week
* **2019-14** Updated to `ONGOING`
* **...** Updated every week
* **2019-28** Updated to `FINISHED`
* **2019-29** First week without changes. Redownload in `3` weeks
* **2019-32** No changes. Redownload in `6` weeks
* **2019-38** No changes. Redownload in `12` weeks
* **2019-50** No changes. Redownload in `12` weeks
* **2020-09** Change detected. Redownload next week.
* **2020-10** First week without changes. Redownload in `2` weeks
* **...**
