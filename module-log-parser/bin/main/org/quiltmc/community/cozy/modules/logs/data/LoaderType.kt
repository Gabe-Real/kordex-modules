/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.quiltmc.community.cozy.modules.logs.data

public sealed class LoaderType(public val name: String) {
	public object Fabric : LoaderType("fabric")
	public object Forge : LoaderType("forge")
	public object Quilt : LoaderType("quilt")

	// Plugin platforms
	public object Paper : LoaderType("paper")
	public object Spigot : LoaderType("spigot")
	public object Bukkit : LoaderType("bukkit")
	public object Velocity : LoaderType("velocity")
	public object Bungeecord : LoaderType("bungeecord")
	public object Waterfall : LoaderType("waterfall")
}
