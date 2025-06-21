/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.quiltmc.community.cozy.modules.logs.services

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.quiltmc.community.cozy.modules.logs.data.Log

@Serializable
private data class MclogsUploadResponse(
	val success: Boolean,
	val id: String? = null,
	val url: String? = null,
	val error: String? = null
)

/**
 * Service for uploading logs to mclo.gs
 */
public class MclogsUploadService(private val client: HttpClient) {
	private val logger: KLogger = KotlinLogging.logger("org.quiltmc.community.cozy.modules.logs.services.MclogsUploadService")
	private val json = Json { ignoreUnknownKeys = true }
	/**
	 * Upload a log to mclo.gs
	 * @param log The log to upload
	 * @return The mclo.gs URL if successful, null otherwise
	 */
	public suspend fun uploadLog(log: Log): String? {
		try {			val response = client.submitForm(
				url = "https://api.mclo.gs/1/log",
				formParameters = parameters {
					append("content", log.content)
				}
			)

			if (response.status.isSuccess()) {
				val responseText = response.bodyAsText()
				val uploadResponse = json.decodeFromString<MclogsUploadResponse>(responseText)
				
				if (uploadResponse.success && uploadResponse.url != null) {
					logger.info { "Successfully uploaded log to mclo.gs: ${uploadResponse.url}" }
					return uploadResponse.url
				} else {
					logger.error { "mclo.gs upload failed: ${uploadResponse.error}" }
				}
			} else {
				logger.error { "mclo.gs upload failed with status: ${response.status}" }
			}
		} catch (e: Exception) {
			logger.error(e) { "Failed to upload log to mclo.gs" }
		}

		return null
	}
}
