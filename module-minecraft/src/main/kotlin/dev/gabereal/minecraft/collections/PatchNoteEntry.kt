package dev.gabereal.minecraft.collections

import kotlinx.serialization.Serializable

@Serializable
public data class PatchNoteEntry(
	val contentPath: String,
	val id: String,
	val image: PatchNoteImage,
	val title: String,
	val type: String,
	val version: String,
)
