import java.io.*;
import java.net.*;
import java.sql.Timestamp;
import java.util.ArrayList;



public class Main {

	public final static int TAMANIOBUFFER = 1024;
	private final static int TIMEOUTLECTURA = 20000;
	private DatagramSocket socketEntrada;
	private DatagramSocket socketSalida;
	private int puerto;
	private InetAddress direccionDestino;
	private int puertoDeDestino;

	private byte[] buffer;
	private boolean firstTime;
	//	public final static String IP = "157.253.218.40";
	public final static String IP = "40.76.10.131";
	public final static String DESCARGA = "DESCARGA";
	public final static String MULTIPLE = "MULTIPLE";
	public final static String HOLA = "HOLA";
	public final static int PUERTO = 11000;
	public boolean estado;
	public String[] archivos;
	public int numero;
	public boolean multiple;
	public long tiempo;
	public File file;
	public int paquetesRecibidos;

	public Main()
	{
		estado = false;
		multiple = false;
		archivos= new String[1];
	}

	public void establecerConexion() throws IOException
	{
		direccionDestino = InetAddress.getByName("localhost");
		puertoDeDestino = PUERTO;
		buffer = new byte[1024];
		enviarInformacion(HOLA.getBytes());
		String linea = new String(recibirInformacion());
		if(linea.startsWith(HOLA))
		{
			estado = true;
			numero = Integer.parseInt(linea.split(":")[1]);
		}
		linea = recibirInformacion().toString();
		if(linea.startsWith("ESTADO"))
		{
			String aux = linea.split(":")[1];
			if(aux.equals("MULTPIPLE"))
			{
				multiple = true;
				seguimientoMultiple();
			}
			else
			{
				linea = recibirInformacion().toString();
				if(linea.startsWith("ARCHIVOS"))
				{
					archivos = linea.split(":")[1].split(";");
				}
			}
		}
	}

	public void seguimientoMultiple() throws IOException {
		boolean estadoExito = false;

		//Recibir nombrearch
		String archivo = recibirInformacion().toString();
		
		//Recibir tam
		String tamnioFile = recibirInformacion().toString();

		//Recibir hash
		String hash = recibirInformacion().toString();

		//No hacer nada con eso por ahora
		long inic = System.currentTimeMillis();

		//descargar arch
		obtenerArchivo(archivo);

		//No hacer nada con eso por ahora
		tiempo = System.currentTimeMillis() - inic;

		if(file.hashCode()== Integer.parseInt(hash))
		{
			estadoExito = true;
		}

		//Log
		Long puntoDeInicio = System.currentTimeMillis();
		Timestamp timestamp = new Timestamp(puntoDeInicio);
		InetAddress inetAddress = InetAddress.getLocalHost();
		EscritorDeLog escritor = new EscritorDeLog(numero, timestamp, archivo, Double.parseDouble(tamnioFile), inetAddress.getHostAddress(), estadoExito, tiempo,paquetesRecibidos, file.length());
		escritor.imprimirLog();
	}

	public void inicioSimple(String archivo) throws IOException 
	{
		boolean estadoExito = false;
		
		String lol = DESCARGA + ":" +  archivo;
		enviarInformacion(lol.getBytes());
		
		//Recibir tam
		String tamnioFile = recibirInformacion().toString();

		System.out.println(tamnioFile);
		//Recibir hash
		String hash = recibirInformacion().toString();

		System.out.println(hash);
		//No hacer nada con eso por ahora
		long inic = System.currentTimeMillis();

		//descargar arch
		obtenerArchivo(archivo);

		//No hacer nada con eso por ahora
		tiempo = System.currentTimeMillis() - inic;

		if(file.hashCode()== Integer.parseInt(hash))
		{
			estadoExito = true;
		}

		//Log
		Long puntoDeInicio = System.currentTimeMillis();
		Timestamp timestamp = new Timestamp(puntoDeInicio);
		InetAddress inetAddress = InetAddress.getLocalHost();
		EscritorDeLog escritor = new EscritorDeLog(numero, timestamp, archivo, Double.parseDouble(tamnioFile), inetAddress.getHostAddress(), estadoExito, tiempo,paquetesRecibidos, file.length());
		escritor.imprimirLog();

	} 

	private void obtenerArchivo(String archivo) throws IOException 
	{
		int c = 0;
		FileOutputStream fos = new FileOutputStream("./data/"+archivo);
		BufferedOutputStream out = new BufferedOutputStream(fos);
		byte[] buffer = new byte[1024];
		int count;

		while ((count = recibirInformacion().length) >= 0)
		{
			fos.write(buffer, 0, count);
			c++;
		}
		paquetesRecibidos = c;
		out.close();
		file = new File("./data/"+archivo);
	}

	public void inicioMultiple(String archivo, String numeroClientes) throws IOException 
	{
		boolean estadoExito = true;
		String lol = MULTIPLE +  ":" +  numeroClientes + ":" +  archivo;
		enviarInformacion(lol.getBytes());

		//Recibir nombrearch
		recibirInformacion().toString();
				
		//Recibir tam
		String tamnioFile = recibirInformacion().toString();

		//Recibir hash
		String hash = recibirInformacion().toString();

		//No hacer nada con eso por ahora
		long inic = System.currentTimeMillis();

		//descargar arch
		obtenerArchivo(archivo);

		//No hacer nada con eso por ahora
		tiempo = System.currentTimeMillis() - inic;

		if(file.hashCode()== Integer.parseInt(hash))
		{
			estadoExito = true;
		}

		//Log
		Long puntoDeInicio = System.currentTimeMillis();
		Timestamp timestamp = new Timestamp(puntoDeInicio);
		InetAddress inetAddress = InetAddress.getLocalHost();
		EscritorDeLog escritor = new EscritorDeLog(numero, timestamp, archivo, Double.parseDouble(tamnioFile), inetAddress.getHostAddress(), estadoExito, tiempo,paquetesRecibidos, file.length());
		escritor.imprimirLog();


	}
	
	private void enviarInformacion(byte[] buffer2) throws IOException
	{

		socketSalida = new DatagramSocket(puerto);
		System.out.println("ENVIO LADO SERVIDOR:" + (new String(buffer2)) + ", puerto: " + puerto);
		//			byte[] buffer2 = informacion.getBytes();
		System.out.println("buffer.length: " + buffer2.length);
		if(buffer2.length > TAMANIOBUFFER)
		{
			System.out.println("Envio multiple requerido");
			double doubleNumSubBuffers = ((double)buffer2.length)/((double)TAMANIOBUFFER);
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
				//					byte[] temp = listaDeBuffers.get(i);
				listaDeBuffers.add(new byte[TAMANIOBUFFER]);

				for(int j = 0; j < TAMANIOBUFFER; j++)
				{
					int contadorPosicion = j + (i*TAMANIOBUFFER);
					//						listaDeBuffers.get(i)[j] = buffer2[contadorPosicion];
					if(contadorPosicion < buffer2.length)
					{
						listaDeBuffers.get(i)[j] = buffer2[contadorPosicion];
					}
					else
					{
						listaDeBuffers.get(i)[j] = 4;
					}
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
			System.out.println("Envio sencillo");
			byte[] bufferTemporal = new byte[TAMANIOBUFFER];
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
	/**
	 * Método que recibe paquetes UDP según un Timeout predefinido y un buffer predefinido.
	 * @return byte[] si hay paquetes, null si no se recibe nada.
	 * @throws IOException
	 */
	private byte[] recibirInformacion()throws IOException
	{
		socketEntrada = new DatagramSocket(puerto);
		System.out.println("RECEPCION LADO SERVIDOR, puerto: " + puerto);
		socketEntrada.setSoTimeout(TIMEOUTLECTURA);
		byte[] respuesta = new byte[TAMANIOBUFFER];
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
			System.out.println("SERVIDOR: El tiempo de lectura expiró");
		}
		socketEntrada.close();
		System.out.println("ARREGLO DE BYTES RECIBIDO: "+ new String(respuesta));
		//        return respuesta;
		boolean control = false;
		if(respuesta.length == TAMANIOBUFFER)
		{
			for(int i = 0; i < TAMANIOBUFFER; i++)
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
			System.out.println("RECEPCIÓN FINAL LADO SERVIDOR: " + new String(respuestaFinal));
			return respuestaFinal;
			
		}
		else
		{
			return respuesta;
		}

	}

}
