package source;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

public class Cliente
{
	private final static int TIMEOUT = 20000;
	private DatagramSocket socketEntrada;
	private DatagramSocket socketSalida;
	private int puerto;
	private InetAddress direccionDestino;
	private int puertoDeDestino;

	private byte[] buffer;
	private boolean firstTime;

	public Cliente(int puerto) throws IOException{
		//        socket = new DatagramSocket(puerto);
		this.puerto = puerto;
		puertoDeDestino = 4445;
		direccionDestino = InetAddress.getByName("localhost");
		firstTime = true;
	}

	public String sendEcho(String msg) throws IOException{
		buffer = new byte[Servidor.TAMANIOBUFFER];
		//        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, direccionDestino, puertoDeDestino);
		//        socket.send(packet);
		String received = "";
		if(firstTime)
		{

			enviarInformacion("nu".getBytes());
			String respuesta = new String(recibirInformacion());
			if(respuesta.equals("READ"))
			{
				enviarInformacion(msg.getBytes());
				received = new String(recibirInformacion());
			}
			else
			{
				System.err.println("El cliente recibio: " + respuesta);
				System.err.println("Respuesta del servidor mal");
			}
			firstTime = false;
		}
		else
		{
			enviarInformacion(msg.getBytes());
			received = new String(recibirInformacion());
		}
		//        enviarInformacion(msg);
		//        packet = new DatagramPacket(buf, buf.length);
		//        socket.receive(packet);
		//        String received = new String(
		//          packet.getData(), 0, packet.getLength());

		return received;
	}
	/**
	 * Método que recibe paquetes UDP según un Timeout predefinido y un buffer predefinido.
	 * @return byte[] si hay paquetes, null si no se recibe nada.
	 * @throws IOException
	 */
	private byte[] recibirInformacion()throws IOException
	{
		socketEntrada = new DatagramSocket(puerto);
		System.out.println("RECEPCION LADO CLIENTE, puerto: " + puerto);
		socketEntrada.setSoTimeout(TIMEOUT);
		byte[] respuesta = new byte[Servidor.TAMANIOBUFFER];
		boolean firstTime = true;
		try
		{
			while(true)
			{
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				socketEntrada.receive(packet);	
				String temp = new String(packet.getData(), 0, packet.getData().length);
				if(firstTime)
				{
					byte[] tempB = packet.getData();
					System.arraycopy(tempB, 0, respuesta, 0, tempB.length);
					firstTime = false;
				}
				else
				{
					byte[] tempA = new byte[respuesta.length];
					System.arraycopy(respuesta, 0, tempA, 0, respuesta.length);
					byte[] tempB = packet.getData();
					respuesta = new byte[tempA.length + tempB.length];
					System.arraycopy(tempA, 0, respuesta, 0, tempA.length);
					System.arraycopy(tempB, 0, respuesta, tempA.length, tempB.length);
				}
				puertoDeDestino = packet.getPort();
				direccionDestino = packet.getAddress();
			}
		}
		catch (IOException e)
		{
			socketEntrada.close();
			System.out.println("CLIENTE: El tiempo de lectura expiró");
		}
		//        byte[] respuesta = new byte[Servidor.TAMANIOBUFFER*listaDeBuffers.size()];
		//       for(int i = 0; i < listaDeBuffers.size(); i++)
		//       {
		//    	   for(int j = 0; j < Servidor.TAMANIOBUFFER; j++)
		//    	   {
		//    		   respuesta[(i*Servidor.TAMANIOBUFFER) + j] = listaDeBuffers.get(i)[j];
		//    	   }
		//       }
		//		System.out.println("Servidor recibio: " + masterAnswer);
		socketEntrada.close();
		System.out.println("ARREGLO DE BYTES RECIBIDO: "+ new String(respuesta));
		//        return respuesta;
		boolean control = false;
		if(respuesta.length == Servidor.TAMANIOBUFFER)
		{
			for(int i = 0; i < Servidor.TAMANIOBUFFER; i++)
			{
				if(respuesta[i] != 0)
				{
					control = true;
				}
			}
			if(control == false)
			{
				respuesta = null;
			}
		}
		if(respuesta != null)
		{
			int valorEncontrado = 0;
			boolean control2 = false;
			for(int i = 0; i < respuesta.length && respuesta != null; i++)
			{
				if(respuesta[i] == 4)
				{
					if(!control2)
					{
						valorEncontrado = i;
						control2 = true;
					}
				}
			}
			byte[] respuestaFinal = new byte[valorEncontrado];
			if(control2)
			{

				for(int i = 0; i < respuestaFinal.length; i++)
				{
					respuestaFinal[i] = respuesta[i];
				}
			}
			else
			{
				respuestaFinal = respuesta;
			}
			System.out.println("RECEPCIÓN FINAL LADO CLIENTE: " + new String(respuestaFinal));
			return respuestaFinal;
		}
		else
		{
			return respuesta;
		}	
	}

	private void enviarInformacion(byte[] buffer2) throws IOException
	{

		socketSalida = new DatagramSocket(puerto);
		System.out.println("ENVIO LADO Cliente:" + (new String(buffer2)) + ", puerto: " + puerto);
		//		byte[] buffer2 = informacion.getBytes();
		System.out.println("buffer.length: " + buffer2.length);
		if(buffer2.length > Servidor.TAMANIOBUFFER)
		{
			System.out.println("Envio multiple requerido");
			double doubleNumSubBuffers = ((double)buffer2.length)/((double)Servidor.TAMANIOBUFFER);
			int numSubBuffers = (int) doubleNumSubBuffers;
			doubleNumSubBuffers = doubleNumSubBuffers*10;
			if (doubleNumSubBuffers % 10 != 0)
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
					//					listaDeBuffers.get(i)[j] = buffer2[contadorPosicion];
					if(contadorPosicion < buffer2.length)
					{
						listaDeBuffers.get(i)[j] = buffer2[contadorPosicion];
					}
					else
					{
						listaDeBuffers.get(i)[j] = 4;
					}
					//					else
					//					{
					//						byte[] elementoTemporal = listaDeBuffers.get(i);
					//						listaDeBuffers.remove(i);
					//						byte[] arreglo = new byte[contadorPosicion-(i*Servidor.TAMANIOBUFFER)];
					//						for(int m = 0; m < arreglo.length; m++)
					//						{
					//							arreglo[m] = elementoTemporal[m];
					//						}
					//						listaDeBuffers.add(arreglo);
					//					}
				}

			}

			for(int k = 0; k < listaDeBuffers.size(); k++)
			{
				System.out.println("TAMAÑO DE LISTA: " + listaDeBuffers.get(k).length);
				DatagramPacket paquete = new DatagramPacket(listaDeBuffers.get(k), listaDeBuffers.get(k).length, direccionDestino, puertoDeDestino);
				socketSalida.send(paquete);
				String contenidoPaquete = new String(listaDeBuffers.get(k), 0, listaDeBuffers.get(k).length);
				System.out.println("Paquete " + k + " enviado");
				System.out.println(contenidoPaquete);
			}
			socketSalida.close();
		}
		else
		{
			//			socketSalida = new DatagramSocket(puerto);
			System.out.println("Envio sencillo");
			byte[] bufferTemporal = new byte[Servidor.TAMANIOBUFFER];
			for(int i = 0; i < bufferTemporal.length; i++)
			{
				if(i < buffer2.length)
				{
					bufferTemporal[i] = buffer2[i];
				}
				else
				{
					bufferTemporal[i] = 4;
				}
			}
			DatagramPacket paquete = new DatagramPacket(bufferTemporal, bufferTemporal.length, direccionDestino, puertoDeDestino);
			socketSalida.send(paquete);
			socketSalida.close();
		}
	}


	public void close() {
		//        socketEntrada.close();
		System.out.println("LOL");
	}
}
