package source;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.ArrayList;

/**
 * Modela una conexion entre 1 cliente y el servidor
 * @author ADMIN
 *
 */
public class Conexion extends Thread
{
//	private final static int TAMANIOBUFFER = 1024;
	private final static int TIMEOUT = 120000;
	private final static int TIMEOUTLECTURA = 10000;
	private final static String DESCARGA= "DESCARGA";
	private final static String MULTIPLE= "MULTIPLE";
	private final static String SALIR= "SALIR";

	private DatagramSocket socketEntrada;
	private DatagramSocket socketSalida;
	/**
	 * modela si ya se envio el nombre de arch
	 */
	private boolean enviadoNomArch;

//	/**
//	 * Atributo que representa el flujo de escritura para el usuario.
//	 */
//	private PrintWriter out;
//
//	/**
//	 * Atributo que represente el flujo de lectura del usuario.
//	 */	
//	private BufferedReader in;

	/**
	 * Atributo que determina el estado de la sesion, true si esta conectado, false si esta desconectado.
	 */
	private boolean estadoSesion;

	private String nomArchivos;

	/**
	 * Modela el servidor
	 */
	private Servidor servidor;
	/**
	 * Tiempo de inicio de la conexion.
	 */
	private long tiempoInicio;
	/**
	 * Tiempo de finalizacion de la conexion.
	 */
	private long tiempoFin;
	/**
	 * Fecha y hora de la conexion.
	 */
	private Timestamp timestamp;
	/**
	 * Modela la id de la conexion
	 */
	private int id;
	/**
	 * Atributo que contiene si el envio fue exitoso o no.
	 */
	private boolean estadoExito;
	/**
	 * Atributo que contiene el tamanio del archivo a enviar.
	 */
	private double tamanioArchivo;
	/**
	 * Direccion IP del Cliente.
	 */
	private String cliente;
	
	private long numeroDePaquetesEnviados;
	private String nomArchivoEnviado;
	private int puertoAsignado;
	private int puertoDeDestino;
	private InetAddress direccionDestino;
	private byte[] buffer;

	// -----------------------------------------------------------------
	// Constructor
	// -----------------------------------------------------------------

	/**
	 * Constructor de la clase. 
	 * @param canal canal de comunicacion con un cliente.
	 * @param administrador Parametro de la clase que conecta el programa con la base de datos.
	 * @throws IOException Excepcion que pueda ser generada debido al lector y escritor.
	 */
	public Conexion (int puertoAsignado, int puertoDestino, InetAddress direccionDestino, int tamanioBuffer, int pId, Servidor pServidor, String nomArchivos) throws IOException
	{
		buffer = new byte[tamanioBuffer];
		this.puertoAsignado = puertoAsignado;
		this.puertoDeDestino = puertoDestino;
		this.direccionDestino = direccionDestino;
		enviadoNomArch = false;
		tamanioArchivo = 0;
		Long puntoDeInicio = System.currentTimeMillis();
		timestamp = new Timestamp(puntoDeInicio);
		tiempoInicio = puntoDeInicio;
		setEstadoSesion(true);
		this.id = pId;
		this.servidor = pServidor;
		this.nomArchivos = nomArchivos;
		estadoExito = false;
	}

	/**
	 * Metodo que cierra la sesion de un usuario.
	 * @throws Exception Si se presenta algun error al cerrar la sesion.
	 */
	public void cerrarSesion(String motivo) throws Exception
	{
//		try
//		{
			estadoSesion = false;
			//out.println("Sesion terminada: " + motivo);
//			in.close();
//			out.close();
//			socket.close();
			long tiempoDeTransferencia = tiempoFin - tiempoInicio;
			long numeroDePaquetesTransmitidos = numeroDePaquetesEnviados;
			long bytesTransmitidos = (long) tamanioArchivo;
			EscritorDeLog escritor = new EscritorDeLog(id, timestamp, nomArchivoEnviado, tamanioArchivo, cliente, estadoExito, tiempoDeTransferencia, numeroDePaquetesEnviados, numeroDePaquetesTransmitidos, bytesTransmitidos);
			escritor.imprimirLog();
//		}
//		catch (IOException e)
//		{		
//			throw new Exception("Error en cierre de sesion:" + e.getMessage());
//		}
	}

	/**
	 * Run del objeto conexion
	 */
	public void run()
	{
		System.out.println("Empezando atencion...");
		try
		{
			//Tiempo de timeout igual a 2 minutos
//			in.readLine();
//			out.println();
			String respuesta1 = "HOLA:"+id;
			enviarInformacion(respuesta1.getBytes());
			StringBuilder estado = new StringBuilder();
			if(servidor.darMultiple())
			{
				estado.append("MULTIPLE");
			}
			else
			{
				estado.append("SIMPLE");
			}
//			out.println("ESTADO:"+estado.toString());
			String respuesta2 = "ESTADO:"+estado.toString();
			enviarInformacion(respuesta2.getBytes());
			while(estadoSesion)
			{	
				if(!servidor.darMultiple())
				{
					String archivos = "ARCHIVOS:"+nomArchivos;
					if (!enviadoNomArch) 
					{
//						out.println(archivos);
						enviarInformacion(archivos.getBytes());
						enviadoNomArch = true;
					}

					//Se inicia timer
					long start = System.currentTimeMillis();

					//Se recibe la solicitud de un metodo
//					String metodoSolicitado = in.readLine();
					
					//Se para el timer
					long end = System.currentTimeMillis();

					//Se calcula el tiempo iddle del usuario
					long duration = (end - start);
					boolean recepcionConfirmada = false;
					String metodoSolicitado = "";
					while(!recepcionConfirmada && duration < TIMEOUT)
					{
						 try
						 {
							 metodoSolicitado = new String(recibirInformacion());
							 recepcionConfirmada = true;
						 }
						 catch(NullPointerException e)
						 {
							 continue;
						 }
					}
					
					//Si el tiempo supera el de timeout se cierra la sesion
					if(duration >= TIMEOUT)
					{
						cerrarSesion("Se supero el tiempo de sesion sin actividad");
					}
					else if(metodoSolicitado.contains(DESCARGA))
					{
						enviarImagen(metodoSolicitado.split(":")[1]);
					}
					else if(metodoSolicitado.contains(MULTIPLE))
					{
						setUpEnvioMultiple(metodoSolicitado);
					}
					else if(metodoSolicitado.equals(SALIR))
					{
						estadoExito = true;
						cerrarSesion("Sesion cerrada por usuario");
					}
				}
				else
				{
					if(sePuedeHacerEnvioMultiple())
					{
//						out.println(servidor.getNombreArchMult().toString());
						String respuesta3 = servidor.getNombreArchMult().toString();
						enviarInformacion(respuesta3.getBytes());
						disminuirCLientesFaltantes();
						enviarImagen(servidor.getNombreArchMult().toString());
					}
				}
			}
		}
		catch(Exception e)
		{
			try
			{
				estadoSesion = false;
				cerrarSesion("Fallo al comenzar la sesion");
//				in.close();
//				out.close();
//				socket.close();
			}
			catch (Exception e2)
			{
				e2.printStackTrace();
			}
			e.printStackTrace( );
		}
	}

	/**
	 * Disminuye los clientes faltantes por recibir el arch
	 */
	private synchronized void disminuirCLientesFaltantes()
	{
		int numClientes = servidor.getNumeroClientesFaltantesEnvioMultiple()-1;
		servidor.setNumeroClientesFaltantesEnvioMultiple(numClientes);
		if(numClientes == 0)
		{
			servidor.envioMultipleCompletado();
		}
	}

	/**
	 * @return si el serv va a hacer envio mult
	 */
	private boolean sePuedeHacerEnvioMultiple() 
	{
		return (servidor.darMultiple() && servidor.darListaDeUsuariosConectados().size() >= servidor.getNumeroClientesEnvioMultiple() && servidor.getNumeroClientesFaltantesEnvioMultiple() != 0);
	}

	/**
	 * Hace las preparaciones para realizar un envio multiple
	 * @param metodoSolicitado
	 */
	private void setUpEnvioMultiple(String metodoSolicitado) 
	{
		servidor.configurarEnvioMultiple(metodoSolicitado);
	}

	/**
	 * Envia la imagen al usuario
	 * @param metodoSolicitado
	 * @throws Exception 
	 */
	private void enviarImagen(String linkFike) throws Exception
	{
		nomArchivoEnviado = linkFike;
		File myFile = new File ("./data/"+linkFike);
		String hash = servidor.getHashes().get(linkFike);

		try
		{		
			//enviar length del archivo en bytes
			tamanioArchivo = (int) myFile.length();
			String respuesta4 = tamanioArchivo + "";
//			out.println(tamanioArchivo);
			enviarInformacion(respuesta4.getBytes());
			//Enviar hash del archivo
			enviarInformacion(hash.getBytes());
//			out.println(hash);

			//Se inicia timer
			tiempoInicio = System.currentTimeMillis();
			
			//Enviar el archivo
			int count;
			int enviados = 0;
			byte[] bufferTemp = new byte[1024];
//			OutputStream outs = socket.getOutputStream();
			BufferedInputStream ins = new BufferedInputStream(new FileInputStream(myFile));
			while ((count = ins.read(bufferTemp)) >= 0) 
			{
//			     outs.write(buffer, 0, count);
			     enviarInformacion(bufferTemp);
			     enviados++;
			}
			tiempoFin = System.currentTimeMillis();
			numeroDePaquetesEnviados = enviados;
			ins.close();
			cerrarSesion("Se descargo el archivo correctamente");
			estadoExito = true;
			
		}
		catch (IOException e) 
		{

		}
	}

	private void enviarInformacion(byte[] buffer2) throws IOException
	{

		socketSalida = new DatagramSocket(puertoAsignado);
		System.out.println("ENVIO LADO SERVIDOR:" + (new String(buffer2)) + ", puerto: " + puertoAsignado);
		//			byte[] buffer2 = informacion.getBytes();
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
				//					byte[] temp = listaDeBuffers.get(i);
				listaDeBuffers.add(new byte[Servidor.TAMANIOBUFFER]);

				for(int j = 0; j < Servidor.TAMANIOBUFFER; j++)
				{
					int contadorPosicion = j + (i*Servidor.TAMANIOBUFFER);
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
	/**
	 * Método que recibe paquetes UDP según un Timeout predefinido y un buffer predefinido.
	 * @return byte[] si hay paquetes, null si no se recibe nada.
	 * @throws IOException
	 */
	private byte[] recibirInformacion()throws IOException
	{
		socketEntrada = new DatagramSocket(puertoAsignado);
		System.out.println("RECEPCION LADO SERVIDOR, puerto: " + puertoAsignado);
		socketEntrada.setSoTimeout(TIMEOUTLECTURA);
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
			System.out.println("SERVIDOR: El tiempo de lectura expiró");
		}
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
			System.out.println("RECEPCIÓN FINAL LADO SERVIDOR: " + new String(respuestaFinal));
			return respuestaFinal;
			
		}
		else
		{
			return respuesta;
		}

	}

	
	// -----------------------------------------------------------------
	// Getters and setters
	// -----------------------------------------------------------------

//	/**
//	 * Metodo que retorna el escritor de salida de la conexion actual.
//	 * @return
//	 */
//	public PrintWriter getOut()
//	{
//		return out;
//	}
//
//	/**
//	 * Metodo que modifica el escritor de salida de la conexion actual.
//	 * @param out
//	 */
//	public void setOut(PrintWriter out)
//	{
//		this.out = out;
//	}
//
//
//	public BufferedReader getIn()
//	{
//		return in;
//	}
//
//	/**
//	 * Metodo que modifica el lector de entrada de la conexion actual.
//	 * @param in
//	 */
//	public void setIn(BufferedReader in)
//	{
//		this.in = in;
//	}

	/**
	 * Metodo que retorna el estado actual de la sesion.
	 * @return
	 */
	public boolean getEstadoSesion()
	{
		return estadoSesion;
	}

	/**
	 * Metodo que establece el estado actual de la sesion.
	 * @param estadoSesion
	 */
	public void setEstadoSesion(boolean estadoSesion)
	{
		this.estadoSesion = estadoSesion;
	}

//	/**
//	 * Retorna el socket
//	 * @return
//	 */
//	public Socket getSocket()
//	{
//		return socket;
//	}
//
//	/**
//	 * Cambia el socket
//	 * @param socket
//	 */
//	public void setSocket(Socket socket)
//	{
//		this.socket = socket;
//	}
	public boolean getIsRunning()
	{
		return estadoSesion;
	}
}