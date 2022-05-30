# ffprobe-jaxb

Use with Java and JAXB API for import [ffprobe](https://ffmpeg.org/ffprobe.html) xml result.

![Java CI with Maven](https://github.com/hdsdi3g/ffprobe-jaxb/workflows/Java%20CI%20with%20Maven/badge.svg)

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=hdsdi3g_ffprobe-jaxb&metric=alert_status)](https://sonarcloud.io/dashboard?id=hdsdi3g_ffprobe-jaxb)

![CodeQL](https://github.com/hdsdi3g/ffprobe-jaxb/workflows/CodeQL/badge.svg)

Start ffprobe like:

```shell
ffprobe -print_format xml -show_streams -show_format -hide_banner -i <my-media-file>`
```

And pass the XML (via stdout) to give to Java (11+):

```java
new FFprobeJAXB(final String xmlContent, final Consumer<String> onWarnLog);
```

You should see another project to use *ffprobe-jaxb*: [fflauncher](https://github.com/hdsdi3g/fflauncher).
