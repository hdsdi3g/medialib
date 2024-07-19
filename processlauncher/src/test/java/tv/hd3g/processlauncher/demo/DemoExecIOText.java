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

	public static final String EXPECTED_OUT = "OUT";
	public static final String EXPECTED_ERR = "ERR";
	public static final String EXPECTED_IN = "InputValue\\1";

	public static final int EXIT_OK = 1;
	public static final int EXIT_BAD_PARAM_LEN = 2;
	public static final int EXIT_BAD_PARAM_VALUE = 3;
	public static final int EXIT_BAD_ENV = 4;
	public static final int EXIT_BAD_IMPORT_ENV = 5;

	public static final String ENV_KEY = "EnvKey";
	public static final String ENV_VALUE = "EnvValue";

	public static void main(final String[] args) {
		System.out.println(EXPECTED_OUT);
		System.err.println(EXPECTED_ERR);

		if (System.getenv().getOrDefault(ENV_KEY, "").equals(ENV_VALUE) == false) {
			System.exit(EXIT_BAD_ENV);
		}

		if (System.getenv().containsKey("PATH") == false) {
			System.exit(EXIT_BAD_IMPORT_ENV);
		}

		if (args.length != 1) {
			System.exit(EXIT_BAD_PARAM_LEN);
		} else {
			if (args[0].equals(EXPECTED_IN) == false) {
				System.exit(EXIT_BAD_PARAM_VALUE);
			} else {
				System.exit(EXIT_OK);
			}
		}
	}

}
