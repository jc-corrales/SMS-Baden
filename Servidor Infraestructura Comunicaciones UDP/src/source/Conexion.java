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
	private final static int TIMEOUT = 120000;
	private final static String DESCARGA= "DESCARGA";
	private final static String MULTIPLE= "MULTIPLE";
	private final static String SALIR= "SALIR";

	/**
	 * Atributo que contiene el socket de la conexion.
	 */
	private DatagramSocket socket; 
	
	private int puertoDeDestino;
	
	private InetAddress direccionDestino;
	
	private String mensaje;
	
	private byte[] buffer;
	
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

	// -----------------------------------------------------------------
	// Constructor
	// -----------------------------------------------------------------

	/**
	 * Constructor de la clase. 
	 * @param canal canal de comunicacion con un cliente.
	 * @param administrador Parametro de la clase que conecta el programa con la base de datos.
	 * @throws IOException Excepcion que pueda ser generada debido al lector y escritor.
	 */
	public Conexion (DatagramPacket paqueteInicial, int pId, Servidor pServidor, String nomArchivos) throws IOException
	{
		socket = new DatagramSocket(Servidor.PUERTO);
		puertoDeDestino = paqueteInicial.getPort();
		direccionDestino = paqueteInicial.getAddress();
		mensaje =  new String(paqueteInicial.getData(), 0 , paqueteInicial.getLength());
		buffer = new byte[Servidor.TAMANIOBUFFER];
		enviadoNomArch = false;
		tamanioArchivo = 0;
		Long puntoDeInicio = System.currentTimeMillis();
		timestamp = new Timestamp(puntoDeInicio);
		tiempoInicio = puntoDeInicio;

//		setSocket(canal);
		setEstadoSesion(true);
		this.id = pId;
		this.servidor = pServidor;
		this.nomArchivos = nomArchivos;
		socket.setSoTimeout(TIMEOUT);
		estadoExito = false;
		cliente = socket.getRemoteSocketAddress().toString();
	}

	/**
	 * Metodo que cierra la sesion de un usuario.
	 * @throws Exception Si se presenta algun error al cerrar la sesion.
	 */
	public void cerrarSesion(String motivo) throws Exception
	{
		try
		{
			estadoSesion = false;
			//out.println("Sesion terminada: " + motivo);
//			in.close();
//			out.close();
			socket.close();
			long tiempoDeTransferencia = tiempoFin - tiempoInicio;
			long numeroDePaquetesTransmitidos = numeroDePaquetesEnviados;
			long bytesTransmitidos = (long) tamanioArchivo;
			EscritorDeLog escritor = new EscritorDeLog(id, timestamp, nomArchivoEnviado, tamanioArchivo, cliente, estadoExito, tiempoDeTransferencia, numeroDePaquetesEnviados, numeroDePaquetesTransmitidos, bytesTransmitidos);
			escritor.imprimirLog();
		}
		catch (Exception e)
		{		
			throw new Exception("Error en cierre de sesion:" + e.getMessage());
		}
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
			in.readLine();
//			out.println("HOLA:"+id);
			enviarInformacion("HOLA:"+id);
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
			enviarInformacion("ESTADO:"+estado.toString());

			while(estadoSesion)
			{	
				if(!servidor.darMultiple())
				{
					String archivos = "ARCHIVOS:"+nomArchivos;
					if (!enviadoNomArch) 
					{
						
//						out.println(archivos);
						enviarInformacion(archivos);
						enviadoNomArch = true;
					}

					//Se inicia timer
					long start = System.currentTimeMillis();

					//Se recibe la solicitud de un metodo
					String metodoSolicitado = in.readLine();

					//Se para el timer
					long end = System.currentTimeMillis();

					//Se calcula el tiempo iddle del usuario
					long duration = (end - start);

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
						enviarInformacion(servidor.getNombreArchMult().toString());
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
				socket.close();
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
//			out.println(tamanioArchivo);
			enviarInformacion(tamanioArchivo + "");
			//Enviar hash del archivo
//			out.println(hash);
			enviarInformacion(hash);

			//Se inicia timer
			tiempoInicio = System.currentTimeMillis();
			
			//Enviar el archivo
			int count;
			int enviados = 0;
			byte[] buffer = new byte[1024];
			OutputStream outs = socket.getOutputStream();
			BufferedInputStream ins = new BufferedInputStream(new FileInputStream(myFile));
			while ((count = ins.read(buffer)) >= 0) 
			{
			     outs.write(buffer, 0, count);
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
	
	/**
	 * Método que parte una cadena en arreglos de Bytes y luego los envía.
	 * @param informacion
	 * @throws IOException
	 */
	private void enviarInformacion(String informacion) throws IOException
	{
		buffer = informacion.getBytes();
		
		if(buffer.length > Servidor.TAMANIOBUFFER)
		{
			int numSubBuffers = (int) Math.ceil(buffer.length/Servidor.TAMANIOBUFFER);
			ArrayList<byte[]> listaDeBuffers = new ArrayList<byte[]>();
			for(int i = 0; i < numSubBuffers; i++)
			{
				byte[] temp = listaDeBuffers.get(i);
				temp = new byte[Servidor.TAMANIOBUFFER];
				listaDeBuffers.set(i, temp);

				for(int j = 0; j < Servidor.TAMANIOBUFFER; j++)
				{
					int contadorPosicion = j + (i*Servidor.TAMANIOBUFFER);
					listaDeBuffers.get(i)[j] = buffer[contadorPosicion];
				}
				DatagramPacket paquete = new DatagramPacket(listaDeBuffers.get(i), listaDeBuffers.get(i).length, direccionDestino, puertoDeDestino);
				socket.send(paquete);
			}
		}
		else
		{
			DatagramPacket paquete = new DatagramPacket(buffer, buffer.length, direccionDestino, puertoDeDestino);
			socket.send(paquete);
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
//	/**
//	 * Metodo que obtiene el lector de entrada de la conexion actual.
//	 * @return
//	 */
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

	/**
	 * Retorna el socket
	 * @return
	 */
	public DatagramSocket getSocket()
	{
		return socket;
	}

	/**
	 * Cambia el socket
	 * @param socket
	 */
	public void setSocket(DatagramSocket socket)
	{
		this.socket = socket;
	}
}