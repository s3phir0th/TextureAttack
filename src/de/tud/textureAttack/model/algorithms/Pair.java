/*******************************************************************************
 * Copyright (c) 2013 Sebastian Funke.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Sebastian Funke - initial API and implementation
 ******************************************************************************/
package de.tud.textureAttack.model.algorithms;

public class Pair<KEY, VALUE> {
	private KEY key;
	private VALUE value;

	public Pair(KEY l, VALUE r) {
		this.key = l;
		this.value = r;
	}

	public KEY getKey() {
		return key;
	}

	public VALUE getValue() {
		return value;
	}

	public void setKey(KEY key) {
		this.key = key;
	}

	public void setValue(VALUE value) {
		this.value = value;
	}

	public String toString() {
		return "(" + key + ", " + value + ")";
	}

}
