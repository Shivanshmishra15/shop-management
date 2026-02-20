package com.shopapp.utils;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.shopapp.models.CartItem;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PdfGenerator {

    public static void generateInvoice(String customerName, String contact, String gstNo, List<CartItem> items, double grandTotal, String filePath) throws DocumentException, IOException {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();

        // Header
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("INVOICE", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        
        document.add(new Paragraph(" ")); // Spacer

        // Shop Details (Hardcoded for demo)
        document.add(new Paragraph("Manglam Mart"));
        document.add(new Paragraph("Fulwariya , Gorakhpur, Uttar Pradesh"));
        document.add(new Paragraph("GST: 12ABCDE1234F1Z5"));
        document.add(new Paragraph("Date: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        
        document.add(new Paragraph(" "));
        
        // Customer Details
        document.add(new Paragraph("Customer: " + customerName));
        document.add(new Paragraph("Contact: " + contact));
        if (gstNo != null && !gstNo.isEmpty()) document.add(new Paragraph("GST No: " + gstNo));
        
        document.add(new Paragraph(" "));

        // Item Table
        PdfPTable table = new PdfPTable(6); // S.No, Desc, HSN, Qty, Rate, Total
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 4, 2, 1, 2, 2});

        addTableHeader(table, "S.No", "Description", "HSN", "Qty", "Rate", "Total");

        int count = 1;
        for (CartItem item : items) {
            table.addCell(String.valueOf(count++));
            table.addCell(item.getDescription());
            table.addCell(item.getHsn());
            table.addCell(String.valueOf(item.getQuantity()));
            table.addCell(String.format("%.2f", item.getRate()));
            table.addCell(String.format("%.2f", item.getTotal()));
        }

        document.add(table);
        
        document.add(new Paragraph(" "));
        
        // Totals
        Paragraph totalPara = new Paragraph("Grand Total: " + String.format("%.2f", grandTotal), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14));
        totalPara.setAlignment(Element.ALIGN_RIGHT);
        document.add(totalPara);
        
        document.close();
    }

    private static void addTableHeader(PdfPTable table, String... headers) {
        for (String header : headers) {
            PdfPCell cell = new PdfPCell();
            cell.setPhrase(new Phrase(header));
            cell.setBackgroundColor(java.awt.Color.LIGHT_GRAY); // AWT Color for OpenPDF
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
    }
}
