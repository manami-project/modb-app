# Merging

+ List of all DCS files is the base.
+ Merging is done per meta data provider sorted by number of anime in descending order
+ First run initially populates the golden record list with the meta data provider having the most anime
+ Next run checks anime for each remaining meta data provider one by one.
+ After that there are two more runs checking any anime which couldn't be merged so far.
+ Merge process first checks for merge locks
+ If none exists a lookup via title is made to find potential golden records
+ Then the probability is calculated which states how likely it is that these two anime should be merged together
+ In case probability is `>=80%` then the anime is merged with golden record offering the highest probability
+ Otherwise the anime will be checked in the next run-through again or a new golden record is created if this was the last run-through

## What does the merging process look like?

An important key factor for the merging logic of this project is that the aim is to only merge with confidence.
It is preferable to avoid merging two entries rather than risk merging them incorrectly.

Merging is done in multiple run-throughs.
The first run-through is done with the anime of the meta data provider which has the most anime.
This initially populates the golden records list. During the merging step, a golden record serves as a provisional final
version of an entry. Once the entire merging process is complete, these golden records form the foundation for the
entries in the finalized dataset. Next run-throughs check anime per meta data provider sorted by the number of anime in
descending order.

While merging entries either a merge lock exists which means that the merging logic is skipped or a list of potential
golden records is retrieved based on the title. Doing a lookup based on titles scales a lot better than calculating the
matching probability for all entries. This means that the matching probability is calculated for all the potential
golden records retrieved based on the title. If the probability is `>=80%` then the anime is merged. If not then the
anime will be reprocessed in the next run.

This implementation uses 3 additional runs to reprocess anime. The idea behind this is that a golden record might
contain additional data after merging with another anime which will then positively influence the probability for an
anime in the reprocess queue.

If an anime hasn't been merged after the maximum number of run-throughs then a separate golden record will be created
for this anime.

## How is the probability being calculated?

There are there properties which are always taken into consideration: `title`, `type` and `episodes`.
Each property creates a value between `0.0` and `1.0`. Where `0.0` means that the values are completely differnet and 
`1.0` means that they are equal.

* `title`: The score for the title is calculated using "jaro winkler similarity".
* `type`: A type is either equal (`1.0`) or not (`0.0`). But because the data from different sources varies a lot, there is another case. Based on experience there is another case for any combination of `SPECIAL` and `ONA`. For this case the value is `0.4` to indicate that it's somewhat possible that these could be the same.
* `episodes`: The closer the values the more likely it is that these values describe the same anime and the higher the score.

**Background on episodes:**
For anime there are often differences in episodes and years. Although they should be the same, because they describe the
same anime, those can differ between meta data providers. An example for episodes is if the number of episodes includes
a recap episode on one meta data provider whereas it's not part of the entry on the other meta data provider.
For years this can especially happen for the winter season overlapping in years or if they use release dates from
different countries.

The following properties are only optional and only taken into consideration if at least one the anime provides data for it:

* `status`: Either equal (`1.0`) or not (`0.0`).
* `yearOfPremiere`: Same as episodes.
* `duration`: Same as episodes.

The final score is calculated as the percentage of the total sum of scores relative to the maximum achievable score.