package com.example.auction.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductValidationService {
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private static final String VALIDATION_API_URL = "http://localhost:5000";
    
    public ProductValidationService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Validate text content (product description)
     */
    public ValidationResult validateText(String auctionName, String productName, String productDescription) {
        try {
            String url = VALIDATION_API_URL + "/validate/text";
            
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("auction_name", auctionName != null ? auctionName : "");
            requestBody.put("product_name", productName != null ? productName : "");
            requestBody.put("product_description", productDescription != null ? productDescription : "");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            
            ValidationResult result = new ValidationResult();
            result.setValid(jsonNode.get("is_valid").asBoolean());
            result.setMessage(jsonNode.has("message") ? jsonNode.get("message").asText() : "");
            
            if (jsonNode.has("flagged_fields")) {
                JsonNode flaggedFields = jsonNode.get("flagged_fields");
                if (flaggedFields.isArray() && flaggedFields.size() > 0) {
                    result.setFlaggedField(flaggedFields.get(0).asText());
                }
            }
            
            return result;
            
        } catch (Exception e) {
            System.err.println("Error validating text: " + e.getMessage());
            // Return valid if validation service is down (fail-safe)
            ValidationResult result = new ValidationResult();
            result.setValid(true);
            result.setMessage("Validation service unavailable");
            return result;
        }
    }
    
    /**
     * Validate product images
     */
    public ValidationResult validateImages(List<String> imageUrls) {
        try {
            String url = VALIDATION_API_URL + "/validate/image";
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("image_urls", imageUrls);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            
            ValidationResult result = new ValidationResult();
            result.setValid(jsonNode.get("is_valid").asBoolean());
            
            if (!result.isValid() && jsonNode.has("detections")) {
                JsonNode detections = jsonNode.get("detections");
                if (detections.isArray() && detections.size() > 0) {
                    JsonNode firstDetection = detections.get(0);
                    if (firstDetection.has("objects") && firstDetection.get("objects").isArray()) {
                        JsonNode objects = firstDetection.get("objects");
                        if (objects.size() > 0) {
                            String detectedClass = objects.get(0).get("class").asText();
                            
                            // Map class names to Vietnamese
                            String vietnameseClassName = mapClassToVietnamese(detectedClass);
                            
                            result.setMessage("Phát hiện vật phẩm cấm '" + vietnameseClassName + "' trong hình ảnh");
                        }
                    }
                }
            }
            
            return result;
            
        } catch (Exception e) {
            System.err.println("Error validating images: " + e.getMessage());
            // Return valid if validation service is down (fail-safe)
            ValidationResult result = new ValidationResult();
            result.setValid(true);
            result.setMessage("Validation service unavailable");
            return result;
        }
    }
    
    /**
     * Validate complete product (text + images)
     */
    public ProductValidationResponse validateProduct(
            String auctionName, 
            String productName, 
            String productDescription,
            List<String> imageUrls) {
        
        ProductValidationResponse response = new ProductValidationResponse();
        
        // Validate text
        ValidationResult textResult = validateText(auctionName, productName, productDescription);
        response.setTextValid(textResult.isValid());
        response.setTextMessage(textResult.getMessage());
        response.setTextFlaggedField(textResult.getFlaggedField());
        
        // Validate images
        ValidationResult imageResult = validateImages(imageUrls);
        response.setImageValid(imageResult.isValid());
        response.setImageMessage(imageResult.getMessage());
        
        // Overall validation
        response.setValid(textResult.isValid() && imageResult.isValid());
        
        return response;
    }
    
    /**
     * Map English class names to Vietnamese
     */
    private String mapClassToVietnamese(String englishClass) {
        switch (englishClass.toLowerCase()) {
            case "knife":
                return "dao";
            case "pistol":
                return "súng lục";
            case "cigarette":
                return "thuốc lá";
            case "weed":
                return "cần sa";
            case "syringe":
                return "kim tiêm";
            default:
                return englishClass;
        }
    }
    
    // Inner classes for response
    public static class ValidationResult {
        private boolean valid;
        private String message;
        private String flaggedField;
        
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getFlaggedField() { return flaggedField; }
        public void setFlaggedField(String flaggedField) { this.flaggedField = flaggedField; }
    }
    
    public static class ProductValidationResponse {
        private boolean valid;
        private boolean textValid;
        private String textMessage;
        private String textFlaggedField;
        private boolean imageValid;
        private String imageMessage;
        
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        
        public boolean isTextValid() { return textValid; }
        public void setTextValid(boolean textValid) { this.textValid = textValid; }
        
        public String getTextMessage() { return textMessage; }
        public void setTextMessage(String textMessage) { this.textMessage = textMessage; }
        
        public String getTextFlaggedField() { return textFlaggedField; }
        public void setTextFlaggedField(String textFlaggedField) { this.textFlaggedField = textFlaggedField; }
        
        public boolean isImageValid() { return imageValid; }
        public void setImageValid(boolean imageValid) { this.imageValid = imageValid; }
        
        public String getImageMessage() { return imageMessage; }
        public void setImageMessage(String imageMessage) { this.imageMessage = imageMessage; }
    }
}
