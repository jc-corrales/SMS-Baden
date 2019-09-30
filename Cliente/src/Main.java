import java.io.*;
import java.net.*;
import java.sql.Timestamp;


public class Main {

	//	public final static String IP = "157.253.218.40";
	public final static String IP = "40.76.10.131";
	public final static String DESCARGA = "DESCARGA";
	public final static String MULTIPLE = "MULTIPLE";
	public final static String HOLA = "HOLA";
	public final static int PUERTO = 11000;
	private Socket canal;
	private PrintWriter outCliente;
	private BufferedReader inCliente;
	public boolean estado;
	public String[] archivos;
	public int numero;
	public boolean multiple;
	public long tiempo;
	public File file;
	public int paquetesRecibidos;

	public Main()
	{
		estado = false;
		multiple = false;
		archivos= new String[1];
	}

	public void establecerConexion() throws IOException
	{
		try
		{
			canal = new Socket( IP, PUERTO);

			inCliente = new BufferedReader( new InputStreamReader( canal.getInputStream( ) ) );
			outCliente = new PrintWriter( canal.getOutputStream( ), true );
		}
		catch (IOException e) {
			abortarConexion();
		} 
				outCliente.println(HOLA);
		String linea = inCliente.readLine();
		if(linea.startsWith(HOLA))
		{
			estado = true;
			numero = Integer.parseInt(linea.split(":")[1]);
		}
		linea = inCliente.readLine();
		if(linea.startsWith("ESTADO"))
		{
			String aux = linea.split(":")[1];
			if(aux.equals("MULTPIPLE"))
			{
				multiple = true;
				seguimientoMultiple();
			}
			else
			{
				linea = inCliente.readLine();
				if(linea.startsWith("ARCHIVOS"))
				{
					archivos = linea.split(":")[1].split(";");
				}
			}
		}
	}

	public void seguimientoMultiple() throws IOException {
		boolean estadoExito = false;

		//Recibir nombrearch
		String archivo = inCliente.readLine();
		
		//Recibir tam
		String tamnioFile = inCliente.readLine();

		//Recibir hash
		String hash = inCliente.readLine();

		//No hacer nada con eso por ahora
		long inic = System.currentTimeMillis();

		//descargar arch
		obtenerArchivo(archivo);

		//No hacer nada con eso por ahora
		tiempo = System.currentTimeMillis() - inic;

		if(file.hashCode()== Integer.parseInt(hash))
		{
			estadoExito = true;
		}

		//Log
		Long puntoDeInicio = System.currentTimeMillis();
		Timestamp timestamp = new Timestamp(puntoDeInicio);
		InetAddress inetAddress = InetAddress.getLocalHost();
		EscritorDeLog escritor = new EscritorDeLog(numero, timestamp, archivo, Double.parseDouble(tamnioFile), inetAddress.getHostAddress(), estadoExito, tiempo,paquetesRecibidos, file.length());
		escritor.imprimirLog();
	}

	public void inicioSimple(String archivo) throws IOException 
	{
		boolean estadoExito = false;

		outCliente.println(DESCARGA + ":" +  archivo);

		//Recibir tam
		String tamnioFile = inCliente.readLine();

		System.out.println(tamnioFile);
		//Recibir hash
		String hash = inCliente.readLine();

		System.out.println(hash);
		//No hacer nada con eso por ahora
		long inic = System.currentTimeMillis();

		//descargar arch
		obtenerArchivo(archivo);

		//No hacer nada con eso por ahora
		tiempo = System.currentTimeMillis() - inic;

		if(file.hashCode()== Integer.parseInt(hash))
		{
			estadoExito = true;
		}

		//Log
		Long puntoDeInicio = System.currentTimeMillis();
		Timestamp timestamp = new Timestamp(puntoDeInicio);
		InetAddress inetAddress = InetAddress.getLocalHost();
		EscritorDeLog escritor = new EscritorDeLog(numero, timestamp, archivo, Double.parseDouble(tamnioFile), inetAddress.getHostAddress(), estadoExito, tiempo,paquetesRecibidos, file.length());
		escritor.imprimirLog();

	} 

	private void obtenerArchivo(String archivo) throws IOException 
	{
		int c = 0;
		FileOutputStream fos = new FileOutputStream("./data/"+archivo);
		BufferedOutputStream out = new BufferedOutputStream(fos);
		byte[] buffer = new byte[1024];
		int count;
		InputStream in = canal.getInputStream();
		while ((count = in.read(buffer)) >= 0)
		{
			fos.write(buffer, 0, count);
			c++;
		}
		paquetesRecibidos = c;
		out.close();
		file = new File("./data/"+archivo);
	}

	public void inicioMultiple(String archivo, String numeroClientes) throws IOException 
	{
		boolean estadoExito = true;
		outCliente.println(MULTIPLE +  ":" +  numeroClientes + ":" +  archivo);

		//Recibir nombrearch
		inCliente.readLine();
				
		//Recibir tam
		String tamnioFile = inCliente.readLine();

		//Recibir hash
		String hash = inCliente.readLine();

		//No hacer nada con eso por ahora
		long inic = System.currentTimeMillis();

		//descargar arch
		obtenerArchivo(archivo);

		//No hacer nada con eso por ahora
		tiempo = System.currentTimeMillis() - inic;

		if(file.hashCode()== Integer.parseInt(hash))
		{
			estadoExito = true;
		}

		//Log
		Long puntoDeInicio = System.currentTimeMillis();
		Timestamp timestamp = new Timestamp(puntoDeInicio);
		InetAddress inetAddress = InetAddress.getLocalHost();
		EscritorDeLog escritor = new EscritorDeLog(numero, timestamp, archivo, Double.parseDouble(tamnioFile), inetAddress.getHostAddress(), estadoExito, tiempo,paquetesRecibidos, file.length());
		escritor.imprimirLog();


	}

	public void abortarConexion()
	{
		try
		{
			canal.close();
		}
		catch(Exception e)
		{

		}
		try
		{
			inCliente.close();
		}
		catch(Exception e)
		{

		}
		try
		{
			outCliente.close();
		}
		catch(Exception e)
		{

		}
	}
}
