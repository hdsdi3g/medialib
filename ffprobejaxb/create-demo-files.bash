#!/bin/bash
# This file is part of medialib as internal and test source.
# It will generate some boring but specific audios and videos files.
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either command 3 of the License, or
# any later command.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
# GNU General Public License for more details.
#
# Copyright (C) Media ex Machina 2023
#
# Usage : just $0
# You need a recent (>v5) ffmpeg setup

set -eu

mkdir -p .demo-media-files
cd .demo-media-files

OPT="-y -hide_banner";
SA="anoisesrc=sample_rate=48000:duration=5:color=pink";
SV="mandelbrot=start_scale=1:bailout=2:end_scale=0.001:size=cif:rate=25,format=pix_fmts=yuv420p";
FC="[0:a][1:a]join=inputs=2:channel_layout=stereo,volume=volume=-18dB[a]";

ffmpeg -f lavfi -i "$SA" -f lavfi -i "$SA" -f lavfi -i "$SV" -t 00:00:05 \
       -filter_complex "$FC" -map "2:0" -map "[a]" $OPT -c:v ffv1 -c:a pcm_s16le test-ffv1.mov

ffmpeg -f lavfi -i "$SA" -f lavfi -i "$SA" -f lavfi -i "$SV" -t 00:00:05 \
       -filter_complex "$FC" -map "2:0" -map "[a]" $OPT -c:v mpeg2video -b:v 15M -c:a mp2 -bf 2 -b:a 256k \
       -f mpegts -metadata service_provider="Media ex Machina" -metadata service_name="Demo render" \
       test-mpeg2.ts

TMP_CHAPTERS="temp-chapters.txt";
cat <<EOF >> "$TMP_CHAPTERS"
[CHAPTER]
TIMEBASE=1/1000
START=1
END=3000
title=Chap One
EOF

ffmpeg -f lavfi -i "$SA" -f lavfi -i "$SA" -f lavfi -i "$SV" -f ffmetadata -i "$TMP_CHAPTERS" -t 00:00:05 \
       -filter_complex "$FC" -map "2:0" -map "[a]" -map_metadata 2 $OPT -c:v vp8 -c:a aac \
       -metadata:g title="Media title" -metadata:s:v:0 akey=avalue -disposition:a:0 karaoke+timed_thumbnails test-vp8.mkv

rm -f "$TMP_CHAPTERS"

