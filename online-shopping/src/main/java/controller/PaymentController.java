package controller;

import java.util.Map;

import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.razorpay.Utils;

import lombok.RequiredArgsConstructor;
import model.Payment;
import repository.PaymentRepository;
import service.PaymentService;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;

    @PostMapping("/create")
    public ResponseEntity<?> createPayment(@RequestBody Map<String, Double> payload) {
        try {
            double amount = payload.get("amount");
            String razorpayOrder = paymentService.createOrder(amount);
            return ResponseEntity.ok(razorpayOrder);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Payment order creation failed: " + e.getMessage());
        }
    }
    
    
    @PostMapping("/razorpay-webhook")
    public String handleWebhook(
        @RequestBody String payload,
        @RequestHeader("X-Razorpay-Signature") String signature) {

        try {
            String webhookSecret = "abc@1947";
            
            // Verify signature
            boolean isValid = Utils.verifyWebhookSignature(
                payload, signature, webhookSecret);

            if (isValid) {
                JSONObject data = new JSONObject(payload);
                String event = data.getString("event");

                if ("payment.captured".equals(event)) {
                    JSONObject payment = data.getJSONObject("payload").getJSONObject("payment");
                    // Save to database
                    paymentRepository.save(new Payment(
                        payment.getString("id"),
                        payment.getLong("amount") / 100, // Convert paise to INR
                        "INR",
                        "completed"
                    ));
                }
            }
        } catch (Exception e) {
            return "ERROR";
        }

        return "OK";
    }
}

