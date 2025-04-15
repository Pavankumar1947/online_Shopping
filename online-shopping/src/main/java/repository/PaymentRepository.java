package repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import model.Payment;

@Repository
public interface PaymentRepository extends CrudRepository<Payment, String>{
	Payment findByRazorpayPaymentId(String razorpayPaymentId);

}
