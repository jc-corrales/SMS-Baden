package source;

import java.io.IOException;
import junit.framework.TestCase;

public class UDPTest extends TestCase{
	private Cliente client;
	private Cliente client2;
	private String respuesta = "Buenos dias, aqui el almirante Gregorio";
	 
//    @Before
    private void setup(){
        try
        {
        	new Servidor(respuesta).start();
            client = new Cliente(4444);
//            client2 = new Cliente(4443);
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
//            String echo2 = client2.sendEcho("PUTA MIERDA!");
//            assertEquals(echo2, "PUTA MIERDA!");
//            client2.sendEcho("ende");
//            client2.close();
//            assertEquals(respuesta, echo);
//            System.out.println(echo);
            echo = client.sendEcho("server is workingd");
            assertFalse(echo.equals("hello server"));
            System.out.println("ECHO FINAL: " + echo);
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
