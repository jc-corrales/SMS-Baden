package source;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

public class Conexion extends Thread
{

	private int puertoDeDestino;
	private InetAddress direccionDestino;
	private DatagramSocket socketEntrada;
	private DatagramSocket socketSalida;
	private byte[] buffer;
	private boolean running;

	private int puertoAsignado;

	public Conexion(int puertoAsignado, int puertoDestino, InetAddress direccion, int tamanioBuffer)
	{

		buffer = new byte[tamanioBuffer];
		direccionDestino = direccion;
		this.puertoAsignado = puertoAsignado;
		this.puertoDeDestino = puertoDestino;
		//		try
		//		{
		//			enviarInformacion("READY");
		//		}
		//		catch (IOException e)
		//		{
		//			System.err.println("Error terrible: " + e.getMessage());
		//		}
	}

	public void run() {
		running = true;
		System.out.println("Puerto " + puertoAsignado + " listo.");
		try
		{
			enviarInformacion("READ".getBytes());
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
				if (received.equals("ende")) {
					running = false;
					continue;
				}
				//                socketEntrada.send(packet);
				if(received != "")
				{
					enviarInformacion(received.getBytes());
				}
			}
		}
		catch(IOException e)
		{
			System.err.println(e.getMessage());
		}
		//        socketEntrada.close();
	}

	private void enviarInformacion(byte[] buffer2) throws IOException
	{

		socketSalida = new DatagramSocket(puertoAsignado);
		System.out.println("ENVIO LADO SERVIDOR:" + (new String(buffer2)) + ", puerto: " + puertoAsignado);
		//		byte[] buffer2 = informacion.getBytes();
		System.out.println("buffer.length: " + buffer2.length);
		if(buffer2.length > Servidor.TAMANIOBUFFER)
		{
			System.out.println("Envio multiple requerido");
			double doubleNumSubBuffers = buffer2.length/Servidor.TAMANIOBUFFER;
			int numSubBuffers = (int) doubleNumSubBuffers;
			if (doubleNumSubBuffers % 1 != 0)
			{
				numSubBuffers++;
			}

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
			//			socketSalida = new DatagramSocket(puertoAsignado);
			System.out.println("Envio sencillo");
			DatagramPacket paquete = new DatagramPacket(buffer2, buffer2.length, direccionDestino, puertoDeDestino);
			socketSalida.send(paquete);
			socketSalida.close();
		}
	}

	private String recibirInformacion()throws IOException
	{

		socketEntrada = new DatagramSocket(puertoAsignado);
		System.out.println("RECEPCION LADO SERVIDOR, puerto: " + puertoAsignado);
		ArrayList<DatagramPacket> listaDePaquetes = new ArrayList<DatagramPacket>(); 

		socketEntrada.setSoTimeout(Servidor.TIMEOUT);
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
		//        byte[] respuesta = new byte[Servidor.TAMANIOBUFFER*listaDeBuffers.size()];
		//       for(int i = 0; i < listaDeBuffers.size(); i++)
		//       {
		//    	   for(int j = 0; j < Servidor.TAMANIOBUFFER; j++)
		//    	   {
		//    		   respuesta[(i*Servidor.TAMANIOBUFFER) + j] = listaDeBuffers.get(i)[j];
		//    	   }
		//       }
		System.out.println("Servidor recibio: " + masterAnswer);
		socketEntrada.close();
		//        return respuesta;
		return masterAnswer;
	}


	public boolean getIsRunning()
	{
		return running;
	}

}
