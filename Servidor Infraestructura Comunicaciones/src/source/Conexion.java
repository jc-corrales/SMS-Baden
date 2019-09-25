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

	// -----------------------------------------------------------------
	// Constructor
	// -----------------------------------------------------------------

	/**
	 * Constructor de la clase. 
	 * @param canal canal de comunicación con un cliente.
	 * @param administrador Parámetro de la clase que conecta el programa con la base de datos.
	 * @throws IOException Excepción que pueda ser generada debido al lector y escritor.
	 */
	public Conexion (Socket canal) throws IOException
	{
		setOut(new PrintWriter( canal.getOutputStream( ), true ));
		setIn(new BufferedReader( new InputStreamReader( canal.getInputStream( ) ) ));
		setSocket(canal);
		setEstadoSesion(true);
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
			out.println("Sesión iniciada con el servidor");
			while(estadoSesion)
			{	
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
					enviarImagen(metodoSolicitado);
				}
				else if (metodoSolicitado.equals(MULTIPLE))
				{
					setUpEnvioMultiple(metodoSolicitado);
				}
				else if(metodoSolicitado.equals(SALIR))
				{
					cerrarSesion("Sesión cerrada por usuario");
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

	private void setUpEnvioMultiple(String metodoSolicitado) 
	{
		// TODO Auto-generated method stub

	}

	/**
	 * Envia la imagen al usuario
	 * @param metodoSolicitado
	 */
	private void enviarImagen(String metodoSolicitado)
	{
		File myFile;
		if (metodoSolicitado.split("-")[1].equals("1"))
		{
			myFile = new File ("./data/imagen");
		}
		else
		{
			myFile = new File ("./data/video");
		}

		try
		{
			//Enviar el archivo
			byte [] mybytearray  = new byte [(int)myFile.length()];
			FileInputStream fis = new FileInputStream(myFile);
			BufferedInputStream bis = new BufferedInputStream(fis);
			bis.read(mybytearray,0,mybytearray.length);
			OutputStream os = socket.getOutputStream();
			os.write(mybytearray,0,mybytearray.length);

			//Cerrar buffer, output y input que fueron usados para enviar el file
			os.flush();
			bis.close();
			os.close();
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