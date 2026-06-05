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
    @Json(name = "location") val location: String? = null,
    @Json(name = "beds") val beds: Int? = null,
    @Json(name = "type") val type: String? = "Villa",
    @Json(name = "isAvailable") val isAvailable: Boolean = true
)
