import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class P2p extends JFrame {
    public P2p() {
        setTitle("");
        setLayout(null);
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel client = new Client(); //panel类
        JPanel server = new Server(); //panel类
        server.setBounds(10, 10, 250, 250);
        client.setBounds(250, 10, 250, 250);
        add(client);
        add(server);
    }

    public static void main(String[] args) {
        try{
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        }//使按钮看起来更好看，像windows窗口
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        catch (InstantiationException e) {
            e.printStackTrace();
        }
        catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        new P2p().setVisible(true);
    }
}

class Client extends JPanel implements ActionListener {
    JFileChooser jfc;
    String ip;
    //	Label lb1 = new Label("请输入主机ip地址：");
    Timer timer;
    JProgressBar jpb;
    JButton jb1;
    JButton jb2;
    JButton jb3;
    JTextField jtf1;
    JTextField jtf2;

    public Client() {

        // 进度条
        timer = new Timer(50, this);
        jpb = new JProgressBar();
        jpb.setOrientation(JProgressBar.HORIZONTAL);
        jpb.setMinimum(0);
        jpb.setMaximum(100);
        jpb.setValue(0);
        jpb.setStringPainted(true);//接收时的显示标志

        this.setLayout(null);
        jfc = new JFileChooser();

        jb1 = new JButton("确定");
        jb2 = new JButton("请选择文件存储位置");
        jb3 = new JButton("接收");
        jtf1 = new JTextField("127.0.0.1");
        jtf2 = new JTextField("");
        jtf1.setBounds(10, 10, 100, 25);
        jb1.setBounds(120, 10, 80, 25);
        jtf2.setBounds(10, 50, 200, 45);
        jb2.setBounds(10, 110, 160, 25);
        jb3.setBounds(10, 140, 80, 25);
        jpb.setBounds(10, 190, 200, 20);
        this.add(jb1);
        this.add(jb2);
        this.add(jb3);
        this.add(jtf1);
        this.add(jtf2);
        this.add(jpb);


        jb1.addActionListener(this);
        jb2.addActionListener(this);
        jb3.addActionListener(this);

        new s_server().start();//由线程启动

    }

    public void actionPerformed(ActionEvent e) {
        int result;
        if (e.getSource() == timer) {
            int value = jpb.getValue();
            jpb.setValue(0);
            if (value < 100) {
                value++;
                jpb.setValue(value);
            } else {
                timer.stop();
                jb2.setText("接收");
            }
        }

        else if ((JButton) e.getSource() == jb2) {
            result = jfc.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = jfc.getSelectedFile();
                jb2.setText(file.getPath());//显示文件路径
            }

        }

        else if ((JButton) e.getSource() == jb1) {
            ip = new String(jtf1.getText());

        }

        else if ((JButton) e.getSource() == jb3) {
            try {
                // 使用本地文件系统接受网络数据并存为新文件
                File file = new File(jb2.getText());
                file.createNewFile();
                RandomAccessFile raf = new RandomAccessFile(file, "rw");
                // 通过Socket连接文件服务器
                Socket server = new Socket(ip, 3108);
                // 创建网络接受流接受服务器文件数据

                InputStream netIn = server.getInputStream();

                InputStream in = new DataInputStream(new BufferedInputStream(
                        netIn));//封装输入流

                // 创建缓冲区缓冲网络数据

                byte[] buf = new byte[20480];
                timer.start();

                int num = in.read(buf);

                while (num != (-1)) {// 是否读完所有数据

                    raf.write(buf, 0, num);// 将数据写往文件

                    raf.skipBytes(num);// 顺序写文件字节

                    num = in.read(buf);// 继续从网络中读取文件

                }

                in.close();
                raf.close();
            }

            catch (IOException q) {
                System.out.println("异常");

            }
        }
    }

    class s_server extends Thread {
        String message;
        float a, b, c;
        int i;

        public void run() {

            while (true) {//循环接收UDP包
                try {
                    byte[]bytes = new byte[1024];
                    DatagramSocket ds = new DatagramSocket(3019);//端口
                    DatagramPacket dp = new DatagramPacket(bytes,1024);//包大小
                    ds.receive(dp);
                    message = new String(bytes,0,bytes.length);
                    message = message.trim();

                    jtf2.setText("文件" + message + "待接收，请设置IP:"+dp.getAddress()+"接收！");
                    ds.close();
                } catch (IOException e) {
                    System.out.println(e);
                }
            }
        }
    }


}

class Server extends JPanel implements ActionListener {
    JButton jb0;
    JButton jb1;
    JFileChooser jfc;//就是一个文件选择窗口
    JTextField jtf;
    String st, st1;
    JProgressBar jpb;
    Timer timer;
    Socket s;

    public Server() {
        this.setLayout(null);
        setSize(300, 300);
        // 进度条
        jpb = new JProgressBar();
        jpb.setOrientation(JProgressBar.HORIZONTAL);
        jpb.setMinimum(0);
        jpb.setMaximum(100);
        jpb.setValue(0);
        jpb.setStringPainted(true);
        timer = new Timer(50, this); // 时间间隔

        jfc = new JFileChooser();
        jtf = new JTextField("请选择待共享的文件");
        jb0 = new JButton("浏览");
        jb1 = new JButton("共享");
        jtf.setBounds(10, 10, 200, 30);
        jb0.setBounds(10, 50, 100, 30);
        jb1.setBounds(10, 90, 100, 30);
        jpb.setBounds(10, 190, 200, 20);
        this.add(jpb);
        this.add(jtf);
        this.add(jb0);
        this.add(jb1);

        jb0.addActionListener(this);
        jb1.addActionListener(this);

    }

    public void actionPerformed(ActionEvent e) {
        int result;
        if (e.getSource() == timer) {
            int value = jpb.getValue();
            jpb.setValue(0);
            if (value < 100) {
                value++;
                jpb.setValue(value);
            } else {
                timer.stop();
                jb0.setText("浏览");
            }
        }
        else if ((JButton) e.getSource() == jb0) {
            jfc.setApproveButtonText("确定");
            jfc.setDialogTitle("选择文件窗口");
            result = jfc.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) // 当用户按下确定
            {
                st = new String(jfc.getSelectedFile().getPath());
                jb0.setText(st);
                st1 = new String(jfc.getSelectedFile().getName());
                jtf.setText("文件" + st1 + "待发送！");

                try {
                    DatagramSocket ds = new DatagramSocket();
                    DatagramPacket dp = new DatagramPacket(jfc.getSelectedFile().getName().getBytes(),jfc.getSelectedFile().getName().getBytes().length,InetAddress.getByName("255.255.255.255"),3019);
                    ds.send(dp);
                    ds.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
        }

        else if ((JButton) e.getSource() == jb1) {
            jb1.setEnabled(false);
            new Thread(){

                @Override
                public void run() {
                    // 创建文件流用来读取文件中的数据
                    try {
                        File file = new File(jb0.getText());

                        FileInputStream fos = new FileInputStream(file);

                        // 创建网络服务器接受客户请求

                        ServerSocket ss = new ServerSocket(3108);

                        Socket client = ss.accept();

                        timer.start();

                        // 创建网络输出流并提供数据包装器

                        OutputStream netOut = client.getOutputStream();

                        OutputStream doc = new DataOutputStream(
                                new BufferedOutputStream(netOut));

                        // 创建文件读取缓冲区

                        byte[] buf = new byte[20480];

                        int num = fos.read(buf);// 读文件

                        while (num != (-1)) {// 是否读完文件

                            doc.write(buf, 0, num);// 把文件数据写出网络缓冲区

                            doc.flush();// 刷新缓冲区把数据写往客户端

                            num = fos.read(buf);// 继续从文件中读取数据

                        }
                        fos.close();

                        doc.close();
                        jb1.setEnabled(true);
                    } catch (FileNotFoundException e1) {
                        JOptionPane.showMessageDialog(null, "请选择共享文件", "错误",
                                JOptionPane.ERROR_MESSAGE);
                        jb1.setEnabled(true);
                    } catch (IOException e1) {
                        JOptionPane.showMessageDialog(null, "IO异常", "错误",
                                JOptionPane.ERROR_MESSAGE);
                        jb1.setEnabled(true);
                    }
                }

            }.start();



        }

    }

    class Win extends WindowAdapter {
        public void windowClosing(WindowEvent event) {
            event.getWindow().dispose();
            System.exit(0);
        }
    }

}