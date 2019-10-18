import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

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
	public DatagramSocket serv;

	/**
	 * Main del server
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception
	{
		Scanner sc= new Scanner(System.in);
		System.out.println("Ingrese el canal a inicializar (1 o 2)");
		int can = sc.nextInt();
		if(can == 1)
		{
			sc.close();
			StreamingServer c = new StreamingServer(CANAL1);
			c.start();
		}
		else if (can == 2)
		{
			sc.close();
			StreamingServer c = new StreamingServer(CANAL2);
			c.start();
		}
		else
		{
			sc.close();
			System.out.println("Solo 2 hay 2 canales implementados");
		}
	    
	}

	/**
	 * Constructor server
	 * @throws Exception
	 */
	public StreamingServer(int puerto)  throws Exception
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

			System.out.println(serv.getPort());
			serv.receive(dp);
			System.out.println(new String(dp.getData()));
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

				rc = new Rectangle(new Point(Interfaz.frame.getX() + 8, Interfaz.frame.getY() + 27), new Dimension(Interfaz.panel.getWidth(), Interfaz.frame.getHeight() / 2));

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
				Thread.sleep(10);
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
	private static JPanel myjp;
	private Canvas canvas;
	public static JFrame frame;
	public static int xpos = 0, ypos = 0;
	private String url;


	// Constructor
	public Interfaz(int canal) 
	{

		// Creating a panel that while contains the canvas
		panel = new JPanel();
		panel.setLayout(new BorderLayout());

		JPanel mypanel = new JPanel();
		mypanel.setLayout(new GridLayout(2, 1));

		canvas = new Canvas();
		canvas.setBackground(Color.BLACK);

		panel.add(canvas, BorderLayout.CENTER);

		mediaPlayerFactory = new MediaPlayerFactory();
		mediaPlayer = mediaPlayerFactory.newEmbeddedMediaPlayer();
		mediaPlayer.setPause(true);
		CanvasVideoSurface videoSurface = mediaPlayerFactory.newVideoSurface(canvas);
		mediaPlayer.setVideoSurface(videoSurface);

		frame = new JFrame("Server stream"+canal);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocation(200, 0);
		frame.setSize(640, 700);
		frame.setAlwaysOnTop(true);

		mypanel.add(panel);
		frame.add(mypanel);
		frame.setVisible(true);
		xpos = frame.getX();
		ypos = frame.getY();

		myjp = new JPanel(new GridLayout(1, 1));

		Button bn = new Button("Elegir video");
		myjp.add(bn);

		mypanel.add(myjp);
		mypanel.revalidate();
		mypanel.repaint();

		bn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser jf = new JFileChooser();
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