package com.example

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.HorizontalDivider
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
import java.io.InputStream
import java.util.UUID

// Palette design tokens - VIP Premium Theme
val VipGold = Color(0xFFD4AF37)
val VipGoldLight = Color(0xFFF9F1D0)
val VipBlack = Color(0xFF0F0F12)
val VipSlate = Color(0xFF1C1C1E)
val VipTextGold = Color(0xFFC5A028)
val VipWhite = Color(0xFFFFFFFF)
val BentoBg = Color(0xFF0A0A0A) // VIP Dark Background
val BentoIndigo = Color(0xFF5856D6)
val BentoIndigoLight = Color(0xFFE5E5EA)
val BentoSlateDark = Color(0xFF1C1C1E)
val BentoSlateText = Color(0xFF8E8E93)
val BentoSlateLight = Color(0xFFC7C7CC)
val BentoOrange = Color(0xFFFF9500)
val BentoOrangeLight = Color(0xFFFFF2E0)
val BentoBlue = Color(0xFF007AFF)
val BentoBlueLight = Color(0xFFE5F1FF)
val BentoBorder = Color(0xFFD1D1D6)

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
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()

    // Notification permission request for Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "Les notifications sont désactivées", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
    
    // Tab states: 0 = Stats, 1 = Listings, 2 = Bookings
    var currentTab by rememberSaveable { mutableStateOf(0) }
    var isSettingsOpen by rememberSaveable { mutableStateOf(false) }
    var isCreateListingOpen by rememberSaveable { mutableStateOf(false) }
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
                            text = "VIP Admin Console",
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp,
                            color = VipGold,
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
                                    .background(VipGold, CircleShape)
                            )
                            Text(
                                text = "PREMIUM ACCESS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = VipGold.copy(alpha = 0.7f),
                                letterSpacing = 1.sp
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
                            .background(VipSlate, CircleShape)
                            .testTag("refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Rafraîchir",
                            tint = VipGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = { isSettingsOpen = true },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(36.dp)
                            .background(VipSlate, CircleShape)
                            .testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Paramètres",
                            tint = VipGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BentoBg,
                    titleContentColor = VipGold,
                    actionIconContentColor = VipGold
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
                containerColor = VipBlack,
                tonalElevation = 12.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = { Icon(Icons.Default.Info, contentDescription = "Stats") },
                    label = { Text("Stats", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = VipGold,
                        unselectedIconColor = BentoSlateLight,
                        selectedTextColor = VipGold,
                        unselectedTextColor = BentoSlateLight,
                        indicatorColor = VipSlate
                    ),
                    modifier = Modifier.testTag("nav_tab_stats")
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Annonces") },
                    label = { Text("Biens", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = VipGold,
                        unselectedIconColor = BentoSlateLight,
                        selectedTextColor = VipGold,
                        unselectedTextColor = BentoSlateLight,
                        indicatorColor = VipSlate
                    ),
                    modifier = Modifier.testTag("nav_tab_listings")
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Réservations") },
                    label = { Text("Réservations", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = VipGold,
                        unselectedIconColor = BentoSlateLight,
                        selectedTextColor = VipGold,
                        unselectedTextColor = BentoSlateLight,
                        indicatorColor = VipSlate
                    ),
                    modifier = Modifier.testTag("nav_tab_bookings")
                )
            }
        },
        floatingActionButton = {
            if (currentTab == 1) {
                FloatingActionButton(
                    onClick = { isCreateListingOpen = true },
                    containerColor = VipGold,
                    contentColor = VipBlack,
                    shape = CircleShape,
                    modifier = Modifier.testTag("add_listing_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Ajouter")
                }
            }
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refreshAll() },
            state = pullToRefreshState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
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
            viewModel = viewModel,
            onDismiss = { isCreateListingOpen = false },
            onCreate = { newListing ->
                viewModel.createListing(newListing) { success ->
                    if (success) {
                        viewModel.resetCreateForm()
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
                    
                    // 1. REVENUE CARD (VIP Gold highlighted card)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(32.dp),
                        colors = CardDefaults.cardColors(containerColor = VipBlack),
                        border = androidx.compose.foundation.BorderStroke(1.dp, VipGold)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Total Revenues",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = VipGold.copy(alpha = 0.8f)
                                )
                                Icon(Icons.Default.Star, contentDescription = null, tint = VipGold, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = String.format("%,.2f DA", stats.displayRevenue),
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Black,
                                color = VipGold,
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
                                        .background(VipGold.copy(alpha = 0.15f), CircleShape)
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "VIP STATUS ACTIVE",
                                        color = VipGold,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                // Simulated visual bar chart metrics on the bottom right
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Box(modifier = Modifier.size(4.dp, 12.dp).background(VipGold.copy(alpha = 0.2f), RoundedCornerShape(2.dp)))
                                    Box(modifier = Modifier.size(4.dp, 20.dp).background(VipGold.copy(alpha = 0.4f), RoundedCornerShape(2.dp)))
                                    Box(modifier = Modifier.size(4.dp, 28.dp).background(VipGold.copy(alpha = 0.6f), RoundedCornerShape(2.dp)))
                                    Box(modifier = Modifier.size(4.dp, 36.dp).background(VipGold, RoundedCornerShape(2.dp)))
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
                            colors = CardDefaults.cardColors(containerColor = VipSlate),
                            border = androidx.compose.foundation.BorderStroke(1.dp, VipGold.copy(alpha = 0.2f))
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
                                        .background(VipGold.copy(alpha = 0.1f), RoundedCornerShape(14.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Home, contentDescription = null, tint = VipGold, modifier = Modifier.size(22.dp))
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
                                        color = VipGold
                                    )
                                }
                            }
                        }

                        // Right: Occupancy Card (col-span-1, row-span-2)
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(containerColor = VipSlate),
                            border = androidx.compose.foundation.BorderStroke(1.dp, VipGold.copy(alpha = 0.2f))
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
                                        .background(VipGold.copy(alpha = 0.1f), RoundedCornerShape(14.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.DateRange, contentDescription = null, tint = VipGold, modifier = Modifier.size(22.dp))
                                }
                                Column {
                                    Text(
                                        text = "ACTIVE",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoSlateLight,
                                        letterSpacing = 0.5.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = stats.bookingsCount.toString(),
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Black,
                                        color = VipGold
                                    )
                                }
                            }
                        }
                    }

                    // 3. RECENT BOOKING SECTION (Wide container - dark slate background)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = VipSlate),
                        border = androidx.compose.foundation.BorderStroke(1.dp, VipGold.copy(alpha = 0.3f))
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
                                        color = VipGold
                                    )
                                    Text(
                                        text = "VIP Activity Logs",
                                        fontSize = 11.sp,
                                        color = VipGold.copy(alpha = 0.5f)
                                    )
                                }
                                Text(
                                    text = "View All",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VipGold,
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
                                }
                            }
                        }
                    }

                    // 4. API & SYSTEM STATUS CARD
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = VipSlate),
                        border = androidx.compose.foundation.BorderStroke(1.dp, VipGold.copy(alpha = 0.1f))
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
                                    .background(VipBlack, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = VipGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "VIP System Status",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VipWhite
                                )
                                Text(
                                    text = "Pour rafraîchir manuellement, utilisez le bouton VIP en haut ou tirez vers le bas.",
                                    fontSize = 11.sp,
                                    color = BentoSlateLight,
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
        colors = CardDefaults.cardColors(containerColor = VipWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder)
    ) {
        Column {
            // Header Image Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(VipSlate)
            ) {
                // Remote Image using Coil
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(decodeImageModel(listing.displayImage))
                        .crossfade(true)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_report_image)
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
                        .background(VipBlack.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .align(Alignment.TopStart)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = VipGold,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = listing.location ?: "",
                            color = VipWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Type Badge at Top Right
                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .background(VipGold, RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Text(
                        text = listing.displayType.uppercase(),
                        color = VipBlack,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                // Gradient Overlay at bottom of the picture
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, VipBlack.copy(alpha = 0.8f))
                            )
                        )
                )

                // Price display in overlay
                Text(
                    text = "${(listing.pricePerDay ?: 0.0).toInt()} DA / JOUR",
                    color = VipGold,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                )
            }

            // Description block & Beds
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = listing.title ?: "",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = VipBlack,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = VipTextGold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Capacité : ${listing.displayBeds} lits",
                        style = MaterialTheme.typography.bodyMedium,
                        color = BentoSlateText,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = listing.description ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BentoSlateText,
                    maxLines = 3,
                    lineHeight = 20.sp,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(20.dp))

                // Availability toggle row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(VipGoldLight.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (listing.displayAvailable) "DISPONIBLE" else "MASQUÉE",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black,
                            color = if (listing.displayAvailable) Color(0xFF15803D) else Color(0xFFB91C1C),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Statut Premium en direct",
                            style = MaterialTheme.typography.labelSmall,
                            color = BentoSlateLight
                        )
                    }
                    Switch(
                        checked = listing.displayAvailable,
                        onCheckedChange = { onAvailabilityChange() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = VipWhite,
                            checkedTrackColor = VipGold,
                            uncheckedThumbColor = BentoSlateLight,
                            uncheckedTrackColor = BentoIndigoLight
                        ),
                        modifier = Modifier.testTag("availability_switch_${listing.id ?: ""}")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onEditPrice,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("edit_price_btn_${listing.id ?: ""}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = VipBlack),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, VipGold)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp), tint = VipGold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("PRIX", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("delete_btn_${listing.id ?: ""}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VipBlack, contentColor = VipGold),
                        border = androidx.compose.foundation.BorderStroke(1.dp, VipGold.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SUPPRIMER", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
        colors = CardDefaults.cardColors(containerColor = VipWhite),
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(VipGoldLight, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = booking.clientName?.firstOrNull()?.toString()?.uppercase() ?: "C",
                                color = VipTextGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = booking.clientName ?: "Anonyme",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = VipBlack
                        )
                    }
                    if (!booking.clientPhone.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp), tint = VipTextGold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = booking.clientPhone,
                                style = MaterialTheme.typography.bodyMedium,
                                color = VipBlack.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .background(statusBg, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = statusText.uppercase(),
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Listing title
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = VipBlack),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Home, contentDescription = null, tint = VipGold, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = booking.listingTitle ?: "Propriété VIP",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = VipGold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Booking Details Layout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dates Column
                Column {
                    Text(
                        text = "SÉJOUR",
                        style = MaterialTheme.typography.labelSmall,
                        color = BentoSlateLight,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${booking.startDate} ➔ ${booking.endDate}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = VipBlack
                    )
                }

                // Balance due Column
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "TOTAL",
                        style = MaterialTheme.typography.labelSmall,
                        color = BentoSlateLight,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = String.format("%,.0f DA", booking.totalPrice ?: 0.0),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = VipBlack
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Actions panel
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (currentStatus != "confirmed") {
                    Button(
                        onClick = { onStatusUpdate("confirmed") },
                        modifier = Modifier.weight(1f).height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VipGold, contentColor = VipBlack),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("CONFIRMER", fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                }

                if (currentStatus != "cancelled") {
                    OutlinedButton(
                        onClick = { onStatusUpdate("cancelled") },
                        modifier = Modifier.weight(1f).height(44.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFB91C1C)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFB91C1C)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("ANNULER", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                    text = "Configurez l'adresse URL de votre API. Si vous utilisez Render, n'oubliez pas d'ajouter '/api/' à la fin si nécessaire.",
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

fun createTempPictureUri(context: Context): Uri? {
    return try {
        val storageDir = context.cacheDir
        val tempFile = File(storageDir, "capture_${System.currentTimeMillis()}.jpg")
        if (tempFile.exists()) tempFile.delete()
        tempFile.createNewFile()
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", tempFile)
    } catch (e: Exception) {
        android.util.Log.e("ImageDEBUG", "Error creating temp file", e)
        null
    }
}

fun uriToBase64(context: Context, uri: Uri): String? {
    return try {
        // Étape 1 : Obtenir uniquement les dimensions de l'image
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        context.contentResolver.openInputStream(uri)?.use { 
            BitmapFactory.decodeStream(it, null, options)
        }

        // Étape 2 : Calculer le facteur de réduction d'échelle (Target max 800px)
        val reqSize = 800
        var inSampleSize = 1
        if (options.outHeight > reqSize || options.outWidth > reqSize) {
            val halfHeight = options.outHeight / 2
            val halfWidth = options.outWidth / 2
            while ((halfHeight / inSampleSize) >= reqSize && (halfWidth / inSampleSize) >= reqSize) {
                inSampleSize *= 2
            }
        }

        // Étape 3 : Charger l'image redimensionnée
        options.inJustDecodeBounds = false
        options.inSampleSize = inSampleSize
        
        val bitmap = context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        } ?: return null

        // Étape 4 : Compresser en JPEG
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
        val imageBytes = outputStream.toByteArray()
        bitmap.recycle()

        // Étape 5 : Encoder en Base64
        val base64Encoded = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
        "data:image/jpeg;base64,$base64Encoded"
    } catch (e: Exception) {
        android.util.Log.e("ImageDEBUG", "Error converting image", e)
        null
    }
}

fun grantCameraPermission(context: Context, uri: Uri) {
    try {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val resInfoList = context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        android.util.Log.d("ImageDEBUG", "Granting permission to ${resInfoList.size} activities")
        for (resolveInfo in resInfoList) {
            val packageName = resolveInfo.activityInfo.packageName
            context.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    } catch (e: Exception) {
        android.util.Log.e("ImageDEBUG", "Error granting permission", e)
    }
}

fun decodeImageModel(model: Any?): Any? {
    if (model is String && model.startsWith("data:image")) {
        return try {
            val base64String = model.substringAfter(",")
            Base64.decode(base64String, Base64.NO_WRAP)
        } catch (e: Exception) {
            null
        }
    }
    return model
}

// ➕ CREATE LISTING DIALOG
@Composable
fun CreateListingDialog(
    viewModel: RentalViewModel,
    onDismiss: () -> Unit,
    onCreate: (Listing) -> Unit
) {
    val title by viewModel.createListingTitle.collectAsState()
    val description by viewModel.createListingDescription.collectAsState()
    val priceStr by viewModel.createListingPrice.collectAsState()
    val imageUrl by viewModel.createListingImageUrl.collectAsState()
    val location by viewModel.createListingLocation.collectAsState()
    val bedsStr by viewModel.createListingBeds.collectAsState()
    val selectedType by viewModel.createListingCategory.collectAsState()

    val context = LocalContext.current
    val cameraUri by viewModel.cameraUri.collectAsState()

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { 
            try {
                val base64 = uriToBase64(context, it)
                if (base64 != null) {
                    viewModel.createListingImageUrl.value = base64
                } else {
                    Toast.makeText(context, "Erreur de traitement de l'image", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Erreur: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) { 
            cameraUri?.let {
                try {
                    val base64 = uriToBase64(context, it)
                    if (base64 != null) {
                        viewModel.createListingImageUrl.value = base64
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Erreur traitement photo: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "Capture annulée ou échouée", Toast.LENGTH_SHORT).show()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val tempUri = createTempPictureUri(context)
            if (tempUri != null) {
                viewModel.setCameraUri(tempUri)
                grantCameraPermission(context, tempUri)
                try {
                    cameraLauncher.launch(tempUri)
                } catch (e: Exception) {
                    Toast.makeText(context, "Impossible de lancer la caméra: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "Permission caméra refusée", Toast.LENGTH_SHORT).show()
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
                    onValueChange = { viewModel.createListingTitle.value = it },
                    label = { Text("Titre de la location") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_create_title")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { viewModel.createListingDescription.value = it },
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
                        onValueChange = { viewModel.createListingPrice.value = it },
                        label = { Text("Prix/jour (€)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_create_price")
                    )

                    OutlinedTextField(
                        value = bedsStr,
                        onValueChange = { viewModel.createListingBeds.value = it },
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
                    onValueChange = { viewModel.createListingLocation.value = it },
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
                    
                    if (imageUrl.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF1F5F9))
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(decodeImageModel(imageUrl))
                                    .crossfade(true)
                                    .placeholder(android.R.drawable.ic_menu_gallery)
                                    .error(android.R.drawable.ic_menu_report_image)
                                    .build(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = { viewModel.createListingImageUrl.value = "" },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                    .size(32.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
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
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                    val tempUri = createTempPictureUri(context)
                                    if (tempUri != null) {
                                        viewModel.setCameraUri(tempUri)
                                        grantCameraPermission(context, tempUri)
                                        try {
                                            cameraLauncher.launch(tempUri)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Erreur caméra: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    permissionLauncher.launch(Manifest.permission.CAMERA)
                                }
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
                        value = imageUrl,
                        onValueChange = { viewModel.createListingImageUrl.value = it },
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
                                    .clickable { viewModel.createListingCategory.value = type }
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
                            val beds = bedsStr.toIntOrNull() ?: 0
                            if (title.isBlank() || description.isBlank() || price == null || location.isBlank() || imageUrl.isBlank()) {
                                isError = true
                            } else {
                                isError = false
                                val categoryKey = when(selectedType) {
                                    "Villa" -> "villas"
                                    "Appartement" -> "appartements"
                                    "Studio" -> "studios"
                                    "Chambre d'hôte" -> "villas" // Fallback to villas
                                    else -> "villas"
                                }
                                val item = Listing(
                                    title = title,
                                    description = description,
                                    pricePerDay = price,
                                    image = imageUrl,
                                    images = listOf(imageUrl),
                                    location = location,
                                    beds = beds,
                                    capacity = beds,
                                    type = selectedType,
                                    category = categoryKey,
                                    isAvailable = true,
                                    available = true
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
                        onClick = { onConfirm() },
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
            tint = Color(0xFFB91C1C),
            modifier = Modifier.size(60.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Erreur Système VIP",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = VipWhite
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            fontSize = 13.sp,
            color = BentoSlateLight,
            modifier = Modifier.padding(horizontal = 16.dp),
            textAlign = TextAlign.Center,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VipGold, contentColor = VipBlack),
            modifier = Modifier.testTag("retry_button").height(48.dp).fillMaxWidth(0.6f)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("RÉESSAYER", fontWeight = FontWeight.ExtraBold)
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
            tint = VipGold.copy(alpha = 0.4f),
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = message,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = VipWhite,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            fontSize = 13.sp,
            color = BentoSlateLight,
            modifier = Modifier.padding(horizontal = 8.dp),
            textAlign = TextAlign.Center
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
    val customerInitials = booking.clientName?.split(" ")
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
            .background(VipSlate, RoundedCornerShape(20.dp))
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
                    .background(VipGold.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = customerInitials,
                    color = VipGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(modifier = Modifier.weight(1f, fill = false)) {
                Text(
                    text = booking.listingTitle ?: "Propriété VIP",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = VipWhite,
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
                text = badgeText.uppercase(),
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
            .background(VipSlate, RoundedCornerShape(20.dp))
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
                    .background(VipGold.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = VipGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Column {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = VipWhite
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
                text = badgeText.uppercase(),
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
