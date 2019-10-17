import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * 
 * @author Grupo 12 infracom
 * Modela el cliente dela conexión UDP
 */
public class ClienteUDPStream 
{
	/**
	 * Modela el datagram de la conexión UDP
	 */
	public static DatagramSocket datagram;

	/**
	 * Main del cliente
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args)
	{
		try 
		{
			datagram = new DatagramSocket();

			byte[] init = new byte[62000];
			init = "givedata".getBytes();

			InetAddress addr = InetAddress.getLocalHost();
			DatagramPacket dp = new DatagramPacket(init,init.length,addr,4321);

			datagram.send(dp);

			DatagramPacket rcv = new DatagramPacket(init, init.length);

			datagram.receive(rcv);
			System.out.println(new String(rcv.getData()));

			System.out.println(datagram.getPort());
			Video vd = new Video();
			vd.start();
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}		
	}
}

/**
 * Clase que se encarga de la visualización del video
 */
class Video extends Thread 
{
	/**
	 * Modela el boton de enviar
	 */
	public Button subir = new Button("Subir video");

	/**
	 * Modela el boton de enviar
	 */
	public Button pausa = new Button("Pausar");

	/**
	 * Modela boolean que indica si el stream esta en pausa
	 */
	public boolean pausar = false;
	
	/**
	 * Modela el frame principal
	 */
	public JFrame framePrincipal = new JFrame();
	
	/**
	 * Modela el panel de video
	 */
	public static JPanel panelVideo = new JPanel(new GridLayout(2,1));
	
	/**
	 * Modela el panel de botones
	 */
	public static JPanel panelBotones = new JPanel(new GridLayout(3,1));
	
	/**
	 * Modela label donde se muestra el stream
	 */
	public JLabel labelVideo = new JLabel();

	/**
	 * Buffer bytes
	 */
	public byte[] bufBytes = new byte[62000];

	/**
	 * Modela el datagram packet que se va a recibir
	 */
	public DatagramPacket dp = new DatagramPacket(bufBytes, bufBytes.length);
	
	/**
	 * Buff de imagen que se va a poner
	 */
	public BufferedImage imgPoner;
	
	/**
	 * Buff de imagen recivida
	 */
	public BufferedImage imgActual;
	
	/**
	 * Img icon para poner al label de video
	 */
	public ImageIcon imc;

	/**
	 * Constructor interfaz video
	 */
	public Video() 
	{
		framePrincipal.setSize(640, 960);
		framePrincipal.setTitle("Vista cliente");
		framePrincipal.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		framePrincipal.setAlwaysOnTop(true);
		framePrincipal.setLayout(new BorderLayout());
		framePrincipal.setVisible(true);
		panelVideo.add(labelVideo);
		panelVideo.add(panelBotones);
		framePrincipal.add(panelVideo);

		panelBotones.add(pausa);
		panelBotones.add(subir);
		pausa.addActionListener(new ActionListener() 
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(pausar)
				{
					pausar = false;
				}
				else
				{
					pausar = true;
				}
			}
		});

		subir.addActionListener(new ActionListener() 
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				subirVideo();
			}

			private void subirVideo() 
			{


			}
		});
	}

	@Override
	public void run() {

		try 
		{	
			do
			{
				ClienteUDPStream.datagram.receive(dp);
				ByteArrayInputStream bais = new ByteArrayInputStream(bufBytes);

				imgActual = ImageIO.read(bais);
				
				if(!pausar)
				{
					imgPoner = imgActual;
				}
				if (imgPoner != null)
				{
					imc = new ImageIcon(imgPoner);
					labelVideo.setIcon(imc);

					Thread.sleep(15);
				}
				framePrincipal.revalidate();
				framePrincipal.repaint();

			} 
			while (true);
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
