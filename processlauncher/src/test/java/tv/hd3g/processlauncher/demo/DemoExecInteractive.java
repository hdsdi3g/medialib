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

import java.util.Scanner;

public class DemoExecInteractive {

	public static final String QUIT = "q";

	public static void main(final String[] args) {
		System.out.println(args[0].toUpperCase());

		final var s = new Scanner(System.in);
		while (s.hasNext()) {
			final var line = s.next();
			if (line.equals(QUIT)) {
				break;
			}
			System.out.println(line.toUpperCase());
		}

		s.close();
	}

}
