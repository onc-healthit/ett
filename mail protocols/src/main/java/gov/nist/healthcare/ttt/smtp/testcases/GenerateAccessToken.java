package gov.nist.healthcare.ttt.smtp.testcases;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

public class GenerateAccessToken {
	public static Logger log = LogManager.getLogger("GenerateAccessToken");

    public String getAccessToken() {
        String accessToken = null;
        try {
            Properties prop = new Properties();
            String path = "./application.properties";
            FileInputStream file = new FileInputStream(path);
            prop.load(file);
            file.close();

            String tokenEndpoint = prop.getProperty("jwt.accesstoken.endpoint");
            String clientId = prop.getProperty("jwt.accesstoken.client");
            String clientSecret = prop.getProperty("jwt.accesstoken.secret");
            log.info(" mail protocols tokenEndpoint :::::" + tokenEndpoint);
    		CloseableHttpClient httpClient = HttpClients.createDefault();
    		HttpPost httpPost = new HttpPost(tokenEndpoint);

    		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
    		urlParameters.add(new BasicNameValuePair("grant_type", "client_credentials"));
    		urlParameters.add(new BasicNameValuePair("client_secret", clientSecret));
    		urlParameters.add(new BasicNameValuePair("client_id", clientId));

    		HttpEntity postParams = new UrlEncodedFormEntity(urlParameters);
    		httpPost.setEntity(postParams);

    		CloseableHttpResponse httpResponse = httpClient.execute(httpPost);

    		log.info("POST Response Status:: "
    				+ httpResponse.getStatusLine().getStatusCode());

    		// print result
    		httpClient.close();

            JSONObject jsonObj =  new JSONObject(Objects.requireNonNull(httpResponse.toString()));
            accessToken = jsonObj.getString("access_token");
            log.info(" accessToken retrieved sucessfully ::::::");
        }catch(Exception clienterror) {
        	log.error("mail protocols HttpClientErrorException  :::::::"+clienterror.getMessage());
            clienterror.printStackTrace();
        }
        return accessToken;

    }
}