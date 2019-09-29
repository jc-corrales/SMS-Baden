package source;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;

public class EscritorDeLog
{
	public static final String PATH_LOGS = "./logs";
	/**
	 * Identificador del Log.
	 */
	private Long ID;
	/**
	 * Fecha y hora de la conexion.
	 */
	private Timestamp timestamp;
	/**
	 * Nombre de archivo enviado.
	 */
	private String nombreArchivo;
	/**
	 * Tamanio del archivo enviado.
	 */
	private Double tamanioArchivo;
	/**
	 * Cliente al que se le envio el archivo.
	 */
	private String cliente;
	/**
	 * Booleano que indica si el envio fue exitoso o no.
	 */
	private Boolean estadoExito;
	/**
	 * Tiempo de transferencia del archivo.
	 */
	private Double tiempoDeTransferencia;
	/**
	 * Numero de paquetes enviados.
	 */
	private long numeroDePaquetesEnviados;
	/**
	 * Numero de paquetes transmitidos.
	 */
	private Long numeroDePaquetesTransmitidos;
	/**
	 * Numero de bytes transmitidos.
	 */
	private Long bytesTransmitidos;
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
			long numeroDePaquetesEnviados, long numeroDePaquetesTransmitidos, long bytesTransmitidos)
	{
		this.ID = id;
		this.timestamp = timestamp;
		this.nombreArchivo = nombreArchivo;
		this.tamanioArchivo = tamanioArchivo;
		this.cliente = cliente;
		this.estadoExito = estadoExito;
		this.tiempoDeTransferencia = tiempoDeTransferencia;
		this.numeroDePaquetesEnviados = numeroDePaquetesEnviados;
		this.numeroDePaquetesTransmitidos = numeroDePaquetesTransmitidos;
		this.bytesTransmitidos = bytesTransmitidos;
	}
	
	public boolean imprimirLog()
	{
		try
		{
			String informacion = 
			"Fecha y Hora de transmision: " + timestamp.getTime() + "\n" +
			"Nombre del Archivo enviado: " + nombreArchivo + "\n" +
			"Tamanio del Archivo enviado (bytes): " + tamanioArchivo + "\n" +
			"Cliente: " + cliente + "\n" +
			"Estado de envio de Entrega: " + estadoExito + "\n" +
			"Tiempo de Transferencia, en milisegundos: " + tiempoDeTransferencia + "\n" +
			"Numero de paquetes enviados: " + numeroDePaquetesEnviados + "\n" +
			"Numero de paquetes transmitidos: " + numeroDePaquetesTransmitidos + "\n" +
			"Numero de bytes transmitidos: " + bytesTransmitidos;
			try
			{
				String time = (timestamp.toString()).replace(":", ".");
				FileWriter writer = new FileWriter(PATH_LOGS+"/"+ID +"."+time+".txt");
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
			System.err.println("Error durante la impresion del Log:" + e.getMessage());
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
	 * @return the tiempoDeTransferencia
	 */
	public Double getTiempoDeTransferencia() {
		return tiempoDeTransferencia;
	}
	/**
	 * @param tiempoDeTransferencia the tiempoDeTransferencia to set
	 */
	public void setTiempoDeTransferencia(Double tiempoDeTransferencia) {
		this.tiempoDeTransferencia = tiempoDeTransferencia;
	}
	/**
	 * @return the numeroDePaquetesRecibidos
	 */
	public Long getNumeroDePaquetesEnviados() {
		return numeroDePaquetesEnviados;
	}
	/**
	 * @param numeroDePaquetesRecibidos the numeroDePaquetesRecibidos to set
	 */
	public void setNumeroDePaquetesEnviados(Long numeroDePaquetesEnviados) {
		this.numeroDePaquetesEnviados = numeroDePaquetesEnviados;
	}

	/**
	 * @return the numeroDePaquetesTransmitidos
	 */
	public Long getNumeroDePaquetesTransmitidos() {
		return numeroDePaquetesTransmitidos;
	}
	/**
	 * @param numeroDePaquetesTransmitidos the numeroDePaquetesTransmitidos to set
	 */
	public void setNumeroDePaquetesTransmitidos(Long numeroDePaquetesTransmitidos) {
		this.numeroDePaquetesTransmitidos = numeroDePaquetesTransmitidos;
	}
}
