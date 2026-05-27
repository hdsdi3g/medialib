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
 * Copyright (C) hdsdi3g for hd3g.tv 2024
 *
 */
package tv.hd3g.ffprobejaxb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.UncheckedIOException;

import org.junit.jupiter.api.Test;

class FFprobeJAXBTest {

    @Test
    void testFoolishXML() {
        assertThrows(UncheckedIOException.class,
                () -> FFprobeJAXB.load(">not an XML<"));
    }

    @Test
    void testBuggyFFprobeXML() {
        assertThrows(IllegalArgumentException.class,
                () -> FFprobeJAXB.load(
                        """
                                <?xml version="1.0" encoding="UTF-8"?>
                                <ffprobe>
                                    <disposition default="1"/>
                                </ffprobe>
                                """));
    }

    @Test
    void testGetCodecTagString() {
        assertThat(FFprobeJAXB.getCodecTagString(null)).isNull();
        assertThat(FFprobeJAXB.getCodecTagString("foo")).isEqualTo("foo");
        assertThat(FFprobeJAXB.getCodecTagString("[0][0][0][0]")).isNull();
    }

    @Test
    void testGetLevelTag() {
        assertThat(FFprobeJAXB.getLevelTag(0)).isZero();
        assertThat(FFprobeJAXB.getLevelTag(-99)).isZero();
        assertThat(FFprobeJAXB.getLevelTag(99)).isEqualTo(99);
    }

    @Test
    void testRemoveUnknown() {
        assertThat(FFprobeJAXB.removeUnknown("foo")).isEqualTo("foo");
        assertThat(FFprobeJAXB.removeUnknown("unknown")).isNull();
        assertThat(FFprobeJAXB.removeUnknown(null)).isNull();
    }
}
