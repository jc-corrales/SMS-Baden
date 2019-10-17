import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
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
public class JavaServer {

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static InetAddress[] inet;
	public static int[] port;
	public static int i;
	static int count = 0;
	public static BufferedReader[] inFromClient;
	public static DataOutputStream[] outToClient;

	public static void main(String[] args) throws Exception
	{
		JavaServer jv = new JavaServer();
	}



	public JavaServer() throws Exception {


		NativeLibrary.addSearchPath("libvlc", "C:\\Program Files (x86)\\VideoLAN\\VLC");

		JavaServer.inet = new InetAddress[30];
		port = new int[30];

		// TODO code application logic here

		inFromClient = new BufferedReader[30];
		outToClient = new DataOutputStream[30];

		DatagramSocket serv = new DatagramSocket(4321);

		byte[] buf = new byte[62000];
		DatagramPacket dp = new DatagramPacket(buf, buf.length);

		Canvas_Demo canv = new Canvas_Demo();
		i = 0;

		SThread[] st = new SThread[30];


		while (true) {

			System.out.println(serv.getPort());
			serv.receive(dp);
			System.out.println(new String(dp.getData()));
			buf = "starts".getBytes();

			inet[i] = dp.getAddress();
			port[i] = dp.getPort();

			DatagramPacket dsend = new DatagramPacket(buf, buf.length, inet[i], port[i]);
			serv.send(dsend);

			Vidthread sendvid = new Vidthread(serv);

			System.out.println("waiting\n ");
			
			System.out.println("connected " + i);


			st[i] = new SThread(i);
			st[i].start();

			if(count == 0)
			{
				Sentencefromserver sen = new Sentencefromserver();
				sen.start();
				count++;
			}

			System.out.println(inet[i]);
			sendvid.start();

			i++;

			if (i == 30) {
				break;
			}
		}
	}
}

class Vidthread extends Thread {

	int clientno;
	JLabel jleb = new JLabel();

	DatagramSocket soc;

	Robot rb = new Robot();

	byte[] outbuff = new byte[62000];

	BufferedImage mybuf;
	ImageIcon img;
	Rectangle rc;

	int bord = Canvas_Demo.panel.getY() - Canvas_Demo.frame.getY();

	public Vidthread(DatagramSocket ds) throws Exception {
		soc = ds;

		System.out.println(soc.getPort());
	}

	public void run()
	{
		while (true) 
		{
			try 
			{

				int num = JavaServer.i;

				rc = new Rectangle(new Point(Canvas_Demo.frame.getX() + 8, Canvas_Demo.frame.getY() + 27),
						new Dimension(Canvas_Demo.panel.getWidth(), Canvas_Demo.frame.getHeight() / 2));

				// System.out.println("another frame sent ");

				mybuf = rb.createScreenCapture(rc);

				img = new ImageIcon(mybuf);

				jleb.setIcon(img);

				// jf.setVisible(true);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();

				ImageIO.write(mybuf, "jpg", baos);

				outbuff = baos.toByteArray();

				for (int j = 0; j < num; j++)
				{
					DatagramPacket dp = new DatagramPacket(outbuff, outbuff.length, JavaServer.inet[j],
							JavaServer.port[j]);
					soc.send(dp);
					baos.flush();
				}
				Thread.sleep(15);

			}
			catch (Exception e)
			{

			}
		}

	}

}

class Canvas_Demo {

	// Create a media player factory
	private MediaPlayerFactory mediaPlayerFactory;

	// Create a new media player instance for the run-time platform
	private EmbeddedMediaPlayer mediaPlayer;

	public static JPanel panel;
	public static JPanel myjp;
	private Canvas canvas;
	public static JFrame frame;
	public static JTextArea ta;
	public static JTextArea txinp;
	public static int xpos = 0, ypos = 0;
	String url = "D:\\DownLoads\\Video\\freerun.MP4";

	// Constructor
	public Canvas_Demo() {

		// Creating a panel that while contains the canvas
		panel = new JPanel();
		panel.setLayout(new BorderLayout());

		JPanel mypanel = new JPanel();
		mypanel.setLayout(new GridLayout(2, 1));

		canvas = new Canvas();
		canvas.setBackground(Color.BLACK);

		panel.add(canvas, BorderLayout.CENTER);

		// Creation a media player :
		mediaPlayerFactory = new MediaPlayerFactory();
		mediaPlayer = mediaPlayerFactory.newEmbeddedMediaPlayer();
		mediaPlayer.setPause(true);
		CanvasVideoSurface videoSurface = mediaPlayerFactory.newVideoSurface(canvas);
		mediaPlayer.setVideoSurface(videoSurface);

		// Construction of the jframe :
		frame = new JFrame("Server stream");
		// frame.setLayout(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocation(200, 0);
		frame.setSize(640, 960);
		frame.setAlwaysOnTop(true);

		// Adding the panel to the
		// panel.setSize(940, 480);
		// mypanel.setLayout(new BorderLayout());
		mypanel.add(panel);
		frame.add(mypanel);
		frame.setVisible(true);
		xpos = frame.getX();
		ypos = frame.getY();

		// Playing the video

		myjp = new JPanel(new GridLayout(4, 1));

		Button bn = new Button("File");
		myjp.add(bn);
		Button sender = new Button("test");

		JScrollPane jpane = new JScrollPane();
		jpane.setSize(300, 200);
		// ta.setEditable(false);
		ta = new JTextArea();
		txinp = new JTextArea();
		jpane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		jpane.add(ta);
		jpane.setViewportView(ta);
		myjp.add(jpane);
		myjp.add(txinp);
		myjp.add(sender);
		ta.setText("Initialized");

		ta.setCaretPosition(ta.getDocument().getLength());

		mypanel.add(myjp);
		mypanel.revalidate();
		mypanel.repaint();

		bn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				JFileChooser jf = new JFileChooser();
				jf.showOpenDialog(frame);
				File f;
				f = jf.getSelectedFile();
				url = f.getPath();
				System.out.println(url);
				ta.setText("check text\n");
				ta.append(url+"\n");
				mediaPlayer.playMedia(url);
				mediaPlayer.enableMarquee(true);
				mediaPlayer.enableLogo(true);
				mediaPlayer.setEnableMouseInputHandling(true);
			}
		});
		sender.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				Sentencefromserver.sendingSentence = txinp.getText();
				txinp.setText(null);
				Canvas_Demo.ta.append("From Myself: " + Sentencefromserver.sendingSentence + "\n");
				Canvas_Demo.myjp.revalidate();
				Canvas_Demo.myjp.repaint();
			}
		});

	}
}

class SThread extends Thread {

	public static String clientSentence;
	int srcid;
	BufferedReader inFromClient = JavaServer.inFromClient[srcid];
	DataOutputStream outToClient[] = JavaServer.outToClient;

	public SThread(int a) {
		srcid = a;
	}

	public void run() {
		while (true) {
			try {

				clientSentence = inFromClient.readLine();

				System.out.println("From Client " + srcid + ": " + clientSentence);
				Canvas_Demo.ta.append("From Client " + srcid + ": " + clientSentence + "\n");

				for(int i=0; i<JavaServer.i; i++){

					if(i!=srcid)
						outToClient[i].writeBytes("Client "+srcid+": "+clientSentence + '\n');	//'\n' is necessary
				}

				Canvas_Demo.myjp.revalidate();
				Canvas_Demo.myjp.repaint();

			} catch (Exception e) {
			}

		}
	}
}

class Sentencefromserver extends Thread {

	public static String sendingSentence;

	public Sentencefromserver() {

	}

	public void run() {

		while (true) {

			try {

				if(sendingSentence.length()>0)
				{
					for (int i = 0; i < JavaServer.i; i++) {
						JavaServer.outToClient[i].writeBytes("From Server: "+sendingSentence+'\n');

					}
					sendingSentence = null;
				}

			} catch (Exception e) {

			}
		}
	}
}
