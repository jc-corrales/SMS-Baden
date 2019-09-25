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
	 * Número de Puerto de entrada.
	 */
	public static final int PUERTO = 8080;
	
	/**
	 * Socket de entrada.
	 */
	private ServerSocket puntoDeEntrada;
	
    /**
     * Es el conjunto de propiedades que contienen la configuración de la aplicación
     */
    private Properties config;
	/**
	 * Booleano que indica el status del servido, true si esta ejecutando.	
	 */
	private boolean status;
	
    /**
     * Es una colección con las conexiones que se están llevando a cabo en este momento
     */
    protected Collection <Conexion> conexiones;
    
    /**
     * Atributo que guarda la información de una nueva conexión.
     */
    private Conexion nuevaConexion;
    /**
     * Inicializa el servidor.
     * @param archivo El archivo de propiedades que tiene la configuración del servidor - archivo != null
     * @throws Exception Se lanza esta excepción si hay problemas con el archivo de propiedades o hay problemas en la conexión a la base de datos.
     */
    public Servidor (String archivo) throws Exception
    {
    	conexiones = new Vector <Conexion> ();
    	cargarConfiguracion(archivo);
    }
    /**
     * Intenta crear e iniciar una nueva conexion con el usuaro que se acaba de conectar. <br>
     * @param socketNuevoCliente El canal que permite la comunicación con el nuevo usuario - socket != null
     * @throws IOException Se lanza esta excepción si se presentan problemas de comunicación
     */
    synchronized private void crearConexion ( Socket socketNuevoCliente ) throws IOException
    {
    	nuevaConexion = new Conexion(socketNuevoCliente);
    	conexiones.add(nuevaConexion);
    	nuevaConexion.start();
//    	verificarInvariante();
    }
    
    /**
     * Carga la configuración a partir de un archivo de propiedades
     * @param archivo El archivo de propiedades que contiene la configuración que requiere el servidor - archivo != null y el archivo debe contener la propiedad
     *        "servidor.puerto" y las propiedades que requiere el administrador de usuarios.
     * @throws Exception Se lanza esta excepción si hay problemas cargando el archivo de propiedades.
     */
    private void cargarConfiguracion( String archivo) throws Exception
    {
        FileInputStream fis = new FileInputStream( archivo );
        config = new Properties( );
        config.load( fis );
        fis.close( );
    }
    
    /**
     * Este método se encarga de recibir todas las conexiones entrantes y crear los encuentros cuando fuera necesario.
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
                // Esperar una nueva conexión
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
     * Retorna una colección actualizada con las conexiones que se están desarrollando actualmente y no han terminado.<br>
     * Si había conexiones en la lista que ya habían terminado deben ser eliminados.
     * @return colección de conexiones.
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
     * Método que termina todas las conexiones y establece el estado de conexión de los usuarios en 0, para evitar problemas debido a un cierre inesperado del servidor.
     * @throws Exception Excepción en caso de que se presente algún problema desconectando a los usuarios.
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
                e.cerrarSesion("Desconexión");	
                }
            }
            conexiones.clear();
    	}
    	catch (Exception e)
    	{
    		throw new Exception ("Error en la desconexión: " + e.getMessage());
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
