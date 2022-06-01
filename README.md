# MediaLib: all libs for media manipulation tools

Licence: LGPL v3

Tested on Windows 10 and Linux. Should be ok on macOS.

Please use Maven and Java 11 (OpenJDK) for build and test.

Use internally Log4j2 for logging.

[![Java CI with Maven](https://github.com/hdsdi3g/medialib/actions/workflows/maven-package.yml/badge.svg)](https://github.com/hdsdi3g/medialib/actions/workflows/maven-package.yml)

[![CodeQL](https://github.com/hdsdi3g/medialib/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/hdsdi3g/medialib/actions/workflows/codeql-analysis.yml)

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=hdsdi3g_medialib&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=hdsdi3g_medialib)

## Processlauncher

Start process with Java, with more practical tools.

### Usage example

See `tv.hd3g.processlauncher.Exec` for shortcuts examples.

### Functionalities

- Create process, watches it, kill it (QUIT), check if run is correctly completed
- Catch std-out and std-err with text support, during the execution, and after.
- Can interact with process on std-in/out/err on the fly
- Provide an API for command line parameters
  - simply add new parameters
  - parse raw command line and extract parameters, manage _"_ and space separation.
  - get parameters value
  - use simple template with command line: the command line can be configurable and code can inject variable values
- Provide an API for search and found executable file after its names, via classpath, system path, configurable paths, and adapt _execnames_ on Windows (add extension).
- Can stop process after a max execution time
- Can be callback just after the run starts and after the running ends.
- Can just prepare and extract a Java ProcessBuilder (for an execution outside this API).
- Manage sub-process killing
- Automatically kill all running process (and sub-process) if the Java app is closing.  

### Test

Use maven and Junit for run internal UT and IT.

### API organisation and relation

[![Java diagram](https://raw.githubusercontent.com/hdsdi3g/medialib/master/processlauncher/code-organization.png)](https://raw.githubusercontent.com/hdsdi3g/medialib/master/processlauncher/code-organization.png)

## fflauncher

FFmpeg API launcher.

## ffprobe-jaxb

Use with Java and JAXB API for import [ffprobe](https://ffmpeg.org/ffprobe.html) xml result.

Start ffprobe like:

```shell
ffprobe -print_format xml -show_streams -show_format -hide_banner -i <my-media-file>`
```

And pass the XML (via stdout) to give to Java (11+):

```java
new FFprobeJAXB(final String xmlContent, final Consumer<String> onWarnLog);
```

You should see *fflauncher* project to use *ffprobe-jaxb*.
