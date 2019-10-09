

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;

public class EscritorDeLog
{
	/**
	 * Identificador del Log.
	 */
	private Long ID;
	/**
	 * Fecha y hora de la conexión.
	 */
	private Timestamp timestamp;
	/**
	 * Nombre de archivo enviado.
	 */
	private String nombreArchivo;
	/**
	 * Tamaño del archivo enviado.
	 */
	private Double tamanioArchivo;
	/**
	 * Cliente al que se le envio el archivo.
	 */
	private String cliente;
	/**
	 * Booleano que indica si el envío fue exitoso o no.
	 */
	private Boolean estadoExito;

	private int  paquetesRecibidos;
	
	/**
	 * Número de bytes recibidos.
	 */
	private Long bytesRecibidos;
	/**
	 * @return the iD
	 */
	public Long getID() {
		return ID;
	}

	/**
	 * @param iD the iD to set
	 */
	public void setID(Long iD) {
		ID = iD;
	}

	public EscritorDeLog(long id, Timestamp timestamp, String nombreArchivo, double tamanioArchivo,
			String cliente, boolean estadoExito, double tiempoDeTransferencia,
			int numeroDePaquetesRecibidos,
			long bytesRecibidos)
	{
		this.ID = id;
		this.timestamp = timestamp;
		this.nombreArchivo = nombreArchivo;
		this.tamanioArchivo = tamanioArchivo;
		this.cliente = cliente;
		this.estadoExito = estadoExito;
		this.bytesRecibidos = bytesRecibidos;
		this.paquetesRecibidos = numeroDePaquetesRecibidos;
	}
	
	public boolean imprimirLog()
	{
		try
		{
			String informacion = 
			"Fecha y Hora de transmisión: " + timestamp.getTime() + "\n" +
			"Nombre del Archivo enviado: " + nombreArchivo + "\n" +
			"Tamaño del Archivo enviado (bytes): " + tamanioArchivo + "\n" +
			"Cliente: " + cliente + "\n" +
			"Estado de envío de Entrega: " + estadoExito + "\n" +
			"Número de paquetes recibidos: " + paquetesRecibidos + "\n" +
			"Número de bytes recibidos: " + bytesRecibidos;
			try
			{
				String time = (timestamp.toString()).replace(":", ".");
				FileWriter writer = new FileWriter("./logs/"+ID +"."+time+".txt");
				BufferedWriter bw = new BufferedWriter(writer);
				bw.write(informacion);
				bw.close();
			}
			catch(IOException e)
			{
				throw new IOException(e.getMessage());
			}
			return true;
		}
		catch (Exception e)
		{
			System.err.println("Error durante la impresión del Log:" + e.getMessage());
			return false;
		}
	}
	
	public Timestamp getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}
	/**
	 * @return the nombreArchivo
	 */
	public String getNombreArchivo() {
		return nombreArchivo;
	}
	/**
	 * @param nombreArchivo the nombreArchivo to set
	 */
	public void setNombreArchivo(String nombreArchivo) {
		this.nombreArchivo = nombreArchivo;
	}
	/**
	 * @return the tamanioArchivo
	 */
	public Double getTamanioArchivo() {
		return tamanioArchivo;
	}
	/**
	 * @param tamanioArchivo the tamanioArchivo to set
	 */
	public void setTamanioArchivo(Double tamanioArchivo) {
		this.tamanioArchivo = tamanioArchivo;
	}
	/**
	 * @return the cliente
	 */
	public String getCliente() {
		return cliente;
	}
	/**
	 * @param cliente the cliente to set
	 */
	public void setCliente(String cliente) {
		this.cliente = cliente;
	}
	/**
	 * @return the estadoExito
	 */
	public Boolean getEstadoExito() {
		return estadoExito;
	}
	/**
	 * @param estadoExito the estadoExito to set
	 */
	public void setEstadoExito(Boolean estadoExito) {
		this.estadoExito = estadoExito;
	}
	/**
	 * @return the bytesRecibidos
	 */
	public Long getBytesRecibidos() {
		return bytesRecibidos;
	}
	/**
	 * @param bytesRecibidos the bytesRecibidos to set
	 */
	public void setBytesRecibidos(Long bytesRecibidos) {
		this.bytesRecibidos = bytesRecibidos;
	}
	
	
}
