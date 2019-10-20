import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;

import javax.swing.*;

import com.sun.jna.NativeLibrary;


import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.CanvasVideoSurface;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

/**
 * Clase que modela el servidor udp
 */
public class StreamingServer extends Thread
{
	private static final int CANAL1 = 1234;
	private static final int CANAL2 = 5678;
	private static final int CANAL3 = 7654;

	/**
	 * lista inet de usuarios
	 */
	public static InetAddress[] arregloInets;

	/**
	 * Lista puertos usuarios
	 */
	public static int[] puertos;

	/**
	 * num usuarios
	 */
	public static int numUsuarios;

	/**
	 *  Datagram socket server
	 */
	public DatagramSocket datagramServidor;

	/**
	 * Main del server
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception
	{
		boolean canal1 = false;
		boolean canal2 = false;
		boolean canal3 = false;
		Scanner sc= new Scanner(System.in);
		while(true)
		{
			System.out.println("Ingrese el canal a inicializar (1 o 2 para stream) o 3 para canal de transmisión de videos \n");
			int can = sc.nextInt();
			if(can == 1)
			{
				if(!canal1)
				{
					canal1 = true;
					System.out.println("Canal con puerto " + CANAL1 + " ha sido correctamemte inicializado");
					StreamingServer c = new StreamingServer(CANAL1);
					c.start();
				}
				else
				{
					System.out.println("Ya está inicializado este canal");
				}
			}
			else if (can == 2)
			{
				if(!canal2)
				{
					canal2 = true;
					System.out.println("Canal con puerto " + CANAL2 + " ha sido correctamemte inicializado");
					StreamingServer c = new StreamingServer(CANAL2);
					c.start();	
				}
				else
				{
					System.out.println("Ya está inicializado este canal");
				}
			}
			else if (can == 3)
			{
				if(!canal3)
				{
					canal3 = true;
					System.out.println("Canal con puerto " + CANAL3 + " ha sido correctamemte inicializado");
					getVideo();
					
				}
				else
				{
					System.out.println("Ya está inicializado este canal");
				}
			}
			else
			{
				sc.close();
				System.out.println("Solo hay 3 canales implementados");
			}
		}
	}

	/**
	 * Se obiene video del cliente
	 * @throws IOException
	 * @throws AWTException
	 */
	private static void getVideo() throws IOException, AWTException
	{
		int numT = 5;
		ExecutorService exec = Executors.newFixedThreadPool(numT);

		boolean status = true;
		try
		{
			ServerSocket puntoDeEntrada = new ServerSocket(CANAL3);
			while(status)
			{
				Socket cliente = puntoDeEntrada.accept();
				ConexionEnvioVideos con = new ConexionEnvioVideos(cliente);
				exec.execute(con);
			}
			puntoDeEntrada.close();
		}
		catch (Exception e)
		{
			System.err.println(e.getMessage());
		}
	}

	/**
	 * Constructor server
	 * @throws IOException 
	 * @throws AWTException 
	 * @throws Exception
	 */
	public StreamingServer(int puerto) throws IOException, AWTException
	{
		NativeLibrary.addSearchPath("libvlc", "C:\\Program Files (x86)\\VideoLAN\\VLC");
		StreamingServer.arregloInets = new InetAddress[25];
		puertos = new int[25];
		DatagramSocket serv = new DatagramSocket(puerto);
		byte[] buf = new byte[62000];
		DatagramPacket dp = new DatagramPacket(buf, buf.length);
		new Interfaz(puerto);
		numUsuarios = 0;
		while (true) 
		{
			serv.receive(dp);
			buf = "starts".getBytes();
			arregloInets[numUsuarios] = dp.getAddress();
			puertos[numUsuarios] = dp.getPort();
			DatagramPacket dsend = new DatagramPacket(buf, buf.length, arregloInets[numUsuarios], puertos[numUsuarios]);
			serv.send(dsend);
			VideoStream sendvid = new VideoStream(serv);
			sendvid.start();
			numUsuarios++;
		}
	}
}

/**
 * Se encarga del envio de paquetes para stream
 */
class VideoStream extends Thread
{
	private JLabel label = new JLabel();
	private DatagramSocket datSock;
	private Robot rb = new Robot();
	private byte[] outbuff = new byte[62000];
	private BufferedImage mybuf;
	private ImageIcon img;
	private Rectangle rc;

	/**
	 * Constructor vid
	 * @param datagram socket
	 * @throws Exception
	 */
	public VideoStream(DatagramSocket datSocket) throws IOException, AWTException
	{
		datSock = datSocket;
	}

	/**
	 * Run del vid
	 */
	public void run()
	{
		while (true) 
		{
			try 
			{
				int num = StreamingServer.numUsuarios;
				rc = new Rectangle(new Point(Interfaz.frame.getX(), Interfaz.frame.getY()), new Dimension(Interfaz.panel.getWidth(), Interfaz.frame.getHeight()));
				mybuf = rb.createScreenCapture(rc);
				img = new ImageIcon(mybuf);
				label.setIcon(img);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(mybuf, "jpg", baos);
				outbuff = baos.toByteArray();
				for (int j = 0; j < num; j++)
				{
					DatagramPacket dp = new DatagramPacket(outbuff, outbuff.length, StreamingServer.arregloInets[j], StreamingServer.puertos[j]);
					datSock.send(dp);
					baos.flush();
				}
				Thread.sleep(2);
			}
			catch (IOException | InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}
}

/**
 * Interfaz server de canales
 */
class Interfaz
{

	private MediaPlayerFactory mediaPlayerFactory;
	private EmbeddedMediaPlayer mediaPlayer;

	public static JPanel panel;
	private Canvas canvas;
	public static JFrame frame;
	public static int xpos = 0, ypos = 0;
	private String url;

	// Constructor
	public Interfaz(int canal) 
	{
		panel = new JPanel();
		panel.setLayout(new BorderLayout());

		JPanel mypanel = new JPanel();
		mypanel.setLayout(new GridLayout(1, 1));

		canvas = new Canvas();
		canvas.setBackground(Color.BLACK);

		panel.add(canvas, BorderLayout.CENTER);

		mediaPlayerFactory = new MediaPlayerFactory();
		mediaPlayer = mediaPlayerFactory.newEmbeddedMediaPlayer();
		CanvasVideoSurface videoSurface = mediaPlayerFactory.newVideoSurface(canvas);
		mediaPlayer.setVideoSurface(videoSurface);

		frame = new JFrame("Server stream"+canal);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocation(200, 0);
		frame.setSize(480, 400);
		frame.setAlwaysOnTop(true);

		mypanel.add(panel);
		frame.add(mypanel);
		frame.setVisible(true);
		xpos = frame.getX();
		ypos = frame.getY();

		Button bn = new Button("Elegir video");
		frame.add(bn, BorderLayout.SOUTH);

		mypanel.revalidate();
		mypanel.repaint();

		bn.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				JFileChooser jf = new JFileChooser("./data");
				jf.showOpenDialog(frame);
				File f;
				f = jf.getSelectedFile();
				url = f.getPath();
				System.out.println(url);
				mediaPlayer.playMedia(url);
			}
		});
	}
}