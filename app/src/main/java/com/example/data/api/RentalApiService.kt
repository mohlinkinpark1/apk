package com.example.data.api

import com.example.data.model.Listing
import com.example.data.model.Booking
import com.example.data.model.PlatformStats
import retrofit2.http.*

interface RentalApiService {

    @GET("listings")
    suspend fun getListings(
        @Header("X-Admin-Token") token: String,
        @Query("all") all: Boolean = true
    ): List<Listing>

    @POST("listings")
    suspend fun createListing(
        @Header("X-Admin-Token") token: String,
        @Body listing: Listing
    ): Listing

    @PATCH("listings/{id}")
    suspend fun updateListing(
        @Header("X-Admin-Token") token: String,
        @Path("id") id: String,
        @Body fields: Map<String, @JvmSuppressWildcards Any?>
    ): Listing

    @DELETE("listings/{id}")
    suspend fun deleteListing(
        @Header("X-Admin-Token") token: String,
        @Path("id") id: String
    ): Any

    @GET("bookings")
    suspend fun getBookings(
        @Header("X-Admin-Token") token: String
    ): List<Booking>

    @PATCH("bookings/{id}")
    suspend fun updateBookingStatus(
        @Header("X-Admin-Token") token: String,
        @Path("id") id: String,
        @Body statusChange: Map<String, String>
    ): Booking

    @GET("stats")
    suspend fun getStats(
        @Header("X-Admin-Token") token: String
    ): PlatformStats
}
