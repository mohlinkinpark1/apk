package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PlatformStats(
    @Json(name = "revenus") val revenues: Double? = null,
    @Json(name = "revenue") val revenue: Double? = null,
    @Json(name = "totalRevenue") val totalRevenue: Double? = null,
    @Json(name = "occupancyRate") val occupancyRate: Double? = null,
    @Json(name = "occupancy") val occupancy: Double? = null,
    @Json(name = "listingsCount") val listingsCount: Int? = null,
    @Json(name = "totalListings") val totalListings: Int? = null,
    @Json(name = "listings") val listings: Int? = null,
    @Json(name = "bookingsCount") val bookingsCount: Int? = null,
    @Json(name = "totalBookings") val totalBookings: Int? = null,
    @Json(name = "bookings") val bookings: Int? = null
) {
    val displayRevenue: Double
        get() = revenues ?: revenue ?: totalRevenue ?: 0.0

    val displayOccupancy: Double
        get() = occupancyRate ?: occupancy ?: 0.0

    val displayListingsCount: Int
        get() = listingsCount ?: totalListings ?: listings ?: 0

    val displayBookingsCount: Int
        get() = bookingsCount ?: totalBookings ?: bookings ?: 0
}
