package gov.nist.healthcare.ttt.webapp.api;

import java.io.File;
import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class CCDAValidationTest {

	public static void main(String[] args) {
		CloseableHttpClient client = HttpClients.createDefault();
		File file = new File("src/main/resources/cda-samples/CCDA_Ambulatory.xml");
		HttpPost post = new HttpPost("http://devccda.sitenv.org/CCDAValidatorServices/r2.0/");
		FileBody fileBody = new FileBody(file);
		// 
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		builder.addPart("file", fileBody);
		HttpEntity entity = builder.build();
		//
		post.setEntity(entity);
		try {
			HttpResponse response = client.execute(post);
			// CONVERT RESPONSE TO STRING
            String result = EntityUtils.toString(response.getEntity());
			System.out.println(result);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
