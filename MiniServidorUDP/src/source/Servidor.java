package source;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;

public class Servidor extends Thread
{
	public final static int TAMANIOBUFFER = 2;
	public final static int TIMEOUT = 10000;
	public final static int TIMEOUTMAESTRO = 100000;
	private int puertoPrincipal = 4445;
	private int puertoDeInicio = 5000;
	private DatagramSocket puntoDeEntrada;
    private boolean running;
    private byte[] buffer = new byte[TAMANIOBUFFER];
    

//    private String respuesta;
    private ArrayList <Conexion> conexiones;
 
    public Servidor(String respuesta) throws IOException {
//        socketEntrada = new DatagramSocket(puerto);
//        this.respuesta = respuesta;
        conexiones = new ArrayList<Conexion>();
        puntoDeEntrada = new DatagramSocket(puertoPrincipal);
    }
 
    public void run() {
        running = true;
        try {
			puntoDeEntrada.setSoTimeout(200000);
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
//        try
//        {
        	while (running) {
        		String received= "";
                try
                {
//                	recibirInformacion();
                	DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                	puntoDeEntrada.receive(packet);
                	asignacionDeSocket(packet);
                }
                catch(IOException e)
                {
                	System.err.println("ERROR DE MIERDA QUE ME HA ESTADO JODIENDO LA GRAN P*** VIDA");
                	running = false;
                	continue;
                }
//                System.out.println("MIREN, RESPUESTA: " + received); 
                if (received.equals("ende")) {
                    running = false;
                    continue;
                }
//                socketEntrada.send(packet);
//                if(received != "")
//                {
//                	enviarInformacion(received);
//                }
            }
//        }
//        catch(IOException e)
//        {
//        	System.err.println(e.getMessage());
//        }
//        socketEntrada.close();
    }
    
    private void recibirInformacion()throws IOException
    {
    	
//    	socketEntrada = new DatagramSocket(puertoPrincipal);
    	System.out.println("INICIO PROCESO DE NUEVO CLIENTE");
//    	ArrayList<DatagramPacket> listaDePaquetes = new ArrayList<DatagramPacket>(); 

//        socketEntrada.setSoTimeout(Servidor.TIMEOUT);
        ArrayList<byte[]> listaDeBuffers = new ArrayList<byte[]>();
//        String masterAnswer = "";
        try
        {
        	while(true)
            {
        		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            	puntoDeEntrada.receive(packet);
//            	listaDePaquetes.add(packet);
            	listaDeBuffers.add(packet.getData());
//            	String temp = new String(packet.getData(), 0, packet.getData().length);
//            	masterAnswer += temp;
//            	puertoDeDestino = packet.getPort();
//            	direccionDestino = packet.getAddress();
            	asignacionDeSocket(packet);
            }
        }
        catch (IOException e)
        {
        	System.out.println("SERVIDOR: El tiempo de lectura expiró");
        }
       
//        System.out.println("Servidor recibio: " + masterAnswer);
        
        
    }
    
    private void asignacionDeSocket(DatagramPacket paquete)
    {
    	int puertoDestino = paquete.getPort();
    	InetAddress direccion = paquete.getAddress();
    	if(conexiones.size() > 0)
    	{
    		for(int i = 0; i < conexiones.size(); i++)
        	{
        		if(conexiones.get(i).getIsRunning() == false)
        		{
        			System.out.println("EN TEORIA CREA NUEVA CONEXION");
        			Conexion nuevaConexion = new Conexion((puertoDeInicio + i), puertoDestino, direccion, TAMANIOBUFFER);
        			conexiones.set(i,nuevaConexion);
        			System.out.println("RUN EJECUTADO");
        			nuevaConexion.run();
        		}
        		else
        		{
        			Conexion nuevaConexion = new Conexion((puertoDeInicio + i + 1), puertoDestino, direccion, TAMANIOBUFFER);
        			conexiones.add(nuevaConexion);
        			nuevaConexion.run();
        		}
        	}
    	}
    	else
    	{
    		Conexion nuevaConexion = new Conexion((puertoDeInicio), puertoDestino, direccion, TAMANIOBUFFER);
			conexiones.add(nuevaConexion);
			nuevaConexion.run();
    	}
    }
    
}
