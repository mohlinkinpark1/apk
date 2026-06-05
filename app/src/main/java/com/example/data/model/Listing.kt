package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Listing(
    @Json(name = "id") val id: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "pricePerDay") val pricePerDay: Double? = null,
    @Json(name = "image") val image: String? = null,
    @Json(name = "images") val images: List<String>? = null,
    @Json(name = "location") val location: String? = null,
    @Json(name = "beds") val beds: Int? = null,
    @Json(name = "capacity") val capacity: Int? = null,
    @Json(name = "type") val type: String? = "Villa",
    @Json(name = "category") val category: String? = null,
    @Json(name = "isAvailable") val isAvailable: Boolean = true,
    @Json(name = "available") val available: Boolean? = null
) {
    val displayImage: String?
        get() = image ?: images?.firstOrNull()

    val displayBeds: Int
        get() = beds ?: capacity ?: 0

    val displayAvailable: Boolean
        get() = available ?: isAvailable
        
    val displayType: String
        get() = type ?: category ?: "Villa"
}
