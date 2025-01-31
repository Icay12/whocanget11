package miniCAD;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;


class shape {
	// type
	static final int _line = 0;
	static final int _circle = 1;
	static final int _rectangle = 2;
	static final int _text = 3;
	static final int _image = 4;

	int type = 0; // default line

	int st_x = 0, st_y = 0, ed_x = 0, ed_y = 0; // position
	Image image;
	String text = null;
	static double threshold=10;
	public shape(){}
	
	public shape(shape that){
		this.type = that.type;
		this.st_x = that.st_x;
		this.st_y = that.st_y;
		this.ed_x = that.ed_x;
		this.ed_y = that.ed_y;
		this.text = that.text;
		this.image = that.image;
	}

	public shape pick(Point p){
		switch(type){
			case _circle:
			{
				if(p.x>=(st_x-Math.abs(ed_y-st_y)-threshold)
						&&p.x<=(st_x-Math.abs(ed_y-st_y)+Math.abs(ed_y-st_y)*2+threshold)
						&&p.y>=(st_y-Math.abs(ed_y-st_y)-threshold)
						&&p.y<=(st_y-Math.abs(ed_y-st_y)+Math.abs(ed_y-st_y)*2+threshold)){
					double distance = Point.distance(st_x, st_y, p.x, p.y);
					double radius = Math.abs(ed_y-st_y);
					if(Math.abs(distance-radius) < threshold)
						return this;
					else return null;
				}
				else{
					return null;
				}
			}
			case _rectangle:
			{
				if(((Math.abs(p.x-Math.min(ed_x, st_x))<=threshold
						   ||Math.abs(p.x-Math.max(ed_x, st_x))<=threshold)
						   &&(p.y>=Math.min(ed_y, st_y)-threshold&&
							  p.y<=Math.max(ed_y, st_y)+threshold))
					       ||(
						   (Math.abs(p.y-Math.min(ed_y, st_y))<=threshold
						   ||Math.abs(p.y-Math.max(ed_y, st_y))<=threshold)
						   &&(p.x>=Math.min(ed_x, st_x)-threshold&&
							  p.x<=Math.max(ed_x, st_x)+threshold))){
							return this;
						}
						else{
							return null;
						}
			}
			case _line:
			{
				if(p.x>=Math.min(ed_x, st_x)
						&&p.x<=Math.max(ed_x, st_x)
						&&p.y>=Math.min(ed_y, st_y)
						&&p.y<=Math.max(ed_y, st_y)){
					int delta_y=(ed_y-st_y);
					int delta_x=(ed_x-st_x);
					double k=(double)delta_y/(double)delta_x;
					double c=ed_y-k*ed_x;
					double denominator=Math.sqrt(1+k*k);
					double numerator=Math.abs(k*p.x+c-p.y);
					if(numerator/denominator<=threshold){
						return this;
					}
					else{
						return null;
					}
				}
				else{
					return null;
				}
			}
			case _text:
			{
				if((p.x>=Math.min(ed_x, st_x)-threshold)
						 &&(p.x<=Math.max(ed_x, st_x)+threshold)
						 &&(p.y>=Math.min(ed_y, st_y)-threshold)
						 &&(p.y<=Math.max(ed_y, st_y)+threshold)){
							return this;
						}
						else{
							return null;
						}
			}
			default:
				return null;
		}
	}
}

public class miniCAD extends JFrame implements MouseListener, MouseMotionListener, KeyListener{
	
	static int _width = 800;
	static int _height = 600;
	static shape cur = new shape();
	static int flag = 0;
	static ArrayList<shape> drawlist = new ArrayList<shape>();
	static BufferedImage bi = new BufferedImage(_width, _height, BufferedImage.TYPE_3BYTE_BGR);
	JMenuBar jmenubar = new JMenuBar();
	private boolean haveClicked = false;
	private Point curpos;
	private int SelectNum;
	private shape needtomove;
	public miniCAD(){		 
		Graphics g = bi.getGraphics();
		g.setColor(Color.white);
		g.fillRect(0, 0, _width, _height);
		
		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
		
		setJMenuBar(jmenubar);
		
		JMenu jm_file = new JMenu("File");
		JMenu jm_tools = new JMenu("Tools");

		jmenubar.add(jm_file);
		jmenubar.add(jm_tools);

		
		JMenuItem jm_file_open = new JMenuItem("Open");
		JMenuItem jm_file_save = new JMenuItem("Save");
		JMenuItem jm_file_close = new JMenuItem("Close");
		jm_file.add(jm_file_open);
		jm_file.add(jm_file_save);
		jm_file.addSeparator();
		jm_file.add(jm_file_close);
		
		ButtonGroup bg_tools = new ButtonGroup();
		JRadioButtonMenuItem jm_tools_line = new JRadioButtonMenuItem("Line");
		JRadioButtonMenuItem jm_tools_circle = new JRadioButtonMenuItem("Circle");
		JRadioButtonMenuItem jm_tools_rect = new JRadioButtonMenuItem("Rectangle");
		JRadioButtonMenuItem jm_tools_text = new JRadioButtonMenuItem("Text");
		JRadioButtonMenuItem jm_tools_move = new JRadioButtonMenuItem("Move");
		jm_tools.add(jm_tools_line);	bg_tools.add(jm_tools_line);
		jm_tools.add(jm_tools_circle);	bg_tools.add(jm_tools_circle);
		jm_tools.add(jm_tools_rect);	bg_tools.add(jm_tools_rect);
		jm_tools.add(jm_tools_text);	bg_tools.add(jm_tools_text);
		jm_tools.addSeparator();
		jm_tools.add(jm_tools_move);	bg_tools.add(jm_tools_move);
				
		this.setSize(_width, _height);
		this.setLocationRelativeTo(null);
		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// define action
		
		
		jm_file_open.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				File curimagefile;
				Image curimage;
				
				JFileChooser jfc = new JFileChooser();
				jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                int ret = jfc.showOpenDialog(jfc);
				if (ret != JFileChooser.APPROVE_OPTION) return;
				curimagefile = jfc.getSelectedFile();
				curimage = Toolkit.getDefaultToolkit().getImage(curimagefile.toString());
				int tmp_type = cur.type;
				cur.type = shape._image;
				cur.image = curimage;
				drawlist.add(new shape(cur));
				cur.type = tmp_type;
				haveClicked = false;
				drawit(curimage);
			}
		});
		jm_file_save.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				File curimagefile;
				JFileChooser jfc = new JFileChooser();
				int ret = jfc.showSaveDialog(jfc);
				if (ret != JFileChooser.APPROVE_OPTION) return;
				curimagefile = jfc.getSelectedFile();
				
				Graphics g = bi.getGraphics();
				Iterator<shape> it = drawlist.iterator();
				while (it.hasNext()) draw(g,it.next());
				
				try{
					ImageIO.write(bi, "jpg", curimagefile);
				} catch (IOException ex){
					ex.printStackTrace();
				}
			}
		});
		jm_file_close.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				System.exit(0);
			}
		});
		jm_tools_line.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				flag = 0;
				cur.type = shape._line;
				haveClicked = false;
			}
		});
		jm_tools_circle.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				flag = 0;
				cur.type = shape._circle;
				haveClicked = false;
			}
		});
		jm_tools_rect.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				flag = 0;
				cur.type = shape._rectangle;
				haveClicked = false;
			}
		});
		jm_tools_text.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				flag = 0;
				cur.type = shape._text;
				cur.text = JOptionPane.showInputDialog("Please input the text:");
				haveClicked = false;
			}
		});
		jm_tools_move.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				flag = 1;
				haveClicked = false;
			}
		});
	}
	
	public void drawit(Image ima){
		Graphics g = bi.getGraphics();
		g.drawImage(ima, 0, 0, _width, _height, this);
	}
	public shape getshape(){
		int i=0;
		for(shape ishape: drawlist){
			if(ishape.pick(curpos) != null){
				drawlist.remove(i);
				return ishape;
			}
			++i;
		}
		
		return null;
	}

	public void draw(Graphics g,shape s){

		switch (s.type){
		case shape._line : g.drawLine(s.st_x, s.st_y, s.ed_x, s.ed_y); break;
		case shape._circle : g.drawOval(s.st_x, s.st_y, Math.abs(s.ed_x-s.st_x), Math.abs(s.ed_y-s.st_y)); break;
		case shape._rectangle : g.drawRect(s.st_x, s.st_y, s.ed_x-s.st_x, s.ed_y-s.st_y); break;
		case shape._text : 	g.setFont(new Font("Courier",Font.BOLD,30));
							g.drawString(s.text, s.st_x, s.st_y);
							break;
		case shape._image : g.drawImage(s.image, 0, 0, _width, _height, this);
		}
	}
	public static void main(String args[]){
		new miniCAD();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		//not in move mode
		if(flag==0 && cur.type!=shape._text){
			if(!haveClicked){
				cur.st_x = e.getX();
				cur.st_y = e.getY();
				haveClicked = true;
			}
			else{
				cur.ed_x = e.getX();
				cur.ed_y = e.getY();	
				drawlist.add(new shape(cur));
				haveClicked = false;
			}
		}

		repaint();
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if(flag==0 && cur.type == shape._text){
			//Scanner in = new Scanner(System.in);
			//String theText = in.nextLine();
			//cur.text = theText;
			cur.st_x = e.getX();
			cur.st_y = e.getY();		
		}
		if(flag == 1){
			cur.st_x = e.getX();
			cur.st_y = e.getY();
			curpos = e.getPoint();
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		if(flag==0 && cur.type == shape._text){
			cur.ed_x = e.getX();
			cur.ed_y = e.getY();
			drawlist.add(new shape(cur));
			
		}
		else if (flag==1){
			cur.ed_x = e.getX();
			cur.ed_y = e.getY();
			if (!drawlist.isEmpty()){
				needtomove = getshape();
				if(needtomove!=null){
				Graphics g = getGraphics();
				
				g.setColor(Color.white);
				switch (needtomove.type){
				case shape._line : g.drawLine(needtomove.st_x, needtomove.st_y, needtomove.ed_x, needtomove.ed_y); break;
				case shape._circle : g.drawOval(needtomove.st_x, needtomove.st_y, Math.abs(needtomove.ed_x-needtomove.st_x), Math.abs(needtomove.ed_y-needtomove.st_y)); break;
				case shape._rectangle : g.drawRect(needtomove.st_x, needtomove.st_y, needtomove.ed_x-needtomove.st_x, needtomove.ed_y-needtomove.st_y); break;
				case shape._text : 	g.setFont(new Font("Courier",Font.BOLD,30));
									g.drawString(needtomove.text, needtomove.st_x, needtomove.st_y);
									break;
				case shape._image : g.drawImage(needtomove.image, 0, 0, _width, _height, this);
				}
				
				needtomove.st_x += cur.ed_x - cur.st_x;
				needtomove.st_y += cur.ed_y - cur.st_y;
				needtomove.ed_x += cur.ed_x - cur.st_x;
				needtomove.ed_y += cur.ed_y - cur.st_y;
				//drawlist.remove(getnum());
				drawlist.add(needtomove);
				}
			}
		}		
		repaint();
	}

	public void paint(Graphics g){
		Iterator<shape> it = drawlist.iterator();
		while (it.hasNext()) draw(g,it.next());
		this.setJMenuBar(jmenubar);
		this.setSize(_width, _height);
		this.setLocationRelativeTo(null);
		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if(flag == 1){
			if (!drawlist.isEmpty()){
			if(e.getKeyCode() == KeyEvent.VK_DELETE){
				//getshape();
				needtomove = drawlist.remove(drawlist.size()-1);
				Graphics g = getGraphics();
				g.setColor(Color.white);
				switch (needtomove.type){
				case shape._line : g.drawLine(needtomove.st_x, needtomove.st_y, needtomove.ed_x, needtomove.ed_y); break;
				case shape._circle : g.drawOval(needtomove.st_x, needtomove.st_y, Math.abs(needtomove.ed_x-needtomove.st_x), Math.abs(needtomove.ed_y-needtomove.st_y)); break;
				case shape._rectangle : g.drawRect(needtomove.st_x, needtomove.st_y, needtomove.ed_x-needtomove.st_x, needtomove.ed_y-needtomove.st_y); break;
				case shape._text : 	g.setFont(new Font("Courier",Font.BOLD,30));
									g.drawString(needtomove.text, needtomove.st_x, needtomove.st_y);
									break;
				case shape._image : g.drawImage(needtomove.image, 0, 0, _width, _height, this);
				}
			}
			}
		}
		repaint();		
	}

	@Override
	public void keyTyped(KeyEvent e) {

	}
	
}