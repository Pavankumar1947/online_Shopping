package controller;

import java.io.IOException;
import java.security.Principal;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import model.Order;
import repository.OrderRepository;
import service.InvoiceService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderInvoiceController {

    private final OrderRepository orderRepo;
    private final InvoiceService invoiceService;

    @GetMapping("/{orderId}/invoice")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable Long orderId, Principal principal) throws IOException {
        Order order = orderRepo.findById(orderId).orElseThrow();
        if (!order.getUser().getEmail().equals(principal.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        byte[] pdf = invoiceService.generateInvoice(order);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice_" + orderId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}

