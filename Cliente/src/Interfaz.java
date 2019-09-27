import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

public class Interfaz extends JFrame implements ActionListener {

	private Main mundo;
	private JLabel estado2;
	private JTextField numero;
	private JButton descargar;
	private JButton probar;
	private JButton conectar;
	private JList<String> lista;
	public Interfaz()
	{
		mundo = new Main();
		
		setSize(600, 700);
		setLayout(new BorderLayout());
		setTitle("Interfaz cliente");
	    setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		JPanel estado = new JPanel();
		JLabel estado1 = new JLabel("Estado:");
		estado2 = new JLabel();
		estado.setLayout(new GridLayout(1, 4));
		conectar = new JButton("Conectar");
		conectar.setActionCommand("CONECTAR");
		conectar.addActionListener(this);
		estado.add(conectar);
		estado.add(new JPanel());
		estado.add(new JPanel());
		estado.add(estado1);
		estado.add(estado2);

		add(estado, BorderLayout.NORTH);
		
		JPanel archivos = new JPanel();
		archivos.setLayout(new BorderLayout());
		archivos.setBorder( new TitledBorder( "Archivos" ) );
		lista = new JList<String>(mundo.archivos);
		lista.setPreferredSize(new Dimension(350, 150));
		archivos.setPreferredSize(new Dimension(560, 200));
		archivos.add(lista, BorderLayout.WEST);
		JPanel clientes = new JPanel();
		clientes.setLayout(new GridLayout(2,1));
		JLabel info = new JLabel("Numero de clientes \n para la prueba:");
		numero = new JTextField();
		clientes.add(info);
		clientes.add(numero);
		archivos.add(clientes, BorderLayout.EAST);
		
		JPanel opciones = new JPanel();
		opciones.setLayout(new GridLayout(1,2));
		descargar = new JButton("Descargar");
		descargar.setActionCommand("DESCARGAR");
		descargar.addActionListener(this);
		probar = new JButton("Probar");
		probar.setActionCommand("PROBAR");
		probar.addActionListener(this);
		opciones.add(descargar);
		opciones.add(probar);
		archivos.add(opciones, BorderLayout.SOUTH);
		
		add(archivos, BorderLayout.CENTER);
		
		JPanel log = new JPanel();
		log.setBorder( new TitledBorder( "Logs" ) );
		
        JTextArea textArea = new JTextArea();
        textArea.setPreferredSize(new Dimension(560, 400));
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        log.add(scrollPane);
    	add(log, BorderLayout.SOUTH);
		
    	actualizar();
	}
	private void actualizar() {
		if(mundo.estado)
		{
			estado2.setText("Conectado");
			estado2.setForeground(Color.green);
		}
		else
		{
			estado2.setText("Desconectado");
			estado2.setForeground(Color.red);
		}

		lista.setListData(mundo.archivos);
		
	}
	public static void main(String[] args) {
		
		Interfaz interfaz = new Interfaz();
		interfaz.setVisible(true);
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		String accion = e.getActionCommand();
		if(accion.equals("DESCARGAR"))
		{
			try {
				mundo.inicioSimple(lista.getSelectedValue());
			} catch (IOException e1) {

			}
		}
		else if(accion.equals("CONECTAR"))
		{
			try {
				mundo.establecerConexion();
				actualizar();
			} catch (IOException e1) {

			}
		}
		else if(accion.equals("PROBAR"))
		{
			try {
				mundo.inicioMultiple(lista.getSelectedValue(), numero.getText());;
				actualizar();
			} catch (IOException e1) {

			}
		}
	}

}
