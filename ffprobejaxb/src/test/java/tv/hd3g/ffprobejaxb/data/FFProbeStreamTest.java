/*
 * This file is part of ffprobejaxb.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * Copyright (C) hdsdi3g for hd3g.tv 2026
 *
 */
package tv.hd3g.ffprobejaxb.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tv.hd3g.ffprobejaxb.data.FFProbeStream.parseFrameRate;

import org.junit.jupiter.api.Test;

import tv.hd3g.ffprobejaxb.FFprobeJAXB;

class FFProbeStreamTest {

    static final FFprobeJAXB source = FFprobeJAXB.load(
            """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <ffprobe>
                        <streams>
                            <stream codec_type="data" codec_name="foo" r_frame_rate="1/2" avg_frame_rate="6/3">
                                <disposition default="1" attached_pic="0" timed_thumbnails="0" still_image="0"/>
                            </stream>
                            <stream codec_type="data" codec_name="foo" r_frame_rate="42" avg_frame_rate="63">
                                <disposition default="0" attached_pic="0" timed_thumbnails="0" still_image="0"/>
                            </stream>
                            <stream codec_type="data" codec_name="foo">
                                <disposition default="0" attached_pic="1" timed_thumbnails="0" still_image="0"/>
                            </stream>
                            <stream codec_type="data" codec_name="foo">
                                <disposition default="0" attached_pic="0" timed_thumbnails="1" still_image="0"/>
                            </stream>
                            <stream codec_type="data" codec_name="foo">
                                <disposition default="0" attached_pic="0" timed_thumbnails="0" still_image="1"/>
                            </stream>
                            <stream codec_type="data" codec_name="foo">
                            </stream>
                            <stream codec_type="data">
                                <disposition default="0" attached_pic="0" timed_thumbnails="0" still_image="0"/>
                            </stream>
                            <stream codec_name="foo">
                            </stream>
                        </streams>
                        <format>
                        </format>
                    </ffprobe>
                    """);

    @Test
    void testIsDefault() {
        assertTrue(source.getStreams().get(0).isDefault());
        assertFalse(source.getStreams().get(1).isDefault());
        assertFalse(source.getStreams().get(2).isDefault());
        assertFalse(source.getStreams().get(3).isDefault());
        assertFalse(source.getStreams().get(4).isDefault());
        assertFalse(source.getStreams().get(5).isDefault());
        assertFalse(source.getStreams().get(6).isDefault());
    }

    @Test
    void testIsSecondary() {
        assertFalse(source.getStreams().get(0).isSecondary());
        assertFalse(source.getStreams().get(1).isSecondary());
        assertTrue(source.getStreams().get(2).isSecondary());
        assertTrue(source.getStreams().get(3).isSecondary());
        assertTrue(source.getStreams().get(4).isSecondary());
        assertFalse(source.getStreams().get(5).isSecondary());
        assertTrue(source.getStreams().get(6).isSecondary());
    }

    @Test
    void testGetComputedRFrameRate() {
        assertThat(source.getStreams().get(0).getComputedRFrameRate()).contains(0.5f);
        assertThat(source.getStreams().get(1).getComputedRFrameRate()).contains(42f);
    }

    @Test
    void testGetComputedAvgFrameRate() {
        assertThat(source.getStreams().get(0).getComputedAvgFrameRate()).contains(2f);
        assertThat(source.getStreams().get(1).getComputedAvgFrameRate()).contains(63f);
    }

    @Test
    void testParseFrameRate() {
        assertThat(parseFrameRate(null)).isEmpty();
        assertThat(parseFrameRate("")).isEmpty();
        assertThat(parseFrameRate(" ")).isEmpty();
        assertThat(parseFrameRate("0/0")).isEmpty();
        assertThat(parseFrameRate("0")).isEmpty();
        assertThat(parseFrameRate("0.0")).isEmpty();
        assertThat(parseFrameRate("-42")).isEmpty();
        assertThat(parseFrameRate("4/")).isEmpty();
        assertThat(parseFrameRate("4/3/2")).isEmpty();
        assertThat(parseFrameRate("4/0")).contains(4f);
        assertThat(parseFrameRate("0/5")).isEmpty();
        assertThat(parseFrameRate("f/d")).isEmpty();
        assertThat(parseFrameRate("42")).contains(42f);
        assertThat(parseFrameRate("4.2")).contains(4.2f);
        assertThat(parseFrameRate("a")).isEmpty();
    }
}
