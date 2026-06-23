package com.proyecto.AccesoUsuarios;

import java.sql.*;

/**
 * Limpia la base de datos Aiven dejando solo los 4 usuarios base
 */
public class DatabaseCleaner {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://agroconecta-mysql-brayanebareno1304-47f1.a.aivencloud.com:28963/defaultdb"
                + "?sslMode=REQUIRED&connectTimeout=30000";
        String user = "avnadmin";
        String pass = System.getenv("AIVEN_PASSWORD") != null
                ? System.getenv("AIVEN_PASSWORD") : System.getProperty("AIVEN_PASSWORD", "");

        System.out.println("🔌 Conectando a Aiven MySQL...");
        try (Connection conn = DriverManager.getConnection(url, user, pass);
             Statement stmt = conn.createStatement()) {

            // 1. Contar antes
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM usuario");
            rs.next();
            int antes = rs.getInt(1);

            // 2. Borrar en orden (hijos primero, padres después)
            String[] queries = {
                "DELETE FROM detalle_orden WHERE orden_id IN (SELECT id FROM orden WHERE usuario_id NOT IN (1,2,3,4))",
                "DELETE FROM orden WHERE usuario_id NOT IN (1,2,3,4)",
                "DELETE FROM direccion WHERE usuario_id NOT IN (1,2,3,4)",
                "DELETE FROM resena WHERE usuario_id NOT IN (1,2,3,4)",
                "DELETE FROM favorito_producto WHERE cliente_id NOT IN (1,2,3,4) OR producto_id IN (SELECT id FROM producto WHERE usuario_id NOT IN (1,2,3,4))",
                "DELETE FROM favorito_campesino WHERE cliente_id NOT IN (1,2,3,4)",
                "DELETE FROM mensaje_soporte WHERE ticket_id IN (SELECT id FROM ticket_soporte WHERE usuario_id NOT IN (1,2,3,4))",
                "DELETE FROM ticket_soporte WHERE usuario_id NOT IN (1,2,3,4)",
                "DELETE FROM producto WHERE usuario_id NOT IN (1,2,3,4)",
                "DELETE FROM ruta WHERE id NOT IN (SELECT DISTINCT ruta_id FROM orden WHERE ruta_id IS NOT NULL)",
                "DELETE FROM notificacion WHERE usuario_id NOT IN (1,2,3,4)",
                "DELETE FROM contacto_horeca",
                "DELETE FROM usuario WHERE id NOT IN (1,2,3,4)",
            };

            int total = 0;
            for (String q : queries) {
                int affected = stmt.executeUpdate(q);
                total += affected;
                System.out.printf("   🗑  %d registros borrados: %s...%n", affected,
                        q.substring(0, Math.min(60, q.length())));
            }

            // 3. Verificar
            rs = stmt.executeQuery("SELECT COUNT(*) FROM usuario");
            rs.next();
            int despues = rs.getInt(1);

            System.out.println("\n═══════════════════════════════");
            System.out.println("  Usuarios antes:  " + antes);
            System.out.println("  Usuarios después: " + despues);
            System.out.println("  Registros borrados: " + total);
            System.out.println("═══════════════════════════════");

            // 4. Mostrar los que quedaron
            rs = stmt.executeQuery("SELECT id, email, rol, nombre_completo FROM usuario ORDER BY id");
            System.out.println("\n📋 Usuarios actuales:");
            while (rs.next()) {
                System.out.printf("   [%d] %s | %s | %s%n",
                        rs.getInt("id"), rs.getString("email"),
                        rs.getString("rol"), rs.getString("nombre_completo"));
            }
            conn.close();
        }
        System.out.println("\n✅ Base de datos limpia.");
    }
}
