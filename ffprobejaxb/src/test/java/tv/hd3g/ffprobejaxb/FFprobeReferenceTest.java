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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import net.datafaker.Faker;
import tv.hd3g.ffprobejaxb.data.FFProbeChapter;
import tv.hd3g.ffprobejaxb.data.FFProbeError;
import tv.hd3g.ffprobejaxb.data.FFProbeFormat;
import tv.hd3g.ffprobejaxb.data.FFProbeKeyValue;
import tv.hd3g.ffprobejaxb.data.FFProbeLibraryVersion;
import tv.hd3g.ffprobejaxb.data.FFProbePixelFormat;
import tv.hd3g.ffprobejaxb.data.FFProbeProgram;
import tv.hd3g.ffprobejaxb.data.FFProbeProgramVersion;
import tv.hd3g.ffprobejaxb.data.FFProbeStream;

class FFprobeReferenceTest {
    static Faker faker = net.datafaker.Faker.instance();

    @Mock
    FFProbeFormat format;
    @Mock
    FFProbeStream stream;
    @Mock
    FFProbeStream defaultStream;

    class FFprobeReferenceImpl implements FFprobeReference {

        @Override
        public Optional<FFProbeFormat> getFormat() {
            return Optional.ofNullable(format);
        }

        @Override
        public List<FFProbeStream> getStreams() {
            return List.of(stream);
        }

        @Override
        public String getXmlContent() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getXSDVersionReference() {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<FFProbeLibraryVersion> getLibraryVersions() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<FFProbeError> getError() {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<FFProbeProgram> getPrograms() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<FFProbeProgramVersion> getProgramVersion() {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<FFProbeChapter> getChapters() {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<FFProbePixelFormat> getPixelFormats() {
            throw new UnsupportedOperationException();
        }

    }

    class FFprobeReferenceImplMultiStreams extends FFprobeReferenceImpl {

        @Override
        public List<FFProbeStream> getStreams() {
            return List.of(defaultStream, stream);
        }
    }

    FFprobeReferenceImpl r;

    @BeforeEach
    void init() throws Exception {
        MockitoAnnotations.openMocks(this).close();
        r = new FFprobeReferenceImpl();
    }

    @AfterEach
    void ends() {
        verifyNoMoreInteractions(format, stream, defaultStream);
    }

    @Test
    void testGetVideoStreams_empty() {
        when(stream.codecType()).thenReturn(faker.numerify("type###"));
        assertThat(r.getVideoStreams()).isEmpty();
        verify(stream, times(1)).codecType();
    }

    @Test
    void testGetVideoStreams() {
        when(stream.codecType()).thenReturn("video");
        when(stream.width()).thenReturn(1);
        when(stream.height()).thenReturn(1);

        final var streams = r.getVideoStreams().toList();
        assertThat(streams).isEqualTo(List.of(stream));

        verify(stream, times(1)).codecType();
        verify(stream, times(1)).width();
        verify(stream, times(1)).height();
        verify(stream, times(1)).isSecondary();
    }

    @Test
    void testGetAudioStreams_empty() {
        when(stream.codecType()).thenReturn(faker.numerify("type###"));
        assertThat(r.getAudioStreams()).isEmpty();
        verify(stream, times(1)).codecType();
    }

    @Test
    void testGetAudioStreams() {
        when(stream.codecType()).thenReturn("audio");
        final var streams = r.getAudioStreams().toList();
        assertThat(streams).isEqualTo(List.of(stream));
        verify(stream, times(1)).codecType();
        verify(stream, times(1)).isSecondary();
    }

    @Test
    void testGetFirstVideoStream_empty() {
        when(stream.codecType()).thenReturn(faker.numerify("type###"));
        assertThat(r.getFirstVideoStream()).isEmpty();
        verify(stream, times(1)).codecType();
    }

    @Nested
    class GetFirstVideoStream {

        @BeforeEach
        void init() {
            when(stream.codecType()).thenReturn("video");
            when(stream.width()).thenReturn(1);
            when(stream.height()).thenReturn(1);

            when(defaultStream.codecType()).thenReturn("video");
            when(defaultStream.width()).thenReturn(1);
            when(defaultStream.height()).thenReturn(1);
            when(defaultStream.isDefault()).thenReturn(true);
        }

        @AfterEach
        void ends() {
            verify(stream, atLeast(1)).codecType();
            verify(stream, atLeast(1)).width();
            verify(stream, atLeast(1)).height();
            verify(stream, atLeast(1)).isSecondary();
        }

        @Test
        void testGetFirstVideoStream_ok() {
            when(stream.codecType()).thenReturn("video");
            assertThat(r.getFirstVideoStream())
                    .isNotEmpty()
                    .contains(stream);
        }

        @Test
        void testGetFirstVideoStream_isSecondary() {
            when(stream.isSecondary()).thenReturn(true);
            assertThat(r.getFirstVideoStream()).isEmpty();
            verify(stream, times(1)).isSecondary();
        }

        @Test
        void testGetFirstVideoStream_sorted() {
            r = new FFprobeReferenceImplMultiStreams();

            assertThat(r.getFirstVideoStream())
                    .isNotEmpty()
                    .contains(defaultStream);

            verify(defaultStream, atLeast(1)).codecType();
            verify(defaultStream, atLeast(1)).width();
            verify(defaultStream, atLeast(1)).height();
            verify(defaultStream, atLeast(1)).isSecondary();

            verify(stream, atLeast(1)).isDefault();
            verify(defaultStream, atLeast(1)).isDefault();
        }
    }

    final FFProbeKeyValue tc0 = new FFProbeKeyValue("timecode", "00:00:00:00");
    final FFProbeKeyValue tc1 = new FFProbeKeyValue("timecode", faker.numerify("##:##:##:##"));

    @Test
    void testGetTimecode_empty() {
        assertThat(r.getTimecode(false)).isEmpty();

        verify(format, atLeast(1)).tags();
        verify(stream, atLeast(1)).tags();
    }

    @Test
    void testGetTimecode_format() {
        when(format.tags()).thenReturn(List.of(tc0));
        assertThat(r.getTimecode(false))
                .isNotEmpty()
                .contains(tc0.value());

        verify(format, atLeast(1)).tags();
    }

    @Test
    void testGetTimecode_stream() {
        when(stream.tags()).thenReturn(List.of(tc0));
        assertThat(r.getTimecode(false))
                .isNotEmpty()
                .contains(tc0.value());

        verify(format, atLeast(1)).tags();
        verify(stream, atLeast(1)).tags();
    }

    @Test
    void testGetTimecode_format_empty() {
        when(format.tags()).thenReturn(List.of(tc0));
        assertThat(r.getTimecode(true)).isEmpty();

        verify(format, atLeast(1)).tags();
    }

    @Test
    void testGetTimecode_stream_empty() {
        when(stream.tags()).thenReturn(List.of(tc0));
        assertThat(r.getTimecode(true)).isEmpty();

        verify(format, atLeast(1)).tags();
        verify(stream, atLeast(1)).tags();
    }

    @Test
    void testGetTimecode_format_value() {
        when(format.tags()).thenReturn(List.of(tc1));
        assertThat(r.getTimecode(false))
                .isNotEmpty()
                .contains(tc1.value());

        verify(format, atLeast(1)).tags();
    }

    @Test
    void testGetTimecode_stream_value() {
        when(stream.tags()).thenReturn(List.of(tc1));
        assertThat(r.getTimecode(false))
                .isNotEmpty()
                .contains(tc1.value());

        verify(format, atLeast(1)).tags();
        verify(stream, atLeast(1)).tags();
    }

    @Test
    void testGetDuration_empty() {
        assertThat(r.getDuration()).isEmpty();
        verify(format, atLeast(1)).duration();
    }

    @Test
    void testGetDuration() {
        final var duration = Math.abs(faker.random().nextFloat());
        when(format.duration()).thenReturn(duration);
        assertThat(r.getDuration()).contains(Duration.ofMillis(Math.round(duration * 1000)));
        verify(format, atLeast(1)).duration();
    }

    @Test
    void testIsDefaultStreamIsSuitable() {
        r = new FFprobeReferenceImplMultiStreams();
        assertFalse(r.isDefaultStreamIsSuitable());

        when(defaultStream.isDefault()).thenReturn(true);
        assertTrue(r.isDefaultStreamIsSuitable());

        when(stream.isDefault()).thenReturn(true);
        assertFalse(r.isDefaultStreamIsSuitable());

        verify(stream, atLeastOnce()).isDefault();
        verify(defaultStream, atLeastOnce()).isDefault();
    }

}
