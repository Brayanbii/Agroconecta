package com.proyecto.AccesoUsuarios.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.proyecto.AccesoUsuarios.model.DetalleOrden;
import com.proyecto.AccesoUsuarios.model.Orden;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.IOException;

@Service
public class PdfService {

    public void exportar(HttpServletResponse response, Orden orden) throws IOException {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();

        // Título
        Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        fontTitle.setSize(18);
        fontTitle.setColor(new Color(34, 139, 34)); // Verde AgroConecta

        Paragraph paragraph = new Paragraph("Recibo de Compra - AgroConecta", fontTitle);
        paragraph.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(paragraph);

        // Datos de la Orden
        Font fontInfo = FontFactory.getFont(FontFactory.HELVETICA);
        fontInfo.setSize(12);

        document.add(new Paragraph("\n"));
        document.add(new Paragraph("Orden #: " + orden.getNumeroOrden(), fontInfo));
        document.add(new Paragraph("Fecha: " + orden.getFechaCreacion(), fontInfo));
        document.add(new Paragraph("Cliente: " + orden.getUsuario().getUserName(), fontInfo));
        document.add(new Paragraph("\n"));

        // Tabla de Productos
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100f);
        table.setWidths(new float[] { 3.0f, 2.0f, 2.0f, 2.0f });
        table.setSpacingBefore(10);

        // Encabezados
        writeTableHeader(table);

        // Datos
        writeTableData(table, orden);

        document.add(table);

        // Total
        Font fontTotal = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        fontTotal.setSize(14);
        Paragraph pTotal = new Paragraph("\nTotal Pagado: $" + orden.getTotal(), fontTotal);
        pTotal.setAlignment(Paragraph.ALIGN_RIGHT);
        document.add(pTotal);

        document.close();
    }

    private void writeTableHeader(PdfPTable table) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(new Color(34, 139, 34));
        cell.setPadding(5);

        Font font = FontFactory.getFont(FontFactory.HELVETICA);
        font.setColor(Color.WHITE);

        cell.setPhrase(new Phrase("Producto", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Precio Unit.", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Cantidad", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Total", font));
        table.addCell(cell);
    }

    private void writeTableData(PdfPTable table, Orden orden) {
        for (DetalleOrden detalle : orden.getDetalles()) {
            table.addCell(detalle.getNombre());
            table.addCell("$" + detalle.getPrecio());
            table.addCell(String.valueOf(detalle.getCantidad()));
            table.addCell("$" + detalle.getTotal());
        }
    }
}