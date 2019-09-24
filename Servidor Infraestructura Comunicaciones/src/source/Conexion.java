package source;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class Conexion extends Thread
{
	
	/**
	 * Atributo que contiene el socket de la conexi�n.
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
	 * Atributo que determina el estado de la sesi�n, true si est� conectado, false si est� desconectado.
	 */
	private boolean estadoSesion;
	// -----------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------
	/**
	 * Constructor de la clase. 
	 * @param canal canal de comunicaci�n con un cliente.
	 * @param administrador Par�metro de la clase que conecta el programa con la base de datos.
	 * @throws IOException Excepci�n que pueda ser generada debido al lector y escritor.
	 */
	public Conexion (Socket canal) throws IOException
	{
		setOut(new PrintWriter( canal.getOutputStream( ), true ));
        setIn(new BufferedReader( new InputStreamReader( canal.getInputStream( ) ) ));
        setSocket(canal);
		
        setEstadoSesion(true);
	}
	/**
	 * M�todo que retorna el escritor de salida de la conexi�n actual.
	 * @return
	 */
	public PrintWriter getOut() {
		return out;
	}
	/**
	 * M�todo que modifica el escritor de salida de la conexi�n actual.
	 * @param out
	 */
	public void setOut(PrintWriter out) {
		this.out = out;
	}
	/**
	 * M�todo que obtiene el lector de entrada de la conexi�n actual.
	 * @return
	 */
	public BufferedReader getIn() {
		return in;
	}
	/**
	 * M�todo que modifica el lector de entrada de la conexi�n actual.
	 * @param in
	 */
	public void setIn(BufferedReader in) {
		this.in = in;
	}
	/**
	 * M�todo que retorna el estado actual de la sesi�n.
	 * @return
	 */
	public boolean getEstadoSesion() {
		return estadoSesion;
	}
	/**
	 * M�todo que establece el estado actual de la sesi�n.
	 * @param estadoSesion
	 */
	public void setEstadoSesion(boolean estadoSesion) {
		this.estadoSesion = estadoSesion;
	}
	public Socket getSocket() {
		return socket;
	}
	public void setSocket(Socket socket) {
		this.socket = socket;
	}
	
	
	/**
	 * M�todo que cierra la sesi�n de un usuario.
	 * @throws Exception Si se presenta alg�n error al cerrar la sesi�n.
	 */
	public void cerrarSesion() throws Exception
	{
		try
		{
			estadoSesion = false;
//			out.println(CERRAR_SESION_OK);
			in.close();
			out.close();
			socket.close();
		}
		catch (IOException e)
		{		
			throw new Exception("Error en cierre de sesion:" + e.getMessage());
		}
	}
	
	public void run()
	{
		//Paso 1, atender el inicio de sesi�n o el registro de un usuario.
		try
		{
			String entrada = in.readLine();
//			String comando = entrada.split(SEPARADOR_COMANDO )[ 0 ];
//			String elDato = entrada.split(SEPARADOR_COMANDO)[1];
//			if(comando.equals(CREAR_CUENTA) )
//			{
//				registrarNuevoUsuario(elDato);
//			}
//			else if (comando.equals(INICIAR_SESION))
//			{
//				iniciarSesion(elDato);
//			}
//			else
//			{
//				throw new Exception ("Comando inv�lido METODO RUN COMANDO: " + entrada);
//			}
			while(estadoSesion)
			{			
				String entrada2 = in.readLine();
				if(estadoSesion)
				{
//					procesarOrdenes(entrada2);
				}
			}
		}
		catch(Exception e)
		{
			try
			{
				estadoSesion = false;
				cerrarSesion();
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
}
