package tv.hd3g.fflauncher.filtering;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import tv.hd3g.fflauncher.ConversionTool;
import tv.hd3g.fflauncher.FFbase;
import tv.hd3g.fflauncher.about.FFAbout;
import tv.hd3g.fflauncher.about.FFAboutFilter;
import tv.hd3g.fflauncher.enums.FilterConnectorType;
import tv.hd3g.processlauncher.cmdline.Parameters;

class FilterChainsTest {

	static final String LONG_CHAIN = "split [main][tmp]; [tmp] crop=iw:ih/2:0:0, vflip [flip]; foobar='my text, with: [c]omas !'; [main][flip] overlay=0:H/2";
	static final String CONDENSED_CHAIN = "split[main][tmp];[tmp]crop=iw:ih/2:0:0,vflip[flip];foobar='my text, with: [c]omas !';[main][flip]overlay=0:H/2";

	FilterChains filterChains;
	String rawNewFilter;
	Filter newFilter;

	@BeforeEach
	void init() {
		filterChains = new FilterChains(LONG_CHAIN);
		rawNewFilter = "newaddedfiler";
		newFilter = new Filter(rawNewFilter);
	}

	@Test
	void testFilterChain() {
		final var fc = new FilterChains();
		assertEquals("", fc.toString());
	}

	@Test
	void testFilterChainString() {
		assertEquals(CONDENSED_CHAIN, filterChains.toString());
	}

	@Test
	void testAddFilterInLastChainFilterBoolean() {
		filterChains.addFilterInLastChain(newFilter, false);
		assertEquals(1, filterChains.getLastChain().indexOf(newFilter));
	}

	@Test
	void testAddFilterInLastChainStringBoolean() {
		filterChains.addFilterInLastChain(rawNewFilter, false);
		assertEquals(1, filterChains.getLastChain().indexOf(newFilter));
	}

	@Test
	void testAddFilterInLastChainFilter_createNewChain() {
		filterChains.addFilterInLastChain(newFilter, true);
		assertEquals(0, filterChains.getLastChain().indexOf(newFilter));
		assertEquals(5, filterChains.getChainsCount());
	}

	@Test
	void testInsertFilterInChainFilterFilter() {
		final var previousFilter = filterChains.getChain(1).get(0);
		assertEquals("[tmp]crop=iw:ih/2:0:0", previousFilter.toString());

		final var selectedChain = filterChains.insertFilterInChain(newFilter, previousFilter);
		assertEquals(filterChains.getChain(1), selectedChain);
		assertEquals(previousFilter, selectedChain.get(0));
		assertEquals(newFilter, selectedChain.get(1));
		assertEquals("vflip[flip]", selectedChain.get(2).toString());
		assertEquals(3, selectedChain.size());
	}

	@Test
	void testInsertFilterInChainFilterFilterLastPos() {
		final var previousFilter = filterChains.getChain(1).get(1);
		assertEquals("vflip[flip]", previousFilter.toString());

		final var selectedChain = filterChains.insertFilterInChain(newFilter, previousFilter);
		assertEquals(filterChains.getChain(1), selectedChain);
		assertEquals(new Filter("[tmp]crop=iw:ih/2:0:0"), selectedChain.get(0));
		assertEquals(previousFilter, selectedChain.get(1));
		assertEquals(newFilter, selectedChain.get(2));
		assertEquals(3, selectedChain.size());
	}

	@Test
	void testInsertFilterInChain_notFound() {
		final var badFilter = new Filter("unknown");
		assertThrows(IllegalArgumentException.class, () -> {
			filterChains.insertFilterInChain(newFilter, badFilter);
		});
	}

	@Test
	void testInsertFilterInChainStringFilter() {
		final var previousFilter = filterChains.getChain(1).get(0);
		assertEquals("[tmp]crop=iw:ih/2:0:0", previousFilter.toString());

		final var selectedChain = filterChains.insertFilterInChain(rawNewFilter, previousFilter);
		assertEquals(filterChains.getChain(1), selectedChain);
		assertEquals(previousFilter, selectedChain.get(0));
		assertEquals(newFilter, selectedChain.get(1));
		assertEquals("vflip[flip]", selectedChain.get(2).toString());
		assertEquals(3, selectedChain.size());
	}

	@Test
	void testGetChainsCount() {
		assertEquals(4, filterChains.getChainsCount());
	}

	@Test
	void testGetChain() {
		var actualChain = filterChains.getChain(0);
		assertNotNull(actualChain);
		assertEquals(1, actualChain.size());
		assertEquals("split[main][tmp]", actualChain.get(0).toString());

		actualChain = filterChains.getChain(3);
		assertNotNull(actualChain);
		assertEquals(1, actualChain.size());
		assertEquals(filterChains.getLastChain(), actualChain);
		assertEquals("[main][flip]overlay=0:H/2", actualChain.get(0).toString());
	}

	@Test
	void testGetAllFiltersInChains() {
		assertEquals(List.of(
				"split[main][tmp]",
				"[tmp]crop=iw:ih/2:0:0",
				"vflip[flip]",
				"foobar='my text, with: [c]omas !'",
				"[main][flip]overlay=0:H/2"),
				filterChains.getAllFiltersInChains().map(Filter::toString).toList());
	}

	@Test
	void testCreateChain() {
		final var newChain = filterChains.createChain();
		assertNotNull(newChain);
		assertTrue(newChain.isEmpty());
		assertEquals(filterChains.getLastChain(), newChain);
		assertEquals(5, filterChains.getChainsCount());
	}

	@Test
	void testRemoveChain() {
		final var actualChain = filterChains.getChain(0);
		filterChains.removeChain(0);
		assertEquals(3, filterChains.getChainsCount());
		assertNotEquals(actualChain, filterChains.getChain(0));
	}

	@Test
	void testGetLastChain() {
		assertEquals(filterChains.getChain(filterChains.getChainsCount() - 1), filterChains.getLastChain());
	}

	@Test
	void testPushFilterChainTo() {
		final var ffbase = Mockito.mock(FFbase.class);
		final var parameters = new Parameters();
		Mockito.when(ffbase.getInternalParameters()).thenReturn(parameters);

		filterChains.pushFilterChainTo("-fparam", ffbase);
		assertEquals("-fparam " + CONDENSED_CHAIN, parameters.toString());
	}

	@Test
	void testSetFilterChainToVar() {
		final var ffbase = Mockito.mock(FFbase.class);
		final var parameterVars = new HashMap<String, Parameters>();
		Mockito.when(ffbase.getParametersVariables()).thenReturn(parameterVars);

		filterChains.setFilterChainToVar("fparam", ffbase);
		assertEquals(CONDENSED_CHAIN, parameterVars.get("fparam").toString());
	}

	@Test
	void testParseStringConversionTool() {
		final var conversionTool = Mockito.mock(ConversionTool.class);
		final var parameters = new Parameters();
		Mockito.when(conversionTool.getInternalParameters()).thenReturn(parameters);
		parameters.addParameters("-f", LONG_CHAIN, "-f", "foo:bar");

		final var chain = FilterChains.parse("-f", conversionTool);
		assertNotNull(chain);
		assertEquals(2, chain.size());
		assertEquals(CONDENSED_CHAIN, chain.get(0).toString());
		assertEquals("foo:bar", chain.get(1).toString());
	}

	@Test
	void testParseStringParameters() {
		final var parameters = new Parameters();
		parameters.addParameters("-f", LONG_CHAIN, "-f", "foo:bar");

		final var chain = FilterChains.parse("-f", parameters);
		assertNotNull(chain);
		assertEquals(2, chain.size());
		assertEquals(CONDENSED_CHAIN, chain.get(0).toString());
		assertEquals("foo:bar", chain.get(1).toString());
	}

	@Test
	void testParseStringParameters_NotFounded() {
		final var chain = FilterChains.parse("-f", new Parameters());
		assertNotNull(chain);
		assertTrue(chain.isEmpty());
	}

	@Test
	void testParseFromReadyToRunParameters() {
		final var conversionTool = Mockito.mock(ConversionTool.class);
		final var parameters = new Parameters();
		Mockito.when(conversionTool.getReadyToRunParameters()).thenReturn(parameters);
		parameters.addParameters("-f", LONG_CHAIN, "-f", "foo:bar");

		final var chain = FilterChains.parseFromReadyToRunParameters("-f", conversionTool);
		assertNotNull(chain);
		assertEquals(2, chain.size());
		assertEquals(CONDENSED_CHAIN, chain.get(0).toString());
		assertEquals("foo:bar", chain.get(1).toString());
	}

	@Test
	void testMerge() {
		final var fc0 = new FilterChains("aaa0,bbb0;ccc0,ddd0");
		final var fc1 = new FilterChains("aa1;bb1;c1;dd1");
		final var fc2 = new FilterChains("aaa2,bbb2,ccc2");

		final var fcM = FilterChains.merge(List.of(fc0, fc1, fc2));

		assertEquals(7, fcM.getChainsCount());
		assertEquals("[aaa0, bbb0]", fcM.getChain(0).toString());
		assertEquals("[ccc0, ddd0]", fcM.getChain(1).toString());
		assertEquals("[aa1]", fcM.getChain(2).toString());
		assertEquals("[bb1]", fcM.getChain(3).toString());
		assertEquals("[c1]", fcM.getChain(4).toString());
		assertEquals("[dd1]", fcM.getChain(5).toString());
		assertEquals("[aaa2, bbb2, ccc2]", fcM.getChain(6).toString());
	}

	@Test
	void testCheckFiltersAvailability() {
		final var about = Mockito.mock(FFAbout.class);
		final var ffFilter0 = Mockito.mock(FFAboutFilter.class);
		final var ffFilter1 = Mockito.mock(FFAboutFilter.class);

		when(about.getFilters()).thenReturn(List.of(ffFilter0, ffFilter1));
		when(ffFilter0.getTag()).thenReturn("ff0");
		when(ffFilter1.getTag()).thenReturn("ff1");

		final var fcNone = new FilterChains("nope, ff1");
		var avail = fcNone.checkFiltersAvailability(about);
		assertNotNull(avail);
		assertEquals(1, avail.size());
		assertEquals("nope", avail.get(0).getFilterName());

		final var fcOk = new FilterChains("ff0, ff1");
		avail = fcOk.checkFiltersAvailability(about);
		assertNotNull(avail);
		assertTrue(avail.isEmpty());
	}

	@Test
	void testCheckFiltersAvailability_ConnectorType() {
		final var about = Mockito.mock(FFAbout.class);
		final var ffFilter0 = Mockito.mock(FFAboutFilter.class);
		final var ffFilter1 = Mockito.mock(FFAboutFilter.class);

		when(about.getFilters()).thenReturn(List.of(ffFilter0, ffFilter1));
		when(ffFilter0.getTag()).thenReturn("fvideo");
		when(ffFilter0.getSourceConnector()).thenReturn(FilterConnectorType.VIDEO);
		when(ffFilter1.getTag()).thenReturn("faudio");
		when(ffFilter1.getSourceConnector()).thenReturn(FilterConnectorType.AUDIO);

		final var fcNone = new FilterChains("nope, fvideo, faudio");
		var avail = fcNone.checkFiltersAvailability(about, FilterConnectorType.VIDEO);
		assertNotNull(avail);
		assertEquals(2, avail.size());
		assertEquals("nope", avail.get(0).getFilterName());
		assertEquals("faudio", avail.get(1).getFilterName());

		final var fcOk = new FilterChains("fvideo");
		avail = fcOk.checkFiltersAvailability(about);
		assertNotNull(avail);
		assertTrue(avail.isEmpty());
	}

}
