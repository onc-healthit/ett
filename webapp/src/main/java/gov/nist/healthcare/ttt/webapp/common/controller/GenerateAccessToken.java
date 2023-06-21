package gov.nist.healthcare.ttt.webapp.common.controller;
import java.util.Objects;


import gov.nist.healthcare.ttt.webapp.common.model.keyclock.Response;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.InputStream;

public class GenerateAccessToken {
    private static final Logger logger = LoggerFactory.getLogger(GenerateAccessToken.class);

    @Value("${jwt.accesstoken.endpoint}")
    String tokenEndpoint;
    @Value("${jwt.accesstoken.client}")
    String clientId;
    @Value("${jwt.accesstoken.secret}")
    String clientSecret;

    public String getAccessToken() {
        String accessToken = null;
        try {
            RestTemplate resTemplate = new RestTemplate();
            Properties prop = new Properties();
            String path = "./application.properties";
            FileInputStream file = new FileInputStream(path);
            prop.load(file);
            file.close();

            tokenEndpoint = prop.getProperty("jwt.accesstoken.endpoint");
            clientId = prop.getProperty("jwt.accesstoken.client");
            clientSecret = prop.getProperty("jwt.accesstoken.secret");
            logger.info("tokenEndpoint :::::" + tokenEndpoint);


            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("grant_type", "client_credentials");
            map.add("client_secret", clientSecret);
            map.add("client_id", clientId);
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
            ResponseEntity<Response> response =  resTemplate.postForEntity(tokenEndpoint, request, Response.class);

            JSONObject jsonObj =  new JSONObject(Objects.requireNonNull(response.getBody()));
            accessToken = jsonObj.getString("access_token");
            logger.info("getAccessToken accessToken  status :::::::"+response.getStatusCode());

        }catch(HttpClientErrorException clienterror) {
            logger.error("HttpClientErrorException  :::::::"+clienterror.getMessage());
            clienterror.printStackTrace();
        }catch(Exception e) {
            e.printStackTrace();
        }
        return accessToken;

    }
}