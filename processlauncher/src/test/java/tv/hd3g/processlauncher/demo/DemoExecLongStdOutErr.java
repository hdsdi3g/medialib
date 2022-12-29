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
 * Copyright (C) hdsdi3g for hd3g.tv 2022
 *
 */
package tv.hd3g.processlauncher.demo;

public class DemoExecLongStdOutErr {

	public static final int COUNT = 1000;
	public static final String STD_OUT = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Etiam maximus pellentesque nulla, in euismod tellus blandit quis. Sed sodales augue augue, nec tempor nunc tincidunt ut. Sed ultrices pellentesque nisl. Phasellus congue neque enim, ut tincidunt elit consectetur ultrices. Duis tempus nibh id risus ultrices porta. Fusce ligula quam, feugiat ac ultrices sit amet, gravida quis dolor. Nunc euismod erat a neque consectetur ultrices. In vel imperdiet velit. Duis neque odio, tincidunt a diam sit amet, pellentesque sollicitudin augue. Nullam mollis neque sem, in mollis dolor scelerisque non. Nunc suscipit nulla consectetur, pellentesque purus et, rhoncus enim. Maecenas ornare, turpis et ultrices vulputate, nisi eros porta urna, non volutpat libero libero ac dui. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Ut bibendum fermentum interdum.";
	public static final String STD_OUT_END = "End";
	public static final String STD_ERR = "Just an error message...";

	public static void main(final String[] args) throws InterruptedException {
		var sendedErr = false;
		for (var pos = 0; pos < COUNT; pos++) {
			System.out.println(STD_OUT);
			if (sendedErr == false && pos > COUNT / 2) {
				System.err.println(STD_ERR);
				sendedErr = true;
			}
		}
		System.out.println(STD_OUT_END);
	}

}
