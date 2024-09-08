# Reviewed isolated entries

+ Merges are reviewed in a manual process.
+ The list of reviewed isolated entries marks anime with a single source as reviewed if they cannot be merged with any other entry.
+ Creating a merge lock for every anime having only one `sources` entry is not done to keep those entries flexible.
+ Merge locks for entries with one source are only created to split entries.

## What are reviewed isolated entries

Changed and new entries after merging are reviewed in a manual process.
The cluster of anime having only one source after merging is special. To keep these entries flexible, merge locks are
not created for those entries. A merge lock for an anime with a single source is only created in case of a split.
In order to mark these anime as reviewed they are added to a list called "reviewed isolated entries". This way they
won't appear in the list of entries which still require manual review.