package service;

import java.util.UUID;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

	@Value("${razorpay.key_id}")
    private String razorpayKeyId;

    @Value("${razorpay.key_secret}")
    private String razorpayKeySecret;

    public String createOrder(double amount) throws RazorpayException {
        log.info("Initiating Razorpay order creation for amount: ₹{}", amount);

        try {
            RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

            JSONObject options = new JSONObject();
            options.put("amount", (int)(amount * 100)); // amount in paise
            options.put("currency", "INR");
            options.put("receipt", UUID.randomUUID().toString());
            options.put("payment_capture", true);

            log.debug("Order options: {}", options.toString());

            Order order = client.orders.create(options);
            log.info("Razorpay order created successfully. Order ID: {}", String.valueOf(order.get("id")));


            return order.toString();

        } catch (RazorpayException e) {
            log.error("Error while creating Razorpay order for amount ₹{}", amount, e);
            throw e; // Let the controller/service layer decide how to handle it
        }
    }
}
