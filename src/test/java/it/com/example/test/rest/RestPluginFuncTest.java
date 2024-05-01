package it.com.example.test.rest;

import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.mockito.Mockito;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import com.example.test.rest.RestPlugin;
import com.example.test.rest.RestPluginModel;
import org.apache.wink.client.Resource;
import org.apache.wink.client.RestClient;

public class RestPluginFuncTest {

    @Before
    public void setup() {

    }

    @After
    public void tearDown() {

    }

    @Test
    public void messageIsValid() {

        String baseUrl = System.getProperty("baseurl");
        String resourceUrl = baseUrl + "/rest/restplugin/1.0/message";

        RestClient client = new RestClient();
        Resource resource = client.resource(resourceUrl);

        RestPluginModel message = resource.get(RestPluginModel.class);

        assertEquals("wrong message","Hello World",message.getMessage());
    }
}
