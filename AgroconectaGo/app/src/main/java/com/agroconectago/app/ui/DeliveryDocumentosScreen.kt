package com.agroconectago.app.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agroconectago.app.data.api.DeliveryRetrofitClient
import com.agroconectago.app.data.model.DeliveryProfileUpdateRequest
import com.agroconectago.app.ui.theme.*
import com.agroconectago.app.viewmodel.DeliveryAuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

// ═══════════ DATOS MASIVOS DE MARCAS Y MODELOS POR TIPO ═══════════

private data class MarcaVehiculo(val nombre: String, val modelos: List<String>)

private val marcasMotos = listOf(
    MarcaVehiculo("AKT", listOf("AKT 110S", "AKT 125", "AKT 150", "CR4 150", "NKD 125", "TT 200", "TTR 200", "EVO 125", "EVO 150", "AKT 180", "SL 150", "RTX 200", "CR5 200", "DS 200", "TT DS 250")),
    MarcaVehiculo("Yamaha", listOf("XTZ 125", "XTZ 150", "XTZ 250", "XTZ 250 Lander", "FZ 150", "FZ 250", "FZ-S 150", "MT-03", "MT-07", "MT-09", "YZF-R3", "YZF-R6", "YZF-R1", "NMax 155", "NMax Connected", "Crypton 115", "Crypton FI", "Ray ZR", "Fascino 125", "SZ 150")),
    MarcaVehiculo("Honda", listOf("CB 110", "CB 160F", "CB 190R", "CB 250 Twister", "CB 300F", "CB 500F", "CBR 250R", "CBR 500R", "XR 150L", "XR 190L", "XRE 300", "XRE 300 Rally", "CRF 250", "PCX 150", "Dio 110", "Elite 125", "Wave 110", "CBF 125", "CBF 160", "Africa Twin")),
    MarcaVehiculo("Suzuki", listOf("GN 125", "Gixxer 150", "Gixxer 250", "Gixxer SF 150", "Gixxer SF 250", "GSX-S 150", "GSX-S 750", "GSX-R150", "V-Strom 250", "V-Strom 650", "DR 150", "DR 650", "Address 110", "Address 125", "Boulevard S40", "Hayabusa", "Intruder 150", "Burgman 125")),
    MarcaVehiculo("Bajaj", listOf("Boxer CT 100", "CT 100 KS", "Discover 125", "Discover 125 ST", "Platino 100", "Pulsar 135", "Pulsar NS 160", "Pulsar NS 200", "Pulsar RS 200", "Pulsar 200 NS FI", "Pulsar 220F", "Dominar 250", "Dominar 400", "Dominar 400 UG", "Avenger 220", "Avenger Street 160", "V15", "M80")),
    MarcaVehiculo("KTM", listOf("Duke 200", "Duke 250", "Duke 390", "Duke 790", "RC 200", "RC 390", "Adventure 250", "Adventure 390", "Adventure 790", "Adventure 890", "Super Duke 1290", "690 Enduro")),
    MarcaVehiculo("Victory", listOf("Bold 110", "Switch 150", "MRX 150", "Bold Sport 200", "Phoenix 200", "Titanium 250", "Victory One 125")),
    MarcaVehiculo("Benelli", listOf("TNT 150", "TNT 250", "TNT 300", "TNT 600", "TRK 251", "TRK 502", "TRK 502X", "Leoncino 250", "Leoncino 500", "Imperiale 400", "302S", "752S")),
    MarcaVehiculo("Royal Enfield", listOf("Classic 350", "Classic 500", "Himalayan 411", "Interceptor 650", "Continental GT 650", "Meteor 350", "Bullet 350", "Thunderbird 350", "Scram 411", "Hunter 350", "Super Meteor 650")),
    MarcaVehiculo("Hero", listOf("Eco 100", "Splendor 110", "Splendor Plus", "Passion 110", "Hunk 150", "Xpulse 200", "Xpulse 200T", "Maverick 440", "Glamour 125", "Xtreme 160", "Xtreme 200", "Karizma XMR 210")),
    MarcaVehiculo("TVS", listOf("Sport 100", "Stryker 125", "Apache RTR 160", "Apache RTR 160 4V", "Apache RTR 200", "Apache RTR 200 4V", "Apache RR 310", "Raider 125", "NTorq 125", "Jupiter 125", "Jupiter 110", "Radeon 110")),
    MarcaVehiculo("SYM", listOf("Jet 14 125", "Citycom 300", "Symphony 125", "Wolf 200", "NH Trazer 190", "Jet X 125", "Maxsym 400", "Husky 150")),
    MarcaVehiculo("CFMoto", listOf("NK 150", "NK 250", "NK 400", "NK 650", "SR 250", "SR 450", "MT 650", "GT 650", "CL-X 700", "Papio XO")),
    MarcaVehiculo("Voge", listOf("300 DS", "300 AC", "500 DS", "500 AC", "650 DS", "525 AC", "900 DS", "Valico 300")),
    MarcaVehiculo("Kymco", listOf("Agility 125", "Like 125", "Like 150", "DTX 125", "X-Town 125", "X-Town 300", "AK 550", "People 125", "Super 8 150")),
    MarcaVehiculo("Keeway", listOf("K-Light 125", "K-Light 202", "RKV 125", "SRV 125", "SRV 250", "V302 C", "Victory 125")),
    MarcaVehiculo("Aprilia", listOf("RS 125", "RS 660", "Tuono 125", "Tuono 660", "SR 150", "SXR 160", "Dorsoduro 900")),
    MarcaVehiculo("BMW Motorrad", listOf("G 310 R", "G 310 GS", "F 750 GS", "F 850 GS", "R 1250 GS", "R 1250 RT", "S 1000 RR", "C 400 GT", "C 650 Sport")),
    MarcaVehiculo("Ducati", listOf("Scrambler 400", "Scrambler 800", "Monster 797", "Monster 937", "Multistrada V2", "Multistrada V4", "Panigale V2", "Panigale V4", "Streetfighter V4", "Diavel 1260")),
    MarcaVehiculo("Harley-Davidson", listOf("Street 500", "Iron 883", "Forty-Eight", "Sportster S", "Street Bob", "Low Rider S", "Fat Boy", "Road King", "Street Glide", "Pan America 1250")),
    MarcaVehiculo("Otro", listOf("No listado"))
)

private val marcasCarros = listOf(
    MarcaVehiculo("Chevrolet", listOf("Spark", "Spark GT", "Spark Life", "Onix", "Onix Turbo", "Aveo", "Corsa Evolution", "Cobalt", "Tracker", "Tracker Turbo", "Captiva", "Captiva Sport", "D-Max", "NHR", "NKR", "NPR", "Joy", "Sail", "Prisma", "Cruze", "Malibu", "Equinox", "Traverse", "Tahoe", "Camaro", "Colorado")),
    MarcaVehiculo("Renault", listOf("Twingo", "Clio", "Logan", "Sandero", "Stepway", "Duster", "Oroch", "Kwid", "Kwid Zen", "Captur", "Koleos", "Master", "Alaskan", "Megane", "Arkana", "Austral", "Kangoo", "Symbol", "Fluence", "Latitude")),
    MarcaVehiculo("Mazda", listOf("2", "2 Sedan", "3", "3 Sedan", "6", "CX-3", "CX-5", "CX-9", "CX-30", "CX-50", "CX-60", "CX-90", "BT-50", "Allegro", "MX-5", "CX-7", "323")),
    MarcaVehiculo("Kia", listOf("Picanto", "Picanto X-Line", "Rio", "Rio Sedan", "Cerato", "Forte", "Sportage", "Sorento", "Sorento Hybrid", "Stonic", "Seltos", "Sonet", "Niro", "Niro EV", "Carnival", "K2500", "K2700", "Mohave", "EV6", "EV9", "Soluto")),
    MarcaVehiculo("Hyundai", listOf("i10", "i25", "i35", "Accent", "Elantra", "Tucson", "Tucson Hybrid", "Creta", "Grand Creta", "Santa Fe", "Santa Fe Hybrid", "H1", "Staria", "Grand i10", "HB20", "Kona", "Kona EV", "Ioniq 5", "Ioniq 6", "Palisade", "Venue", "Bayon")),
    MarcaVehiculo("Toyota", listOf("Yaris", "Corolla", "Corolla Cross", "Hilux", "Fortuner", "Prado", "Land Cruiser 300", "RAV4", "RAV4 Hybrid", "4Runner", "Avanza", "Rush", "SW4", "Camry", "Tundra", "Sequoia", "Supra", "C-HR")),
    MarcaVehiculo("Nissan", listOf("March", "Versa", "Sentra", "Qashqai", "X-Trail", "Frontier NP300", "Pathfinder", "Patrol", "Kicks", "Leaf", "Murano", "Armada", "GT-R", "Juke", "Altima", "370Z")),
    MarcaVehiculo("Ford", listOf("Fiesta", "Fiesta Sedan", "Focus", "Fusion", "Escape", "Explorer", "EcoSport", "Ranger", "F-150", "F-250", "F-350", "Bronco", "Bronco Sport", "Edge", "Expedition", "Mustang", "Maverick", "Territory", "Transit")),
    MarcaVehiculo("Volkswagen", listOf("Gol", "Gol Trend", "Voyage", "Polo", "Polo GTS", "Virtus", "Jetta", "Jetta GLI", "T-Cross", "Taos", "Tiguan", "Touareg", "Amarok", "Saveiro", "Nivus", "Passat", "Arteon", "Teramont", "Atlas")),
    MarcaVehiculo("Suzuki", listOf("Alto", "Alto 800", "Celerio", "Swift", "Baleno", "Dzire", "Vitara", "Grand Vitara", "Jimny", "Jimny 5P", "S-Cross", "APV", "XL7", "Ertiga", "Ignis", "Fronx")),
    MarcaVehiculo("Citroen", listOf("C3", "C3 Aircross", "C4", "C4 Cactus", "C4 Lounge", "C5 Aircross", "Berlingo", "Jumpy", "Jumper", "DS3", "DS4", "DS5", "Basalt")),
    MarcaVehiculo("Peugeot", listOf("208", "208 GT", "301", "308", "3008", "5008", "Expert", "Partner", "Boxer", "Landtrek", "Rifter", "2008", "RCZ")),
    MarcaVehiculo("BMW", listOf("Serie 1", "Serie 2", "Serie 3", "Serie 4", "Serie 5", "Serie 7", "X1", "X2", "X3", "X4", "X5", "X6", "X7", "M3", "M5", "M8", "Z4", "iX", "i4", "i7")),
    MarcaVehiculo("Mercedes-Benz", listOf("Clase A", "Clase C", "Clase E", "Clase S", "GLA", "GLB", "GLC", "GLE", "GLS", "Clase G", "Sprinter", "Vito", "Viano", "AMG GT", "EQS", "EQB", "EQE", "EQC")),
    MarcaVehiculo("Audi", listOf("A1", "A3", "A3 Sedan", "A4", "A5", "A6", "A7", "A8", "Q2", "Q3", "Q5", "Q7", "Q8", "e-tron", "Q4 e-tron", "TT", "R8", "RS3", "RS5", "RS6")),
    MarcaVehiculo("Subaru", listOf("Impreza", "Legacy", "Outback", "Forester", "XV", "Crosstrek", "BRZ", "WRX", "Ascent", "Solterra")),
    MarcaVehiculo("Mitsubishi", listOf("Mirage", "Lancer", "L200", "Montero", "Montero Sport", "Outlander", "Eclipse Cross", "ASX", "Xpander", "Xforce")),
    MarcaVehiculo("Jeep", listOf("Renegade", "Compass", "Wrangler", "Wrangler Rubicon", "Gladiator", "Cherokee", "Grand Cherokee", "Commander", "Grand Wagoneer", "Avenger")),
    MarcaVehiculo("RAM", listOf("700", "1000", "1500", "2500", "3500", "4000", "TRX", "Rampage")),
    MarcaVehiculo("BYD", listOf("F0", "F3", "e1", "e2", "Dolphin", "Dolphin Mini", "Seagull", "Yuan Plus", "Song Plus", "Han", "Tang", "Seal", "Shark")),
    MarcaVehiculo("Changan", listOf("Alsvin", "Eado", "CS15", "CS35", "CS55", "CS75", "Uni-T", "Uni-K", "Hunter", "Star 9")),
    MarcaVehiculo("Chery", listOf("QQ", "Tiggo 2", "Tiggo 4", "Tiggo 7", "Tiggo 8", "Arrizo 5", "Arrizo 6", "Omoda 5", "iCar 03")),
    MarcaVehiculo("Geely", listOf("CK", "Emgrand", "Coolray", "Azkarra", "Geometry C", "Tugella", "Okavango", "Starray")),
    MarcaVehiculo("JAC", listOf("S2", "S3", "S5", "T6", "T8", "X200", "Sunray", "JS4", "E-JS4", "E-Sunray")),
    MarcaVehiculo("Volvo", listOf("S60", "S90", "V60", "V90", "XC40", "XC40 Recharge", "XC60", "XC90", "C40", "EX30", "EX90")),
    MarcaVehiculo("SEAT", listOf("Ibiza", "Leon", "Arona", "Ateca", "Tarraco", "Cupra Formentor", "Cupra Born", "Cupra Leon")),
    MarcaVehiculo("JMC", listOf("Carrying", "Vigus", "Grand Avenue", "Boarding", "Yuhu", "Landwind")),
    MarcaVehiculo("DFSK", listOf("C31", "C35", "C37", "Glory 330", "Glory 500", "Glory 580", "Glory 600", "Mini EV", "Seres 3")),
    MarcaVehiculo("Foton", listOf("Midi", "View", "Tunland", "Tunland G7", "Gratour", "Aumark")),
    MarcaVehiculo("MG", listOf("MG 3", "MG 5", "MG 6", "MG ZS", "MG ZS EV", "MG HS", "MG RX5", "MG One", "MG Marvel R", "MG Cyberster")),
    MarcaVehiculo("Otro", listOf("No listado"))
)

private val marcasCamiones = listOf(
    MarcaVehiculo("Chevrolet", listOf("NHR", "NHR55", "NKR", "NKR55", "NPR", "NPR75", "NQR", "NQR90", "FRR", "FTR", "FTR32", "FTR34", "FVR", "FVR34", "FVR90", "C70", "Kodiak", "Kodiak 26000", "Triton", "Silverado 3500")),
    MarcaVehiculo("International", listOf("4700", "4700 SCD", "4900", "4900 SFA", "7600", "7600 SBA", "9200", "9200i", "ProStar", "ProStar+", "LoneStar", "TranStar", "WorkStar", "DuraStar", "PayStar", "HX Series", "HV Series", "LT Series", "RH Series", "MV Series")),
    MarcaVehiculo("Hino", listOf("300", "300 915", "300 1016", "300 1017", "500", "500 1424", "500 1626", "500 1726", "500 2226", "700", "700 2841", "700 3241", "Dutro", "Dutro 816", "Ranger", "Ranger 500", "Ranger 700", "XL", "XL8", "Profia")),
    MarcaVehiculo("Kenworth", listOf("T370", "T370S", "T440", "T440S", "T680", "T680S", "T800", "T800S", "T880", "W900", "W900L", "W990", "C500", "C510", "K200", "K220", "T410", "T660")),
    MarcaVehiculo("Freightliner", listOf("M2 106", "M2 106 Plus", "M2 112", "M2 112 Plus", "Cascadia", "Cascadia Evolution", "Columbia", "Columbia CL120", "Coronado", "Coronado 132", "FLD 120", "FLD 132", "CL120", "Century Class", "Classic XL", "Argosy", "108SD", "114SD", "122SD")),
    MarcaVehiculo("Foton", listOf("Auman", "Auman EST-A", "Auman EST-M", "Aumark", "Aumark S", "Aumark TX", "Aumark BJ", "Tunland", "Tunland G7", "View C2", "View CS2", "Midi", "Gratour", "Auv", "Forland")),
    MarcaVehiculo("JAC", listOf("N55", "N55 4x2", "N75", "N75 4x2", "N90", "N120", "Sunray", "Sunray 3.5T", "X200", "X200 4x2", "T6", "T6 Doble Cabina", "T8", "T8 Doble Cabina", "Galloper", "Refine M4")),
    MarcaVehiculo("Dodge", listOf("Ram 2500", "Ram 3500", "Ram 4000", "Ram 5500", "Ram 6500", "Ram 700", "Ram 1000", "Dakota", "Durango", "Journey")),
    MarcaVehiculo("Volkswagen", listOf("Delivery 9.170", "Delivery 11.180", "Delivery 13.190", "Delivery 15.200", "Constellation 17.280", "Constellation 19.320", "Constellation 23.280", "Constellation 25.320", "Constellation 26.280", "Worker 8.160", "Worker 9.160", "Worker 11.180", "Worker 13.190", "Worker 15.190", "Worker 17.230", "Meteor 19.320", "Meteor 28.440")),
    MarcaVehiculo("Ford", listOf("Cargo 816", "Cargo 816S", "Cargo 1117", "Cargo 1517", "Cargo 1519", "Cargo 1722", "Cargo 1933", "Cargo 2422", "Cargo 2428", "Cargo 2628", "Cargo 3128", "F-350", "F-450", "F-550", "F-650", "F-750", "F-4000", "F-6000", "Transit 350")),
    MarcaVehiculo("Mercedes-Benz", listOf("Atego 1016", "Atego 1318", "Atego 1518", "Atego 1726", "Atego 2428", "Atego 2730", "Accelo 815", "Accelo 1016", "Axor 1933", "Axor 2541", "Axor 3340", "Actros 1841", "Actros 2041", "Actros 2548", "Actros 2653", "Actros 3348", "Sprinter 315", "Sprinter 415", "Sprinter 516", "Vito 111", "Vito 114")),
    MarcaVehiculo("Scania", listOf("G 410", "G 450", "G 500", "P 310", "P 360", "P 410", "R 410", "R 450", "R 500", "R 540", "R 620", "R 730", "S 450", "S 500", "S 540", "S 730", "XT G 410", "XT P 360", "Streamline")),
    MarcaVehiculo("Volvo Trucks", listOf("FM 370", "FM 410", "FM 460", "FM 500", "FMX 370", "FMX 410", "FMX 460", "FH 460", "FH 500", "FH 540", "FH 750", "FH16 750", "FE 280", "FE 320", "FL 240", "FL 280", "VM 270", "VM 330", "VN 440", "VNR 640")),
    MarcaVehiculo("Iveco", listOf("Daily 35S14", "Daily 45S17", "Daily 50C18", "Daily 70C21", "Tector 150E21", "Tector 170E28", "Tector 240E28", "Tector 310E28", "Cursor 330E35", "Cursor 380E37", "Hi-Way 440", "Hi-Way 480", "Stralis 440", "Stralis 480", "Trakker 380", "Trakker 450")),
    MarcaVehiculo("Shacman", listOf("X3000 6x4", "X3000 4x2", "F3000 6x4", "F3000 8x4", "M3000 6x4", "M3000 4x2", "L3000 6x4", "H3000 6x4", "SX5250", "SX3315")),
    MarcaVehiculo("Sinotruk", listOf("Howo 371", "Howo 380", "Howo 420", "Howo T7H", "Howo T5G", "Howo N7G", "Howo Max", "Sitrak C7H", "Sitrak C9H", "V7G", "Mine King")),
    MarcaVehiculo("Mack", listOf("Granite", "Granite MHD", "Pinnacle", "Pinnacle CHU", "Anthem", "TerraPro", "LR", "LR BEV", "MD", "MD Electric")),
    MarcaVehiculo("Western Star", listOf("4700SB", "4800SB", "4800TS", "4900EX", "4900FA", "5700XE", "49X", "47X", "6900XD")),
    MarcaVehiculo("Otro", listOf("No listado"))
)

private val coloresVehiculo = listOf(
    "Blanco", "Negro", "Rojo", "Azul", "Plata / Gris", "Verde", "Amarillo", "Naranja", "Vinotinto", "Beige",
    "Dorado", "Gris Oscuro", "Azul Oscuro", "Verde Oscuro", "Cafe", "Morado", "Rosado", "Celeste", "Bronce", "Otro"
)

private val categoriasLicencia = listOf(
    "A1 - Motos hasta 125cc", "A2 - Motos mas de 125cc",
    "B1 - Automovil particular", "B2 - Automovil particular y moto", "B3 - Automovil particular, moto y camioneta",
    "C1 - Camiones livianos (hasta 3.5T)", "C2 - Camiones medianos (hasta 10T)", "C3 - Camiones pesados y tractocamiones"
)

private val anosVehiculo = (2000..2026).map { it.toString() }

// ══════════ PANTALLA PRINCIPAL ═══════════

@Composable
fun DeliveryDocumentosScreen(
    viewModel: DeliveryAuthViewModel,
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(100); isVisible = true }

    var docsSubidos by remember { mutableStateOf(mapOf<String, String>()) }
    var uploadEnProgreso by remember { mutableStateOf<String?>(null) }
    var sendingVerification by remember { mutableStateOf(false) }
    var motivosRechazo by remember { mutableStateOf(mapOf<String, String>()) }
    var docsRechazados by remember { mutableStateOf(setOf<String>()) }

    val totalSecciones = 6
    val seccionesCompletas = remember { derivedStateOf {
        var c = 0
        if ("licencia_frontal" in docsSubidos && "licencia_trasera" in docsSubidos) c++
        if ("perfil" in docsSubidos) c++
        if ("tarjeta_propiedad" in docsSubidos) c++
        if ("soat" in docsSubidos) c++
        if ("tecnomecanica" in docsSubidos) c++
        if ("cedula" in docsSubidos) c++
        c
    }}
    val completas = seccionesCompletas.value
    val todosSubidos = completas >= totalSecciones

    var vehiculoMarca by remember { mutableStateOf("") }
    var vehiculoModelo by remember { mutableStateOf("") }
    var vehiculoPlaca by remember { mutableStateOf("") }
    var vehiculoAnio by remember { mutableStateOf("") }
    var vehiculoColor by remember { mutableStateOf("") }
    var vehiculoLicencia by remember { mutableStateOf("") }
    var numeroIdentidad by remember { mutableStateOf("") }
    var showVehicleForm by remember { mutableStateOf(false) }
    var formSaved by remember { mutableStateOf(false) }
    var vehiculoDataGuardado by remember { mutableStateOf(vehiculoPlaca.isNotBlank()) }
    val tipoVehiculo = remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        try {
            val p = DeliveryRetrofitClient.api.getProfile()
            tipoVehiculo.value = p.tipoVehiculo ?: ""
            val rechazoJson = p.motivoRechazo
            if (!rechazoJson.isNullOrBlank() && rechazoJson.startsWith("{")) {
                try {
                    val parsed = mutableMapOf<String, String>()
                    val pairs = rechazoJson.removeSurrounding("{", "}").split("\",\"").map { it.trim('"') }
                    pairs.forEach { pair ->
                        val parts = pair.split("\":\"", limit = 2)
                        if (parts.size == 2) parsed[parts[0].trim()] = parts[1].trim()
                    }
                    motivosRechazo = parsed
                    docsRechazados = parsed.keys
                } catch (_: Exception) {}
            }
            val subidos = mutableMapOf<String, String>()
            if (!p.fotoLicenciaFrontalUrl.isNullOrBlank()) subidos["licencia_frontal"] = p.fotoLicenciaFrontalUrl
            if (!p.fotoLicenciaTraseraUrl.isNullOrBlank()) subidos["licencia_trasera"] = p.fotoLicenciaTraseraUrl
            if (!p.fotoPerfil.isNullOrBlank()) subidos["perfil"] = p.fotoPerfil
            if (!p.fotoTarjetaPropiedadUrl.isNullOrBlank()) subidos["tarjeta_propiedad"] = p.fotoTarjetaPropiedadUrl
            if (!p.fotoSOATUrl.isNullOrBlank()) subidos["soat"] = p.fotoSOATUrl
            if (!p.fotoTecnomecanicaUrl.isNullOrBlank()) subidos["tecnomecanica"] = p.fotoTecnomecanicaUrl
            if (!p.fotoCedulaUrl.isNullOrBlank()) subidos["cedula"] = p.fotoCedulaUrl
            numeroIdentidad = p.numeroIdentidad ?: ""
            docsSubidos = subidos
        } catch (_: Exception) {}
    }

    fun subirDocumento(tipo: String, uri: Uri) {
        scope.launch {
            uploadEnProgreso = tipo
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val fileName = uri.lastPathSegment ?: "documento.jpg"
                val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
                inputStream?.use { input -> FileOutputStream(file).use { output -> input.copyTo(output) } }
                val body = MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("tipo", tipo)
                    .addFormDataPart("archivo", file.name, file.asRequestBody("image/jpeg".toMediaType()))
                    .build()
                val request = okhttp3.Request.Builder().url(com.agroconectago.app.data.api.ApiConfig.BASE_URL + "api/delivery/documento").post(body).build()
                withContext(Dispatchers.IO) { DeliveryRetrofitClient.httpClient.newCall(request).execute() }
                val nuevos = docsSubidos.toMutableMap(); nuevos[tipo] = fileName; docsSubidos = nuevos
                file.delete()
            } catch (_: Exception) {} finally { uploadEnProgreso = null }
        }
    }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { u -> uploadEnProgreso?.let { tipo -> subirDocumento(tipo, u) } }
    }

    fun abrirPicker(tipo: String) { uploadEnProgreso = tipo; imagePicker.launch("image/*") }

    fun enviarVerificacion() {
        scope.launch {
            sendingVerification = true
            try { DeliveryRetrofitClient.api.enviarVerificacion(); onComplete() }
            catch (_: Exception) {}
            sendingVerification = false
        }
    }

    fun guardarDatosVehiculo() {
        scope.launch {
            try {
                DeliveryRetrofitClient.api.updateProfile(DeliveryProfileUpdateRequest(
                    placaVehiculo = vehiculoPlaca.trim().uppercase(), marcaVehiculo = vehiculoMarca,
                    modeloVehiculo = vehiculoModelo, anioVehiculo = vehiculoAnio.filter { it.isDigit() }.toIntOrNull() ?: 0,
                    colorVehiculo = vehiculoColor, licenciaConduccion = vehiculoLicencia
                )); formSaved = true
            } catch (_: Exception) {}
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // ANIMATIONS - APPLE 2026 AESTHETIC
    // ═══════════════════════════════════════════════════════════════
    val infiniteTransition = rememberInfiniteTransition(label = "docs")

    val orb1X by infiniteTransition.animateFloat(
        initialValue = 0.8f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(6000, easing = EaseInOutCubic), RepeatMode.Reverse), label = "orb1X"
    )
    val orb1Y by infiniteTransition.animateFloat(
        initialValue = 0.15f, targetValue = 0.25f,
        animationSpec = infiniteRepeatable(tween(7000, easing = EaseInOutCubic), RepeatMode.Reverse), label = "orb1Y"
    )
    val orb2X by infiniteTransition.animateFloat(
        initialValue = 0.1f, targetValue = 0.2f,
        animationSpec = infiniteRepeatable(tween(8000, easing = EaseInOutCubic), RepeatMode.Reverse), label = "orb2X"
    )
    val orb2Y by infiniteTransition.animateFloat(
        initialValue = 0.8f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(9000, easing = EaseInOutCubic), RepeatMode.Reverse), label = "orb2Y"
    )
    val orbAlpha by infiniteTransition.animateFloat(
        initialValue = 0.03f, targetValue = 0.06f,
        animationSpec = infiniteRepeatable(tween(4000), RepeatMode.Reverse), label = "orbAlpha"
    )
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.02f, targetValue = 0.04f,
        animationSpec = infiniteRepeatable(tween(3000), RepeatMode.Reverse), label = "glow"
    )

    // Progress bar animation
    val progressAnim by animateFloatAsState(
        targetValue = if (isVisible) completas.toFloat() / totalSecciones else 0f,
        animationSpec = tween(1200, delayMillis = 400, easing = EaseOutCubic),
        label = "progress"
    )

    // ═══════════════════════════════════════════════════════════════
    // UI - ULTRA PREMIUM REDESIGN
    // ═══════════════════════════════════════════════════════════════
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFAFBFA),
                        Color(0xFFF5F7F5),
                        Color(0xFFF0F2F0)
                    )
                )
            )
    ) {
        // Organic background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(DeliveryBrand600.copy(alpha = orbAlpha * 1.5f), Color.Transparent)
                        ),
                        radius = size.width * 0.8f,
                        center = Offset(size.width * orb1X, size.height * orb1Y)
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(DeliveryAccent600.copy(alpha = orbAlpha), Color.Transparent)
                        ),
                        radius = size.width * 0.6f,
                        center = Offset(size.width * orb2X, size.height * orb2Y)
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(DeliveryBrand500.copy(alpha = glowPulse), Color.Transparent)
                        ),
                        radius = size.width * 0.4f,
                        center = Offset(size.width * 0.5f, size.height * 0.5f)
                    )
                }
        )

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            // ═══════════════════════════════════════════════════════
            // HEADER + PROGRESS INDICATOR
            // ═══════════════════════════════════════════════════════
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(600)) + slideInVertically(tween(600), initialOffsetY = { -20 })
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        var backScale by remember { mutableStateOf(1f) }
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.linearGradient(colors = listOf(Color.White, Color(0xFFFAFBFC)))
                                )
                                .border(1.dp, Color(0xFFE8ECEF).copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                                .shadow(8.dp, RoundedCornerShape(14.dp), spotColor = Color(0x08000000))
                                .graphicsLayer { scaleX = backScale; scaleY = backScale }
                                .clickable {
                                    backScale = 0.93f
                                    onBack()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Slate600, modifier = Modifier.size(20.dp))
                        }
                        LaunchedEffect(backScale) {
                            if (backScale != 1f) { delay(100); backScale = 1f }
                        }
                        Spacer(Modifier.width(14.dp))
                        Text("Documentos", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Slate900, modifier = Modifier.weight(1f), letterSpacing = (-0.3).sp)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(DeliveryBrand50)
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text("$completas/$totalSecciones", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DeliveryBrand600)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Paso 2 de 2",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DeliveryBrand600,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            "Documentos",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Slate400
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Slate200)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progressAnim)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(DeliveryBrand500, DeliveryBrand600)
                                    )
                                )
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ═══════════════════════════════════════════════════════
            // WELCOME CARD
            // ══════════════════════════════════════════════════════
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(700, delayMillis = 100, easing = EaseOutCubic)) +
                        slideInVertically(tween(700, delayMillis = 100, easing = EaseOutCubic), initialOffsetY = { -30 })
            ) {
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDFA)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DeliveryBrand400.copy(alpha = 0.2f))
                ) {
                    Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(DeliveryBrand500, DeliveryBrand600)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.RocketLaunch, null, tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("Casi listo!", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Slate900, letterSpacing = (-0.3).sp)
                            Spacer(Modifier.height(4.dp))
                            Text("Sube tus documentos para empezar a generar ganancias como Agrosocio.", fontSize = 13.sp, color = Slate500, lineHeight = 18.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ══════════════════════════════════════════════════════
            // REJECTION BANNER
            // ═══════════════════════════════════════════════════════
            AnimatedVisibility(
                visible = docsRechazados.isNotEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA))
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFEF4444).copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Warning, null, tint = Color(0xFFDC2626), modifier = Modifier.size(20.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Text("${docsRechazados.size} documento(s) rechazado(s)", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = Color(0xFF991B1B))
                        }
                        Spacer(Modifier.height(12.dp))
                        motivosRechazo.forEach { (tipo, motivo) ->
                            val label = when (tipo) { "licencia_frontal" -> "Licencia Frontal"; "licencia_trasera" -> "Licencia Trasera"; "tarjeta_propiedad" -> "Tarjeta Propiedad"; "soat" -> "SOAT"; "perfil" -> "Foto Perfil"; "tecnomecanica" -> "Tecnomecanica"; else -> tipo }
                            Row(Modifier.padding(vertical = 3.dp)) {
                                Text("$label: ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF991B1B))
                                Text(motivo, fontSize = 12.sp, color = Color(0xFF7F1D1D))
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // ═══════════════════════════════════════════════════════
            // DOC 1: Licencia
            // ═══════════════════════════════════════════════════════
            PremiumDocCard(1, "Licencia de Conducir", "Sube tu licencia colombiana vigente (frente y trasera)", "Motos: A1 o A2 · Automoviles: B1 o B2 · Camiones: C1, C2 o C3") {
                UploadRow("licencia_frontal", "Parte Frontal", Icons.Filled.CreditCard, docsSubidos, uploadEnProgreso, ::abrirPicker, "licencia_frontal" in docsRechazados, motivosRechazo["licencia_frontal"] ?: "")
                Spacer(Modifier.height(8.dp))
                UploadRow("licencia_trasera", "Parte Trasera", Icons.Filled.CreditCard, docsSubidos, uploadEnProgreso, ::abrirPicker, "licencia_trasera" in docsRechazados, motivosRechazo["licencia_trasera"] ?: "")
            }

            // ═══════════════════════════════════════════════════════
            // DOC 2: Perfil
            // ═══════════════════════════════════════════════════════
            PremiumDocCard(2, "Foto de Perfil", "Selfie clara, fondo neutro, sin gafas oscuras", "Ejemplo: selfie con buena luz, mirando al frente, sin gorra") {
                UploadRow("perfil", "Subir foto de perfil", Icons.Filled.AddAPhoto, docsSubidos, uploadEnProgreso, ::abrirPicker, "perfil" in docsRechazados, motivosRechazo["perfil"] ?: "")
            }

            // ══════════════════════════════════════════════════════
            // DOC 3: Tarjeta Propiedad + DATOS DEL VEHICULO
            // ═══════════════════════════════════════════════════════
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(700, delayMillis = 400, easing = EaseOutCubic)) +
                        slideInVertically(tween(700, delayMillis = 400, easing = EaseOutCubic), initialOffsetY = { 20 })
            ) {
                Card(
                    Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(DeliveryBrand500, DeliveryBrand600)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("3", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color.White)
                            }
                            Spacer(Modifier.width(14.dp))
                            Column {
                                Text("Tarjeta de Propiedad y Datos del Vehiculo", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Slate900, letterSpacing = (-0.2).sp)
                                Text("Licencia de transito + informacion completa del vehiculo", fontSize = 12.sp, color = Slate400)
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        UploadRow("tarjeta_propiedad", "Subir tarjeta de propiedad", Icons.Filled.CreditCard, docsSubidos, uploadEnProgreso, ::abrirPicker, "tarjeta_propiedad" in docsRechazados, motivosRechazo["tarjeta_propiedad"] ?: "")
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = {
                                CustomTabsIntent.Builder().setShowTitle(true).setShareState(CustomTabsIntent.SHARE_STATE_OFF).build()
                                    .launchUrl(context, Uri.parse("https://portalpublico.runt.gov.co/#/consulta-vehiculo/consulta/consulta-ciudadana"))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DeliveryBrand600)
                        ) {
                            Icon(Icons.Filled.OpenInBrowser, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Verificar vehiculo en RUNT", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Slate100)
                                .border(1.dp, Slate200, RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Copyright (c) 2026 RUNT · Consulta ciudadana oficial", fontSize = 10.sp, color = Slate400)
                        }
                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = Slate200, thickness = 1.dp)
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Build, null, tint = DeliveryBrand600, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Datos del Vehiculo", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = Slate900)
                            if (vehiculoDataGuardado) {
                                Spacer(Modifier.width(8.dp))
                                Icon(Icons.Filled.CheckCircle, null, tint = Color(0xFF22C55E), modifier = Modifier.size(18.dp))
                                Text("Guardado", fontSize = 12.sp, color = Color(0xFF22C55E), fontWeight = FontWeight.SemiBold)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        VehicleForm(
                            tipoVehiculo.value, vehiculoMarca, { vehiculoMarca = it; vehiculoModelo = "" },
                            vehiculoModelo, { vehiculoModelo = it }, vehiculoPlaca, { vehiculoPlaca = it.uppercase() },
                            vehiculoAnio, { vehiculoAnio = it }, vehiculoColor, { vehiculoColor = it },
                            vehiculoLicencia, { vehiculoLicencia = it }
                        ) {
                            guardarDatosVehiculo()
                            vehiculoDataGuardado = true
                        }
                    }
                }
            }

            // ═══════════════════════════════════════════════════════
            // DOC 4: SOAT
            // ═══════════════════════════════════════════════════════
            PremiumDocCard(4, "SOAT Vigente", "Seguro Obligatorio de Accidentes de Transito", "Debe estar vigente y coincidir con la placa del vehiculo") {
                UploadRow("soat", "Subir SOAT", Icons.Filled.HealthAndSafety, docsSubidos, uploadEnProgreso, ::abrirPicker, "soat" in docsRechazados, motivosRechazo["soat"] ?: "")
            }

            // ═══════════════════════════════════════════════════════
            // DOC 5: Tecnomecanica
            // ═══════════════════════════════════════════════════════
            PremiumDocCard(5, "Tecnomecanica", "Certificado de revision tecnomecanica y emisiones", "Obligatorio para vehiculos de mas de 2 años de antiguedad") {
                UploadRow("tecnomecanica", "Subir certificado", Icons.AutoMirrored.Filled.Assignment, docsSubidos, uploadEnProgreso, ::abrirPicker, "tecnomecanica" in docsRechazados, motivosRechazo["tecnomecanica"] ?: "")
            }

            // ═══════════════════════════════════════════════════════
            // DOC 6: Cedula de Ciudadania
            // ═══════════════════════════════════════════════════════
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(700, delayMillis = 600, easing = EaseOutCubic)) +
                        slideInVertically(tween(700, delayMillis = 600, easing = EaseOutCubic), initialOffsetY = { 20 })
            ) {
                Card(
                    Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(DeliveryBrand500, DeliveryBrand600)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("6", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color.White)
                            }
                            Spacer(Modifier.width(14.dp))
                            Column {
                                Text("Cedula de Ciudadania", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Slate900, letterSpacing = (-0.2).sp)
                                Text("Documento de identidad colombiano", fontSize = 12.sp, color = Slate400)
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        UploadRow("cedula", "Subir foto de la cedula", Icons.Filled.CreditCard, docsSubidos, uploadEnProgreso, ::abrirPicker, "cedula" in docsRechazados, motivosRechazo["cedula"] ?: "")
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = numeroIdentidad,
                            onValueChange = { numeroIdentidad = it.filter { c -> c.isDigit() } },
                            placeholder = { Text("Numero de cedula (ej: 1234567890)", fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Filled.Pin, null, Modifier.size(18.dp)) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DeliveryBrand600,
                                unfocusedBorderColor = Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        )
                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    try {
                                        DeliveryRetrofitClient.api.updateProfile(DeliveryProfileUpdateRequest(numeroIdentidad = numeroIdentidad.trim()))
                                    } catch (_: Exception) {}
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DeliveryAccent600),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("Guardar numero de cedula", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ═══════════════════════════════════════════════════════
            // FINAL SUBMIT BUTTON
            // ═══════════════════════════════════════════════════════
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(700, delayMillis = 700, easing = EaseOutCubic)) +
                        slideInVertically(tween(700, delayMillis = 700, easing = EaseOutCubic), initialOffsetY = { 20 })
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    val scale by animateFloatAsState(
                        targetValue = if (isPressed) 0.95f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "scale"
                    )

                    Button(
                        onClick = { enviarVerificacion() },
                        enabled = todosSubidos && !sendingVerification,
                        interactionSource = interactionSource,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DeliveryBrand600,
                            contentColor = Color.White,
                            disabledContainerColor = DeliveryBrand600.copy(alpha = 0.4f),
                            disabledContentColor = Color.White.copy(alpha = 0.7f)
                        ),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 0.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .graphicsLayer { scaleX = scale; scaleY = scale }
                            .shadow(
                                elevation = if (todosSubidos && !sendingVerification) 24.dp else 0.dp,
                                shape = RoundedCornerShape(20.dp),
                                spotColor = DeliveryBrand600.copy(alpha = 0.4f),
                                ambientColor = DeliveryBrand600.copy(alpha = 0.2f)
                            )
                    ) {
                        Text("Enviar Documentos a Revision", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = Color.White, letterSpacing = 0.3.sp)
                        Spacer(Modifier.width(12.dp))
                        if (sendingVerification) {
                            CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.5.dp, color = Color.White)
                        } else {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    if (!todosSubidos) {
                        Text(
                            "Faltan ${totalSecciones - completas} seccion(es) para enviar",
                            fontSize = 12.sp,
                            color = Slate400,
                            modifier = Modifier.padding(bottom = 16.dp),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// PREMIUM DOC CARD COMPONENT
// ═══════════════════════════════════════════════════════════════════
@Composable
private fun PremiumDocCard(number: Int, title: String, subtitle: String, hint: String, content: @Composable () -> Unit) {
    val isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {}

    AnimatedVisibility(
        visible = true,
        enter = fadeIn(tween(700, delayMillis = (number * 100), easing = EaseOutCubic)) +
                slideInVertically(tween(700, delayMillis = (number * 100), easing = EaseOutCubic), initialOffsetY = { 20 })
    ) {
        Card(
            Modifier.fillMaxWidth().padding(vertical = 8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(DeliveryBrand500, DeliveryBrand600)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("$number", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color.White)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Slate900, letterSpacing = (-0.2).sp)
                        Text(subtitle, fontSize = 12.sp, color = Slate400)
                    }
                }
                Spacer(Modifier.height(16.dp))
                content()
                Spacer(Modifier.height(8.dp))
                Text(hint, fontSize = 11.sp, color = Slate400, lineHeight = 16.sp)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// PREMIUM UPLOAD ROW COMPONENT
// ═══════════════════════════════════════════════════════════════════
@Composable
private fun UploadRow(tipo: String, label: String, iconDrawable: ImageVector, docs: Map<String, String>, uploading: String?, onPick: (String) -> Unit, rejected: Boolean = false, rejectReason: String = "") {
    val fileName = docs[tipo]
    val isUploaded = fileName != null && !rejected
    val isUploading = uploading == tipo

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (isPressed) 0.96f else 1f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "uploadScale"
    )

    val bgColor by animateColorAsState(
        when {
            rejected -> Color(0xFFFEF2F2)
            isUploading -> Color(0xFFEFF6FF)
            isUploaded -> Color(0xFFF0FDF4)
            else -> Color(0xFFF6F8FA)
        },
        tween(300, easing = EaseOutCubic),
        label = "bgColor"
    )

    val borderColor by animateColorAsState(
        when {
            rejected -> Color(0xFFFECACA)
            isUploading -> DeliveryBrand400.copy(alpha = 0.4f)
            isUploaded -> Color(0xFFBBF7D0)
            else -> Color(0xFFE8ECEF)
        },
        tween(300, easing = EaseOutCubic),
        label = "borderColor"
    )

    Row(
        Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(interactionSource = interactionSource, indication = null) { if (!isUploading) onPick(tipo) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when {
            rejected -> {
                Box(
                    Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFEF4444)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Close, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF991B1B))
                    Text(rejectReason.ifBlank { "Debes volver a subir este documento" }, fontSize = 11.sp, color = Color(0xFFEF4444), maxLines = 2)
                }
                Icon(Icons.Filled.Warning, null, tint = Color(0xFFEF4444), modifier = Modifier.size(22.dp))
            }
            isUploading -> {
                Box(
                    Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(DeliveryBrand50),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.5.dp, color = DeliveryBrand600)
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
                    Text("Subiendo archivo...", fontSize = 11.sp, color = DeliveryBrand600, fontWeight = FontWeight.Medium)
                }
            }
            isUploaded -> {
                Box(
                    Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF22C55E)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
                    Text(fileName, fontSize = 11.sp, color = Color(0xFF16A34A), maxLines = 1, fontWeight = FontWeight.Medium)
                }
                Icon(Icons.Filled.CheckCircle, null, tint = Color(0xFF22C55E), modifier = Modifier.size(24.dp))
            }
            else -> {
                Box(
                    Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Slate100),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(iconDrawable, null, tint = Slate400, modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.width(14.dp))
                Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Slate500, modifier = Modifier.weight(1f))
                Icon(Icons.Filled.CloudUpload, null, tint = DeliveryBrand400, modifier = Modifier.size(22.dp))
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// VEHICLE FORM COMPONENT
// ══════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VehicleForm(
    tipoVehiculo: String, marca: String, onMarcaChange: (String) -> Unit, modelo: String, onModeloChange: (String) -> Unit,
    placa: String, onPlacaChange: (String) -> Unit, anio: String, onAnioChange: (String) -> Unit, color: String, onColorChange: (String) -> Unit,
    licencia: String, onLicenciaChange: (String) -> Unit, onSave: () -> Unit
) {
    val marcasList = when (tipoVehiculo) { "MOTO" -> marcasMotos; "CAMION" -> marcasCamiones; else -> marcasCarros }
    val modelosList = marcasList.find { it.nombre == marca }?.modelos ?: emptyList()
    var showMarca by remember { mutableStateOf(false) }; var showModelo by remember { mutableStateOf(false) }
    var showColor by remember { mutableStateOf(false) }; var showAnio by remember { mutableStateOf(false) }
    var showLicencia by remember { mutableStateOf(false) }

    Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Datos del vehiculo - ${when (tipoVehiculo) { "MOTO" -> "Motocicleta"; "CAMION" -> "Camion"; else -> "Automovil" }}", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = DeliveryBrand900)
        OutlinedTextField(value = placa, onValueChange = onPlacaChange, placeholder = { Text("Placa: ABC123", fontSize = 13.sp) }, leadingIcon = { Icon(Icons.Filled.Numbers, null, Modifier.size(18.dp)) }, singleLine = true, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DeliveryBrand600, unfocusedBorderColor = Color(0xFFE2E8F0)), modifier = Modifier.fillMaxWidth().height(50.dp))
        Box {
            OutlinedTextField(value = marca, onValueChange = {}, readOnly = true, placeholder = { Text("Selecciona la marca", fontSize = 13.sp) }, leadingIcon = { Icon(Icons.Filled.Warehouse, null, Modifier.size(18.dp)) }, trailingIcon = { IconButton(onClick = { showMarca = true }) { Icon(Icons.Filled.ArrowDropDown, null) } }, singleLine = true, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DeliveryBrand600, unfocusedBorderColor = Color(0xFFE2E8F0)), modifier = Modifier.fillMaxWidth().height(50.dp).clickable { showMarca = true })
            DropdownMenu(expanded = showMarca, onDismissRequest = { showMarca = false }, modifier = Modifier.heightIn(max = 300.dp)) { marcasList.forEach { m -> DropdownMenuItem(text = { Text(m.nombre) }, onClick = { onMarcaChange(m.nombre); showMarca = false }) } }
        }
        Box {
            OutlinedTextField(value = modelo, onValueChange = {}, readOnly = true, placeholder = { Text("Selecciona el modelo", fontSize = 13.sp) }, leadingIcon = { Icon(Icons.AutoMirrored.Filled.DirectionsBike, null, Modifier.size(18.dp)) }, trailingIcon = { IconButton(onClick = { if (marca.isNotBlank()) showModelo = true }) { Icon(Icons.Filled.ArrowDropDown, null) } }, enabled = marca.isNotBlank(), singleLine = true, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DeliveryBrand600, unfocusedBorderColor = Color(0xFFE2E8F0)), modifier = Modifier.fillMaxWidth().height(50.dp).clickable(enabled = marca.isNotBlank()) { if (marca.isNotBlank()) showModelo = true })
            DropdownMenu(expanded = showModelo, onDismissRequest = { showModelo = false }, modifier = Modifier.heightIn(max = 300.dp)) { modelosList.forEach { m -> DropdownMenuItem(text = { Text(m) }, onClick = { onModeloChange(m); showModelo = false }) } }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.weight(1f)) {
                OutlinedTextField(value = anio, onValueChange = {}, readOnly = true, placeholder = { Text("Ano", fontSize = 13.sp) }, leadingIcon = { Icon(Icons.Filled.CalendarMonth, null, Modifier.size(18.dp)) }, trailingIcon = { IconButton(onClick = { showAnio = true }) { Icon(Icons.Filled.ArrowDropDown, null) } }, singleLine = true, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DeliveryBrand600, unfocusedBorderColor = Color(0xFFE2E8F0)), modifier = Modifier.fillMaxWidth().height(50.dp).clickable { showAnio = true })
                DropdownMenu(expanded = showAnio, onDismissRequest = { showAnio = false }, modifier = Modifier.heightIn(max = 250.dp)) { anosVehiculo.forEach { a -> DropdownMenuItem(text = { Text(a) }, onClick = { onAnioChange(a); showAnio = false }) } }
            }
            Box(Modifier.weight(1f)) {
                OutlinedTextField(value = color, onValueChange = {}, readOnly = true, placeholder = { Text("Color", fontSize = 13.sp) }, leadingIcon = { Icon(Icons.Filled.Palette, null, Modifier.size(18.dp)) }, trailingIcon = { IconButton(onClick = { showColor = true }) { Icon(Icons.Filled.ArrowDropDown, null) } }, singleLine = true, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DeliveryBrand600, unfocusedBorderColor = Color(0xFFE2E8F0)), modifier = Modifier.fillMaxWidth().height(50.dp).clickable { showColor = true })
                DropdownMenu(expanded = showColor, onDismissRequest = { showColor = false }, modifier = Modifier.heightIn(max = 250.dp)) { coloresVehiculo.forEach { c -> DropdownMenuItem(text = { Text(c) }, onClick = { onColorChange(c); showColor = false }) } }
            }
        }
        Box {
            OutlinedTextField(value = licencia, onValueChange = {}, readOnly = true, placeholder = { Text("Categoria de licencia", fontSize = 13.sp) }, leadingIcon = { Icon(Icons.Filled.CardMembership, null, Modifier.size(18.dp)) }, trailingIcon = { IconButton(onClick = { showLicencia = true }) { Icon(Icons.Filled.ArrowDropDown, null) } }, singleLine = true, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DeliveryBrand600, unfocusedBorderColor = Color(0xFFE2E8F0)), modifier = Modifier.fillMaxWidth().height(50.dp).clickable { showLicencia = true })
            DropdownMenu(expanded = showLicencia, onDismissRequest = { showLicencia = false }) { categoriasLicencia.forEach { l -> DropdownMenuItem(text = { Text(l) }, onClick = { onLicenciaChange(l); showLicencia = false }) } }
        }
        Button(onClick = onSave, shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = DeliveryAccent600), modifier = Modifier.fillMaxWidth().height(48.dp)) { Text("Guardar datos del vehiculo", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
    }
}
