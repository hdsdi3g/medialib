# ffprobe-jaxb

Use with Java and JAXB API for import [ffprobe](https://ffmpeg.org/ffprobe.html) xml result.

Start ffprobe like to generate a XML file compilant:

```shell
ffprobe -print_format xml -show_streams -show_format -show_programs -show_chapters -hide_banner -i <my-media-file>`
```

Optionally, you can add/use `-show_library_versions`, `-show_program_version`, `-show_error`, and  `-show_pixel_formats`.

You should see another project to use *ffprobe-jaxb*: [fflauncher](https://github.com/hdsdi3g/fflauncher).

## Add new XSDs, after a new ffmpeg release

You can found a new XSD on `./doc/ffprobe.xsd` FFmpeg git repository.

This documentation is only for *ffprobe-jaxb* maintenance.

> If the new XSD is the same at the most recent in the `old xsd refs` directory.

Do nothing.

> If the new XSD adds new XML attributes 

Do an *upgrade*.

> If the new XSD change the XML struct, rename/removes attributes, add tags...

Do an *add*.

On both *upgrade* and *add*:

  - put the new XSD on `src/main/resources`
  - update pom.xml: change the generated package name      `project/build/plugins/plugin[jaxb-maven-plugin]/configuration/generatePackage` from `org.ffmpeg.ffprobeXXX` to `org.ffmpeg.ffprobeYYY` where XXX and YYY are the current/newer ffmpeg release version dot less.
  - `mvn clean` / `mvn compile`
  - Move the new generated code from `target/generated-sources/xjc` to `src/main/java`.
  - Move the XSD from `src/main/resources` to the `old xsd refs` dir.
  - Create a new class in `tv.hd3g.ffprobejaxb` **cloned from the most recent JAXB class** `FFprobeJAXBXXX` to `FFprobeJAXBYYY`.
  - Change the class `FFprobeJAXBYYY` imports from `import org.ffmpeg.ffprobeXXX.*;` to  `import org.ffmpeg.ffprobeYYY.*;`
  - "Adapt" the code.
  - Declare the new `FFprobeJAXBYYY` class in `FFprobeXSDVersion`, **newer entry on first**.
  - Run tests
  - Maybe update the end-to-end ffprobe test

## Do an end-to-end ffprobe test

On posix/Linux, install ffmpeg (a by default, not need to get some fancy codecs) + bash + wget + the full toolkit to build ffmpeg itself (make...).

Run `create-demo-files.bash`. It will produce video test files on `.demo-media-files` directory.

Add the new ffmpeg version/git tag on the bottom of `extract-compile-ffmpeg.bash`.

Run `extract-compile-ffmpeg.bash`.

It will:
  - download all ffprobe versions mentioned on the script on `.ffmpeg-compile`.
  - make (compile only) ffprobe
  - extract XSD from source
  - run all ffprobes to the previously created demo-files
  - push all the produced XMLs, **only if missing** on the `src/test/resources`

During the next E2E test run, all will be check.

Free feel to adapt E2E java code if the test it to sensible (like file sizes / bitrates).

Only that can prove ffprobejaxb can run on all XMLs produced by all (current) ffprobe versions.
