package source;

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
	/**
	 * Tiempo de transferencia del archivo.
	 */
	private Double tiempoDeTransferencia;
	/**
	 * Número de paquetes enviados.
	 */
	private long numeroDePaquetesEnviados;
	/**
	 * Número de paquetes recibidos.
	 */
	private Long numeroDePaquetesRecibidos;
	/**
	 * Número de paquetes transmitidos.
	 */
	private Long numeroDePaquetesTransmitidos;
	/**
	 * Número de bytes recibidos.
	 */
	private Long bytesRecibidos;
	/**
	 * Número de bytes transmitidos.
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
			long numeroDePaquetesEnviados,
			long numeroDePaquetesRecibidos, long numeroDePaquetesTransmitidos, 
			long bytesRecibidos, long bytesTransmitidos)
	{
		this.ID = id;
		this.timestamp = timestamp;
		this.nombreArchivo = nombreArchivo;
		this.tamanioArchivo = tamanioArchivo;
		this.cliente = cliente;
		this.estadoExito = estadoExito;
		this.tiempoDeTransferencia = tiempoDeTransferencia;
		this.numeroDePaquetesEnviados = numeroDePaquetesEnviados;
		this.numeroDePaquetesRecibidos = numeroDePaquetesRecibidos;
		this.numeroDePaquetesTransmitidos = numeroDePaquetesTransmitidos;
		this.bytesRecibidos = bytesRecibidos;
		this.bytesTransmitidos = bytesTransmitidos;
	}
	
	public boolean imprimirLog()
	{
		try
		{
			String informacion = 
			"Fecha y Hora de transmisión: " + timestamp.getTime() + "\n" +
			"Nombre del Archivo enviado: " + nombreArchivo + "\n" +
			"Tamaño del Archivo enviado: " + tamanioArchivo + "\n" +
			"Cliente: " + cliente + "\n" +
			"Estado de envío de Entrega: " + estadoExito + "\n" +
			"Tiempo de Transferencia, en milisegundos: " + tiempoDeTransferencia + "\n" +
			"Número de paquetes enviados: " + numeroDePaquetesEnviados + "\n" +
			"Número de paquetes recibidos: " + numeroDePaquetesRecibidos + "\n" +
			"Número de paquetes transmitidos: " + numeroDePaquetesTransmitidos + "\n" +
			"Número de bytes recibidos: " + bytesRecibidos + "\n" +
			"Número de bytes transmitidos: " + bytesTransmitidos;
			try
			{
				FileWriter writer = new FileWriter(Servidor.PATH_LOGS);
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
	 * @return the numeroDePaquetesRecibidos
	 */
	public Long getNumeroDePaquetesRecibidos() {
		return numeroDePaquetesRecibidos;
	}
	/**
	 * @param numeroDePaquetesRecibidos the numeroDePaquetesRecibidos to set
	 */
	public void setNumeroDePaquetesRecibidos(Long numeroDePaquetesRecibidos) {
		this.numeroDePaquetesRecibidos = numeroDePaquetesRecibidos;
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
