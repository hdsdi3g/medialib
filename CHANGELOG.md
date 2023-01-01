# All Medialib projects upgrades needs

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

