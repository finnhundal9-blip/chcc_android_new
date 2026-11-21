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
        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = forest)) {
            Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("VELKOMMEN TIL CHCC", fontSize = 12.sp, color = sage, fontWeight = FontWeight.Bold)
                Text("Kvalitet fra markedet", fontSize = 29.sp, color = Color.White, fontWeight = FontWeight.Bold, lineHeight = 34.sp)
                Text("Frukt & grønt, sjømat, vilt og fine food til proffmarkedet.", fontSize = 15.sp, color = Color.White)
                Button(onClick = onOffers, modifier = Modifier.fillMaxWidth().height(52.dp), colors = ButtonDefaults.buttonColors(containerColor = terracotta), shape = RoundedCornerShape(14.dp)) { Text("SE VÅRE TILBUD", fontWeight = FontWeight.Bold) }
            }
        }
        Button(onClick = onMember, modifier = Modifier.fillMaxWidth().height(54.dp), colors = ButtonDefaults.buttonColors(containerColor = orange, contentColor = ink), shape = RoundedCornerShape(16.dp)) { Text("+Medlem – fordeler og innmelding", fontWeight = FontWeight.Bold) }
        Button(onClick = onCustomer, modifier = Modifier.fillMaxWidth().height(54.dp), colors = ButtonDefaults.buttonColors(containerColor = orange, contentColor = ink), shape = RoundedCornerShape(16.dp)) { Text("Kunde – mitt kundekort", fontWeight = FontWeight.Bold) }
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
                    Text("Kundenummer", fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = input, onValueChange = { if (it.length <= 32) input = it }, label = { Text("Skriv inn ditt kundenummer") }, singleLine = true, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters), isError = input.isNotEmpty() && !valid)
                    Text("Tillatt: bokstaver, tall og vanlige tegn (maks 32).", fontSize = 12.sp)
                    Button(onClick = { val value = input.trim(); prefs.edit().putString(CUSTOMER_KEY, value).apply(); savedId = value; input = value; editing = false }, enabled = valid && input.trim().isNotEmpty(), modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = orange, contentColor = ink)) { Text("Lagre kundekort") }
                    if (savedId.isNotBlank()) TextButton(onClick = { input = savedId; editing = false }) { Text("Avbryt") }
                }
            }
        } else {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = rounded, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("CHCC KUNDEKORT", fontWeight = FontWeight.Bold, color = forest)
                    if (barcode != null) Image(bitmap = barcode, contentDescription = "Code 128 kundestrekkode", modifier = Modifier.fillMaxWidth().height(135.dp), contentScale = ContentScale.Fit)
                    else Text("Kunne ikke generere strekkoden.", color = terracotta)
                    Text(savedId, fontSize = 19.sp, fontWeight = FontWeight.Bold, color = ink, textAlign = TextAlign.Center)
                    Text("CODE 128", fontSize = 11.sp, color = Color.Gray)
                }
            }
            OutlinedButton(onClick = { input = savedId; editing = true }, modifier = Modifier.fillMaxWidth()) { Text("Endre kundenummer") }
        }
        Text("Kundenummeret lagres kun på denne telefonen og kan vises uten internett.", color = ink, fontSize = 13.sp, textAlign = TextAlign.Center)
        Text("Kundekortet er selvregistrert og bekrefter ikke at nummeret tilhører deg.", color = Color.DarkGray, fontSize = 12.sp, textAlign = TextAlign.Center)
    }
}
