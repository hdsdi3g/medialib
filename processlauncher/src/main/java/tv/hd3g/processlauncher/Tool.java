package tv.hd3g.processlauncher;

import java.io.File;
import java.util.Arrays;

import tv.hd3g.processlauncher.cmdline.ExecutableFinder;

public final class Tool {

	private Tool() {
	}

	/**
	 * During some maven operation, on Linux, executable state can be drop.
	 */
	public static void patchTestExec() {
		if (File.separator.equals("\\")) {
			/**
			 * Test is running on windows, cancel this patch.
			 */
			return;
		}
		Arrays.stream(System.getProperty("java.class.path").split(File.pathSeparator)).map(File::new).filter(
		        ExecutableFinder.isValidDirectory).flatMap(dir -> Arrays.stream(dir.listFiles())).filter(
		                subFile -> (subFile.isFile() && subFile.canExecute() == false && subFile.getName().equals(
		                        "test-exec"))).findFirst().ifPresent(f -> {
			                        System.out.println(f.getAbsolutePath() // NOSONAR
			                                           + " has not the executable bit, set it now.");
			                        f.setExecutable(true);// NOSONAR
		                        });
	}

}
