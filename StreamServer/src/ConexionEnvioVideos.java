import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class ConexionEnvioVideos extends Thread
{
	/**
	 * Atributo que contiene el socket de la conexion.
	 */
	private Socket socket; 

	/**
	 * Atributo que represente el flujo de lectura del usuario.
	 */	
	private BufferedReader in;

	/**
	 * Atributo que determina el estado de la sesion, true si esta conectado, false si esta desconectado.
	 */
	private boolean estadoSesion;

	// -----------------------------------------------------------------
	// Constructor
	// -----------------------------------------------------------------

	/**
	 * Constructor de la clase. 
	 * @param canal canal de comunicacion con un cliente.
	 * @param administrador Parametro de la clase que conecta el programa con la base de datos.
	 * @throws IOException Excepcion que pueda ser generada debido al lector y escritor.
	 */
	public ConexionEnvioVideos (Socket canal) throws IOException
	{
		setIn(new BufferedReader( new InputStreamReader( canal.getInputStream( ) ) ));
		setSocket(canal);
		setEstadoSesion(true);
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
			in.close();
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
		try
		{
			while(estadoSesion)
			{	
				//Recibe el nombre del archibo
				String nombreFile = in.readLine();
		        descargarImagen(nombreFile);
			}
		}
		catch(Exception e)
		{
			try
			{
				estadoSesion = false;
				cerrarSesion("Fallo al comenzar la sesion");
				in.close();
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
	 * Envia la imagen al usuario
	 * @param metodoSolicitado
	 * @throws Exception 
	 */
	private void descargarImagen(String linkFike) throws Exception
	{
		try
		{		

			//Recibir archivo
			FileOutputStream fos = new FileOutputStream("./data/"+linkFike);
			BufferedOutputStream out = new BufferedOutputStream(fos);
			byte[] buffer = new byte[1024];
			int count;
			InputStream in = socket.getInputStream();
			while ((count = in.read(buffer)) >= 0)
			{
				fos.write(buffer, 0, count);
			}
			out.close();
			fos.close();
			cerrarSesion("Se descargo el archivo correctamente");
		}
		catch (IOException e) 
		{

		}
	}


	// -----------------------------------------------------------------
	// Getters and setters
	// -----------------------------------------------------------------

	/**
	 * Metodo que obtiene el lector de entrada de la conexion actual.
	 * @return
	 */
	public BufferedReader getIn()
	{
		return in;
	}

	/**
	 * Metodo que modifica el lector de entrada de la conexion actual.
	 * @param in
	 */
	public void setIn(BufferedReader in)
	{
		this.in = in;
	}

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
