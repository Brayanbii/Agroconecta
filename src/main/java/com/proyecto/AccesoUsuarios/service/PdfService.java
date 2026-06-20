package com.proyecto.AccesoUsuarios.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.proyecto.AccesoUsuarios.model.DetalleOrden;
import com.proyecto.AccesoUsuarios.model.Orden;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class PdfService {

    private static final Color TEAL = new Color(13, 148, 136);
    private static final Color TEAL_DARK = new Color(15, 118, 110);
    private static final Color TEAL_DEEP = new Color(19, 78, 74);
    private static final Color EMERALD = new Color(16, 185, 129);
    private static final Color AMBER = new Color(245, 158, 11);
    private static final Color SLATE_50 = new Color(248, 250, 252);
    private static final Color SLATE_100 = new Color(241, 245, 249);
    private static final Color SLATE_600 = new Color(71, 85, 105);
    private static final Color SLATE_800 = new Color(30, 41, 59);
    private static final Color WHITE = Color.WHITE;

    public void exportar(HttpServletResponse response, Orden orden) throws IOException {
        Document doc = new Document(PageSize.A4, 40, 40, 50, 50);
        PdfWriter writer = PdfWriter.getInstance(doc, response.getOutputStream());
        doc.open();

        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        nf.setMinimumFractionDigits(0); nf.setMaximumFractionDigits(0);

        // ═══════════════════════════════════════════
        //  HERO CARD — App Store Style
        // ═══════════════════════════════════════════
        PdfPTable heroCard = new PdfPTable(1);
        heroCard.setWidthPercentage(100f);
        PdfPCell heroCell = new PdfPCell();
        heroCell.setBackgroundColor(TEAL_DARK);
        heroCell.setPadding(32);
        heroCell.setBorder(Rectangle.NO_BORDER);
        heroCell.setBorderColor(TEAL);
        heroCell.setBorderWidth(0);
        heroCell.setUseVariableBorders(true);
        heroCell.setBorderWidthTop(0); heroCell.setBorderWidthBottom(0);
        heroCell.setBorderWidthLeft(0); heroCell.setBorderWidthRight(0);

        // Logo leaf icon
        Paragraph logoLine = new Paragraph();
        logoLine.setAlignment(Element.ALIGN_CENTER);
        logoLine.setSpacingAfter(10);
        Chunk leaf = new Chunk("🌿", FontFactory.getFont(FontFactory.HELVETICA, 28));
        logoLine.add(leaf);
        heroCell.addElement(logoLine);

        Paragraph brand = new Paragraph("AgroConecta", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 26, WHITE));
        brand.setAlignment(Element.ALIGN_CENTER);
        brand.setSpacingAfter(2);
        heroCell.addElement(brand);

        Paragraph tagline = new Paragraph("Del campo a tu mesa", FontFactory.getFont(FontFactory.HELVETICA, 10, new Color(153, 246, 228)));
        tagline.setAlignment(Element.ALIGN_CENTER);
        tagline.setSpacingAfter(14);
        heroCell.addElement(tagline);

        // Divider
        PdfPTable div = new PdfPTable(1);
        div.setWidthPercentage(40f);
        div.setHorizontalAlignment(Element.ALIGN_CENTER);
        PdfPCell divCell = new PdfPCell();
        divCell.setFixedHeight(1f);
        divCell.setBackgroundColor(new Color(255, 255, 255, 64));
        divCell.setBorder(Rectangle.NO_BORDER);
        div.addCell(divCell);
        heroCell.addElement(div);

        Paragraph receiptLabel = new Paragraph("RECIBO DE COMPRA", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, new Color(153, 246, 228)));
        receiptLabel.setAlignment(Element.ALIGN_CENTER);
        receiptLabel.setSpacingBefore(10);
        heroCell.addElement(receiptLabel);

        heroCard.addCell(heroCell);
        doc.add(heroCard);

        doc.add(spacer(16));

        // ═══════════════════════════════════════════
        //  ORDER DETAILS — Two column card
        // ═══════════════════════════════════════════
        PdfPTable detailsCard = createCard();
        PdfPCell detailsInner = new PdfPCell();
        detailsInner.setBorder(Rectangle.NO_BORDER);
        detailsInner.setPadding(20);

        PdfPTable detailsGrid = new PdfPTable(2);
        detailsGrid.setWidthPercentage(100f);
        detailsGrid.setWidths(new float[]{1f, 1f});

        String fecha = orden.getFechaCreacion() != null ?
            orden.getFechaCreacion().format(DateTimeFormatter.ofPattern("dd MMM yyyy '·' h:mm a")) : "—";

        detailItem(detailsGrid, "📦 Orden", orden.getNumeroOrden());
        detailItem(detailsGrid, "📅 Fecha", fecha);
        detailItem(detailsGrid, "📌 Estado", formatEstado(orden.getEstado()));
        detailItem(detailsGrid, "🚚 Envio", orden.getTipoEnvio() != null ? orden.getTipoEnvio() : "Estandar");
        detailItem(detailsGrid, "👤 Cliente", orden.getUsuario() != null ? orden.getUsuario().getNombreCompleto() : "—");
        detailItem(detailsGrid, "📍 Entrega", orden.getDireccionEnvio() != null ? orden.getDireccionEnvio() : "—");

        detailsInner.addElement(detailsGrid);
        detailsCard.addCell(detailsInner);
        doc.add(detailsCard);

        doc.add(spacer(12));

        // ═══════════════════════════════════════════
        //  PRODUCTS — Modern card table
        // ═══════════════════════════════════════════
        PdfPTable productsCard = createCard();
        PdfPCell productsInner = new PdfPCell();
        productsInner.setBorder(Rectangle.NO_BORDER);
        productsInner.setPadding(20);

        Paragraph productsTitle = new Paragraph("Productos", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, SLATE_800));
        productsTitle.setSpacingAfter(10);
        productsInner.addElement(productsTitle);

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100f);
        table.setWidths(new float[]{4f, 2.5f, 1.5f, 2.5f});

        // Header
        Font thFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, SLATE_600);
        String[] hdrs = {"PRODUCTO", "PRECIO UNIT.", "CANT.", "SUBTOTAL"};
        for (String h : hdrs) {
            PdfPCell th = new PdfPCell(new Phrase(h, thFont));
            th.setBorder(Rectangle.NO_BORDER);
            th.setBorderWidthBottom(1.5f);
            th.setBorderColor(new Color(226, 232, 240));
            th.setPadding(6);
            th.setPaddingBottom(4);
            table.addCell(th);
        }

        Font tdFont = FontFactory.getFont(FontFactory.HELVETICA, 9, SLATE_800);
        Font tdNum = FontFactory.getFont(FontFactory.HELVETICA, 9, SLATE_600);

        for (DetalleOrden d : orden.getDetalles()) {
            table.addCell(cell(d.getNombre() != null ? d.getNombre() : "—", tdFont, Element.ALIGN_LEFT));
            table.addCell(cell("$" + nf.format(val(d.getPrecio())), tdNum, Element.ALIGN_CENTER));
            table.addCell(cell(String.valueOf(valInt(d.getCantidad())), tdNum, Element.ALIGN_CENTER));
            table.addCell(cell("$" + nf.format(val(d.getTotal())), tdFont, Element.ALIGN_RIGHT));
        }

        productsInner.addElement(table);
        productsCard.addCell(productsInner);
        doc.add(productsCard);

        doc.add(spacer(12));

        // ═══════════════════════════════════════════
        //  TOTALS CARD
        // ═══════════════════════════════════════════
        PdfPTable totalsCard = createCard();
        PdfPCell totalsInner = new PdfPCell();
        totalsInner.setBorder(Rectangle.NO_BORDER);
        totalsInner.setPadding(20);

        double subtotal = val(orden.getSubtotalProductos());
        double envio = val(orden.getCostoEnvio());
        double tarifa = val(orden.getTarifaPlataforma());
        double pasarela = val(orden.getCostoPasarela());
        double total = val(orden.getTotal());

        addTotalLine(totalsInner, "Subtotal", "$" + nf.format(subtotal), false);
        if (envio > 0) addTotalLine(totalsInner, "Costo de envio", "$" + nf.format(envio), false);
        if (tarifa > 0) addTotalLine(totalsInner, "Tarifa plataforma", "$" + nf.format(tarifa), false);
        if (pasarela > 0) addTotalLine(totalsInner, "Costo pasarela", "$" + nf.format(pasarela), false);

        // Separator
        Paragraph sep = new Paragraph(" ");
        sep.setSpacingBefore(4);
        sep.setSpacingAfter(4);
        totalsInner.addElement(sep);
        PdfPTable sepLine = new PdfPTable(1);
        sepLine.setWidthPercentage(100f);
        PdfPCell sl = new PdfPCell();
        sl.setFixedHeight(1f);
        sl.setBackgroundColor(new Color(226, 232, 240));
        sl.setBorder(Rectangle.NO_BORDER);
        sepLine.addCell(sl);
        totalsInner.addElement(sepLine);

        addTotalLine(totalsInner, "Total pagado", "$" + nf.format(total), true);

        totalsCard.addCell(totalsInner);
        doc.add(totalsCard);

        // ═══════════════════════════════════════════
        //  FOOTER
        // ═══════════════════════════════════════════
        doc.add(spacer(20));
        Paragraph thanks = new Paragraph("Gracias por elegir AgroConecta   ·   Conectamos el campo colombiano con tu hogar",
            FontFactory.getFont(FontFactory.HELVETICA, 7, SLATE_600));
        thanks.setAlignment(Element.ALIGN_CENTER);
        doc.add(thanks);

        Paragraph legal = new Paragraph("Recibo digital valido · Conservalo para tus registros · agroconecta.com",
            FontFactory.getFont(FontFactory.HELVETICA, 6, new Color(148, 163, 184)));
        legal.setAlignment(Element.ALIGN_CENTER);
        legal.setSpacingBefore(4);
        doc.add(legal);

        doc.close();
    }

    // ── HELPERS ──

    private double val(Double d) { return d != null ? d : 0.0; }
    private int valInt(Integer i) { return i != null ? i : 0; }

    private PdfPTable createCard() {
        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(100f);
        PdfPCell outer = new PdfPCell();
        outer.setBackgroundColor(WHITE);
        outer.setPadding(0);
        outer.setBorder(Rectangle.BOX);
        outer.setBorderColor(new Color(226, 232, 240));
        outer.setBorderWidth(1f);
        t.addCell(outer);
        return t;
    }

    private Paragraph spacer(int pt) {
        Paragraph p = new Paragraph(" ");
        p.setSpacingBefore(pt / 2f);
        p.setSpacingAfter(pt / 2f);
        return p;
    }

    private void detailItem(PdfPTable table, String label, String value) {
        PdfPCell lc = new PdfPCell(new Phrase(label, FontFactory.getFont(FontFactory.HELVETICA, 9, SLATE_600)));
        lc.setBorder(Rectangle.NO_BORDER);
        lc.setPadding(3);
        table.addCell(lc);

        PdfPCell vc = new PdfPCell(new Phrase(value, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, SLATE_800)));
        vc.setBorder(Rectangle.NO_BORDER);
        vc.setPadding(3);
        table.addCell(vc);
    }

    private PdfPCell cell(String text, Font font, int align) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setBorder(Rectangle.NO_BORDER);
        c.setBorderWidthBottom(0.5f);
        c.setBorderColor(new Color(241, 245, 249));
        c.setPadding(7);
        c.setHorizontalAlignment(align);
        return c;
    }

    private void addTotalLine(PdfPCell container, String label, String value, boolean bold) {
        Paragraph p = new Paragraph();
        p.setSpacingBefore(3);
        p.setSpacingAfter(3);

        Chunk labelChunk = new Chunk(label + "  ", FontFactory.getFont(FontFactory.HELVETICA, bold ? 11 : 9,
            bold ? SLATE_800 : SLATE_600));
        p.add(labelChunk);

        float pageWidth = PageSize.A4.getWidth() - 80;
        float labelWidth = bold ? 100f : 90f;
        float dotsWidth = pageWidth - labelWidth - 100f;

        int dots = Math.max(1, (int)(dotsWidth / 3.2f));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < dots; i++) sb.append("·");
        Chunk dotsChunk = new Chunk(sb.toString(), FontFactory.getFont(FontFactory.HELVETICA, 6, new Color(203, 213, 225)));
        p.add(dotsChunk);

        Chunk valueChunk = new Chunk("  " + value, FontFactory.getFont(FontFactory.HELVETICA_BOLD, bold ? 16 : 10,
            bold ? TEAL_DARK : SLATE_800));
        p.add(valueChunk);
        p.setAlignment(Element.ALIGN_RIGHT);
        container.addElement(p);
    }

    private String formatEstado(String e) {
        if (e == null) return "—";
        return switch (e) {
            case "ENTREGADO" -> "✅ Entregado";
            case "EN_CAMINO", "RECOGIDO" -> "🚀 En camino";
            case "CANCELADO", "RECHAZADO" -> "❌ Cancelado";
            default -> "⏳ " + e;
        };
    }
}
