package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.NetworkManager
import com.example.data.model.Booking
import com.example.data.model.Listing
import com.example.data.model.PlatformStats
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    object Loading : UiState<Nothing>
    data class Success<out T>(val data: T) : UiState<T>
    data class Error(val exception: Throwable, val message: String) : UiState<Nothing>
}

class RentalViewModel(application: Application) : AndroidViewModel(application) {

    private val networkManager = NetworkManager(application)

    private val _listingsState = MutableStateFlow<UiState<List<Listing>>>(UiState.Loading)
    val listingsState: StateFlow<UiState<List<Listing>>> = _listingsState.asStateFlow()

    private val _bookingsState = MutableStateFlow<UiState<List<Booking>>>(UiState.Loading)
    val bookingsState: StateFlow<UiState<List<Booking>>> = _bookingsState.asStateFlow()

    private val _statsState = MutableStateFlow<UiState<PlatformStats>>(UiState.Loading)
    val statsState: StateFlow<UiState<PlatformStats>> = _statsState.asStateFlow()

    private val _baseUrlState = MutableStateFlow(networkManager.baseUrl)
    val baseUrlState: StateFlow<String> = _baseUrlState.asStateFlow()

    private val _adminTokenState = MutableStateFlow(networkManager.adminToken)
    val adminTokenState: StateFlow<String> = _adminTokenState.asStateFlow()

    // Real-time messages for operations (e.g. "Annonce supprimée", "Statut mis à jour")
    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    init {
        refreshAll()
    }

    fun refreshAll() {
        fetchStats()
        fetchListings()
        fetchBookings()
    }

    fun updateConnectionSettings(newBaseUrl: String, newToken: String) {
        viewModelScope.launch {
            try {
                networkManager.baseUrl = newBaseUrl
                networkManager.adminToken = newToken
                _baseUrlState.value = networkManager.baseUrl
                _adminTokenState.value = networkManager.adminToken
                _toastMessage.emit("Paramètres de connexion sauvegardés")
                refreshAll()
            } catch (e: Exception) {
                _toastMessage.emit("Erreur de format d'URL : ${e.localizedMessage}")
            }
        }
    }

    fun fetchListings() {
        viewModelScope.launch {
            _listingsState.value = UiState.Loading
            try {
                val service = networkManager.getService()
                val listings = service.getListings(networkManager.adminToken)
                _listingsState.value = UiState.Success(listings)
            } catch (e: Exception) {
                e.printStackTrace()
                _listingsState.value = UiState.Error(e, e.localizedMessage ?: "Erreur de chargement des annonces")
            }
        }
    }

    fun fetchBookings() {
        viewModelScope.launch {
            _bookingsState.value = UiState.Loading
            try {
                val service = networkManager.getService()
                val bookings = service.getBookings(networkManager.adminToken)
                _bookingsState.value = UiState.Success(bookings)
            } catch (e: Exception) {
                e.printStackTrace()
                _bookingsState.value = UiState.Error(e, e.localizedMessage ?: "Erreur de chargement des réservations")
            }
        }
    }

    fun fetchStats() {
        viewModelScope.launch {
            _statsState.value = UiState.Loading
            try {
                val service = networkManager.getService()
                val stats = service.getStats(networkManager.adminToken)
                _statsState.value = UiState.Success(stats)
            } catch (e: Exception) {
                e.printStackTrace()
                _statsState.value = UiState.Error(e, e.localizedMessage ?: "Erreur de chargement des statistiques")
            }
        }
    }

    fun createListing(listing: Listing, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val service = networkManager.getService()
                service.createListing(networkManager.adminToken, listing)
                _toastMessage.emit("Annonce '${listing.title ?: ""}' créée avec succès !")
                refreshAll()
                onComplete(true)
            } catch (e: Exception) {
                _toastMessage.emit("Erreur création : ${e.localizedMessage}")
                onComplete(false)
            }
        }
    }

    fun toggleListingAvailability(listing: Listing) {
        viewModelScope.launch {
            val updatedAvailable = !listing.isAvailable
            try {
                val service = networkManager.getService()
                val id = listing.id ?: return@launch
                service.updateListing(
                    token = networkManager.adminToken,
                    id = id,
                    fields = mapOf(
                        "isAvailable" to updatedAvailable,
                        "available" to updatedAvailable
                    )
                )
                _toastMessage.emit("Disponibilité de '${listing.title ?: ""}' mise à jour")
                // Quick optimistic update or simple refetch
                refreshAll()
            } catch (e: Exception) {
                _toastMessage.emit("Erreur de modification : ${e.localizedMessage}")
            }
        }
    }

    fun updateListingPrice(listing: Listing, newPrice: Double, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val service = networkManager.getService()
                val id = listing.id ?: return@launch
                service.updateListing(
                    token = networkManager.adminToken,
                    id = id,
                    fields = mapOf(
                        "pricePerDay" to newPrice,
                        "price" to newPrice
                    )
                )
                _toastMessage.emit("Prix de '${listing.title ?: ""}' mis à jour à $newPrice €")
                refreshAll()
                onComplete(true)
            } catch (e: Exception) {
                _toastMessage.emit("Erreur modification prix : ${e.localizedMessage}")
                onComplete(false)
            }
        }
    }

    fun deleteListing(listing: Listing) {
        viewModelScope.launch {
            try {
                val service = networkManager.getService()
                val id = listing.id ?: return@launch
                service.deleteListing(networkManager.adminToken, id)
                _toastMessage.emit("Annonce '${listing.title ?: ""}' supprimée")
                refreshAll()
            } catch (e: Exception) {
                _toastMessage.emit("Erreur de suppression : ${e.localizedMessage}")
            }
        }
    }

    fun updateBookingStatus(booking: Booking, newStatus: String) {
        viewModelScope.launch {
            try {
                val service = networkManager.getService()
                val id = booking.id ?: return@launch
                service.updateBookingStatus(
                    token = networkManager.adminToken,
                    id = id,
                    statusChange = mapOf("status" to newStatus)
                )
                val statusText = when (newStatus) {
                    "confirmed" -> "confirmée"
                    "cancelled" -> "annulée"
                    else -> "mise en attente"
                }
                _toastMessage.emit("Réservation de ${booking.customerName ?: "Client"} $statusText !")
                refreshAll()
            } catch (e: Exception) {
                _toastMessage.emit("Erreur statut réservation : ${e.localizedMessage}")
            }
        }
    }
}
