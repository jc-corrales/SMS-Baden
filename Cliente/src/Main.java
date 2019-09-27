import java.io.*;
import java.net.*;

public class Main {

	public final static String IP = "157.253.218.40";
	public final static String DESCARGA = "DESCARGA";
	public final static String HOLA = "HOLA";
	public final static int PUERTO = 11000;
	private Socket canal;
	private PrintWriter outCliente;
	private BufferedReader inCliente;
	public boolean estado;
	public String[] archivos;
	public int numero;
	public boolean simple;
	
	public Main()
	{
		estado = false;
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
		}
		linea = inCliente.readLine();
		System.out.println(linea);
		if(linea.startsWith("ARCHIVOS"))
		{
			archivos = linea.split(":")[1].split(";");
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
	public void pedirArchivo(String archivo) throws IOException
	{
		System.out.println(archivo);
		outCliente.write(DESCARGA + ":" + archivo);
		String linea = inCliente.readLine();
	}
}
