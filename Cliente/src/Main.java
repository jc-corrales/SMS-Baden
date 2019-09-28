import java.io.*;
import java.net.*;

public class Main {

//	public final static String IP = "157.253.218.40";
	public final static String IP = "localhost";
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
			outCliente = new PrintWriter( canal.getOutputStream( ) );
		}
		catch (IOException e) {
			abortarConexion();
		} 
		outCliente.print(HOLA);
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
		// TODO Auto-generated method stub
		//Como sea que se lee el archivo.
		String linea = inCliente.readLine();
		//No hacer nada con eso por ahora
		tiempo = System.currentTimeMillis();
		linea = inCliente.readLine();
		tiempo = tiempo - Long.parseLong(linea);
		linea = inCliente.readLine();
		
		if(file.hashCode()== Integer.parseInt(linea))
		{
			outCliente.write("OK");
		}
	}

	public void inicioSimple(String archivo) throws IOException 
	{

		outCliente.write(DESCARGA + ":" +  archivo);
		//Como sea que se lee el archivo.
		String linea = inCliente.readLine();
		//No hacer nada con eso por ahora
		tiempo = System.currentTimeMillis();
		linea = inCliente.readLine();
		tiempo = tiempo - Long.parseLong(linea);
		linea = inCliente.readLine();
		
		if(file.hashCode()== Integer.parseInt(linea))
		{
			outCliente.write("OK");
		}
		
	} 

	public void inicioMultiple(String archivo, String numeroClientes) throws IOException 
	{
		outCliente.write(MULTIPLE +  ":" +  numeroClientes + ":" +  archivo);
		//Como sea que se lee el archivo.
		String linea = inCliente.readLine();
		//No hacer nada con eso por ahora
		tiempo = System.currentTimeMillis();
		linea = inCliente.readLine();
		tiempo = tiempo - Long.parseLong(linea);
		linea = inCliente.readLine();
		
		if(file.hashCode()== Integer.parseInt(linea))
		{
			outCliente.write("OK");
		}

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
