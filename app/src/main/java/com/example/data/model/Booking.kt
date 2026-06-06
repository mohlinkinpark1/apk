package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Booking(
    @Json(name = "id") val id: String? = null,
    @Json(name = "listingId") val listingId: String? = null,
    @Json(name = "listingTitle") val listingTitle: String? = null,
    @Json(name = "clientName") val clientName: String? = null,
    @Json(name = "clientPhone") val clientPhone: String? = null,
    @Json(name = "startDate") val startDate: String? = null,
    @Json(name = "endDate") val endDate: String? = null,
    @Json(name = "totalPrice") val totalPrice: Double? = null,
    @Json(name = "status") val status: String? = "pending"
)
