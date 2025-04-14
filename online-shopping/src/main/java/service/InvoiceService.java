package service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.CartItem;
import model.Order;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {

    public byte[] generateInvoice(Order order) throws IOException {
    	log.info("Generating invoice for Order ID: {}", order.getId());
    	
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();
        try {
        PdfWriter.getInstance(document, out);
        document.open();
        
        log.debug("Adding invoice header and order details...");
        document.add(new Paragraph("BOYS CLOTHING - ORDER INVOICE"));
        document.add(new Paragraph("Order ID: " + order.getId()));
        document.add(new Paragraph("User Email: " + order.getUser().getEmail()));
        document.add(new Paragraph("Status: " + order.getStatus()));
        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(3);
        table.addCell("Product");
        table.addCell("Qty");
        table.addCell("Price");
        
        log.debug("Adding cart items to the invoice...");
        for (CartItem item : order.getItems()) {
            table.addCell(item.getProduct().getName());
            table.addCell(String.valueOf(item.getQuantity()));
            table.addCell("₹" + item.getProduct().getPrice());
        }

        document.add(table);
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Total Amount: ₹" + order.getTotalAmount()));
        log.info("Invoice generation completed for Order ID: {}", order.getId());
        document.close();

        } catch (Exception e) {
            log.error("Error while generating invoice for Order ID: {}", order.getId(), e);
            throw new IOException("Failed to generate invoice", e);
        } finally {
            document.close();
            out.close();
        }
        return out.toByteArray();
    }
}


