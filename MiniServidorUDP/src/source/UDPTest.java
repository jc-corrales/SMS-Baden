package source;

import java.io.IOException;
import junit.framework.TestCase;

public class UDPTest extends TestCase{
	private Cliente client;
	private String respuesta = "Buenos dias, aqui el almirante Gregorio";
	 
//    @Before
    private void setup(){
        try
        {
        	new Servidor(respuesta).start();
            client = new Cliente();
        }
        catch(IOException e)
        {
        	System.err.println(e.getMessage());
        }
    }
 
//    @Test
    public void testWhenCanSendAndReceivePacket_thenCorrect() throws IOException{
    	try
    	{
    		setup();
            String echo = client.sendEcho("hello server");
            assertEquals("hello server", echo);
//            assertEquals(respuesta, echo);
//            System.out.println(echo);
            echo = client.sendEcho("server is workingd");
            assertFalse(echo.equals("hello server"));
//            assertEquals(respuesta, echo);
    	}
    	catch (IOException e)
    	{
    		System.err.println(e.getMessage());
    	}
    }
 
//    @After
    public void tearDown() throws IOException{
        client.sendEcho("ende");
        client.close();
    }

}
