package dev.gabereal.minecraft.collections

import kotlinx.serialization.Serializable

@Serializable
public data class PatchNoteEntries(
	val entries: List<PatchNoteEntry>,
	val version: Int,
)
