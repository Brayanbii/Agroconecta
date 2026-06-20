package com.proyecto.AccesoUsuarios.controller;

import com.proyecto.AccesoUsuarios.model.Usuario;
import com.proyecto.AccesoUsuarios.repository.UsuarioRepository;
import com.proyecto.AccesoUsuarios.service.AuthUsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.proyecto.AccesoUsuarios.service.UploadFileService;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/delivery")
public class DeliveryController {

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private AuthUsuarioService authUsuarioService;

    @Autowired
    private UploadFileService uploadService;

    @PutMapping("/perfil")
    public ResponseEntity<Map<String, Object>> actualizarPerfil(
            Authentication auth,
            @RequestBody Map<String, Object> body) {

        Usuario usuario = authUsuarioService.getAuthenticatedUser(auth);
        if (usuario == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "No autenticado"));
        }
        if (!"REPARTIDOR".equals(usuario.getRol())) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Solo repartidores"));
        }

        String strVal;
        Double dblVal;
        Integer intVal;
        java.time.LocalDate dateVal;

        strVal = getNonBlank(body, "municipioOrigen");
        if (strVal != null) {
            usuario.setMunicipioOrigen(strVal);
            if (!hasNonBlank(body, "latitud") && !hasNonBlank(body, "longitud")) {
                double[] coords = obtenerCoordenadasCiudad(strVal);
                usuario.setLatitud(coords[0]);
                usuario.setLongitud(coords[1]);
            }
        }
        strVal = getNonBlank(body, "tipoVehiculo"); if (strVal != null) usuario.setTipoVehiculo(strVal);
        strVal = getNonBlank(body, "placaVehiculo"); if (strVal != null) usuario.setPlacaVehiculo(strVal);
        strVal = getNonBlank(body, "marcaVehiculo"); if (strVal != null) usuario.setMarcaVehiculo(strVal);
        strVal = getNonBlank(body, "modeloVehiculo"); if (strVal != null) usuario.setModeloVehiculo(strVal);
        intVal = getPositiveInt(body, "anioVehiculo"); if (intVal != null) usuario.setAnioVehiculo(intVal);
        dblVal = getPositiveDouble(body, "capacidadCargaKg"); if (dblVal != null) usuario.setCapacidadCargaKg(dblVal);
        strVal = getNonBlank(body, "licenciaConduccion"); if (strVal != null) usuario.setLicenciaConduccion(strVal);
        strVal = getNonBlank(body, "colorVehiculo"); if (strVal != null) usuario.setColorVehiculo(strVal);
        strVal = getNonBlank(body, "numeroIdentidad"); if (strVal != null) usuario.setNumeroIdentidad(strVal);
        dblVal = getNonBlank(body, "latitud") != null ? parseDoubleSafe(body.get("latitud")) : null; if (dblVal != null) usuario.setLatitud(dblVal);
        dblVal = getNonBlank(body, "longitud") != null ? parseDoubleSafe(body.get("longitud")) : null; if (dblVal != null) usuario.setLongitud(dblVal);
        dateVal = getNonBlankLocalDate(body, "fechaNacimiento"); if (dateVal != null) usuario.setFechaNacimiento(dateVal);

        usuarioRepo.save(usuario);
        return ResponseEntity.ok(Map.of("success", true, "message", "Perfil actualizado"));
    }

    @PostMapping("/documento")
    public ResponseEntity<Map<String, Object>> subirDocumento(
            Authentication auth,
            @RequestParam("tipo") String tipo,
            @RequestParam("archivo") MultipartFile archivo) {

        Usuario usuario = authUsuarioService.getAuthenticatedUser(auth);
        if (usuario == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "No autenticado"));
        }

        try {
            String fileId = uploadService.saveImage(archivo);

            switch (tipo) {
                case "licencia_frontal": usuario.setFotoLicenciaFrontalUrl(fileId); break;
                case "licencia_trasera": usuario.setFotoLicenciaTraseraUrl(fileId); break;
                case "tarjeta_propiedad": usuario.setFotoTarjetaPropiedadUrl(fileId); break;
                case "soat": usuario.setFotoSOATUrl(fileId); break;
                case "tecnomecanica": usuario.setFotoTecnomecanicaUrl(fileId); break;
                case "perfil": usuario.setFotoPerfil(fileId); break;
                case "cedula": usuario.setFotoCedulaUrl(fileId); break;
                default:
                    return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Tipo de documento no valido"));
            }

            usuarioRepo.save(usuario);
            return ResponseEntity.ok(Map.of("success", true, "url", fileId, "message", "Documento subido"));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Error al subir: " + e.getMessage()));
        }
    }

    @PostMapping("/enviar-verificacion")
    public ResponseEntity<Map<String, Object>> enviarVerificacion(Authentication auth) {
        Usuario usuario = authUsuarioService.getAuthenticatedUser(auth);
        if (usuario == null) return ResponseEntity.status(401).body(Map.of("success", false, "message", "No autenticado"));
        usuario.setEstadoVerificacion("EN_REVISION");
        usuarioRepo.save(usuario);
        return ResponseEntity.ok(Map.of("success", true, "message", "Documentos enviados a revision"));
    }

    @GetMapping("/perfil")
    public ResponseEntity<Map<String, Object>> obtenerPerfil(Authentication auth) {
        Usuario usuario = authUsuarioService.getAuthenticatedUser(auth);
        if (usuario == null) return ResponseEntity.status(401).body(Map.of("success", false, "message", "No autenticado"));

        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("municipioOrigen", nvl(usuario.getMunicipioOrigen()));
        data.put("tipoVehiculo", nvl(usuario.getTipoVehiculo()));
        data.put("placaVehiculo", nvl(usuario.getPlacaVehiculo()));
        data.put("marcaVehiculo", nvl(usuario.getMarcaVehiculo()));
        data.put("modeloVehiculo", nvl(usuario.getModeloVehiculo()));
        data.put("anioVehiculo", usuario.getAnioVehiculo() != null ? usuario.getAnioVehiculo() : 0);
        data.put("capacidadCargaKg", usuario.getCapacidadCargaKg() != null ? usuario.getCapacidadCargaKg() : 0.0);
        data.put("licenciaConduccion", nvl(usuario.getLicenciaConduccion()));
        data.put("colorVehiculo", nvl(usuario.getColorVehiculo()));
        data.put("estadoVerificacion", nvl(usuario.getEstadoVerificacion()));
        data.put("fotoLicenciaFrontalUrl", nvl(usuario.getFotoLicenciaFrontalUrl()));
        data.put("fotoLicenciaTraseraUrl", nvl(usuario.getFotoLicenciaTraseraUrl()));
        data.put("fotoTarjetaPropiedadUrl", nvl(usuario.getFotoTarjetaPropiedadUrl()));
        data.put("fotoSOATUrl", nvl(usuario.getFotoSOATUrl()));
        data.put("fotoTecnomecanicaUrl", nvl(usuario.getFotoTecnomecanicaUrl()));
        data.put("fotoPerfil", nvl(usuario.getFotoPerfil()));
        data.put("fotoCedulaUrl", nvl(usuario.getFotoCedulaUrl()));
        data.put("numeroIdentidad", nvl(usuario.getNumeroIdentidad()));
        data.put("fechaNacimiento", usuario.getFechaNacimiento() != null ? usuario.getFechaNacimiento().toString() : "");
        data.put("motivoRechazo", nvl(usuario.getMotivoRechazo()));
        return ResponseEntity.ok(data);
    }

    private String nvl(String val) { return val != null ? val : ""; }

    private String getNonBlank(Map<String, Object> body, String key) {
        Object val = body.get(key);
        if (val == null) return null;
        String s = val.toString().trim();
        return s.isEmpty() ? null : s;
    }

    private boolean hasNonBlank(Map<String, Object> body, String key) {
        return getNonBlank(body, key) != null;
    }

    private Integer getPositiveInt(Map<String, Object> body, String key) {
        Object val = body.get(key);
        if (val == null) return null;
        try {
            int i = Integer.parseInt(val.toString());
            return i > 0 ? i : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double getPositiveDouble(Map<String, Object> body, String key) {
        Object val = body.get(key);
        if (val == null) return null;
        try {
            double d = Double.parseDouble(val.toString());
            return d > 0.0 ? d : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double parseDoubleSafe(Object val) {
        if (val == null) return null;
        try {
            double d = Double.parseDouble(val.toString());
            return d != 0.0 ? d : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private java.time.LocalDate getNonBlankLocalDate(Map<String, Object> body, String key) {
        String s = getNonBlank(body, key);
        if (s == null) return null;
        try {
            return java.time.LocalDate.parse(s);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Coordenadas aproximadas de las principales ciudades colombianas.
     * Si la ciudad no esta en la lista, retorna coordenadas de Bogota como default.
     */
    private double[] obtenerCoordenadasCiudad(String nombre) {
        if (nombre == null) return new double[]{4.7110, -74.0721};
        String c = nombre.toLowerCase().trim();

        // Quitar parentesis y contenido extra (ej: "Barbosa (Santander)" -> "Barbosa")
        if (c.contains("(")) c = c.substring(0, c.indexOf("(")).trim();

        return switch (c) {
            case "bogota", "bogotá", "bogota d.c.", "bogotá d.c." -> new double[]{4.7110, -74.0721};
            case "medellin", "medellín" -> new double[]{6.2476, -75.5658};
            case "cali", "santiago de cali" -> new double[]{3.4516, -76.5320};
            case "barranquilla" -> new double[]{10.9685, -74.7813};
            case "cartagena", "cartagena de indias" -> new double[]{10.3910, -75.5144};
            case "bucaramanga" -> new double[]{7.1193, -73.1227};
            case "cucuta", "cúcuta" -> new double[]{7.8939, -72.5078};
            case "pereira" -> new double[]{4.8087, -75.6906};
            case "ibague", "ibagué" -> new double[]{4.4389, -75.2322};
            case "manizales" -> new double[]{5.0703, -75.5138};
            case "armenia" -> new double[]{4.5339, -75.6811};
            case "villavicencio" -> new double[]{4.1420, -73.6266};
            case "santa marta" -> new double[]{11.2408, -74.1990};
            case "pasto" -> new double[]{1.2089, -77.2780};
            case "neiva" -> new double[]{2.9273, -75.2819};
            case "monteria", "montería" -> new double[]{8.7480, -75.8814};
            case "valledupar" -> new double[]{10.4631, -73.2532};
            case "popayan", "popayán" -> new double[]{2.4448, -76.6147};
            case "sincelejo" -> new double[]{9.3047, -75.3978};
            case "tunja" -> new double[]{5.5446, -73.3578};
            case "riohacha" -> new double[]{11.5444, -72.9078};
            case "florencia" -> new double[]{1.6144, -75.6062};
            case "quibdo", "quibdó" -> new double[]{5.6947, -76.6611};
            case "yopal" -> new double[]{5.3378, -72.3959};
            case "mocoa" -> new double[]{1.1520, -76.6520};
            case "leticia" -> new double[]{-4.2157, -69.9414};
            case "inirida", "inírida" -> new double[]{3.8653, -67.9239};
            case "mitu", "mitú" -> new double[]{1.1983, -70.1736};
            case "san jose del guaviare" -> new double[]{2.5725, -72.6461};
            case "arauca" -> new double[]{7.0847, -70.7591};
            case "puerto carreño" -> new double[]{6.1845, -67.4883};

            // Santander
            case "barbosa" -> new double[]{5.9317, -73.6147};
            case "socorro" -> new double[]{6.4690, -73.2606};
            case "san gil" -> new double[]{6.5552, -73.1336};
            case "velez", "vélez" -> new double[]{6.0133, -73.6756};
            case "barrancabermeja" -> new double[]{7.0653, -73.8549};
            case "giron", "girón" -> new double[]{7.0708, -73.1706};
            case "floridablanca" -> new double[]{7.0647, -73.0867};
            case "piedecuesta" -> new double[]{6.9898, -73.0533};

            // Boyaca
            case "duitama" -> new double[]{5.8265, -73.0337};
            case "sogamoso" -> new double[]{5.7143, -72.9339};
            case "chiquinquira", "chiquinquirá" -> new double[]{5.6163, -73.8172};
            case "paipa" -> new double[]{5.7800, -73.1175};

            // Cundinamarca
            case "soacha" -> new double[]{4.5793, -74.2168};
            case "zipaquira", "zipaquirá" -> new double[]{5.0280, -74.0058};
            case "facatativa", "facatativá" -> new double[]{4.8137, -74.3554};
            case "chía" -> new double[]{4.8588, -74.0601};
            case "fusagasuga", "fusagasugá" -> new double[]{4.3372, -74.3644};
            case "girardot" -> new double[]{4.3038, -74.8030};

            // Antioquia
            case "bello" -> new double[]{6.3350, -75.5580};
            case "envigado" -> new double[]{6.1738, -75.5918};
            case "itagui", "itagüí" -> new double[]{6.1687, -75.6117};
            case "rionegro" -> new double[]{6.1552, -75.3746};
            case "apartado", "apartadó" -> new double[]{7.8830, -76.6347};
            case "turbo" -> new double[]{8.0940, -76.7280};

            // Valle
            case "palmira" -> new double[]{3.5370, -76.2981};
            case "buenaventura" -> new double[]{3.8889, -77.0730};
            case "tulua", "tuluá" -> new double[]{4.0847, -76.1986};
            case "cartago" -> new double[]{4.7482, -75.9127};
            case "buga" -> new double[]{3.9003, -76.3013};
            case "jamundi", "jamundí" -> new double[]{3.2600, -76.5400};

            // Atlantico
            case "soledad" -> new double[]{10.9185, -74.7636};
            case "malambo" -> new double[]{10.8577, -74.7746};
            case "baranoa" -> new double[]{10.7954, -74.9167};

            // Bolivar
            case "magangue", "magangué" -> new double[]{9.2410, -74.7530};
            case "mompox", "mompós" -> new double[]{9.2414, -74.4270};
            case "turbaco" -> new double[]{10.3314, -75.4138};

            // Magdalena
            case "cienaga", "ciénaga" -> new double[]{11.0070, -74.2485};
            case "fundacion", "fundación" -> new double[]{10.5197, -74.1866};

            // Norte de Santander
            case "ocaña" -> new double[]{8.2377, -73.3560};
            case "pamplona" -> new double[]{7.3761, -72.6484};

            // Huila
            case "pitalito" -> new double[]{1.8500, -76.0500};
            case "garzon", "garzón" -> new double[]{2.1960, -75.6276};
            case "la plata" -> new double[]{2.3920, -75.8920};

            // Narino
            case "ipiales" -> new double[]{0.8275, -77.6380};
            case "tumaco" -> new double[]{1.8067, -78.7683};

            // Quindio
            case "calarca", "calarcá" -> new double[]{4.5295, -75.6395};

            // Risaralda
            case "dosquebradas" -> new double[]{4.8410, -75.6770};
            case "santa rosa de cabal" -> new double[]{4.8680, -75.6210};

            // Tolima
            case "espinal" -> new double[]{4.1490, -74.8850};
            case "melgar" -> new double[]{4.2050, -74.6430};
            case "honda" -> new double[]{5.2040, -74.7400};

            // Meta
            case "acacias", "acacías" -> new double[]{3.9869, -73.7580};
            case "granada" -> new double[]{3.5440, -73.7070};

            // Caldas
            case "la dorada" -> new double[]{5.4550, -74.6646};
            case "chinchina", "chinchiná" -> new double[]{4.9860, -75.6030};

            default -> new double[]{4.7110, -74.0721}; // Bogota default
        };
    }
}
