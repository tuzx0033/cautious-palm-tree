package com.example.demo.music.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class SupabaseStorageService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.anonKey}")
    private String anonKey;

    @Value("${supabase.bucket}")
    private String bucketName;

    public String uploadFile(MultipartFile file, String folder) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + anonKey);
        headers.set("Content-Type", file.getContentType());
    
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename().replace(" ", "_");
        HttpEntity<byte[]> requestEntity = new HttpEntity<>(file.getBytes(), headers);
    
        String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + folder + "/" + fileName;
        restTemplate.exchange(uploadUrl, HttpMethod.POST, requestEntity, String.class);
    
        return supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + folder + "/" + fileName;
    }

    public void deleteFile(String fileName, String folder) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + anonKey);

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        String deleteUrl = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + folder + "/" + fileName;

        restTemplate.exchange(deleteUrl, HttpMethod.DELETE, requestEntity, String.class);
    }
}