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
		./configure --disable-ffmpeg --disable-ffplay
		make -j $(nproc)
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

make_and_test "6.1.1"
make_and_test "6.2-dev"
make_and_test "6.1"
make_and_test "5.1.4"
make_and_test "2.8.22"
make_and_test "3.4.13"

make_and_test "4.1.11"
make_and_test "4.2.9"
make_and_test "4.3.6"
make_and_test "4.4.4"

make_and_test "5.0.3"
make_and_test "5.1.3"
make_and_test "6.0"
make_and_test "6.1-dev"

