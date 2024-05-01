package ut.com.example.test.rest;

import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.mockito.Mockito;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import com.example.test.rest.RestPlugin;
import com.example.test.rest.RestPluginModel;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.GenericEntity;

public class RestPluginTest {

    @Before
    public void setup() {

    }

    @After
    public void tearDown() {

    }

    @Test
    public void messageIsValid() {
        RestPlugin resource = new RestPlugin();

        Response response = resource.getMessage();
        final RestPluginModel message = (RestPluginModel) response.getEntity();

        assertEquals("wrong message","Hello World",message.getMessage());
    }
}
