package no.carlheftye.tilbud

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
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
            .map {
                Offer(it.optString("Produkt"), it.optString("Beskrivelse"),
                    it.optDouble("Pris", 0.0), it.optString("BildeUrl"), it.optString("Gruppe"))
            }
    } finally { connection.disconnect() }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { FreshApp() }
    }
}

@Composable
private fun FreshApp() {
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
                offers = withContext(Dispatchers.IO) { readOffers() }
                error = ""
            } catch (e: Exception) {
                error = "Kunne ikke hente tilbud. Sjekk internett og prøv igjen."
            } finally { loading = false }
        }
    }
    val colors = lightColorScheme(primary = forest, onPrimary = Color.White,
        background = cream, onBackground = ink, surface = Color.White,
        onSurface = ink, secondary = terracotta)
    MaterialTheme(colorScheme = colors) {
        Scaffold(containerColor = cream, bottomBar = {
            NavigationBar(containerColor = Color.White) {
                listOf(Triple("Hjem", Icons.Default.Home, 0),
                    Triple("Tilbud", Icons.Default.LocalOffer, 1),
                    Triple("Om Carl Heftye", Icons.Default.Info, 2)).forEach { (label, icon, index) ->
                    NavigationBarItem(selected = page == index, onClick = { page = index },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label, maxLines = 1, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = forest, selectedTextColor = forest,
                            indicatorColor = sage))
                }
            }
        }) { inset ->
            when (page) {
                0 -> HomePage(Modifier.padding(inset), onOffers = { page = 1 }, onMember = { page = 3 })
                1 -> {
                    val groups = listOf("Alle") + offers.map { it.group }.filter { it.isNotBlank() }.distinct().sorted()
                    val filtered = offers.filter {
                        (selectedGroup == "Alle" || it.group == selectedGroup) &&
                            (it.name.contains(search, true) || it.description.contains(search, true) || it.group.contains(search, true))
                    }
                    Column(Modifier.fillMaxSize().padding(inset).padding(horizontal = 14.dp)) {
                        Row(Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically) {
                            Text("Aktuelle tilbud", fontSize = 27.sp, fontWeight = FontWeight.Bold, color = forest)
                            TextButton(onClick = { refresh++ }) { Text("Oppdater") }
                        }
                        OutlinedTextField(value = search, onValueChange = { search = it },
                            label = { Text("Søk etter produkt") }, singleLine = true,
                            shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White))
                        Spacer(Modifier.height(12.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 12.dp)) {
                            items(groups.size) { i ->
                                val group = groups[i]
                                FilterChip(selected = selectedGroup == group,
                                    onClick = { selectedGroup = group }, label = { Text(group) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = sage, selectedLabelColor = forest))
                            }
                        }
                        if (loading) LinearProgressIndicator(Modifier.fillMaxWidth(), color = forest)
                        if (error.isNotBlank()) {
                            Text(error, color = terracotta)
                            TextButton(onClick = { refresh++ }) { Text("Prøv igjen") }
                        }
                        if (!loading && error.isBlank() && filtered.isEmpty()) {
                            Text("Ingen aktive tilbud funnet.", color = ink)
                        }
                        LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 18.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(filtered) { offer -> OfferCard(offer) }
                        }
                    }
                }
                2 -> AboutPage(Modifier.padding(inset))
                3 -> MemberPage(Modifier.padding(inset), onBack = { page = 0 })
            }
        }
    }
}

@Composable
private fun Logo(modifier: Modifier = Modifier) {
    Image(painter = painterResource(R.drawable.chcc_logo),
        contentDescription = "Carl Heftye Cash & Carry logo",
        modifier = modifier, contentScale = ContentScale.Fit)
}

@Composable
private fun HomePage(modifier: Modifier = Modifier, onOffers: () -> Unit, onMember: () -> Unit) {
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Logo(Modifier.fillMaxWidth().height(148.dp))
        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = forest)) {
            Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("VELKOMMEN TIL CHCC", fontSize = 12.sp, color = sage, fontWeight = FontWeight.Bold)
                Text("Kvalitet fra markedet", fontSize = 29.sp, color = Color.White, fontWeight = FontWeight.Bold, lineHeight = 34.sp)
                Text("Frukt & grønt, sjømat, vilt og fine food til proffmarkedet.",
                    fontSize = 15.sp, color = Color.White)
                Button(onClick = onOffers, modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = terracotta),
                    shape = RoundedCornerShape(14.dp)) {
                    Text("SE VÅRE TILBUD", fontWeight = FontWeight.Bold)
                }
            }
        }
        Button(onClick = onMember, modifier = Modifier.fillMaxWidth().height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500), contentColor = ink),
            shape = RoundedCornerShape(16.dp)) {
            Text("+Medlem – fordeler og innmelding", fontWeight = FontWeight.Bold)
        }
        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Velkommen innom", fontSize = 21.sp, fontWeight = FontWeight.Bold, color = forest)
                Text("Opplev torgfølelsen hos Carl Heftye Cash & Carry.", color = ink)
                HorizontalDivider(color = sage)
                Text("Mandag–fredag  07:00–16:00", color = ink)
                Text("Lørdag  07:00–15:00", color = ink)
            }
        }
    }
}

@Composable
private fun OfferCard(item: Offer) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column {
            if (item.image.startsWith("https://")) {
                AsyncImage(model = item.image, contentDescription = item.name,
                    modifier = Modifier.fillMaxWidth().height(145.dp).background(Color.White),
                    contentScale = ContentScale.Fit)
            } else {
                Box(Modifier.fillMaxWidth().height(145.dp).background(sage),
                    contentAlignment = Alignment.Center) {
                    Text("Bilde kommer", fontSize = 12.sp, color = forest)
                }
            }
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 15.sp,
                    maxLines = 2, overflow = TextOverflow.Ellipsis, color = ink)
                Text(item.description, fontSize = 12.sp, maxLines = 2,
                    overflow = TextOverflow.Ellipsis, color = Color(0xFF66746B))
                if (item.group.isNotBlank()) Text(item.group, fontSize = 11.sp, color = forest)
                Text(NumberFormat.getCurrencyInstance(Locale.forLanguageTag("nb-NO")).format(item.price),
                    fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, color = terracotta)
            }
        }
    }
}

@Composable
private fun AboutPage(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)) {
        Logo(Modifier.fillMaxWidth().height(120.dp))
        Text("Om oss i CHCC", fontSize = 27.sp, fontWeight = FontWeight.Bold, color = forest)
        Text("Carl Heftye Cash & Carry AS (CHCC) er bygget på Carl Heftye Engros AS, kjent for sin historie som grossist til HORECA-markedet i Oslo.")
        Text("Carl Heftye Cash & Carry er et utsalg for proffmarkedet i Oslo og omegn.")
        Text("Vårt fokus er på frukt & grønt, sjømat og vilt og annen fine food mot proffmarkedet til grossistpriser.")
        Text("Hos CHCC er målet å skape en torgfølelse, der kundene har muligheten for en god handel innenfor rammene av trygg mat.")
        Text("Vår stolthet ligger i kvaliteten på varer og varesortimentet, service og det lille ekstra.")
        Text("Med økt varesortiment og høy kvalitet ønsker vi å fungere som inspirasjon til proffmarkedet.")
        Card(colors = CardDefaults.cardColors(containerColor = sage), shape = RoundedCornerShape(18.dp)) {
            Text("VÅR BUTIKK ER MILJØTÅRNSERTIFISERT", modifier = Modifier.fillMaxWidth().padding(18.dp),
                fontWeight = FontWeight.Bold, color = forest)
        }
        Text("Våre åpningstider", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = forest)
        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text("Mandag–fredag", fontWeight = FontWeight.Bold)
                Text("07:00–16:00")
                HorizontalDivider(color = sage)
                Text("Lørdag", fontWeight = FontWeight.Bold)
                Text("07:00–15:00")
            }
        }
        Text("Kontakt oss", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = forest)
        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Telefon: 23 37 81 00", modifier = Modifier.clickable {
                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:23378100")))
                }, color = forest, fontWeight = FontWeight.Bold)
                HorizontalDivider(color = sage)
                Text("Vakttelefon: 907 11 539", modifier = Modifier.clickable {
                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:90711539")))
                }, color = forest, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(18.dp))
    }
}


@Composable
private fun MemberPage(modifier: Modifier = Modifier, onBack: () -> Unit) {
    val context = LocalContext.current
    fun openLink(url: String) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TextButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text("Tilbake til Hjem")
        }
        Logo(Modifier.fillMaxWidth().height(105.dp))
        Text("CHCC +Medlem", fontSize = 29.sp, fontWeight = FontWeight.Bold, color = forest)
        Text("Få mer ut av hver handel", fontSize = 19.sp, fontWeight = FontWeight.SemiBold, color = ink)
        Card(shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF0D2))) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Fordeler som +Medlem", fontWeight = FontWeight.Bold, fontSize = 19.sp, color = ink)
                Text("Som medlem sparer våre kunder mer ved hver handel i form av bonuspoeng og andre varer som har spesielt gode avtaler og rabatter.")
                Text("Hver måned ønsker vi å presentere et knippe med produkter som har spesielt gode avslag i tillegg til våre faste rabattprodukter.")
            }
        }
        Card(shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Gratis medlemskap", fontWeight = FontWeight.Bold, fontSize = 19.sp, color = forest)
                Text("Det er gratis å være +Medlem.")
                Text("Bonuspoeng har en varighet på 1 år.")
                HorizontalDivider(color = sage)
                Text("For å kunne bli +Medlem må du allerede ha opprettet en kundekonto hos oss.")
                Button(onClick = { openLink("https://www.chcc.no/bli-kunde/") },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = forest)) {
                    Text("Opprett kundekonto")
                }
                Button(onClick = { openLink("https://diller.app/CARLHEFTYEAS") },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500), contentColor = ink)) {
                    Text("Bli +Medlem", fontWeight = FontWeight.Bold)
                }
                Text("Innmelding skjer via den eksterne tjenesten Diller.",
                    fontSize = 12.sp, color = Color(0xFF66746B))
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}
