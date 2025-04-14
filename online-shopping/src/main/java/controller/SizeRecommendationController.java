package controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import service.SizeLogicService;

@RestController
@RequestMapping("/api/size")
@RequiredArgsConstructor
public class SizeRecommendationController {

    private final SizeLogicService sizeLogicService;
    
    
    @GetMapping("/recommend")
    public ResponseEntity<String> recommendSize(@RequestParam int height, @RequestParam int weight, @RequestParam int age) {
        String size;

        if (height < 100 || weight < 15) {
            size = "XS";
        } else if (height < 120 || weight < 20) {
            size = "S";
        } else if (height < 140 || weight < 30) {
            size = "M";
        } else if (height < 160 || weight < 40) {
            size = "L";
        } else {
            size = "XL";
        }

        return ResponseEntity.ok(size);
    }
    
    @PostMapping("/size")
    public ResponseEntity<String> recommendSize(@RequestBody
    		Map<String, Object> data) {
        int age = (int) data.get("age");
        double height = Double.parseDouble(data.get("height").toString());
        double weight = Double.parseDouble(data.get("weight").toString());

        String size = recommendBasedOnLogic(age, height, weight);
        return ResponseEntity.ok(size);
    }

    private String recommendBasedOnLogic(int age, double height, double weight) {
        // Basic logic – you can replace with ML later
        if (age <= 2 || height < 85) return "XS";
        if (age <= 4 || height < 100) return "S";
        if (age <= 6 || height < 115) return "M";
        if (age <= 8 || height < 130) return "L";
        if (age <= 10 || height < 145) return "XL";
        return "XXL";
    }


}

