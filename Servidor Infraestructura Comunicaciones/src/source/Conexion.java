package source;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Timestamp;

/**
 * Modela una conexión entre 1 cliente y el servidor
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
	 * Atributo que contiene el socket de la conexión.
	 */
	private Socket socket; 
	
	/**
	 * modela si ya se envio el nombre de arch
	 */
	private boolean enviadoNomArch;

	/**
	 * Atributo que representa el flujo de escritura para el usuario.
	 */
	private PrintWriter out;

	/**
	 * Atributo que represente el flujo de lectura del usuario.
	 */	
	private BufferedReader in;

	/**
	 * Atributo que determina el estado de la sesión, true si está conectado, false si está desconectado.
	 */
	private boolean estadoSesion;

	private String nomArchivos;

	/**
	 * Modela el servidor
	 */
	private Servidor servidor;
	/**
	 * Tiempo de inicio de la conexión.
	 */
	private long tiempoInicio;
	/**
	 * Tiempo de finalización de la conexión.
	 */
	private long tiempoFin;
	/**
	 * Fecha y hora de la conexion.
	 */
	private Timestamp timestamp;
	/**
	 * Modela la id de la conexión
	 */
	private int id;
	/**
	 * Atributo que contiene si el envío fue exitoso o no.
	 */
	private boolean estadoExito;
	/**
	 * Atributo que contiene el tamaño del archivo a enviar.
	 */
	private double tamanioArchivo;
	/**
	 * Dirección IP del Cliente.
	 */
	private String cliente;
	
	private long numeroDePaquetesEnviados;
	private String nomArchivoEnviado;

	// -----------------------------------------------------------------
	// Constructor
	// -----------------------------------------------------------------

	/**
	 * Constructor de la clase. 
	 * @param canal canal de comunicación con un cliente.
	 * @param administrador Parámetro de la clase que conecta el programa con la base de datos.
	 * @throws IOException Excepción que pueda ser generada debido al lector y escritor.
	 */
	public Conexion (Socket canal, int pId, Servidor pServidor, String nomArchivos) throws IOException
	{
		enviadoNomArch = false;
		tamanioArchivo = 0;
		Long puntoDeInicio = System.currentTimeMillis();
		timestamp = new Timestamp(puntoDeInicio);
		tiempoInicio = puntoDeInicio;
		setOut(new PrintWriter( canal.getOutputStream( ), true ));
		setIn(new BufferedReader( new InputStreamReader( canal.getInputStream( ) ) ));
		setSocket(canal);
		setEstadoSesion(true);
		this.id = pId;
		this.servidor = pServidor;
		this.nomArchivos = nomArchivos;
		socket.setSoTimeout(TIMEOUT);
		estadoExito = false;
		cliente = socket.getRemoteSocketAddress().toString();
	}

	/**
	 * Método que cierra la sesión de un usuario.
	 * @throws Exception Si se presenta algún error al cerrar la sesión.
	 */
	public void cerrarSesion(String motivo) throws Exception
	{
		try
		{
			estadoSesion = false;
			out.println("Sesión terminada: " + motivo);
			in.close();
			out.close();
			socket.close();
			long tiempoDeTransferencia = tiempoFin - tiempoInicio;
			long bytesRecibidos = (long) tamanioArchivo;
			long bytesTransmitidos = (long) tamanioArchivo;
			long numeroDePaquetesRecibidos = 1;
			long numeroDePaquetesTransmitidos = 1;
			EscritorDeLog escritor = new EscritorDeLog(id, timestamp, nomArchivoEnviado, tamanioArchivo, cliente, estadoExito, tiempoDeTransferencia, numeroDePaquetesEnviados, numeroDePaquetesRecibidos, numeroDePaquetesTransmitidos, bytesRecibidos, bytesTransmitidos);
			escritor.imprimirLog();
		}
		catch (IOException e)
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
			out.println("HOLA:"+id);
			StringBuilder estado = new StringBuilder();
			if(servidor.darMultiple())
			{
				estado.append("MULTIPLE");
			}
			else
			{
				estado.append("SIMPLE");
			}
			out.println("ESTADO:"+estado.toString());

			while(estadoSesion)
			{	
				if(!servidor.darMultiple())
				{
					String archivos = "ARCHIVOS:"+nomArchivos;
					if (!enviadoNomArch) 
					{
						out.println(archivos);
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

					//Si el tiempo supera el de timeout se cierra la sesión
					if(duration >= TIMEOUT)
					{
						cerrarSesion("Se superó el tiempo de sesión sin actividad");
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
						cerrarSesion("Sesión cerrada por usuario");
					}
				}
				else
				{
					if(sePuedeHacerEnvioMultiple())
					{
						out.println(servidor.getNombreArchMult().toString());
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
				cerrarSesion("Fallo al comenzar la sesión");
				in.close();
				out.close();
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
			out.println(tamanioArchivo);
			
			//Enviar hash del archivo
			out.println(hash);

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


	// -----------------------------------------------------------------
	// Getters and setters
	// -----------------------------------------------------------------

	/**
	 * Método que retorna el escritor de salida de la conexión actual.
	 * @return
	 */
	public PrintWriter getOut()
	{
		return out;
	}

	/**
	 * Método que modifica el escritor de salida de la conexión actual.
	 * @param out
	 */
	public void setOut(PrintWriter out)
	{
		this.out = out;
	}

	/**
	 * Método que obtiene el lector de entrada de la conexión actual.
	 * @return
	 */
	public BufferedReader getIn()
	{
		return in;
	}

	/**
	 * Método que modifica el lector de entrada de la conexión actual.
	 * @param in
	 */
	public void setIn(BufferedReader in)
	{
		this.in = in;
	}

	/**
	 * Método que retorna el estado actual de la sesión.
	 * @return
	 */
	public boolean getEstadoSesion()
	{
		return estadoSesion;
	}

	/**
	 * Método que establece el estado actual de la sesión.
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
	public Socket getSocket()
	{
		return socket;
	}

	/**
	 * Cambia el socket
	 * @param socket
	 */
	public void setSocket(Socket socket)
	{
		this.socket = socket;
	}
}