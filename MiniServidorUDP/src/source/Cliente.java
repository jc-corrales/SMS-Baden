package source;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

public class Cliente
{
	public final static int TIMEOUT = 20000;
	private DatagramSocket socketEntrada;
	private DatagramSocket socketSalida;
	private int puerto = 4444;
    private InetAddress direccionDestino;
    private int puertoDeDestino = 4445;
 
    private byte[] buffer;
 
    public Cliente() throws IOException{
//        socket = new DatagramSocket(puerto);
        direccionDestino = InetAddress.getByName("localhost");
    }
 
    public String sendEcho(String msg) throws IOException{
        buffer = new byte[Servidor.TAMANIOBUFFER];
//        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, direccionDestino, puertoDeDestino);
//        socket.send(packet);
        enviarInformacion(msg);
//        packet = new DatagramPacket(buf, buf.length);
//        socket.receive(packet);
//        String received = new String(
//          packet.getData(), 0, packet.getLength());
        String received = recibirInformacion();
        return received;
    }
 
    private String recibirInformacion()throws IOException
    {
    	
    	socketEntrada = new DatagramSocket(puerto);
    	System.out.println("RECEPCION LADO CLIENTE");
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
        	System.out.println("CLIENTE: El tiempo de lectura expiró");
        }
       
        System.out.println("Cliente recibio: " + masterAnswer);
        socketEntrada.close();
        return masterAnswer;
    }
    
    private void enviarInformacion(String informacion) throws IOException
	{
    	
    	socketSalida = new DatagramSocket(puerto);
//    	System.out.println("PRE");
//    	DatagramSocket socketSalida2 = new DatagramSocket(puerto);
//    	System.out.println("POST");
//    	socketSalida2.close();
    	System.out.println("ENVIO LADO CLIENTE:" + informacion);
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
    
    
    public void close() {
//        socketEntrada.close();
    	System.out.println("LOL");
    }
}
