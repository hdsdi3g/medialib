/*
 * This file is part of processlauncher.
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
 * Copyright (C) hdsdi3g for hd3g.tv 2018
 *
 */
package tv.hd3g.processlauncher.demo;

public class DemoExecIOText {

	public static final String expectedOut = "OUT";
	public static final String expectedErr = "ERR";
	public static final String expectedIn = "InputValue\\1";

	public static final int exitOk = 1;
	public static final int exitBadParamLen = 2;
	public static final int exitBadParamValue = 3;
	public static final int exitBadEnv = 4;
	public static final int exitBadImportEnv = 5;

	public static final String ENV_KEY = "EnvKey";
	public static final String ENV_VALUE = "EnvValue";

	public static void main(final String[] args) {
		System.out.println(expectedOut);
		System.err.println(expectedErr);

		if (System.getenv().getOrDefault(ENV_KEY, "").equals(ENV_VALUE) == false) {
			System.exit(exitBadEnv);
		}

		if (System.getenv().containsKey("PATH") == false) {
			System.exit(exitBadImportEnv);
		}

		if (args.length != 1) {
			System.exit(exitBadParamLen);
		} else {
			if (args[0].equals(expectedIn) == false) {
				System.exit(exitBadParamValue);
			} else {
				System.exit(exitOk);
			}
		}
	}

}
