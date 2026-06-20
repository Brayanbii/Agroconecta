package com.agroconectago.app.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agroconectago.app.data.model.DeliveryProfileUpdateRequest
import com.agroconectago.app.ui.theme.*
import com.agroconectago.app.viewmodel.DeliveryAuthViewModel
import kotlinx.coroutines.delay

// ═══════════ CIUDADES DE COLOMBIA (A-Z) ═══════════
internal val ciudadesColombia = listOf(
    "Abejorral", "Abrego", "Acacias", "Acevedo", "Achí", "Agrado", "Aguachica", "Aguadas", "Aguazul",
    "Agustín Codazzi", "Aipe", "Albania (Guajira)", "Albania (Santander)", "Albán", "Alcalá", "Aldana",
    "Alejandría", "Algarrobo", "Algeciras", "Almaguer", "Almeida", "Alpujarra", "Altamira", "Alto Baudó",
    "Altos del Rosario", "Alvarado", "Amagá", "Amalfi", "Ambalema", "Anapoima", "Ancuya", "Andalucía",
    "Andes", "Angelópolis", "Angostura", "Anolaima", "Anorí", "Anserma", "Ansermanuevo", "Antioquia",
    "Anzá", "Anzoátegui", "Apartadó", "Apía", "Apulo", "Aquitania", "Aracataca", "Aranzazu", "Aratoca",
    "Arauca", "Arauquita", "Arbeláez", "Arboleda", "Arboledas", "Arboletes", "Arcabuco", "Arenal",
    "Argelia (Antioquia)", "Argelia (Cauca)", "Argelia (Valle)", "Ariguaní", "Arjona", "Armenia (Antioquia)",
    "Armenia (Quindío)", "Armero", "Arroyohondo", "Astrea", "Ataco", "Atrato", "Ayapel",
    "Bagadó", "Bahía Solano", "Bajo Baudó", "Balboa (Cauca)", "Balboa (Risaralda)", "Baranoa",
    "Baraya", "Barbacoas", "Barbosa (Antioquia)", "Barbosa (Santander)", "Barichara",
    "Barranca de Upía", "Barrancabermeja", "Barrancas", "Barranco de Loba", "Barranco Minas",
    "Barranquilla", "Becerril", "Belalcázar", "Belén (Boyacá)", "Belén (Nariño)", "Belén de Bajirá",
    "Belén de los Andaquíes", "Belén de Umbría", "Bellavista", "Bello", "Belmira", "Beltrán",
    "Berbeo", "Betania", "Betéitiva", "Betulia (Antioquia)", "Betulia (Santander)", "Bituima",
    "Boavita", "Bochalema", "Bogotá D.C.", "Bojacá", "Bojayá", "Bolívar (Antioquia)",
    "Bolívar (Cauca)", "Bolívar (Santander)", "Bolívar (Valle)", "Bosconia", "Boyacá", "Briceño (Antioquia)",
    "Briceño (Boyacá)", "Bucaramanga", "Bucarasica", "Buenaventura", "Buenavista (Boyacá)",
    "Buenavista (Córdoba)", "Buenavista (Quindío)", "Buenavista (Sucre)", "Buesaco", "Bugalagrande",
    "Buriticá", "Busbanzá",
    "Cabrera (Cundinamarca)", "Cabrera (Santander)", "Cabuyaro", "Cáceres", "Cachipay", "Cáchira",
    "Cácota", "Caicedo", "Caicedonia", "Caimito", "Cajamarca", "Cajibío", "Cajicá", "Calamar (Bolívar)",
    "Calamar (Guaviare)", "Calarcá", "Caldas", "Caldono", "California", "Calima", "Caloto",
    "Calzada Larga", "Campamento", "Campo de la Cruz", "Campoalegre", "Campohermoso",
    "Canalete", "Candelaria (Atlántico)", "Candelaria (Valle)", "Cantagallo", "Cantón de San Pablo",
    "Caparrapí", "Capitanejo", "Cáqueza", "Caracolí", "Caramanta", "Carcasí", "Carepa", "Carmen de Apicalá",
    "Carmen de Carupa", "Carmen de Viboral", "Carmen del Darién", "Carolina", "Cartagena de Indias",
    "Cartagena del Chairá", "Cartago", "Carurú", "Casabianca", "Castilla la Nueva", "Caucasia",
    "Cavasa", "Cepitá", "Cereté", "Cerinza", "Cerro de San Antonio", "Cerroazul", "Chachagüí",
    "Chaguaní", "Chalán", "Chameza", "Chaparral", "Charalá", "Charta", "Chía", "Chibolo", "Chigorodó",
    "Chima (Córdoba)", "Chima (Santander)", "Chimichagua", "Chimá", "Chinácota", "Chinavita",
    "Chinchiná", "Chinú", "Chipaque", "Chipatá", "Chiquinquirá", "Chíquiza", "Chiriguaná", "Chiscas",
    "Chita", "Chitagá", "Chitaraque", "Chivatá", "Choachí", "Chocontá", "Cicuco", "Ciénaga",
    "Ciénaga de Oro", "Cimitarra", "Circasia", "Cisneros", "Ciudad Bolívar", "Clemencia", "Cocorná",
    "Coello", "Cogua", "Colombia", "Colón (Nariño)", "Colón (Putumayo)", "Colosó", "Cómbita",
    "Concepción (Antioquia)", "Concepción (Santander)", "Concordia (Antioquia)", "Concordia (Magdalena)",
    "Condoto", "Confines", "Consacá", "Contadero", "Contratación", "Convención", "Copacabana",
    "Coper", "Córdoba (Bolívar)", "Córdoba (Nariño)", "Córdoba (Quindío)", "Corinto", "Cormacarena",
    "Coromoro", "Corozal", "Corrales", "Cota", "Cotorra", "Covarachía", "Coveñas", "Coyaima",
    "Cravo Norte", "Cuaspud", "Cubará", "Cubarral", "Cucaita", "Cucunubá", "Cúcuta", "Cucutilla",
    "Cuítiva", "Cumaral", "Cumaribo", "Cumbal", "Cumbitara", "Cunday", "Curillo", "Curití", "Curumaní",
    "Dabeiba", "Dagua", "Dibulla", "Distracción", "Dolores", "Don Matías", "Doncello", "Dosquebradas",
    "Duitama", "Durania",
    "Ebéjico", "El Águila", "El Bagre", "El Banco", "El Bordo", "El Cairo", "El Calvario",
    "El Cantón del San Pablo", "El Carmen (Norte de Santander)", "El Carmen de Atrato",
    "El Carmen de Bolívar", "El Castillo", "El Cerrito", "El Charco", "El Cocuy", "El Colegio",
    "El Copey", "El Doncello", "El Dorado", "El Dovio", "El Encanto", "El Espino", "El Guacamayo",
    "El Guamo", "El Litoral del San Juan", "El Molino", "El Paso", "El Paujil", "El Peñol",
    "El Piñón", "El Playón", "El Retén", "El Retiro", "El Roble", "El Rosal", "El Rosario",
    "El Santuario", "El Tablón de Gómez", "El Tambo (Cauca)", "El Tambo (Nariño)", "El Tarra",
    "El Zulia", "Elías", "Encino", "Enciso", "Entrerríos", "Envigado", "Espinal",
    "Facatativá", "Falán", "Filadelfia", "Filandia", "Firavitoba", "Flandes", "Florencia (Cauca)",
    "Florencia (Caquetá)", "Floresta", "Florida", "Floridablanca", "Fómeque", "Fonseca",
    "Fortul", "Fosca", "Francisco Pizarro", "Fredonia", "Fresno", "Frontino", "Fuente de Oro",
    "Fundación", "Funes", "Funza", "Fúquene", "Fusagasugá",
    "Gachalá", "Gachancipá", "Gachantivá", "Gachetá", "Galán", "Galapa", "Galeras", "Gama",
    "Gamarra", "Gambita", "Gámeza", "Garagoa", "Garzón", "Génova", "Gigante", "Ginebra",
    "Giraldo", "Girardot", "Girardota", "Girón", "Gómez Plata", "González", "Gramalote",
    "Granada (Antioquia)", "Granada (Cundinamarca)", "Granada (Meta)", "Guaca", "Guacamayas",
    "Guacarí", "Guachetá", "Guachucal", "Guadalupe (Antioquia)", "Guadalupe (Huila)",
    "Guadalupe (Santander)", "Guaduas", "Guaitarilla", "Gualmatán", "Guamal (Magdalena)",
    "Guamal (Meta)", "Guamo", "Guapí", "Guapotá", "Guaranda", "Guarne", "Guasca", "Guatapé",
    "Guataquí", "Guatavita", "Guateque", "Guática", "Guavatá", "Guayabal de Síquima",
    "Guayabetal", "Guayata", "Güepsa", "Güicán", "Gutiérrez",
    "Hacarí", "Hatillo de Loba", "Hato", "Hato Corozal", "Hatonuevo", "Heliconia", "Herrán",
    "Herveo", "Hispania", "Hobo", "Honda", "Ibagué", "Icononzo", "Iles", "Imués", "Inírida",
    "Inzá", "Ipiales", "Iquira", "Isnos", "Istmina", "Itagüí", "Ituango",
    "Jamundí", "Jardín", "Jenesano", "Jericó (Antioquia)", "Jericó (Boyacá)", "Jerusalén",
    "Jesús María", "Jordán", "Juan de Acosta", "Junín", "Juradó",
    "La Apartada", "La Argentina", "La Belleza", "La Calera", "La Capilla", "La Ceja", "La Celia",
    "La Chamba", "La Chorrera", "La Cruz", "La Cumbre", "La Dorada", "La Esperanza", "La Estrella",
    "La Florida", "La Gloria", "La Guadalupe", "La Jagua de Ibirico", "La Jagua del Pilar",
    "La Llanada", "La Macarena", "La Merced", "La Mesa", "La Montañita", "La Palma", "La Paz (Cesar)",
    "La Paz (Santander)", "La Pedrera", "La Peña", "La Pintada", "La Plata", "La Playa", "La Primavera",
    "La Salina", "La Sierra", "La Tebaida", "La Tola", "La Unión (Antioquia)", "La Unión (Nariño)",
    "La Unión (Sucre)", "La Unión (Valle)", "La Uvita", "La Vega (Cauca)", "La Vega (Cundinamarca)",
    "La Victoria (Amazonas)", "La Victoria (Valle)", "La Virginia", "Labateca", "Labranzagrande",
    "Landázuri", "Lebrija", "Leiva", "Lejanías", "Lenguazaque", "Lérida", "Leticia", "Líbano",
    "Linares", "Lloró", "López de Micay", "Lorica", "Los Andes", "Los Córdobas", "Los Palmitos",
    "Los Patios", "Los Santos", "Lourdes", "Luruaco",
    "Macanal", "Macaravita", "Maceo", "Machetá", "Madrid", "Magangué", "Magüí", "Mahates",
    "Maicao", "Majagual", "Málaga", "Malambo", "Mallama", "Manatí", "Manaure (Cesar)",
    "Manaure (Guajira)", "Maní", "Manizales", "Manta", "Manuelita", "Mapiripán", "Margarita",
    "María la Baja", "Marinilla", "Maripí", "Mariquita", "Marmato", "Marquetalia", "Marsella",
    "Marulanda", "Matanza", "Medellín", "Medina", "Medio Atrato", "Medio Baudó", "Medio San Juan",
    "Melgar", "Mercaderes", "Mesetas", "Milán", "Miraflores (Boyacá)", "Miraflores (Guaviare)",
    "Miranda", "Mistrató", "Mitú", "Mocoa", "Mogotes", "Molagavita", "Momil", "Mompós",
    "Mongua", "Monguí", "Moniquirá", "Montebello", "Montecristo", "Montelíbano", "Montenegro",
    "Montería", "Monterrey", "Morales (Bolívar)", "Morales (Cauca)", "Morelia", "Morichal",
    "Morroa", "Mosquera (Cundinamarca)", "Mosquera (Nariño)", "Motavita", "Moñitos", "Murillo",
    "Murindó", "Mutatá", "Mutiscua",
    "Nariño (Antioquia)", "Nariño (Cundinamarca)", "Nariño (Nariño)", "Natagaima", "Nechí",
    "Nemocón", "Neiva", "Nilo", "Nimaima", "Nobsa", "Nocaima", "Norcasia", "Norosí",
    "Nueva Granada", "Nuevo Colón", "Nunchía", "Nuquí",
    "Obando", "Ocaña", "Oiba", "Oicatá", "Olaya", "Olaya Herrera", "Onzaga", "Oporapa",
    "Orito", "Orocué", "Ortega", "Ospina", "Otanche", "Ovejas", "Pachavita",
    "Pacho", "Pácora", "Padilla", "Páez (Boyacá)", "Páez (Cauca)", "Paicol", "Pailitas",
    "Paime", "Paipa", "Pajarito", "Palermo", "Palestina (Caldas)", "Palestina (Huila)",
    "Palmar", "Palmar de Varela", "Palmas del Socorro", "Palmira", "Palmito", "Palocabildo",
    "Pamplona", "Pamplonita", "Pance", "Pandi", "Panqueba", "Paquisha", "Páramo", "Paratebueno",
    "Pasca", "Pasto", "Patía", "Pauna", "Paya", "Paz del Río", "Paz de Ariporo", "Pedraza",
    "Pelaya", "Peñón", "Pensilvania", "Peque", "Pereira", "Pesca", "Piamonte", "Pie de Cuesta",
    "Piedecuesta", "Piedras", "Piendamó", "Pijao", "Pijiño del Carmen", "Pinchote",
    "Pinillos", "Piojó", "Pisba", "Pital", "Pitalito", "Pivijay", "Planadas",
    "Planeta Rica", "Plato", "Policarpa", "Polonuevo", "Ponedera", "Popayán", "Pore",
    "Potrerillo", "Potreritos", "Potosí", "Pradera", "Prado", "Providencia", "Pueblo Bello",
    "Pueblo Nuevo", "Pueblo Rico", "Pueblorrico", "Puente Nacional", "Puerres", "Puerto Alegría",
    "Puerto Arica", "Puerto Asís", "Puerto Berrío", "Puerto Bogotá", "Puerto Boyacá",
    "Puerto Caicedo", "Puerto Carreño", "Puerto Colombia", "Puerto Concordia", "Puerto Escondido",
    "Puerto Gaitán", "Puerto Guzmán", "Puerto Inírida", "Puerto Leguízamo", "Puerto Lleras",
    "Puerto López", "Puerto Nare", "Puerto Nariño", "Puerto Parra", "Puerto Rico (Caquetá)",
    "Puerto Rico (Meta)", "Puerto Rondón", "Puerto Salgar", "Puerto Santander", "Puerto Tejada",
    "Puerto Triunfo", "Puerto Wilches", "Pulí", "Pupiales", "Puracé", "Purificación", "Purísima",
    "Quebradanegra", "Quetame", "Quibdó", "Quimbaya", "Quinchía", "Quípama", "Quipile",
    "Ragonvalia", "Ramiriquí", "Ráquira", "Recetor", "Regidor", "Remedios", "Remolino",
    "Repelón", "Restrepo (Meta)", "Restrepo (Valle)", "Retiro", "Ricaurte (Cundinamarca)",
    "Ricaurte (Nariño)", "Río de Oro", "Río Iró", "Río Quito", "Río Viejo", "Rioblanco",
    "Riofrío", "Riohacha", "Rionegro (Antioquia)", "Rionegro (Santander)", "Risaralda",
    "Rivera", "Roberto Payán", "Roldanillo", "Roncesvalles", "Rondón", "Rosas",
    "Sabana de Torres", "Sabanagrande", "Sabanalarga (Antioquia)", "Sabanalarga (Atlántico)",
    "Sabanalarga (Casanare)", "Sabanas de San Ángel", "Sabaneta", "Saboyá", "Sácama",
    "Sáchica", "Sahagún", "Saladoblanco", "Salamina (Caldas)", "Salamina (Magdalena)",
    "Salazar", "Saldaña", "Salento", "Salgar", "Samacá", "Samaniego", "Samaná",
    "Sampués", "San Agustín", "San Alberto", "San Andrés (San Andrés)", "San Andrés (Santander)",
    "San Andrés de Cuerquia", "San Andrés de Sotavento", "San Antero", "San Antonio",
    "San Antonio del Tequendama", "San Benito", "San Benito Abad", "San Bernardo",
    "San Bernardo del Viento", "San Calixto", "San Carlos (Antioquia)", "San Carlos (Córdoba)",
    "San Carlos de Guaroa", "San Cayetano (Cundinamarca)", "San Cayetano (Norte de Santander)",
    "San Cristóbal", "San Diego", "San Eduardo", "San Estanislao", "San Fernando",
    "San Francisco (Antioquia)", "San Francisco (Cundinamarca)", "San Francisco (Putumayo)",
    "San Gil", "San Jacinto", "San Jacinto del Cauca", "San Jerónimo", "San Joaquín",
    "San José", "San José de Albán", "San José de la Montaña", "San José de Miranda",
    "San José de Pare", "San José del Fragua", "San José del Guaviare", "San José del Palmar",
    "San Juan de Arama", "San Juan de Betulia", "San Juan de Rioseco", "San Juan de Urabá",
    "San Juan del Cesar", "San Juan Nepomuceno", "San Juanito", "San Lorenzo",
    "San Luis (Antioquia)", "San Luis (Tolima)", "San Luis de Cubarral", "San Luis de Gaceno",
    "San Luis de Palenque", "San Marcos", "San Martín (Cesar)", "San Martín (Meta)",
    "San Martín de Loba", "San Mateo", "San Miguel (Putumayo)", "San Miguel (Santander)",
    "San Miguel de Sema", "San Onofre", "San Pablo (Bolívar)", "San Pablo (Nariño)",
    "San Pablo de Borbur", "San Pedro (Sucre)", "San Pedro (Valle)", "San Pedro de Cartago",
    "San Pedro de los Milagros", "San Pedro de Urabá", "San Pelayo", "San Rafael",
    "San Roque", "San Sebastián", "San Sebastián de Buenavista", "San Vicente (Antioquia)",
    "San Vicente de Chucurí", "San Vicente del Caguán", "San Zenón", "Sandoná",
    "Santa Ana", "Santa Bárbara (Antioquia)", "Santa Bárbara (Nariño)", "Santa Bárbara (Santander)",
    "Santa Bárbara de Pinto", "Santa Catalina", "Santa Cruz de Lorica", "Santa Fe de Antioquia",
    "Santa Helena del Opón", "Santa Isabel", "Santa Lucía", "Santa María (Boyacá)",
    "Santa María (Huila)", "Santa Marta", "Santa Rosa (Bolívar)", "Santa Rosa (Cauca)",
    "Santa Rosa de Cabal", "Santa Rosa de Osos", "Santa Rosa de Viterbo", "Santa Rosa del Sur",
    "Santa Rosalía", "Santa Sofía", "Santacruz", "Santana", "Santander de Quilichao",
    "Santiago (Norte de Santander)", "Santiago (Putumayo)", "Santiago de Cali", "Santiago de Tolú",
    "Santo Domingo", "Santo Tomás", "Santodomingo", "Santuario (Risaralda)", "Sapuyes",
    "Saravena", "Sardinata", "Sasaima", "Sativanorte", "Sativasur", "Segovia",
    "Sesquilé", "Sevilla", "Siachoque", "Sibaté", "Sibundoy", "Silos", "Silvania",
    "Silvia", "Simacota", "Simijaca", "Simití", "Sincelejo", "Sincé", "Sipí",
    "Sitionuevo", "Soacha", "Soatá", "Socha", "Socorro", "Socotá", "Sogamoso",
    "Solano", "Soledad", "Solita", "Somondoco", "Sonsón", "Sopetrán", "Soplaviento",
    "Sopó", "Sora", "Soracá", "Sotaquirá", "Sotará", "Suaita", "Suan",
    "Suárez (Cauca)", "Suárez (Tolima)", "Suaza", "Subachoque", "Sucre (Cauca)",
    "Sucre (Santander)", "Sucre (Sucre)", "Suesca", "Supatá", "Supía", "Suratá",
    "Susacón", "Sutamarchán", "Sutatenza",
    "Tadó", "Talaigua Nuevo", "Tamalameque", "Támara", "Tame", "Támesis", "Tangua",
    "Tarapacá", "Tarazá", "Tarqui", "Tarso", "Tasco", "Tausa", "Tello", "Tena",
    "Tenerife", "Tenjo", "Tenza", "Teorama", "Teruel", "Tesalia", "Tibacuy",
    "Tibaná", "Tibasosa", "Tibirita", "Tibú", "Tierralta", "Timaná", "Timbío",
    "Timbiquí", "Tinjacá", "Tipacoque", "Tiquisio", "Titiribí", "Toca", "Tocaima",
    "Tocancipá", "Togüí", "Toledo (Antioquia)", "Toledo (Norte de Santander)",
    "Tolú", "Tolú Viejo", "Tópaga", "Toral", "Toro", "Tota", "Totoro",
    "Trinidad", "Trujillo", "Tubará", "Tuluá", "Tumaco", "Tunja", "Tununguá",
    "Túquerres", "Turbaco", "Turbaná", "Turbo", "Turmequé", "Tuta", "Tutazá",
    "Ubalá", "Ubaque", "Ubaté", "Ulloa", "Umbita", "Une", "Unguía", "Unión Panamericana",
    "Uramita", "Uribe", "Uribía", "Urrao", "Urumita", "Usiacurí", "Útica",
    "Valdivia", "Valencia", "Valledupar", "Valle de San José", "Valle de San Juan",
    "Valle del Guamuez", "Valledupar", "Valparaíso (Antioquia)", "Valparaíso (Caquetá)",
    "Vegachí", "Vélez", "Venadillo", "Venecia (Antioquia)", "Venecia (Cundinamarca)",
    "Ventaquemada", "Vergara", "Versalles", "Vetilla", "Vianí", "Victoria",
    "Vigía del Fuerte", "Vijes", "Villa Caro", "Villa de Leyva", "Villa del Rosario",
    "Villa Garzón", "Villa Rica", "Villagómez", "Villahermosa", "Villamaría",
    "Villanueva (Bolívar)", "Villanueva (Casanare)", "Villanueva (Guajira)",
    "Villanueva (Santander)", "Villapinzón", "Villarrica", "Villavicencio",
    "Villavieja", "Villeta", "Viotá", "Viracachá", "Vista Hermosa", "Viterbo",
    "Yacopí", "Yacuanquer", "Yaguará", "Yalí", "Yarumal", "Yolombó", "Yondó",
    "Yopal", "Yotoco", "Yumbo", "Zambrano", "Zapatoca", "Zaragoza", "Zarzal",
    "Zetaquira", "Zipacón", "Zipaquirá", "Zona Bananera"
)

data class VehiculoTipo(
    val id: String, val label: String, val icon: ImageVector,
    val descripcion: String, val capacidad: String,
    val documentos: List<String>
)

private val vehiculosTipos = listOf(
    VehiculoTipo(
        id = "MOTO", label = "Motocicleta", icon = Icons.Filled.Motorcycle,
        descripcion = "Ideal para entregas rapidas en ciudad",
        capacidad = "Hasta 50 kg",
        documentos = listOf("Licencia de conduccion A2", "Tarjeta de propiedad", "SOAT vigente", "Tecnomecanica")
    ),
    VehiculoTipo(
        id = "AUTOMOVIL", label = "Automovil", icon = Icons.Filled.DirectionsCar,
        descripcion = "Perfecto para cargas medianas urbanas",
        capacidad = "Hasta 300 kg",
        documentos = listOf("Licencia de conduccion B1", "Tarjeta de propiedad", "SOAT vigente", "Tecnomecanica")
    ),
    VehiculoTipo(
        id = "CAMION", label = "Camion", icon = Icons.Filled.LocalShipping,
        descripcion = "Para transporte intermunicipal de carga",
        capacidad = "Mas de 500 kg",
        documentos = listOf("Licencia de conduccion C1", "Tarjeta de propiedad", "SOAT vigente", "Tecnomecanica", "Seguro de carga")
    )
)

@Composable
fun DeliveryProfileCompletionScreen(
    viewModel: DeliveryAuthViewModel,
    onProfileComplete: () -> Unit
) {
    // ═══════════════════════════════════════════════════════════════
    // LOGIC PRESERVED - NO CHANGES
    // ══════════════════════════════════════════════════════════════
    val estaCargando by viewModel.estaCargando.collectAsState()
    val perfilActualizado by viewModel.perfilActualizado.collectAsState()
    val mensajePerfil by viewModel.mensajePerfil.collectAsState()

    var ciudadSeleccionada by remember { mutableStateOf("") }
    var ciudadBusqueda by remember { mutableStateOf("") }
    var mostrarListaCiudades by remember { mutableStateOf(false) }
    var vehiculoSeleccionado by remember { mutableStateOf("") }
    var errorCampo by remember { mutableStateOf<String?>(null) }

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(100); isVisible = true }

    LaunchedEffect(perfilActualizado) {
        if (perfilActualizado) {
            delay(1200)
            viewModel.limpiarMensajePerfil()
            onProfileComplete()
        }
    }

    val ciudadesFiltradas = remember(ciudadBusqueda) {
        if (ciudadBusqueda.isBlank()) emptyList()
        else ciudadesColombia.filter { it.contains(ciudadBusqueda, ignoreCase = true) }.take(20)
    }

    // ═══════════════════════════════════════════════════════════════
    // ANIMATIONS - APPLE 2026 AESTHETIC
    // ═══════════════════════════════════════════════════════════════
    val infiniteTransition = rememberInfiniteTransition(label = "prof")

    // Organic background orbs
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
        targetValue = if (isVisible) 0.5f else 0f,
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
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            // ═══════════════════════════════════════════════════════
            // PROGRESS INDICATOR - MINIMALIST
            // ═══════════════════════════════════════════════════════
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(600)) + slideInVertically(tween(600), initialOffsetY = { -20 })
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Paso 1 de 2",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DeliveryBrand600,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            "Perfil basico",
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

            Spacer(Modifier.height(32.dp))

            // ═══════════════════════════════════════════════════════
            // HEADER - STAGGERED ENTRANCE
            // ═══════════════════════════════════════════════════════
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(700, delayMillis = 100, easing = EaseOutCubic)) +
                        slideInVertically(tween(700, delayMillis = 100, easing = EaseOutCubic), initialOffsetY = { -40 })
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val logoGlow by infiniteTransition.animateFloat(
                        initialValue = 0.3f, targetValue = 0.5f,
                        animationSpec = infiniteRepeatable(tween(2500), RepeatMode.Reverse), label = "logoGlow"
                    )
                    val logoScale by infiniteTransition.animateFloat(
                        initialValue = 0.98f, targetValue = 1.02f,
                        animationSpec = infiniteRepeatable(tween(3000), RepeatMode.Reverse), label = "logoScale"
                    )

                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .graphicsLayer { scaleX = logoScale; scaleY = logoScale }
                            .clip(RoundedCornerShape(22.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(DeliveryBrand500, DeliveryBrand600, DeliveryBrand900)
                                )
                            )
                            .shadow(
                                elevation = 28.dp,
                                shape = RoundedCornerShape(22.dp),
                                spotColor = DeliveryBrand600.copy(alpha = logoGlow),
                                ambientColor = DeliveryBrand600.copy(alpha = logoGlow * 0.5f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(Color.White.copy(alpha = 0.15f), Color.Transparent),
                                        center = Offset(50f, 22f),
                                        radius = 65f
                                    )
                                )
                        )
                        Icon(Icons.Filled.RocketLaunch, null, tint = Color.White, modifier = Modifier.size(40.dp))
                    }

                    Spacer(Modifier.height(20.dp))

                    Text(
                        "Bienvenido, Agrosocio!",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Slate900,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Completa tu perfil y empieza a generar ganancias\nentregando la cosecha colombiana.",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Slate400,
                        lineHeight = 20.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // ═══════════════════════════════════════════════════════
            // SUCCESS CARD
            // ═══════════════════════════════════════════════════════
            AnimatedVisibility(
                visible = perfilActualizado,
                enter = fadeIn(tween(500)) + scaleIn(tween(500, easing = EaseOutBack))
            ) {
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = DeliveryBrand50),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DeliveryBrand400.copy(alpha = 0.3f))
                ) {
                    Column(Modifier.padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.CheckCircle, null, tint = DeliveryBrand600, modifier = Modifier.size(52.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("Datos guardados", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Slate900, letterSpacing = (-0.3).sp)
                        Spacer(Modifier.height(8.dp))
                        Text("Ahora sube tus documentos para completar tu registro como Agrosocio.", fontSize = 14.sp, color = Slate400, lineHeight = 20.sp)
                        Spacer(Modifier.height(12.dp))
                        Text("Redirigiendo...", fontSize = 12.sp, color = DeliveryBrand400, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // ══════════════════════════════════════════════════════
            // FORM - STAGGERED ENTRANCE
            // ═══════════════════════════════════════════════════════
            AnimatedVisibility(
                visible = !perfilActualizado && isVisible,
                enter = fadeIn(tween(700, delayMillis = 200, easing = EaseOutCubic)) +
                        slideInVertically(tween(700, delayMillis = 200, easing = EaseOutCubic), initialOffsetY = { 30 })
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.White.copy(alpha = 0.95f), Color(0xFFFAFBFC).copy(alpha = 0.9f))
                            )
                        )
                        .border(1.dp, Color(0xFFE8ECEF).copy(alpha = 0.6f), RoundedCornerShape(28.dp))
                        .shadow(20.dp, RoundedCornerShape(28.dp), spotColor = Color(0x08000000))
                        .padding(28.dp)
                ) {
                    // ═══════════════════════════════════════════════
                    // CITY SELECTOR - PREMIUM CARD
                    // ═══════════════════════════════════════════════
                    Text("Donde repartiras?", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Slate900, letterSpacing = (-0.3).sp)
                    Text("Selecciona la ciudad donde realizas las entregas", fontSize = 13.sp, color = Slate400)
                    Spacer(Modifier.height(14.dp))

                    // City input card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFFFAFBFC), Color.White)
                                )
                            )
                            .border(
                                1.5.dp,
                                if (mostrarListaCiudades) DeliveryBrand600 else Color(0xFFE2E8F0),
                                RoundedCornerShape(18.dp)
                            )
                            .shadow(
                                elevation = if (mostrarListaCiudades) 8.dp else 0.dp,
                                shape = RoundedCornerShape(18.dp),
                                spotColor = DeliveryBrand600.copy(alpha = 0.15f)
                            )
                    ) {
                        OutlinedTextField(
                            value = if (mostrarListaCiudades) ciudadBusqueda else ciudadSeleccionada,
                            onValueChange = {
                                ciudadBusqueda = it
                                ciudadSeleccionada = if (!mostrarListaCiudades) it else ciudadSeleccionada
                                mostrarListaCiudades = true
                            },
                            placeholder = { Text("Buscar ciudad...", fontSize = 14.sp, color = Slate400, fontWeight = FontWeight.Medium) },
                            leadingIcon = {
                                Icon(Icons.Filled.LocationOn, null, tint = DeliveryBrand600, modifier = Modifier.size(22.dp))
                            },
                            trailingIcon = {
                                if (ciudadSeleccionada.isNotEmpty() && !mostrarListaCiudades) {
                                    IconButton(onClick = { ciudadSeleccionada = ""; ciudadBusqueda = "" }, Modifier.size(28.dp)) {
                                        Icon(Icons.Filled.Close, null, tint = Slate400, modifier = Modifier.size(18.dp))
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(18.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                cursorColor = DeliveryBrand600,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth().height(58.dp)
                        )
                    }

                    // City dropdown
                    AnimatedVisibility(
                        visible = mostrarListaCiudades && ciudadesFiltradas.isNotEmpty(),
                        enter = expandVertically(tween(300, easing = EaseOutCubic)) + fadeIn(tween(300)),
                        exit = shrinkVertically(tween(200)) + fadeOut(tween(200))
                    ) {
                        Card(
                            Modifier.fillMaxWidth().heightIn(max = 220.dp),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            LazyColumn {
                                items(ciudadesFiltradas) { ciudad ->
                                    val cityInteraction = remember { MutableInteractionSource() }
                                    val cityPressed by cityInteraction.collectIsPressedAsState()
                                    val cityScale by animateFloatAsState(
                                        if (cityPressed) 0.97f else 1f,
                                        spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                        label = "city"
                                    )

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .graphicsLayer { scaleX = cityScale; scaleY = cityScale }
                                            .clickable(interactionSource = cityInteraction, indication = null) {
                                                ciudadSeleccionada = ciudad
                                                ciudadBusqueda = ""
                                                mostrarListaCiudades = false
                                                errorCampo = null
                                            }
                                            .padding(horizontal = 16.dp, vertical = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Filled.LocationOn, null, tint = Slate400, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(12.dp))
                                        Text(ciudad, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Slate700)
                                    }
                                }
                            }
                        }
                    }

                    if (ciudadSeleccionada.isNotEmpty() && !mostrarListaCiudades) {
                        Spacer(Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CheckCircle, null, tint = DeliveryBrand600, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Ciudad seleccionada: $ciudadSeleccionada", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DeliveryBrand600)
                        }
                    }

                    Spacer(Modifier.height(28.dp))

                    // ═══════════════════════════════════════════════
                    // VEHICLE TYPE SELECTOR - PREMIUM CARDS
                    // ═══════════════════════════════════════════════
                    Text("Metodo de entrega", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Slate900, letterSpacing = (-0.3).sp)
                    Text("Selecciona el vehiculo que usaras para repartir", fontSize = 13.sp, color = Slate400)
                    Spacer(Modifier.height(14.dp))

                    vehiculosTipos.forEach { vt ->
                        val isSelected = vehiculoSeleccionado == vt.id
                        val vehicleInteraction = remember { MutableInteractionSource() }
                        val vehiclePressed by vehicleInteraction.collectIsPressedAsState()
                        val vehicleScale by animateFloatAsState(
                            if (vehiclePressed) 0.96f else if (isSelected) 1.02f else 1f,
                            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                            label = "vehicle"
                        )

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .graphicsLayer { scaleX = vehicleScale; scaleY = vehicleScale }
                                .clickable(interactionSource = vehicleInteraction, indication = null) {
                                    vehiculoSeleccionado = vt.id
                                    errorCampo = null
                                },
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) DeliveryBrand50 else Color.White
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                if (isSelected) 2.dp else 1.5.dp,
                                if (isSelected) DeliveryBrand400 else Color(0xFFE2E8F0)
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = if (isSelected) 6.dp else 0.dp
                            )
                        ) {
                            Column(Modifier.padding(18.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        Modifier
                                            .size(52.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(
                                                if (isSelected) Brush.linearGradient(
                                                    listOf(DeliveryBrand500, DeliveryBrand600)
                                                ) else Brush.linearGradient(
                                                    listOf(Slate100, Color.White)
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            vt.icon,
                                            null,
                                            tint = if (isSelected) Color.White else Slate500,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                    Spacer(Modifier.width(16.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            vt.label,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = if (isSelected) DeliveryBrand900 else Slate700,
                                            letterSpacing = (-0.2).sp
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        Text(vt.descripcion, fontSize = 12.sp, color = Slate400)
                                        Spacer(Modifier.height(3.dp))
                                        Text(
                                            vt.capacidad,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = DeliveryBrand600
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            Icons.Filled.CheckCircle,
                                            null,
                                            tint = DeliveryBrand600,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }

                                AnimatedVisibility(
                                    visible = isSelected,
                                    enter = expandVertically(tween(300, easing = EaseOutCubic)) + fadeIn(tween(300)),
                                    exit = shrinkVertically(tween(200)) + fadeOut(tween(200))
                                ) {
                                    Column(Modifier.padding(top = 14.dp)) {
                                        Text("Documentos obligatorios:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate700)
                                        Spacer(Modifier.height(6.dp))
                                        vt.documentos.forEach { doc ->
                                            Row(Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Filled.Description, null, tint = DeliveryAccent600, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(8.dp))
                                                Text(doc, fontSize = 12.sp, color = Slate500)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ═══════════════════════════════════════════════
                    // ERROR MESSAGES
                    // ═══════════════════════════════════════════════
                    AnimatedVisibility(
                        visible = errorCampo != null,
                        enter = fadeIn(tween(300)) + slideInVertically(tween(300), initialOffsetY = { -6 }),
                        exit = fadeOut(tween(200))
                    ) {
                        errorCampo?.let {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 14.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        Brush.linearGradient(colors = listOf(ErrorBg, ErrorBg.copy(alpha = 0.8f)))
                                    )
                                    .border(1.dp, ErrorBorder, RoundedCornerShape(16.dp))
                                    .padding(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.Top) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFFDC2626).copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.ErrorOutline, null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Text(it, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFFDC2626), lineHeight = 18.sp, modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = mensajePerfil != null && !perfilActualizado,
                        enter = fadeIn(tween(300)),
                        exit = fadeOut(tween(200))
                    ) {
                        mensajePerfil?.let {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = if (errorCampo != null) 12.dp else 14.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        Brush.linearGradient(colors = listOf(ErrorBg, ErrorBg.copy(alpha = 0.8f)))
                                    )
                                    .border(1.dp, ErrorBorder, RoundedCornerShape(16.dp))
                                    .padding(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.Top) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFFDC2626).copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.ErrorOutline, null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Text(it, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFFDC2626), lineHeight = 18.sp, modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            // ═══════════════════════════════════════════════════════
            // CONTINUE BUTTON - ENHANCED TACTILE FEEDBACK
            // ═══════════════════════════════════════════════════════
            if (!perfilActualizado) {
                Spacer(Modifier.height(28.dp))

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
                    onClick = {
                        errorCampo = when {
                            ciudadSeleccionada.isBlank() -> "Selecciona una ciudad"
                            vehiculoSeleccionado.isBlank() -> "Selecciona un tipo de vehiculo"
                            else -> null
                        }
                        if (errorCampo == null) {
                            viewModel.actualizarPerfil(
                                DeliveryProfileUpdateRequest(
                                    municipioOrigen = ciudadSeleccionada,
                                    tipoVehiculo = vehiculoSeleccionado
                                )
                            )
                        }
                    },
                    enabled = !estaCargando,
                    interactionSource = interactionSource,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeliveryBrand600,
                        contentColor = Color.White,
                        disabledContainerColor = DeliveryBrand600.copy(alpha = 0.45f),
                        disabledContentColor = Color.White.copy(alpha = 0.7f)
                    ),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .graphicsLayer { scaleX = scale; scaleY = scale }
                        .shadow(
                            elevation = if (!estaCargando) 24.dp else 0.dp,
                            shape = RoundedCornerShape(20.dp),
                            spotColor = DeliveryBrand600.copy(alpha = 0.4f),
                            ambientColor = DeliveryBrand600.copy(alpha = 0.2f)
                        )
                ) {
                    Text("Continuar", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, letterSpacing = 0.5.sp, color = Color.White)
                    Spacer(Modifier.width(12.dp))
                    if (estaCargando) {
                        CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.5.dp, color = Color.White)
                    } else {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                }

                Spacer(Modifier.height(24.dp))
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
