import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * 
 * @author Grupo 12 infracom
 * Modela el cliente dela conexión UDP
 */
public class ClienteUDPStream 
{
	public static final int CANAL1 = 1234;
	public static final int CANAL2 = 5678;
	public static final int CANAL3 = 7654;

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
		Video vd = new Video();
		vd.start();
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
	public Button canal1 = new Button("Ver canal 1");

	/**
	 * Modela el boton de enviar
	 */
	public Button canal2 = new Button("Ver canal 2");

	/**
	 * Modela el boton de enviar
	 */
	public Button subir = new Button("Subir video al servidor");

	/**
	 * Modela el boton de enviar
	 */
	public Button pausa = new Button("Pausar");

	/**
	 * Modela boolean que indica si el stream esta en pausa
	 */
	public boolean pausar = false;

	/**
	 * Modela boolean que indica si el stream esta en pausa
	 */
	public boolean upload = false;

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
	public static JPanel panelBotones = new JPanel(new GridLayout(2,2));

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
		framePrincipal.setSize(480, 600);
		framePrincipal.setTitle("Cliente stream UDP");
		framePrincipal.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		framePrincipal.setAlwaysOnTop(true);
		framePrincipal.setLayout(new BorderLayout());
		framePrincipal.setVisible(true);
		panelVideo.setSize(480, 400);
		panelVideo.add(labelVideo);
		panelVideo.add(panelBotones);
		framePrincipal.add(panelVideo);

		panelBotones.add(canal1);
		panelBotones.add(canal2);
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
					pausa.setLabel("Pausar");
				}
				else
				{
					pausar = true;
					pausa.setLabel("Resumir");
				}
			}
		});

		subir.addActionListener(new ActionListener() 
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					Socket canal = new Socket("localhost", ClienteUDPStream.CANAL3);

					PrintWriter outCliente = new PrintWriter(canal.getOutputStream( ), true);				
					
					//Seleccionar archivo
					boolean seguir = false;
					StringBuilder url = new StringBuilder();
					File myFile = new File("");
					while(!seguir)
					{
						JFileChooser jf = new JFileChooser("./data");
						jf.showOpenDialog(framePrincipal);
						
						myFile = jf.getSelectedFile();
						url = new StringBuilder(myFile.getName()); 
						System.out.println(url.toString());
						if(url.toString().endsWith("mp4") || url.toString().endsWith("webm") || url.toString().endsWith(".avi") || url.toString().endsWith("mov") || url.toString().endsWith("flv"))
						{
							seguir = true;
						}
						else
						{
							JOptionPane.showMessageDialog(framePrincipal, "Se admiten únicamente archivos de video con extenciones como mp4, webm, avi, mov o flv"
									, "Warning", JOptionPane.WARNING_MESSAGE);
						}
					}
					
					//Enviar nombre file al server
					outCliente.println(url.toString());
					
					//Enviar el archivo
					int count;
					byte[] buffer = new byte[1024];
					OutputStream outs = canal.getOutputStream();
					BufferedInputStream ins = new BufferedInputStream(new FileInputStream(myFile));
					while ((count = ins.read(buffer)) >= 0) 
					{
						outs.write(buffer, 0, count);
					}
					outs.flush();
					ins.close();
					canal.close();
				} 
				catch (IOException e1)
				{
					e1.printStackTrace();
				}

			}
		});

		canal1.addActionListener(new ActionListener() 
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					upload = false;
					ClienteUDPStream.datagram = new DatagramSocket();

					byte[] init = new byte[62000];
					init = "givedata".getBytes();

					InetAddress addr = InetAddress.getLocalHost();
					DatagramPacket dp = new DatagramPacket(init,init.length,addr,ClienteUDPStream.CANAL1);

					ClienteUDPStream.datagram.send(dp);

					DatagramPacket rcv = new DatagramPacket(init, init.length);

					ClienteUDPStream.datagram.receive(rcv);
				}		
				catch (IOException e1)
				{
					e1.printStackTrace();
				}	
			}
		});

		canal2.addActionListener(new ActionListener() 
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					upload = false;
					ClienteUDPStream.datagram = new DatagramSocket();

					byte[] init = new byte[62000];
					init = "givedata".getBytes();

					InetAddress addr = InetAddress.getLocalHost();
					DatagramPacket dp = new DatagramPacket(init,init.length,addr,ClienteUDPStream.CANAL2);

					ClienteUDPStream.datagram.send(dp);

					DatagramPacket rcv = new DatagramPacket(init, init.length);

					ClienteUDPStream.datagram.receive(rcv);
					System.out.println(new String(rcv.getData()));

					System.out.println(ClienteUDPStream.datagram.getPort());
				}		
				catch (IOException e1)
				{
					e1.printStackTrace();
				}	
			}
		});
	}

	@Override
	public void run() {

		try 
		{	
			do
			{
				if(ClienteUDPStream.datagram != null && !upload)
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

						Thread.sleep(10);
					}
					framePrincipal.revalidate();
					framePrincipal.repaint();
				}
			} 
			while (true);
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
