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
 * Modela una conexi�n entre 1 cliente y el servidor
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
	 * Atributo que contiene el socket de la conexi�n.
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
	 * Atributo que determina el estado de la sesi�n, true si est� conectado, false si est� desconectado.
	 */
	private boolean estadoSesion;

	private String nomArchivos;

	/**
	 * Modela el servidor
	 */
	private Servidor servidor;
	/**
	 * Tiempo de inicio de la conexi�n.
	 */
	private long tiempoInicio;
	/**
	 * Tiempo de finalizaci�n de la conexi�n.
	 */
	private long tiempoFin;
	/**
	 * Fecha y hora de la conexion.
	 */
	private Timestamp timestamp;
	/**
	 * Modela la id de la conexi�n
	 */
	private int id;
	/**
	 * Atributo que contiene si el env�o fue exitoso o no.
	 */
	private boolean estadoExito;
	/**
	 * Atributo que contiene el tama�o del archivo a enviar.
	 */
	private double tamanioArchivo;
	/**
	 * Direcci�n IP del Cliente.
	 */
	private String cliente;
	
	private long numeroDePaquetesEnviados;
	private String nomArchivoEnviado;

	// -----------------------------------------------------------------
	// Constructor
	// -----------------------------------------------------------------

	/**
	 * Constructor de la clase. 
	 * @param canal canal de comunicaci�n con un cliente.
	 * @param administrador Par�metro de la clase que conecta el programa con la base de datos.
	 * @throws IOException Excepci�n que pueda ser generada debido al lector y escritor.
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
	 * M�todo que cierra la sesi�n de un usuario.
	 * @throws Exception Si se presenta alg�n error al cerrar la sesi�n.
	 */
	public void cerrarSesion(String motivo) throws Exception
	{
		try
		{
			estadoSesion = false;
			out.println("Sesi�n terminada: " + motivo);
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

					//Si el tiempo supera el de timeout se cierra la sesi�n
					if(duration >= TIMEOUT)
					{
						cerrarSesion("Se super� el tiempo de sesi�n sin actividad");
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
						cerrarSesion("Sesi�n cerrada por usuario");
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
				cerrarSesion("Fallo al comenzar la sesi�n");
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
	 * M�todo que retorna el escritor de salida de la conexi�n actual.
	 * @return
	 */
	public PrintWriter getOut()
	{
		return out;
	}

	/**
	 * M�todo que modifica el escritor de salida de la conexi�n actual.
	 * @param out
	 */
	public void setOut(PrintWriter out)
	{
		this.out = out;
	}

	/**
	 * M�todo que obtiene el lector de entrada de la conexi�n actual.
	 * @return
	 */
	public BufferedReader getIn()
	{
		return in;
	}

	/**
	 * M�todo que modifica el lector de entrada de la conexi�n actual.
	 * @param in
	 */
	public void setIn(BufferedReader in)
	{
		this.in = in;
	}

	/**
	 * M�todo que retorna el estado actual de la sesi�n.
	 * @return
	 */
	public boolean getEstadoSesion()
	{
		return estadoSesion;
	}

	/**
	 * M�todo que establece el estado actual de la sesi�n.
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