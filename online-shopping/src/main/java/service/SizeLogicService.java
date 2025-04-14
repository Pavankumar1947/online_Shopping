package service;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SizeLogicService {

	public String recommendSize(int age, double heightCm, double weightKg) {
        log.info("Calculating size recommendation for Age: {}, Height: {} cm, Weight: {} kg", age, heightCm, weightKg);

        if (age <= 0 || heightCm <= 0 || weightKg <= 0) {
            log.warn("Invalid input values received - Age: {}, Height: {}, Weight: {}", age, heightCm, weightKg);
            return "Invalid Input";
        }

        String size;
        if (age <= 2) {
            size = "XS";
        } else if (age <= 4 && heightCm < 100 && weightKg < 16) {
            size = "S";
        } else if (age <= 6 && heightCm < 115 && weightKg < 20) {
            size = "M";
        } else if (age <= 8 && heightCm < 130 && weightKg < 25) {
            size = "L";
        } else if (age <= 12 && heightCm < 150 && weightKg < 35) {
            size = "XL";
        } else {
            size = "XXL";
        }

        log.info("Recommended size: {}", size);
        return size;
    }
}
