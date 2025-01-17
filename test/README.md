# modb-test

## What does this lib do?

This lib contains all essential dependencies as well as some convenience functions and classes for creating tests in _modb_ prefixed kotlin projects.

## Features

### Essential test dependencies

* junit5
* kotlin-test for junit5
* assertj
* wiremock

### Easy access to test resources

```kotlin
// access to a file in src/test/resources
val file: Path = testResource("file.txt") // for src/test/resources/file.txt
val file: Path = testResource("dir/subdir/file.txt") // for src/test/resources/dir/subdir/file.txt

// reading the content of a file in src/test/resources into a String
val fileContent: String = loadTestResource("file.txt") // for src/test/resources/file.txt
val fileContent: String = loadTestResource("dir/subdir/file.txt") // for src/test/resources/dir/subdir/file.txt
```

### Temporary directory

Creates a temporary directory and ensures that it will be deleted after test execution even if the test fails due to an exception. 

```kotlin
tempDirectory {
    Files.exist(tempDir)
}
```

### Mock server test cases

Create mock server test cases.

```kotlin
internal class SampleTest : MockServerTestCase<WireMockServer> by WireMockServerCreator() {

    @Test
    fun `test case`() {
        serverInstance.stubFor(
            head(urlPathEqualTo("/test")).willReturn(
                    aResponse()
                        .withHeader("Content-Type", "text/plain")
                        .withStatus(200)
            )
        )
    }
}
```