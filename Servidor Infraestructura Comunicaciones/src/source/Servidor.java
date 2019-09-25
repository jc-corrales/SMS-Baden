package source;

import java.net.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;


import java.io.*;

public class Servidor
{
	/**
	 * N�mero de Puerto de entrada.
	 */
	public static final int PUERTO = 8080;
	
	/**
	 * Socket de entrada.
	 */
	private ServerSocket puntoDeEntrada;
	
    /**
     * Es el conjunto de propiedades que contienen la configuraci�n de la aplicaci�n
     */
    private Properties config;
	/**
	 * Booleano que indica el status del servido, true si esta ejecutando.	
	 */
	private boolean status;
	
    /**
     * Es una colecci�n con las conexiones que se est�n llevando a cabo en este momento
     */
    protected Collection <Conexion> conexiones;
    
    /**
     * Atributo que guarda la informaci�n de una nueva conexi�n.
     */
    private Conexion nuevaConexion;
    /**
     * Inicializa el servidor.
     * @param archivo El archivo de propiedades que tiene la configuraci�n del servidor - archivo != null
     * @throws Exception Se lanza esta excepci�n si hay problemas con el archivo de propiedades o hay problemas en la conexi�n a la base de datos.
     */
    public Servidor (String archivo) throws Exception
    {
    	conexiones = new Vector <Conexion> ();
    	cargarConfiguracion(archivo);
    }
    /**
     * Intenta crear e iniciar una nueva conexion con el usuaro que se acaba de conectar. <br>
     * @param socketNuevoCliente El canal que permite la comunicaci�n con el nuevo usuario - socket != null
     * @throws IOException Se lanza esta excepci�n si se presentan problemas de comunicaci�n
     */
    synchronized private void crearConexion ( Socket socketNuevoCliente ) throws IOException
    {
    	nuevaConexion = new Conexion(socketNuevoCliente);
    	conexiones.add(nuevaConexion);
    	nuevaConexion.start();
//    	verificarInvariante();
    }
    
    /**
     * Carga la configuraci�n a partir de un archivo de propiedades
     * @param archivo El archivo de propiedades que contiene la configuraci�n que requiere el servidor - archivo != null y el archivo debe contener la propiedad
     *        "servidor.puerto" y las propiedades que requiere el administrador de usuarios.
     * @throws Exception Se lanza esta excepci�n si hay problemas cargando el archivo de propiedades.
     */
    private void cargarConfiguracion( String archivo) throws Exception
    {
        FileInputStream fis = new FileInputStream( archivo );
        config = new Properties( );
        config.load( fis );
        fis.close( );
    }
    
    /**
     * Este m�todo se encarga de recibir todas las conexiones entrantes y crear los encuentros cuando fuera necesario.
     */
    public void recibirConexiones( )
    {
    	String aux = config.getProperty( "servidor.puerto" );
        int puerto = Integer.parseInt( aux );
        try
        {
            puntoDeEntrada = new ServerSocket( puerto );

            while( true )
            {
                // Esperar una nueva conexi�n
                Socket socketNuevoCliente = puntoDeEntrada.accept( );

                // Intentar iniciar un encuentro con el nuevo cliente
                crearConexion( socketNuevoCliente );
            }
        }
        catch( IOException e )
        {
            e.printStackTrace( );
        }
        finally
        {
            try
            {
            	puntoDeEntrada.close( );
            }
            catch( IOException e )
            {
                e.printStackTrace( );
            }
        }
    }
    
    /**
     * Retorna una colecci�n actualizada con las conexiones que se est�n desarrollando actualmente y no han terminado.<br>
     * Si hab�a conexiones en la lista que ya hab�an terminado deben ser eliminados.
     * @return colecci�n de conexiones.
     */
    public Collection <Conexion> darListaDeUsuariosConectados()
    {
    	Collection <Conexion> listaDeUsuarios = new Vector<Conexion>();
    	Iterator <Conexion>iter = conexiones.iterator( );
        while( iter.hasNext( ) )
        {
            Conexion e = ( Conexion )iter.next( );
            if( e.getEstadoSesion() )
                listaDeUsuarios.add( e );
        }
        //Reemplazar la lista antigua con la actualizada.
        conexiones = listaDeUsuarios;
        return conexiones;
    }
    /**
     * M�todo que termina todas las conexiones y establece el estado de conexi�n de los usuarios en 0, para evitar problemas debido a un cierre inesperado del servidor.
     * @throws Exception Excepci�n en caso de que se presente alg�n problema desconectando a los usuarios.
     */
    public void desconexionDeSeguridad()throws Exception
    {
    	try
    	{
        	Iterator <Conexion>iter = conexiones.iterator( );
            while( iter.hasNext( ) )
            {
                Conexion e = ( Conexion )iter.next( );
                if( e.getEstadoSesion() )
                {
                e.cerrarSesion("Desconexi�n");	
                }
            }
            conexiones.clear();
    	}
    	catch (Exception e)
    	{
    		throw new Exception ("Error en la desconexi�n: " + e.getMessage());
    	}
    }
//	public static void main(String args[])
//	{
//		status = true;
//		try
//		{
//			puntoDeEntrada = new ServerSocket(PUERTO);
//			while(status)
//			{
//				Socket cliente = puntoDeEntrada.accept();
//				
//				
//			}
//		}
//		catch (Exception e)
//		{
//			System.err.println(e.getMessage());
//		}
//	}
}
