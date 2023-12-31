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
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

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
import tv.hd3g.ffprobejaxb.data.FFProbeStreamDisposition;

class FFprobeReferenceTest {
	static Faker faker = net.datafaker.Faker.instance();

	@Mock
	FFProbeFormat format;
	@Mock
	FFProbeStream stream;
	@Mock
	FFProbeStream defaultStream;
	@Mock
	FFProbeStreamDisposition disposition;
	@Mock
	FFProbeStreamDisposition defaultDisposition;

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

	FFprobeReferenceImpl r;

	@BeforeEach
	void init() throws Exception {
		MockitoAnnotations.openMocks(this).close();
		r = new FFprobeReferenceImpl();
	}

	@AfterEach
	void ends() {
		verifyNoMoreInteractions(format, stream, disposition, defaultStream, defaultDisposition);
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
		final var streams = r.getVideoStreams().toList();
		assertThat(streams).isEqualTo(List.of(stream));
		verify(stream, times(1)).codecType();
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
			when(stream.disposition()).thenReturn(disposition);
		}

		@AfterEach
		void ends() {
			verify(stream, atLeast(1)).codecType();
			verify(stream, atMost(4)).disposition();
			verify(disposition, atMost(1)).attachedPic();
			verify(disposition, atMost(1)).timedThumbnails();
			verify(disposition, atMost(1)).stillImage();
		}

		@Test
		void testGetFirstVideoStream_ok() {
			assertThat(r.getFirstVideoStream())
					.isNotEmpty()
					.contains(stream);
		}

		@Test
		void testGetFirstVideoStream_attached() {
			when(disposition.attachedPic()).thenReturn(true);
			assertThat(r.getFirstVideoStream()).isEmpty();
		}

		@Test
		void testGetFirstVideoStream_timedThumbnails() {
			when(disposition.timedThumbnails()).thenReturn(true);
			assertThat(r.getFirstVideoStream()).isEmpty();
		}

		@Test
		void testGetFirstVideoStream_stillImage() {
			when(disposition.stillImage()).thenReturn(true);
			assertThat(r.getFirstVideoStream()).isEmpty();
		}

		class FFprobeReferenceImplMultiStreams extends FFprobeReferenceImpl {

			@Override
			public List<FFProbeStream> getStreams() {
				return List.of(defaultStream, stream);
			}
		}

		@Test
		void testGetFirstVideoStream_sorted() {
			r = new FFprobeReferenceImplMultiStreams();
			when(defaultStream.codecType()).thenReturn("video");
			when(defaultStream.disposition()).thenReturn(defaultDisposition);
			when(defaultDisposition.asDefault()).thenReturn(true);

			assertThat(r.getFirstVideoStream())
					.isNotEmpty()
					.contains(defaultStream);

			verify(disposition, atMost(1)).asDefault();

			verify(defaultStream, atLeast(1)).codecType();
			verify(defaultStream, atLeast(1)).disposition();
			verify(defaultDisposition, atMost(1)).attachedPic();
			verify(defaultDisposition, atMost(1)).timedThumbnails();
			verify(defaultDisposition, atMost(1)).stillImage();
			verify(defaultDisposition, atMost(1)).asDefault();
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
}
