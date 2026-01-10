# Data quality

By creating the anime-offline-database project I always aimed for a dataset with the highest quality.
However, data is difficult and this project relies on other sources for its data. That means that I have to do my best
to validate the data wherever possible and transform or edit it where necessary to ensure a consistent and high quality.
This resulted in the creation of various quality checks, quality gates and transformations on different levels.

## Unexpected values in converter

When initially implementing a `Converter` I check each property which requires mapping for possible values
in the source set. Each `Converter` is written in a way that unexpected or unmapped values will throw an error
indicating that there is a need for adjustment. This is intentional.

## Validating anime data directly

The business object `AnimeRaw` checks itself when it is created. The idea was that an anime is always in a validated
state and that it's not possible to forget to validate it. To improve the performance I created a workaround which
bypasses the validation. The assumption is that data deserialized from the finalized dataset is always valid. The data
may not be checked during the deserialization, but it is checked during serialization to make up for that.
The data is not only validated, but normalized as well.

## DCS checks

At this stage all data has been downloaded and converted.
The [DCS](dcs.md) system checks degrading quality in a metadata provider. While the `Converter` implementations can 
only alert unmapped values, the DCS can assess all properties over all entries for each metadata provider.
This is an indicator for changes in the source data which requires adjustments in the respective `Converter`
implementation.

## Post Processors

After downloading sources, converting raw data, merging entries and updating DCS files, a set of post processors is
executed. This is the last gate that the data has to pass in a run. The `PostProcessor` interface contains
implementations for data quality and consistency checks.

### Removing dead entries

Metadata providers seem to have difficulties to remove related anime if they delete an anime from their databases.


### Consistency checks

Each file and list is checked for consistence and plausibility.