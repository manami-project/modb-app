# Merge locks

+ Anime are merged automatically
+ Merging is not perfect
+ Manual adjustments are required
+ A merge lock is a fixation letting the application know which anime must be merged together
+ It is nothing more than a list of `source` URIs

## What are merge locks?

The application merges anime automatically. There are three cases resulting from merging in an automated process:

1. Perfect match. Anime from different meta data provider which belong together are merged together
2. Anime are not merged together altough they could be merged to one anime.
3. Two or more anime from different meta data provider are merged into a single anime altough they should not be, because they describe different releases.

The third case is very likely to happen if anime have the same title and very few other data which helps to distinguish them. Especially for this case
it's necessary to create a manual override which prevents merging entries which should not be merged together.

Merge locks are a mechanism to override whatever the automated merge process would do. It's a set of `source` URIs of anime.
A merge lock tells the application to merge specific entries together.

Over time merge locks have also been used to verify entries (case 1) and increase the merge density (case 2).