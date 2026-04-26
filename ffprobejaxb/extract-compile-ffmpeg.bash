#!/bin/bash

set -eu

DEMO_VIDEO_DIR=".demo-media-files";
if [ ! -d "$DEMO_VIDEO_DIR" ]; then
	./create-demo-files.bash
fi

DEMO_VIDEO_MOV="$DEMO_VIDEO_DIR/test-ffv1.mov";
DEMO_VIDEO_TS="$DEMO_VIDEO_DIR/test-mpeg2.ts";
DEMO_VIDEO_VP8="$DEMO_VIDEO_DIR/test-vp8.mkv";

mkdir -p .ffmpeg-compile
cd .ffmpeg-compile

function video_test() {
	local VIDEOF="../$1";
	local VERSION="$2";
	local XML_FILE="../src/test/resources/$(basename "$VIDEOF")-$VERSION.xml";
	if [ ! -f "$XML_FILE" ]; then
		./ffprobe-$VERSION -of xml -show_streams -show_format -show_programs -show_chapters -show_library_versions -show_program_version -show_error -show_pixel_formats -i "$VIDEOF" > "$XML_FILE";
	fi
}

function make_and_test() {
	local VERSION="$1";
	local NAME="n$VERSION.tar.gz";
	local DIR_NAME="FFmpeg-n$VERSION";
	local FFPROBE_DEST="ffprobe-$VERSION";

	if [ ! -f "$FFPROBE_DEST" ]; then
		if [ ! -f "$NAME" ]; then
			wget "https://github.com/FFmpeg/FFmpeg/archive/refs/tags/$NAME"
		fi
		if [ ! -d "$DIR_NAME" ]; then
			tar xf "$NAME";
		fi
		cd "$DIR_NAME";
		LANG="en_US.UTF-8" ./configure --disable-ffmpeg --disable-ffplay
		LANG="en_US.UTF-8" make -j $(nproc)
		mv ffprobe "../$FFPROBE_DEST"
		mv doc/ffprobe.xsd "../$FFPROBE_DEST.xsd"
		cd ..
	fi

	video_test "$DEMO_VIDEO_MOV" "$VERSION"
	video_test "$DEMO_VIDEO_TS" "$VERSION"
	video_test "$DEMO_VIDEO_VP8" "$VERSION"

	if [ -f "$NAME" ]; then
		rm -f "$NAME";
	fi
	if [ -d "$DIR_NAME" ]; then
		rm -rf "$DIR_NAME";
	fi
}


# make_and_test "8.2-dev"

# https://github.com/FFmpeg/FFmpeg/tags
# Last updated on the 8 march 2026
