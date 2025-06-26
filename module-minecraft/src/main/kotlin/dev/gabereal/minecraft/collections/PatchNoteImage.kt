package dev.gabereal.minecraft.collections

import kotlinx.serialization.Serializable

@Serializable
public data class PatchNoteImage(
	val title: String,
	val url: String,
)
