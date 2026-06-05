package com.example

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.api.NetworkManager
import com.example.data.model.Booking
import com.example.data.model.Listing
import com.example.data.model.PlatformStats
import com.example.ui.RentalViewModel
import com.example.ui.UiState
import com.example.ui.theme.MyApplicationTheme
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID

// Palette design tokens matching the Bento HTML Theme Guidelines
val BentoBg = Color(0xFFF7F9FC)
val BentoIndigo = Color(0xFF4F46E5)
val BentoIndigoLight = Color(0xFFEEF2FF)
val BentoSlateDark = Color(0xFF0F172A)
val BentoSlateText = Color(0xFF64748B)
val BentoSlateLight = Color(0xFF94A3B8)
val BentoOrange = Color(0xFFEA580C)
val BentoOrangeLight = Color(0xFFFFF7ED)
val BentoBlue = Color(0xFF2563EB)
val BentoBlueLight = Color(0xFFEFF6FF)
val BentoBorder = Color(0xFFE2E8F0)

class MainActivity : ComponentActivity() {
    private val viewModel: RentalViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: RentalViewModel) {
    val context = LocalContext.current
    
    // Tab states: 0 = Stats, 1 = Listings, 2 = Bookings
    var currentTab by remember { mutableStateOf(0) }
    var isSettingsOpen by remember { mutableStateOf(false) }
    var isCreateListingOpen by remember { mutableStateOf(false) }
    var isEditPriceOpenForListing by remember { mutableStateOf<Listing?>(null) }

    val baseUrl by viewModel.baseUrlState.collectAsState()
    val adminToken by viewModel.adminTokenState.collectAsState()

    // Receive toast notifications live
    LaunchedEffect(Unit) {
        viewModel.toastMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .background(BentoBg),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Admin Console",
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp,
                            color = BentoSlateDark,
                            letterSpacing = (-1).sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFF22C55E), CircleShape)
                            )
                            Text(
                                text = "API CONNECTED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoSlateText,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.refreshAll() },
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(36.dp)
                            .background(BentoIndigoLight, CircleShape)
                            .testTag("refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Rafraîchir les données",
                            tint = BentoIndigo,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = { isSettingsOpen = true },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(36.dp)
                            .background(BentoIndigoLight, CircleShape)
                            .testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Paramètres de connexion",
                            tint = BentoIndigo,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BentoBg,
                    titleContentColor = BentoSlateDark,
                    actionIconContentColor = BentoIndigo
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = { Icon(Icons.Default.Info, contentDescription = "Statistiques") },
                    label = { Text("Stats", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BentoIndigo,
                        unselectedIconColor = BentoSlateLight,
                        selectedTextColor = BentoSlateDark,
                        unselectedTextColor = BentoSlateLight,
                        indicatorColor = BentoIndigoLight
                    ),
                    modifier = Modifier.testTag("nav_tab_stats")
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Annonces") },
                    label = { Text("Annonces", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BentoIndigo,
                        unselectedIconColor = BentoSlateLight,
                        selectedTextColor = BentoSlateDark,
                        unselectedTextColor = BentoSlateLight,
                        indicatorColor = BentoIndigoLight
                    ),
                    modifier = Modifier.testTag("nav_tab_listings")
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Réservations") },
                    label = { Text("Réservations", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BentoIndigo,
                        unselectedIconColor = BentoSlateLight,
                        selectedTextColor = BentoSlateDark,
                        unselectedTextColor = BentoSlateLight,
                        indicatorColor = BentoIndigoLight
                    ),
                    modifier = Modifier.testTag("nav_tab_bookings")
                )
            }
        },
        floatingActionButton = {
            if (currentTab == 1) {
                FloatingActionButton(
                    onClick = { isCreateListingOpen = true },
                    containerColor = BentoIndigo,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("add_listing_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Ajouter une annonce")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BentoBg)
        ) {
            when (currentTab) {
                0 -> StatsTabContent(viewModel = viewModel, onNavigateToBookings = { currentTab = 2 })
                1 -> ListingsTabContent(
                    viewModel = viewModel,
                    onEditPriceUrl = { isEditPriceOpenForListing = it }
                )
                2 -> BookingsTabContent(viewModel = viewModel)
            }
        }
    }

    // Settings Configuration Dialog
    if (isSettingsOpen) {
        ConnectionSettingsDialog(
            currentBaseUrl = baseUrl,
            currentToken = adminToken,
            onDismiss = { isSettingsOpen = false },
            onSave = { newUrl, newToken ->
                viewModel.updateConnectionSettings(newUrl, newToken)
                isSettingsOpen = false
            }
        )
    }

    // Add Listing Dialog
    if (isCreateListingOpen) {
        CreateListingDialog(
            onDismiss = { isCreateListingOpen = false },
            onCreate = { newListing ->
                viewModel.createListing(newListing) { success ->
                    if (success) {
                        isCreateListingOpen = false
                    }
                }
            }
        )
    }

    // Edit Price Dialog
    isEditPriceOpenForListing?.let { listing ->
        EditPriceDialog(
            listing = listing,
            onDismiss = { isEditPriceOpenForListing = null },
            onSave = { newPrice ->
                viewModel.updateListingPrice(listing, newPrice) { success ->
                    if (success) {
                        isEditPriceOpenForListing = null
                    }
                }
            }
        )
    }
}

// 📊 STATS TAB CONTENT (BENTO GRID PATTERN)
@Composable
fun StatsTabContent(viewModel: RentalViewModel, onNavigateToBookings: () -> Unit) {
    val statsState by viewModel.statsState.collectAsState()
    val bookingsState by viewModel.bookingsState.collectAsState()

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (val state = statsState) {
            is UiState.Loading -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = BentoIndigo)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Chargement des statistiques...", style = MaterialTheme.typography.bodyMedium, color = BentoSlateText)
                }
            }
            is UiState.Error -> {
                ErrorView(
                    message = state.message,
                    onRetry = { viewModel.fetchStats() }
                )
            }
            is UiState.Success -> {
                val stats = state.data
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    
                    // 1. REVENUE CARD (Huge highlight bento container - indigo themed)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(32.dp),
                        colors = CardDefaults.cardColors(containerColor = BentoIndigo)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Text(
                                text = "Global Revenue",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = String.format("€%,.2f", stats.displayRevenue),
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = (-1).sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(Color.White.copy(alpha = 0.18f), CircleShape)
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "+12% this month",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                // Simulated visual bar chart metrics on the bottom right
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Box(modifier = Modifier.size(4.dp, 12.dp).background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(2.dp)))
                                    Box(modifier = Modifier.size(4.dp, 20.dp).background(Color.White.copy(alpha = 0.4f), RoundedCornerShape(2.dp)))
                                    Box(modifier = Modifier.size(4.dp, 28.dp).background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(2.dp)))
                                    Box(modifier = Modifier.size(4.dp, 36.dp).background(Color.White, RoundedCornerShape(2.dp)))
                                }
                            }
                        }
                    }

                    // 2. GRID OF STANDARD CARDS (Split side-by-side row)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Left: Listings Card (col-span-1, row-span-2)
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Rounded square emoji holder
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(BentoOrangeLight, RoundedCornerShape(14.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🏠", fontSize = 20.sp)
                                }
                                Column {
                                    Text(
                                        text = "LISTINGS",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoSlateLight,
                                        letterSpacing = 0.5.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = stats.displayListingsCount.toString(),
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Black,
                                        color = BentoSlateDark
                                    )
                                }
                            }
                        }

                        // Right: Occupancy Card (col-span-1, row-span-2)
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Rounded square emoji holder
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(BentoBlueLight, RoundedCornerShape(14.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("📅", fontSize = 20.sp)
                                }
                                Column {
                                    Text(
                                        text = "OCCUPANCY",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoSlateLight,
                                        letterSpacing = 0.5.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = String.format("%.0f%%", stats.displayOccupancy),
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Black,
                                        color = BentoSlateDark
                                    )
                                }
                            }
                        }
                    }

                    // 3. RECENT BOOKING SECTION (Wide container - dark slate background)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = BentoSlateDark)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Recent Bookings",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Activity logs",
                                        fontSize = 11.sp,
                                        color = BentoSlateLight
                                    )
                                }
                                Text(
                                    text = "View All",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF818CF8),
                                    modifier = Modifier
                                        .clickable { onNavigateToBookings() }
                                        .padding(4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            // Extract bottom items from DB state if available, or fallback to mock
                            val bookingsList = when (val bs = bookingsState) {
                                is UiState.Success -> bs.data.take(2)
                                else -> emptyList()
                            }

                            if (bookingsList.isNotEmpty()) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    bookingsList.forEach { booking ->
                                        BentoBookingRow(booking)
                                    }
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    BentoDemoBookingRow(
                                        initials = "JD",
                                        title = "Villa Horizon",
                                        period = "July 12 - July 15",
                                        status = "pending"
                                    )
                                    BentoDemoBookingRow(
                                        initials = "MS",
                                        title = "Ocean Suite",
                                        period = "July 10 - July 12",
                                        status = "confirmed"
                                    )
                                }
                            }
                        }
                    }

                    // 4. API & SYSTEM STATUS CARD
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(BentoIndigoLight, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = BentoIndigo,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Système en direct",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoSlateDark
                                )
                                Text(
                                    text = "Pour rafraîchir manuellement, utilisez le bouton de synchronisation en haut.",
                                    fontSize = 11.sp,
                                    color = BentoSlateText,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// 🏠 LISTINGS TAB CONTENT
@Composable
fun ListingsTabContent(
    viewModel: RentalViewModel,
    onEditPriceUrl: (Listing) -> Unit
) {
    val listingsState by viewModel.listingsState.collectAsState()

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (val state = listingsState) {
            is UiState.Loading -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = BentoIndigo)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Chargement des annonces...", style = MaterialTheme.typography.bodyMedium, color = BentoSlateText)
                }
            }
            is UiState.Error -> {
                ErrorView(
                    message = state.message,
                    onRetry = { viewModel.fetchListings() }
                )
            }
            is UiState.Success -> {
                val listings = state.data
                if (listings.isEmpty()) {
                    EmptyStateView(
                        message = "Aucune annonce disponible sur la plateforme.",
                        subtitle = "Cliquez sur le bouton '+' en bas pour ajouter votre première villa."
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                            .also { Modifier.padding(vertical = 12.dp) }
                    ) {
                        items(listings) { listing ->
                            ListingItemCard(
                                listing = listing,
                                onAvailabilityChange = { viewModel.toggleListingAvailability(listing) },
                                onEditPrice = { onEditPriceUrl(listing) },
                                onDelete = { viewModel.deleteListing(listing) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ListingItemCard(
    listing: Listing,
    onAvailabilityChange: () -> Unit,
    onEditPrice: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        DeleteConfirmDialog(
            title = listing.title ?: "",
            onDismiss = { showDeleteConfirm = false },
            onConfirm = {
                showDeleteConfirm = false
                onDelete()
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("listing_card_${listing.id ?: "new"}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder)
    ) {
        Column {
            // Header Image Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0xFFF1F5F9))
            ) {
                // Remote Image using Coil
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(listing.image)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Image de la villa",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    alignment = Alignment.Center
                )

                // Location Pill at Top Left
                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .align(Alignment.TopStart)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = listing.location ?: "",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Type Badge at Top Right
                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .background(BentoIndigoLight, RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Text(
                        text = listing.type ?: "Villa",
                        color = BentoIndigo,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Gradient Overlay at bottom of the picture
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                            )
                        )
                )

                // Price display in overlay
                Text(
                    text = "${(listing.pricePerDay ?: 0.0).toInt()} € / jour",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                )
            }

            // Description block & Beds
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = listing.title ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BentoSlateDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = BentoSlateText
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Capacité : ${listing.beds ?: 0} lits",
                        style = MaterialTheme.typography.bodySmall,
                        color = BentoSlateText
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = listing.description ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BentoSlateText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Availability toggle row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color(0xFFF8FAFC),
                            RoundedCornerShape(14.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (listing.isAvailable) "Disponible à la location" else "Masquée / Indisponible",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (listing.isAvailable) Color(0xFF10B981) else Color(0xFFEF4444)
                        )
                        Text(
                            text = "Statut en direct sur le web",
                            style = MaterialTheme.typography.labelSmall,
                            color = BentoSlateLight
                        )
                    }
                    Switch(
                        checked = listing.isAvailable,
                        onCheckedChange = { onAvailabilityChange() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = BentoIndigo,
                            uncheckedThumbColor = BentoSlateLight,
                            uncheckedTrackColor = BentoBorder
                        ),
                        modifier = Modifier.testTag("availability_switch_${listing.id ?: ""}")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onEditPrice,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("edit_price_btn_${listing.id ?: ""}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = BentoSlateDark
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Modifier prix", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("delete_btn_${listing.id ?: ""}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFEE2E2),
                            contentColor = Color(0xFFEF4444)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Supprimer", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// 📅 BOOKINGS TAB CONTENT
@Composable
fun BookingsTabContent(viewModel: RentalViewModel) {
    val bookingsState by viewModel.bookingsState.collectAsState()

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (val state = bookingsState) {
            is UiState.Loading -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = BentoIndigo)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Chargement des réservations...", style = MaterialTheme.typography.bodyMedium, color = BentoSlateText)
                }
            }
            is UiState.Error -> {
                ErrorView(
                    message = state.message,
                    onRetry = { viewModel.fetchBookings() }
                )
            }
            is UiState.Success -> {
                val bookings = state.data
                if (bookings.isEmpty()) {
                    EmptyStateView(
                        message = "Aucune réservation client trouvée.",
                        subtitle = "Les demandes apparaîtront ici dès qu'un utilisateur réservera une de vos villas."
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                            .also { Modifier.padding(vertical = 12.dp) }
                    ) {
                        items(bookings) { booking ->
                            BookingItemCard(
                                booking = booking,
                                onStatusUpdate = { newStatus ->
                                    viewModel.updateBookingStatus(booking, newStatus)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookingItemCard(
    booking: Booking,
    onStatusUpdate: (String) -> Unit
) {
    val currentStatus = booking.status ?: "pending"

    // Set colors for the status chip matching bento style
    val (statusText, statusBg, statusColor) = when (currentStatus) {
        "confirmed" -> Triple("Confirmée", Color(0xFF10B981).copy(alpha = 0.15f), Color(0xFF10B981))
        "cancelled" -> Triple("Annulée", Color(0xFFEF4444).copy(alpha = 0.15f), Color(0xFFEF4444))
        else -> Triple("En attente", Color(0xFFF59E0B).copy(alpha = 0.15f), Color(0xFFF59E0B))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("booking_card_${booking.id ?: "new"}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Customer Header and Status Badge Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Client : ${booking.customerName ?: "Utilisateur Anonyme"}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BentoSlateDark
                    )
                    Text(
                        text = "ID: ${booking.id?.take(8) ?: "N/A"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = BentoSlateLight
                    )
                }
                Box(
                    modifier = Modifier
                        .background(statusBg, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Listing title
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF8FAFC)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        tint = BentoIndigo,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = booking.listingTitle ?: "Villa non répertoriée",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = BentoSlateDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Booking Details Layout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Dates Column
                Column {
                    Text(
                        text = "Période de séjour",
                        style = MaterialTheme.typography.labelSmall,
                        color = BentoSlateLight
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = BentoSlateText
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${booking.startDate} au ${booking.endDate}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = BentoSlateDark
                        )
                    }
                }

                // Balance due Column
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Montant Total",
                        style = MaterialTheme.typography.labelSmall,
                        color = BentoSlateLight
                    )
                    Text(
                        text = String.format("%.2f €", booking.totalPrice ?: 0.0),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = BentoIndigo
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Actions panel for booking status change
            Column {
                Text(
                    text = "Gérer la réservation :",
                    style = MaterialTheme.typography.labelSmall,
                    color = BentoSlateText,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (currentStatus != "confirmed") {
                        Button(
                            onClick = { onStatusUpdate("confirmed") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("confirm_booking_btn_${booking.id ?: ""}"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BentoIndigo,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Confirmer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (currentStatus != "cancelled") {
                        Button(
                            onClick = { onStatusUpdate("cancelled") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("cancel_booking_btn_${booking.id ?: ""}"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFEE2E2),
                                contentColor = Color(0xFFEF4444)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Annuler", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (currentStatus != "pending" && currentStatus == "cancelled") {
                        OutlinedButton(
                            onClick = { onStatusUpdate("pending") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("pending_booking_btn_${booking.id ?: ""}"),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = BentoSlateDark
                            )
                        ) {
                            Text("Rétablir", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ⚙️ CONNECTION SETTINGS DIALOG
@Composable
fun ConnectionSettingsDialog(
    currentBaseUrl: String,
    currentToken: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var url by remember { mutableStateOf(currentBaseUrl) }
    var token by remember { mutableStateOf(currentToken) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder),
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Connexion Backend",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = BentoSlateDark
                )
                Text(
                    text = "Configurez l'adresse IP / URL de votre backend web ainsi que la clé de sécurité admin pour vos communications privées.",
                    fontSize = 12.sp,
                    color = BentoSlateText
                )

                // Base URL Textfield
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Base URL (API)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("setting_input_url"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                )

                // Admin Token Textfield
                OutlinedTextField(
                    value = token,
                    onValueChange = { token = it },
                    label = { Text("Clé secrète (X-Admin-Token)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("setting_input_token"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Actions Buttons row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            url = NetworkManager.DEFAULT_BASE_URL
                            token = NetworkManager.DEFAULT_ADMIN_TOKEN
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_reset_settings"),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder)
                    ) {
                        Text("Défauts", fontWeight = FontWeight.Bold, color = BentoSlateDark)
                    }

                    Button(
                        onClick = { onSave(url, token) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BentoIndigo,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("btn_save_settings")
                    ) {
                        Text("Sauvegarder", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

fun createTempPictureUri(context: Context): Uri {
    val tempFile = File.createTempFile("img_", ".jpg", context.externalCacheDir ?: context.cacheDir).apply {
        deleteOnExit()
    }
    return FileProvider.getUriForFile(context, "com.aistudio.rentaladmin.xskydb.fileprovider", tempFile)
}

fun uriToBase64(context: Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream) ?: return null
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        val byteArray = outputStream.toByteArray()
        "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.NO_WRAP)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// ➕ CREATE LISTING DIALOG
@Composable
fun CreateListingDialog(
    onDismiss: () -> Unit,
    onCreate: (Listing) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var bedsStr by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Villa") }

    val context = LocalContext.current
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { 
            val base64 = uriToBase64(context, it)
            if (base64 != null) {
                imageUrl = base64
            }
        }
    }

    var photoUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) { 
            photoUri?.let {
                val base64 = uriToBase64(context, it)
                if (base64 != null) {
                    imageUrl = base64
                }
            }
        }
    }

    val propertyTypes = listOf("Villa", "Appartement", "Studio", "Chambre d'hôte")
    var isError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder),
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Créer une annonce",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = BentoSlateDark
                )

                if (isError) {
                    Text(
                        text = "Veuillez remplir tous les champs correctement.",
                        color = Color(0xFFEF4444),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titre de la location") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_create_title")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description détaillée") },
                    maxLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_create_desc")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        label = { Text("Prix/jour (€)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_create_price")
                    )

                    OutlinedTextField(
                        value = bedsStr,
                        onValueChange = { bedsStr = it },
                        label = { Text("Nombre lits") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_create_beds")
                    )
                }

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Emplacement (ex: Ghazaouet)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_create_location")
                )

                // Image Selection Section
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Image de l'annonce :",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoSlateDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (!imageUrl.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF1F5F9))
                        ) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = { imageUrl = "" },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                    .size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { galleryLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Galerie", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val tempUri = createTempPictureUri(context)
                                photoUri = tempUri
                                cameraLauncher.launch(tempUri)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Appareil", fontSize = 12.sp)
                        }
                    }
                    
                    // Optionnel: Garder un champ texte pour l'URL si besoin
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = imageUrl ?: "",
                        onValueChange = { imageUrl = it },
                        label = { Text("Ou URL de l'image") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp)
                    )
                }

                // Select Property Type
                Column {
                    Text(
                        text = "Type de logement :",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoSlateDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        propertyTypes.forEach { type ->
                            val isSelected = selectedType == type
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isSelected) BentoIndigo else Color(0xFFF1F5F9),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedType = type }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = type,
                                    fontSize = 10.sp,
                                    color = if (isSelected) Color.White else BentoSlateText,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_close_create"),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder)
                    ) {
                        Text("Fermer", fontWeight = FontWeight.Bold, color = BentoSlateDark)
                    }

                    Button(
                        onClick = {
                            val price = priceStr.toDoubleOrNull()
                            val beds = bedsStr.toIntOrNull()
                            if (title.isBlank() || description.isBlank() || price == null || beds == null || location.isBlank() || imageUrl.isBlank()) {
                                isError = true
                            } else {
                                isError = false
                                val item = Listing(
                                    title = title,
                                    description = description,
                                    pricePerDay = price,
                                    image = imageUrl,
                                    location = location,
                                    beds = beds,
                                    type = selectedType,
                                    isAvailable = true
                                )
                                onCreate(item)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BentoIndigo,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("btn_save_create")
                    ) {
                        Text("Créer", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ✏️ EDIT PRICE DIALOG
@Composable
fun EditPriceDialog(
    listing: Listing,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    var priceStr by remember { mutableStateOf((listing.pricePerDay ?: 0.0).toInt().toString()) }
    var isError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder),
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Modifier le prix",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = BentoSlateDark
                )
                Text(
                    text = "Modifier le tarif journalier de '${listing.title ?: ""}'.",
                    fontSize = 12.sp,
                    color = BentoSlateText
                )

                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it },
                    label = { Text("Tarif en euros (€)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_edit_price")
                )

                if (isError) {
                    Text(
                        text = "Veuillez entrer un nombre valide.",
                        color = Color(0xFFEF4444),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder)
                    ) {
                        Text("Annuler", fontWeight = FontWeight.Bold, color = BentoSlateDark)
                    }

                    Button(
                        onClick = {
                            val value = priceStr.toDoubleOrNull()
                            if (value != null && value > 0.0) {
                                isError = false
                                onSave(value)
                            } else {
                                isError = true
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BentoIndigo,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_save_edit_price")
                    ) {
                        Text("Enregistrer", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ⚠️ DELETE CONFIRM DIALOG
@Composable
fun DeleteConfirmDialog(
    title: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder),
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFEF4444)
                    )
                    Text(
                        text = "Supprimer l'annonce ?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = BentoSlateDark
                    )
                }

                Text(
                    text = "Voulez-vous vraiment supprimer définitivement l'annonce '${title}' du serveur ? Cette action est irréversible.",
                    fontSize = 12.sp,
                    color = BentoSlateText
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder)
                    ) {
                        Text("Annuler", fontWeight = FontWeight.Bold, color = BentoSlateDark)
                    }

                    Button(
                        onClick = onConfirm,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_confirm_delete"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFEE2E2),
                            contentColor = Color(0xFFEF4444)
                        )
                    ) {
                        Text("Supprimer", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Helper Components
@Composable
fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = Color(0xFFEF4444),
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Oups ! Une erreur est survenue",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = BentoSlateDark
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = message,
            fontSize = 12.sp,
            color = BentoSlateText,
            modifier = Modifier.padding(horizontal = 16.dp),
            maxLines = 4,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = BentoIndigo
            ),
            modifier = Modifier.testTag("retry_button")
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Réessayer", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun EmptyStateView(message: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = BentoSlateLight,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = BentoSlateDark
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            fontSize = 12.sp,
            color = BentoSlateText,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

// Stats Tab Row custom helpers to hold live layout bindings or fallback demo bookings nicely formatted
@Composable
fun BentoBookingRow(booking: Booking) {
    val customerInitials = booking.customerName?.split(" ")
        ?.mapByNotNullForInitials()
        ?.joinToString("")?.take(2)?.uppercase() ?: "CL"

    val status = booking.status ?: "pending"
    val (badgeText, badgeBg, badgeColor) = when (status) {
        "confirmed" -> Triple("Confirmed", Color(0xFF10B981).copy(alpha = 0.15f), Color(0xFF34D399))
        "cancelled" -> Triple("Cancelled", Color(0xFFEF4444).copy(alpha = 0.15f), Color(0xFFF87171))
        else -> Triple("Pending", Color(0xFFF59E0B).copy(alpha = 0.15f), Color(0xFFFBBF24))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = customerInitials,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(modifier = Modifier.weight(1f, fill = false)) {
                Text(
                    text = booking.listingTitle ?: "Villa non répertoriée",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${booking.startDate} - ${booking.endDate}",
                    fontSize = 11.sp,
                    color = BentoSlateLight
                )
            }
        }
        
        Box(
            modifier = Modifier
                .background(badgeBg, RoundedCornerShape(12.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = badgeText,
                color = badgeColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun BentoDemoBookingRow(
    initials: String,
    title: String,
    period: String,
    status: String
) {
    val (badgeText, badgeBg, badgeColor) = when (status) {
        "confirmed" -> Triple("Confirmed", Color(0xFF10B981).copy(alpha = 0.15f), Color(0xFF34D399))
        "cancelled" -> Triple("Cancelled", Color(0xFFEF4444).copy(alpha = 0.15f), Color(0xFFF87171))
        else -> Triple("Pending", Color(0xFFF59E0B).copy(alpha = 0.15f), Color(0xFFFBBF24))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Column {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = period,
                    fontSize = 11.sp,
                    color = BentoSlateLight
                )
            }
        }
        
        Box(
            modifier = Modifier
                .background(badgeBg, RoundedCornerShape(12.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = badgeText,
                color = badgeColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

private fun List<String>.mapByNotNullForInitials(): List<String> {
    val list = mutableListOf<String>()
    for (item in this) {
        if (item.isNotEmpty()) {
            list.add(item.first().toString())
        }
    }
    return list
}
