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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import tv.hd3g.ffprobejaxb.FFprobeJAXB;

class FFProbeStreamTest {

    static final FFprobeJAXB source = FFprobeJAXB.load(
            """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <ffprobe>
                        <streams>
                            <stream>
                                <disposition default="1" attached_pic="0" timed_thumbnails="0" still_image="0"/>
                            </stream>
                            <stream>
                                <disposition default="0" attached_pic="0" timed_thumbnails="0" still_image="0"/>
                            </stream>
                            <stream>
                                <disposition default="0" attached_pic="1" timed_thumbnails="0" still_image="0"/>
                            </stream>
                            <stream>
                                <disposition default="0" attached_pic="0" timed_thumbnails="1" still_image="0"/>
                            </stream>
                            <stream>
                                <disposition default="0" attached_pic="0" timed_thumbnails="0" still_image="1"/>
                            </stream>
                            <stream>
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
    }

    @Test
    void testIsSecondary() {
        assertFalse(source.getStreams().get(0).isSecondary());
        assertFalse(source.getStreams().get(1).isSecondary());
        assertTrue(source.getStreams().get(2).isSecondary());
        assertTrue(source.getStreams().get(3).isSecondary());
        assertTrue(source.getStreams().get(4).isSecondary());
        assertFalse(source.getStreams().get(5).isSecondary());
    }

}
