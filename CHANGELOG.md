# All Medialib projects upgrades needs

Correct bugs with FFmpeg v7:

 - Parse EBUR128 from lavfi metadata instead of strerr
 - Not parse anything from stderr
 - Set ffmpeg loglevel to warn
 - Correct ffprobe result SAX entries (remove `coded_picture_number` and `display_picture_number`, rename `pkt_duration` to `duration`, and `pkt_duration_time` to `duration_time`, but keep previous behavior compatible)
 - Correct FFAbout with the new ffmpeg output syntax

## 15.0.2

Update FFprobeJAXB to handle to ffprobe XML v7.0 #88

## 15.0.1

Update deps #85

## 15.0.0

Deeply refactor FFprobeJAXB to manage multiple ffprobe XSD versions #73

## 14.1.0

Update parent starter project to 19.0.0 #80

## 14.0.2

Bugfix: Better protection against special numbers, like "nan" or "-inf" on fflauncher #78

## 14.0.1

Bugfix: Correct Set by SortedSet for AudioFilterEbur128 (fflauncher) to correct Set stability in peakMode #76

## 14.0.0

Manage filter list with importFromOffline MediaAnalyserResult/Session #74

It breaks the actual code for this beacause MediaAnalyserResult was not symetrical: import/export looses informations on offline mode.

## 13.0.0

FFmpeg, LavfiMetadataFilterParser, API fix: rename flatFactor to flatness (and move it from float to long) #71

## 12.3.0

Maintenance correction for FFprobeJAXB/MediaSummary:

 - Bug correction: Correct #68 with "0" as profile for MediaSummary
 - Set some public access for MediaSummary #67
 - Update ffprobe xsd from 4.x to 6.0 #66
 - Display new ffprobe dispositions to MediaSummary #66

## 12.2.0

Add "data" stream type for ffprobe (fflauncher) #63 (bug)

## 12.1.3

Bug correction: correct missing ffmpeg filter option for idet (`rep_thres`) #61

## 12.1.2

Bug correction: correct MediaAnalyserSessionFilterContext class name #59

## 12.1.1

Bug correction: correct Number error with parseFloat (LavfiMetadataFilterParser) #57

## 12.1.0

Bug correction and internal API update:

 - Correct NPE with MediaAnalyserResult, refactor the relation between Result and Session, and create MediaAnalyserSessionFilterContext #55
 - Manage timecode entries for lavfi lines (just log it), instead of just Exception

## 12.0.2

Bug correction

Correct #53 and some bugs with MediaSummary:

 - add correct video level tag for MPEG2, H264, HEVC and AV1 based on ffmpeg source
 - remove "set not by default"
 - replace "Has B frames" by "with B frames"
 - replace "rng" by "col" (space, transfer, primaries)
 - correct null with audio ChannelLayout
 - correct format size lower than 1 MB.

Correct NumberFormatException during parsing ffmpeg progress #52

## 12.0.1

Maintenance version: update prodlib starter version from 12.3.1 to 18.0.2, set explicit jaxb-api and jaxb-runtime version (sticked to the older JAXB), code clean and refactor, remove log4j2 to logback.

Beware to explicit force jaxb-runtime to 2.3.8:

            <dependency>
                <groupId>org.glassfish.jaxb</groupId>
                <artifactId>jaxb-runtime</artifactId>
                <version>2.3.8</version>
            </dependency>

## 11.1.0

Bug correction:

Protect against missing fields in FFprobeResultSAX #46

Correct #45 with Float parsing and lavfi metadatas

## 11.0.0

Maintenance, bug correction and refactoring about fflauncher MediaAnalyser/ContainerAnalyser #39 #40 #41 #42

Correct behavior with stderr event and non ebur128 lines, create LavfiMetadataFilterParser and move it all LavfiMtdProgram Frames and Events Extractors, implements ImpEx RAW text result to produce offline results to MediaAnalyser/ContainerAnalyser, implements ImpEx RAW text result to produce offline results to MediaAnalyser/ContainerAnalyser #33

Correct ffprobeJAXB bug if media source don't show bitrate in video or audio stream #43

Create and compute GOPStatItem in fflauncher #44

Update lift.toml (Sonatype-lift): add ignoreRules Var, Varifier and UnnecessarilyFullyQualified

## 10.1.0

Correct bugs:

Better behavior with Progress if ffmpeg crash on boot: catch SocketException #32

Add missing video boolean option for aphasemeter filter (by default ignore video output) #30

Display ffmpeg and ffprobe command lines on run MediaAnalyser/ContainerAnalyser

Correct parse and detection on ebur128 stderr output (nan values and missing summary) #35

Remove metadata filters added by default in MediaAnalyser

Add better created/configured filters in AddFiltersTraits

Create FFprobeJAXB MediaSummary #28

Correct #27 manage FFAboutVersion with ffprobe and ffplay

ProgressBlock don't show correct "getOutTimeMs"

Correct Cropdetect filter, remove default mode=black #36

## 10.0.0

Change parent deps from parent to starter, add explicit deps to hamcrest and lombok: resolve #15

Update parent project version from 12.1.0 to 12.3.0, correct #18 #19

Update parent version from 12.3.0 to 12.3.1: explicit add of Commons Compress #25

### Big fflauncher improvent

Light the big FFmpeg class to traits (code clean), move FFmpeg enums to enums package #11

Refactor Filter API, create Traits for it #13

Create new ffmpeg filters: ametadata, aphasemeter, siti, metadata, mestimate, idet, freezedetect, cropdetect, blockdetect, blackdetect, volumedetect, silencedetect, ebur128 and astats #13

Implements filters (deep audio and video) analyzers, and create ffmpeg MediaAnalyser recipe #14

Create ffmpeg's lavfi filter parsers to extract filter results in Java objects #14

Implements ffmpeg progress via local socket listener #16

Create ContainerAnalyser: parse ffprobe XML result, streams and packets #17

### Processlauncher update

Code clean, and create DirectStandardOutputStdErrRetention to get process's stdout as InputStream, and stderr as text retention #26

## 9.0.1

Switch from Java 11 (LTS) to Java 17 (LTS) #9

