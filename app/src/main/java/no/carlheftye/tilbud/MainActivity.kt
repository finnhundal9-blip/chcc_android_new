package no.carlheftye.tilbud

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.text.NumberFormat
import java.util.Locale

private const val ENDPOINT = "https://script.google.com/macros/s/AKfycbzRRK32WEGTkArU3Un_CjxgLROPEETU_0Zc1uQubj3eI56qle6UOrCVAKwly2ES-_pj/exec"
private val forest = Color(0xFF285943)
private val cream = Color(0xFFF8F6EF)
private val sage = Color(0xFFD9E5D5)
private val terracotta = Color(0xFFD97842)
private val ink = Color(0xFF24352D)
private val orange = Color(0xFFFFA500)
private val rounded = RoundedCornerShape(18.dp)

data class Offer(val name: String, val description: String, val price: Double, val image: String, val group: String)

private fun readOffers(): List<Offer> {
    val connection = (URL(ENDPOINT).openConnection() as HttpURLConnection).apply {
        connectTimeout = 12000
        readTimeout = 12000
        instanceFollowRedirects = true
    }
    try {
        if (connection.responseCode !in 200..299) error("HTTP ${connection.responseCode}")
        val json = JSONArray(connection.inputStream.bufferedReader().use { it.readText() })
        return (0 until json.length()).map { json.getJSONObject(it) }
            .filter { it.optBoolean("Aktiv", false) }
            .map { Offer(it.optString("Produkt"), it.optString("Beskrivelse"), it.optDouble("Pris", 0.0), it.optString("BildeUrl"), it.optString("Gruppe")) }
    } finally { connection.disconnect() }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { FreshApp() } }
}

@Composable private fun Logo(modifier: Modifier = Modifier) {
    Image(painterResource(R.drawable.chcc_logo), "Carl Heftye Cash & Carry logo", modifier, contentScale = ContentScale.Fit)
}

@Composable private fun FreshApp() {
    var page by remember { mutableIntStateOf(0) }
    var offers by remember { mutableStateOf<List<Offer>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var refresh by remember { mutableIntStateOf(0) }
    var search by remember { mutableStateOf("") }
    var selectedGroup by remember { mutableStateOf("Alle") }
    LaunchedEffect(page, refresh) {
        if (page == 1) {
            loading = true
            try { offers = withContext(Dispatchers.IO) { readOffers() }; error = "" }
