package source;

import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.io.*;

public class Servidor
{
	public final static String DESTINOLOGS = "";
	/**
	 * Número de Puerto de entrada.
	 */
	public static final int PUERTO = 11000;

	/**
	 * Define con que algoritmo se crea el hash
	 */
	public static final String ALGORITMO_HASH = "MD5";

	/**
	 * Define path de ubicacion archivos
	 */
	public static final String PATH_ARCHIVOS = "./data";

	/**
	 * Socket de entrada.
	 */
	private static ServerSocket puntoDeEntrada;

	/**
	 * Es el conjunto de propiedades que contienen la configuración de la aplicación
	 */
	private Properties config;

	/**
	 * Booleano que indica el status del servido, true si esta ejecutando.	
	 */
	private static boolean status;

	/**
	 * Numero de clientes a los que les falta recibir el envio multiple
	 */
	private int numeroClientesFaltantesEnvioMultiple;
	
	/**
	 * Numero de clientes a los que les falta recibir el envio multiple
	 */
	private int numeroClientesEnvioMultiple;
	
	/**
	 * Es una colección con las conexiones que se están llevando a cabo en este momento
	 */
	protected Collection <Conexion> conexiones;

	/**
	 * Representa el array de hash de los archivos => Key = Nombre del archivo, Value = hash del archivo
	 */
	private LinkedHashMap<String, String> hashes;

	/**
	 * Modela si se debe hacer un envio multiple
	 */
	private boolean envioMultiple;

	/**
	 * Modela el nombre del archivo al cuál se debe hacer envió multiple
	 */
	private StringBuilder nombreArchMult;

	/**
	 * Modela un string donde se contienen todos los nombres de los archivos disponibles para descargar
	 */
	private String nombresArchivosDisponibles;
	
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
		//Inicializar lista de hashes
		hashes = new LinkedHashMap<>();
		generarHashes();
		envioMultiple = false;
		numeroClientesEnvioMultiple = 0;
		numeroClientesFaltantesEnvioMultiple = 0;
		nombresArchivosDisponibles = darNomArchivos();
		
		
		//Inicializar lista conecciones y cargar conf de archivo
		conexiones = new Vector <Conexion> ();
		//cargarConfiguracion(archivo);
	}

	/**
	 * Genera los hashes de los archivos
	 * @throws NoSuchAlgorithmException 
	 */
	private void generarHashes() throws NoSuchAlgorithmException
	{
		try (Stream<Path> walk = Files.walk(Paths.get(PATH_ARCHIVOS)))
		{

			List<String> result = walk.filter(Files::isRegularFile).map(x -> x.toString()).collect(Collectors.toList());

			for (String nombreArchivo : result)
			{
				File file = new File(nombreArchivo);
				String nomGuardar = nombreArchivo.replace(".\\data\\","");
				hashes.put(nomGuardar, darHashFile(file));
			}

		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	/**
	 * Intenta crear e iniciar una nueva conexion con el usuaro que se acaba de conectar. <br>
	 * @param socketNuevoCliente El canal que permite la comunicación con el nuevo usuario - socket != null
	 * @throws IOException Se lanza esta excepción si se presentan problemas de comunicación
	 */
	synchronized private void crearConexion ( Socket socketNuevoCliente ) throws IOException
	{
		//nuevaConexion = new Conexion(socketNuevoCliente,0);
		conexiones.add(nuevaConexion);
		nuevaConexion.start();
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

	/**
	 * Retorna un string que representa el hash de un archivo
	 * @param fileSolicitado
	 * @return hash representado en string
	 * @throws NoSuchAlgorithmException 
	 * @throws IOException 
	 */
	public String darHashFile(File file) throws NoSuchAlgorithmException, IOException
	{
		//Generar hash con MD5
		MessageDigest md5Digest = MessageDigest.getInstance(ALGORITMO_HASH);

		//Retornar hash
		return getFileChecksum(md5Digest, file);
	}

	/**
	 * Genera el hash dado el método y el archivo
	 * @param digest
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private static String getFileChecksum(MessageDigest digest, File file) throws IOException
	{
		return Integer.toString(file.hashCode());
	}

	/**
	 * Main del servidor
	 * @param args
	 */
	public static void main(String args[])
	{
		int numT = 5;
		ExecutorService exec = Executors.newFixedThreadPool(numT);
		System.out.println("Creado pool de tamaño "+ numT);
		int idThread = 0;
		status = true;
		try
		{
			Servidor servidor = new Servidor("");
			puntoDeEntrada = new ServerSocket(PUERTO);
			while(status)
			{
				Socket cliente = puntoDeEntrada.accept();
				System.out.println("Cliente " + idThread + " inició sesión.");
				Conexion con = new Conexion(cliente,idThread, servidor, servidor.nombresArchivosDisponibles);
				exec.execute(con);
				idThread++;
				servidor.conexiones.add(con);
			}
		}
		catch (Exception e)
		{
			System.err.println(e.getMessage());
		}
	}

	/**
	 * Genera el nomArchivos
	 * @return
	 */
	private String darNomArchivos()
	{
		StringBuilder ret = new StringBuilder();
		int cont = 1;
		int size = hashes.size();
		for (String nomArch : hashes.keySet())
		{
			if(cont == size)
			{
				ret.append(nomArch);
			}
			else
			{
				ret.append(nomArch+";");
			}
		}
		return ret.toString();
	}

	/**
	 * @return the hashes
	 */
	public LinkedHashMap<String, String> getHashes()
	{
		return hashes;
	}

	/**
	 * Se configura para que se haga envio multiple
	 * @param metodoSolicitado
	 */
	public synchronized String configurarEnvioMultiple(String metodoSolicitado)
	{
		if(envioMultiple)
		{
			return "Ya esta programado un envio multiple";
		}
		else
		{
			String[] data = metodoSolicitado.split(":");
			numeroClientesFaltantesEnvioMultiple = Integer.parseInt(data[1]);
			numeroClientesEnvioMultiple = Integer.parseInt(data[1]);
			nombreArchMult = new StringBuilder(data[2]);
			envioMultiple = true;
			return "ok";
		}
	}
	
	public boolean darMultiple()
	{
		return envioMultiple;
	}

	/**
	 * @return the nombreArchMult
	 */
	public StringBuilder getNombreArchMult() {
		return nombreArchMult;
	}

	/**
	 * @param nombreArchMult the nombreArchMult to set
	 */
	public void setNombreArchMult(StringBuilder nombreArchMult) {
		this.nombreArchMult = nombreArchMult;
	}

	/**
	 * @return the numeroClientesFaltantesEnvioMultiple
	 */
	public int getNumeroClientesFaltantesEnvioMultiple() {
		return numeroClientesFaltantesEnvioMultiple;
	}

	/**
	 * @param numeroClientesFaltantesEnvioMultiple the numeroClientesFaltantesEnvioMultiple to set
	 */
	public void setNumeroClientesFaltantesEnvioMultiple(int numeroClientesFaltantesEnvioMultiple) {
		this.numeroClientesFaltantesEnvioMultiple = numeroClientesFaltantesEnvioMultiple;
	}

	/**
	 * @return the numeroClientesEnvioMultiple
	 */
	public int getNumeroClientesEnvioMultiple() {
		return numeroClientesEnvioMultiple;
	}

	/**
	 * @param numeroClientesEnvioMultiple the numeroClientesEnvioMultiple to set
	 */
	public void setNumeroClientesEnvioMultiple(int numeroClientesEnvioMultiple) {
		this.numeroClientesEnvioMultiple = numeroClientesEnvioMultiple;
	}

	public void envioMultipleCompletado()
	{
		envioMultiple = false;
	}
}