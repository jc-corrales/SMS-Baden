package source;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.sql.Timestamp;
import java.util.ArrayList;

public class Servidor extends Thread
{
	public final static int TAMANIOBUFFER = 2;
	public final static int TIMEOUT = 10000;
	private int puerto = 4445;
	private DatagramSocket socketEntrada;
	private DatagramSocket socketSalida;
    private boolean running;
    private byte[] buffer = new byte[TAMANIOBUFFER];
    private int puertoDeDestino;
    private InetAddress direccionDestino;
    private String respuesta;
 
    public Servidor(String respuesta) throws IOException {
//        socketEntrada = new DatagramSocket(puerto);
        this.respuesta = respuesta;
    }
 
    public void run() {
        running = true;
 
        try
        {
        	while (running) {
        		String received= "";
                try
                {
                	received = recibirInformacion();
                }
                catch(IOException e)
                {
                	System.err.println("ERROR DE MIERDA QUE ME HA ESTADO JODIENDO LA GRAN P*** VIDA");
                	continue;
                }
                System.out.println("MIREN, RESPUESTA: " + received); 
                if (received.equals("ende")) {
                    running = false;
                    continue;
                }
//                socketEntrada.send(packet);
                if(received != "")
                {
                	enviarInformacion(received);
                }
            }
        }
        catch(IOException e)
        {
        	System.err.println(e.getMessage());
        }
        socketEntrada.close();
    }
    
    
    private void enviarInformacion(String informacion) throws IOException
	{
    	
    	socketSalida = new DatagramSocket(puerto);
    	System.out.println("ENVIO LADO SERVIDOR:" + informacion);
		byte[] buffer2 = informacion.getBytes();
		System.out.println("buffer.length: " + buffer2.length);
		if(buffer2.length > Servidor.TAMANIOBUFFER)
		{
			System.out.println("Envio multiple requerido");
			int numSubBuffers = (int) Math.ceil(buffer2.length/Servidor.TAMANIOBUFFER);
			ArrayList<byte[]> listaDeBuffers = new ArrayList<byte[]>();
			System.out.println("Sub Buffers requeridos: " + numSubBuffers);
			for(int i = 0; i < numSubBuffers; i++)
			{
//				byte[] temp = listaDeBuffers.get(i);
				listaDeBuffers.add(new byte[Servidor.TAMANIOBUFFER]);

				for(int j = 0; j < Servidor.TAMANIOBUFFER; j++)
				{
					int contadorPosicion = j + (i*Servidor.TAMANIOBUFFER);
					listaDeBuffers.get(i)[j] = buffer2[contadorPosicion];
				}
				System.out.println("TAMAÑO DE LISTA: " + listaDeBuffers.get(i).length);
				DatagramPacket paquete = new DatagramPacket(listaDeBuffers.get(i), listaDeBuffers.get(i).length, direccionDestino, puertoDeDestino);
				socketSalida.send(paquete);
				String contenidoPaquete = new String(listaDeBuffers.get(i), 0, listaDeBuffers.get(i).length);
				System.out.println("Paquete " + i + " enviado");
				System.out.println(contenidoPaquete);
			}
			socketSalida.close();
		}
		else
		{
			socketSalida = new DatagramSocket(puerto);
			System.out.println("Envio sencillo");
			DatagramPacket paquete = new DatagramPacket(buffer2, buffer2.length, direccionDestino, puertoDeDestino);
			socketSalida.send(paquete);
			socketSalida.close();
		}
	}
    
    private String recibirInformacion()throws IOException
    {
    	
    	socketEntrada = new DatagramSocket(puerto);
    	System.out.println("RECEPCION LADO SERVIDOR");
    	ArrayList<DatagramPacket> listaDePaquetes = new ArrayList<DatagramPacket>(); 

        socketEntrada.setSoTimeout(TIMEOUT);
        ArrayList<byte[]> listaDeBuffers = new ArrayList<byte[]>();
        String masterAnswer = "";
        try
        {
        	while(true)
            {
        		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            	socketEntrada.receive(packet);
            	listaDePaquetes.add(packet);
            	listaDeBuffers.add(packet.getData());
            	String temp = new String(packet.getData(), 0, packet.getData().length);
            	masterAnswer += temp;
            	puertoDeDestino = packet.getPort();
            	direccionDestino = packet.getAddress();
            }
        }
        catch (IOException e)
        {
        	socketEntrada.close();
        	System.out.println("SERVIDOR: El tiempo de lectura expiró");
        }
       
        System.out.println("Servidor recibio: " + masterAnswer);
        socketEntrada.close();
        return masterAnswer;
    }
}
