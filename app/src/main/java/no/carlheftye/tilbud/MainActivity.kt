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
private val cream = Color(0xFFBDBDBD)
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
            try {
                offers = withContext(Dispatchers.IO) {
                    readOffers()
                }
                error = ""
            } catch (e: Exception) {
                error = "Kunne ikke hente tilbud. Sjekk internett og prøv igjen."
            } finally {
                loading = false
            }
        }
    }
    MaterialTheme(colorScheme = lightColorScheme(primary = forest, onPrimary = Color.White, background = cream, onBackground = ink, surface = Color.White, onSurface = ink, secondary = terracotta)) {
        Scaffold(containerColor = cream, bottomBar = {
            NavigationBar(containerColor = Color.White) {
                listOf(Triple("Hjem", Icons.Default.Home, 0), Triple("Tilbud", Icons.Default.LocalOffer, 1), Triple("Om Carl Heftye", Icons.Default.Info, 2)).forEach { (label, icon, index) ->
                    NavigationBarItem(selected = page == index, onClick = { page = index }, icon = { Icon(icon, contentDescription = label) }, label = { Text(label, maxLines = 1, fontSize = 10.sp) }, colors = NavigationBarItemDefaults.colors(selectedIconColor = forest, selectedTextColor = forest, indicatorColor = sage))
                }
            }
        }) { inset ->
            when (page) {
                0 -> HomePage(Modifier.padding(inset), { page = 1 }, { page = 3 }, { page = 4 })
                1 -> {
                    val groups = listOf("Alle") + offers.map { it.group }.filter { it.isNotBlank() }.distinct().sorted()
                    val filtered = offers.filter { (selectedGroup == "Alle" || it.group == selectedGroup) && (it.name.contains(search, true) || it.description.contains(search, true) || it.group.contains(search, true)) }
                    Column(Modifier.fillMaxSize().padding(inset).padding(horizontal = 14.dp)) {
                        Row(Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Aktuelle tilbud", fontSize = 27.sp, fontWeight = FontWeight.Bold, color = forest)
                            TextButton(onClick = { refresh++ }) { Text("Oppdater") }
                        }
                        OutlinedTextField(search, { search = it }, label = { Text("Søk etter produkt") }, singleLine = true, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White))
                        Spacer(Modifier.height(12.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 12.dp)) {
                            items(groups.size) { i -> val group = groups[i]; FilterChip(selected = selectedGroup == group, onClick = { selectedGroup = group }, label = { Text(group) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = sage, selectedLabelColor = forest)) }
                        }
                        if (loading) LinearProgressIndicator(Modifier.fillMaxWidth(), color = forest)
                        if (error.isNotBlank()) { Text(error, color = terracotta); TextButton(onClick = { refresh++ }) { Text("Prøv igjen") } }
                        if (!loading && error.isBlank() && filtered.isEmpty()) Text("Ingen aktive tilbud funnet.")
                        LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 18.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(filtered) { OfferCard(it) }
                        }
                    }
                }
                2 -> AboutPage(Modifier.padding(inset))
                3 -> MemberPage(Modifier.padding(inset), { page = 0 })
                4 -> CustomerCardPage(Modifier.padding(inset), { page = 0 })
            }
        }
    }
}

@Composable private fun HomePage(modifier: Modifier, onOffers: () -> Unit, onMember: () -> Unit, onCustomer: () -> Unit) {
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Logo(Modifier.fillMaxWidth().height(148.dp))
        Image(
            painter = painterResource(R.drawable.chcc_weekly_offer),
            contentDescription = "Ukens tilbud – trykk for å se tilbudene",
            modifier = Modifier.fillMaxWidth()
                .aspectRatio(1769f / 889f)
                .clip(RoundedCornerShape(18.dp))
                .clickable(onClick = onOffers),
            contentScale = ContentScale.Fit
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onMember, modifier = Modifier.weight(1f).height(68.dp),
                contentPadding = PaddingValues(horizontal = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = orange, contentColor = ink),
                shape = RoundedCornerShape(16.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("+Medlem", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Fordeler og innmelding", fontSize = 10.sp, maxLines = 1)
                }
            }
            Button(onClick = onCustomer, modifier = Modifier.weight(1f).height(68.dp),
                contentPadding = PaddingValues(horizontal = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = orange, contentColor = ink),
                shape = RoundedCornerShape(16.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Kunde", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Mitt kundekort", fontSize = 11.sp)
                }
            }
        }
        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Velkommen innom", fontSize = 21.sp, fontWeight = FontWeight.Bold, color = forest)
                Text("Opplev torgfølelsen hos Carl Heftye Cash & Carry.")
                HorizontalDivider(color = sage)
                Text("Mandag–fredag  07:00–16:00")
                Text("Lørdag  07:00–15:00")
            }
        }
    }
}

@Composable private fun OfferCard(item: Offer) {
    Card(modifier = Modifier.fillMaxWidth(), shape = rounded, elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column {
            if (item.image.startsWith("https://")) AsyncImage(model = item.image, contentDescription = item.name, modifier = Modifier.fillMaxWidth().height(145.dp).background(Color.White), contentScale = ContentScale.Fit)
            else Box(Modifier.fillMaxWidth().height(145.dp).background(sage), contentAlignment = Alignment.Center) { Text("Bilde kommer", fontSize = 12.sp, color = forest) }
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, color = ink)
                Text(item.description, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, color = Color(0xFF66746B))
                if (item.group.isNotBlank()) Text(item.group, fontSize = 11.sp, color = forest)
                Text(NumberFormat.getCurrencyInstance(Locale.forLanguageTag("nb-NO")).format(item.price), fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, color = terracotta)
            }
        }
    }
}

// NEW: Directions open in Google Maps (or the browser), without a Maps API key.
@Composable private fun StoreLocationCard() {
    val context = LocalContext.current
    val address = "Brobekkveien 80, 0582 Oslo"
    val mapsUrl = "https://www.google.com/maps/dir/?api=1&destination=" + Uri.encode(address)
    Card(modifier = Modifier.fillMaxWidth(), shape = rounded, colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Finn frem til oss", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = forest)
            Text("Carl Heftye Cash & Carry", fontWeight = FontWeight.Bold)
            Text(address)
            Button(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(mapsUrl))) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = orange, contentColor = ink), shape = RoundedCornerShape(14.dp)) {
                Text("Få veibeskrivelse", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable private fun AboutPage(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
        Logo(Modifier.fillMaxWidth().height(120.dp))
        Text("Om oss i CHCC", fontSize = 27.sp, fontWeight = FontWeight.Bold, color = forest)
        Text("Carl Heftye Cash & Carry AS (CHCC) er bygget på Carl Heftye Engros AS, kjent for sin historie som grossist til HORECA-markedet i Oslo.")
        Text("Carl Heftye Cash & Carry er et utsalg for proffmarkedet i Oslo og omegn.")
        Text("Vårt fokus er på frukt & grønt, sjømat og vilt og annen fine food mot proffmarkedet til grossistpriser.")
        Text("Hos CHCC er målet å skape en torgfølelse, der kundene har muligheten for en god handel innenfor rammene av trygg mat.")
        Text("Vår stolthet ligger i kvaliteten på varer og varesortimentet, service og det lille ekstra.")
        Text("Med økt varesortiment og høy kvalitet ønsker vi å fungere som inspirasjon til proffmarkedet.")
        StoreLocationCard()
        Card(colors = CardDefaults.cardColors(containerColor = sage), shape = rounded) {
            Text("VÅR BUTIKK ER MILJØTÅRNSERTIFISERT", modifier = Modifier.fillMaxWidth().padding(18.dp), fontWeight = FontWeight.Bold, color = forest)
        }
        Text("Våre åpningstider", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = forest)
        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = rounded) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text("Mandag–fredag", fontWeight = FontWeight.Bold); Text("07:00–16:00")
                HorizontalDivider(color = sage)
                Text("Lørdag", fontWeight = FontWeight.Bold); Text("07:00–15:00")
            }
        }
        Text("Kontakt oss", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = forest)
        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = rounded) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Telefon: 23 37 81 00", modifier = Modifier.clickable { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:23378100"))) }, color = forest, fontWeight = FontWeight.Bold)
                HorizontalDivider(color = sage)
                Text("Vakttelefon: 907 11 539", modifier = Modifier.clickable { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:90711539"))) }, color = forest, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(18.dp))
    }
}

@Composable private fun MemberPage(modifier: Modifier = Modifier, onBack: () -> Unit) {
    val context = LocalContext.current
    fun openLink(url: String) { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TextButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = null); Spacer(Modifier.width(6.dp)); Text("Tilbake til Hjem") }
        Logo(Modifier.fillMaxWidth().height(105.dp))
        Text("CHCC +Medlem", fontSize = 29.sp, fontWeight = FontWeight.Bold, color = forest)
        Text("Få mer ut av hver handel", fontSize = 19.sp, fontWeight = FontWeight.SemiBold, color = ink)
        Card(shape = rounded, colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF0D2))) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Fordeler som +Medlem", fontWeight = FontWeight.Bold, fontSize = 19.sp)
                Text("Som medlem sparer våre kunder mer ved hver handel i form av bonuspoeng og andre varer som har spesielt gode avtaler og rabatter.")
                Text("Hver måned ønsker vi å presentere et knippe med produkter som har spesielt gode avslag i tillegg til våre faste rabattprodukter.")
            }
        }
        Card(shape = rounded, colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Gratis medlemskap", fontWeight = FontWeight.Bold, fontSize = 19.sp, color = forest)
                Text("Det er gratis å være +Medlem."); Text("Bonuspoeng har en varighet på 1 år.")
                HorizontalDivider(color = sage)
                Text("For å kunne bli +Medlem må du allerede ha opprettet en kundekonto hos oss.")
                Button(onClick = { openLink("https://www.chcc.no/bli-kunde/") }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = forest)) { Text("Opprett kundekonto") }
                Button(onClick = { openLink("https://diller.app/CARLHEFTYEAS") }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = orange, contentColor = ink)) { Text("Bli +Medlem") }
                Text("Innmelding skjer via den eksterne tjenesten Diller.", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

private const val CUSTOMER_PREFS = "chcc_customer_card"
private const val CUSTOMER_KEY = "customer_id"
private const val CUSTOMER_NAME_KEY = "customer_name"
private fun barcodeBitmap(value: String): Bitmap {
    val hints = mapOf<EncodeHintType, Any>(EncodeHintType.MARGIN to 16)
    val matrix = MultiFormatWriter().encode(value, BarcodeFormat.CODE_128, 1100, 310, hints)
    val pixels = IntArray(matrix.width * matrix.height)
    for (y in 0 until matrix.height) for (x in 0 until matrix.width) {
        pixels[y * matrix.width + x] = if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
    }
    return Bitmap.createBitmap(pixels, matrix.width, matrix.height, Bitmap.Config.ARGB_8888)
}

@Composable private fun CustomerCardPage(modifier: Modifier = Modifier, onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences(CUSTOMER_PREFS, Context.MODE_PRIVATE) }
    var savedId by remember { mutableStateOf(prefs.getString(CUSTOMER_KEY, "") ?: "") }
    var input by remember { mutableStateOf(savedId) }
    var savedName by remember { mutableStateOf(prefs.getString(CUSTOMER_NAME_KEY, "") ?: "") }
    var nameInput by remember { mutableStateOf(savedName) }
    var editing by remember { mutableStateOf(savedId.isBlank()) }
    val valid = input.isNotBlank() && input.length <= 32 && input.all { it.code in 32..126 }
    val barcode = remember(savedId) { if (savedId.isBlank()) null else runCatching { barcodeBitmap(savedId).asImageBitmap() }.getOrNull() }
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TextButton(onClick = onBack, modifier = Modifier.align(Alignment.Start)) { Icon(Icons.Default.ArrowBack, contentDescription = null); Spacer(Modifier.width(6.dp)); Text("Tilbake til Hjem") }
        Logo(Modifier.fillMaxWidth().height(112.dp))
        Text("Mitt kundekort", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = forest)
        Text("Vis strekkoden i kassen", color = ink)
        if (editing) {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = rounded, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Kundenavn", fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = nameInput, onValueChange = { if (it.length <= 100) nameInput = it }, label = { Text("Skriv inn kundenavn") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Text("Kundenummer", fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = input, onValueChange = { if (it.length <= 32) input = it }, label = { Text("Skriv inn ditt kundenummer") }, singleLine = true, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters), isError = input.isNotEmpty() && !valid)
                    Text("Tillatt: bokstaver, tall og vanlige tegn (maks 32).", fontSize = 12.sp)
                    Button(onClick = { val value = input.trim(); val name = nameInput.trim(); prefs.edit().putString(CUSTOMER_KEY, value).putString(CUSTOMER_NAME_KEY, name).apply(); savedId = value; savedName = name; input = value; nameInput = name; editing = false }, enabled = valid && input.trim().isNotEmpty() && nameInput.isNotBlank(), modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = orange, contentColor = ink)) { Text("Lagre kundekort") }
                    if (savedId.isNotBlank()) TextButton(onClick = { input = savedId; nameInput = savedName; editing = false }) { Text("Avbryt") }
                }
            }
        } else {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = rounded, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("CHCC KUNDEKORT", fontWeight = FontWeight.Bold, color = forest)
                    if (savedName.isNotBlank()) Text(savedName, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = ink, textAlign = TextAlign.Center)
                    if (barcode != null) Image(bitmap = barcode, contentDescription = "Code 128 kundestrekkode", modifier = Modifier.fillMaxWidth().height(135.dp), contentScale = ContentScale.Fit)
                    else Text("Kunne ikke generere strekkoden.", color = terracotta)
                    Text(savedId, fontSize = 19.sp, fontWeight = FontWeight.Bold, color = ink, textAlign = TextAlign.Center)
                    Text("CODE 128", fontSize = 11.sp, color = Color.Gray)
                }
            }
            OutlinedButton(onClick = { input = savedId; nameInput = savedName; editing = true }, modifier = Modifier.fillMaxWidth()) { Text("Endre kundekort") }
        }
        Text("Kundenavn og kundenummer lagres kun på denne telefonen og kan vises uten internett.", color = ink, fontSize = 13.sp, textAlign = TextAlign.Center)
        Text("Kundekortet er selvregistrert og bekrefter ikke at nummeret tilhører deg.", color = Color.DarkGray, fontSize = 12.sp, textAlign = TextAlign.Center)
    }
}
