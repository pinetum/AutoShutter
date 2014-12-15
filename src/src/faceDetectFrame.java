package src;

import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.objdetect.CascadeClassifier;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.Date;

/**
 * Created by pinetum Lin (s1010329) on 2014/12/2.
 */
public class faceDetectFrame extends JFrame{
    //****************************//

    final int cameraNumber = 0 ;
    final private String str_explan = "<html><body style=\"font-size:15px;padding-left:30px;\">1.請點選選單列" +
            "<span style=\"color: #FF0000;\">Camera</span>中的<span style=\"color: #FF0000;\">peoples</span>設定拍照人數<br>" +
            "2.再點選<span style=\"color: #FF0000;\">Start Capture</span>開始自動判斷人數拍照</body></html>";



    //****************************//

    public static faceDetectFrame pointer;
    private VideoCapture cam;
    private Thread t_capture;
    private JLabel label_image;
    private JLabel label_explain;
    private JPanel panel_ctrl;
    private CascadeClassifier faceDetector;
    private int n_targetFace=3;
    private JMenuBar bar_main;
    private JMenu menu_Camera;
    private JMenu menu_Help;
    private JMenu menu_numOfPeople;
    private JMenuItem mu_item_about;
    private JMenuItem mu_item_start;
    private JMenuItem mu_item_stop;
    private JRadioButtonMenuItem rad_btn_num_3;
    private JRadioButtonMenuItem rad_btn_num_4;
    private JRadioButtonMenuItem rad_btn_num_5;
    private JRadioButtonMenuItem rad_btn_num_6;
    private ButtonGroup rad_group;


    public faceDetectFrame(){
        super("face");
        pointer = this;
        inital();
        addListener();


    }


    public static void main (String[] argc){
        faceDetectFrame app = new faceDetectFrame();
        app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        app.setVisible(true);
    }




    private void inital(){
        setSize(600, 400);
        setLocationRelativeTo(null);
        System.loadLibrary("opencv_java249x64");
        String[] str= {"1","2","3"};
        faceDetector = new CascadeClassifier("lbpcascade_frontalface.xml");//getClass().getResource("/lbpcascade_frontalface.xml").getPath());
        label_image = new JLabel();
        label_explain = new JLabel();
        panel_ctrl = new JPanel();
        bar_main = new JMenuBar();
        menu_Camera = new JMenu("Camera...");
        menu_Help = new JMenu("Help");
        menu_numOfPeople = new JMenu("peoples");
        mu_item_about = new JMenuItem("About");
        mu_item_start = new JMenuItem("Start Capture");
        mu_item_stop = new JMenuItem("Stop Capture");
        rad_btn_num_3 = new JRadioButtonMenuItem("3");
        rad_btn_num_4 = new JRadioButtonMenuItem("4");
        rad_btn_num_5 = new JRadioButtonMenuItem("5");
        rad_btn_num_6 = new JRadioButtonMenuItem("6");



        rad_group = new ButtonGroup();
        rad_group.add(rad_btn_num_3);
        rad_group.add(rad_btn_num_4);
        rad_group.add(rad_btn_num_5);
        rad_group.add(rad_btn_num_6);

        rad_btn_num_3.setSelected(true);

        menu_numOfPeople.add(rad_btn_num_3);
        menu_numOfPeople.add(rad_btn_num_4);
        menu_numOfPeople.add(rad_btn_num_5);
        menu_numOfPeople.add(rad_btn_num_6);


        toogleCameraCtrMenuItem(false);


        menu_Help.add(mu_item_about);
        menu_Camera.add(mu_item_start);
        menu_Camera.add(mu_item_stop);
        menu_Camera.add(menu_numOfPeople);
        bar_main.add(menu_Camera);
        bar_main.add(menu_Help);



        label_explain.setText(str_explan);
        add(bar_main,BorderLayout.NORTH);
        add(panel_ctrl, BorderLayout.EAST);
        add(label_explain,BorderLayout.CENTER);
        add(label_image, BorderLayout.SOUTH);

    }
    private void addListener(){
        mu_item_start.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startCamera();
            }
        });
        mu_item_stop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopCamera();
            }
        });
        menu_numOfPeople.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.print(e.getID());
            }
        });

        mu_item_about.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(pointer,"developer:Pinetum\nName:林冠廷\nStdID:1010329","About",JOptionPane.CLOSED_OPTION);
            }
        });
        ActionListener actLten_rad_num_of_pepple = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JRadioButtonMenuItem mm = (JRadioButtonMenuItem)e.getSource();
                n_targetFace = Integer.valueOf(mm.getText());
            }
        };

        rad_btn_num_3.addActionListener(actLten_rad_num_of_pepple);
        rad_btn_num_4.addActionListener(actLten_rad_num_of_pepple);
        rad_btn_num_5.addActionListener(actLten_rad_num_of_pepple);
        rad_btn_num_6.addActionListener(actLten_rad_num_of_pepple);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (JOptionPane.showConfirmDialog(pointer,
                        "Are you sure to Exit", "Really Exit?",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
                    if(cam.isOpened()) cam.release();
                    System.exit(0);
                }
            }
        });

    }
    public void findFace(Mat image){
        Scalar color_rect = new Scalar(0, 255, 0);
        MatOfRect faceDetections = new MatOfRect();
        faceDetector.detectMultiScale(image, faceDetections);
        int faces = faceDetections.toArray().length;

        if(n_targetFace == faces){
            Date now = new Date();
            System.out.println(String.format("Detected %s faces", faces));
            color_rect = new Scalar(0,0,255);

            try{
                //Highgui.imwrite("IMG_"+now.toString(),image);

                //ImageIO.write(toBufferedImage(image), "jpg",new File("IMG_"+now.toString()+".jpg"));
            }catch (Exception e){System.out.print("write image file error:" + e.toString());}
            System.out.println(String.format("write photo!"));
            //stopCamera();
        }

        if(faceDetections.toArray().length > 0){
            for (Rect rect : faceDetections.toArray()) {
                Core.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), color_rect,2);

            }
        }
        setSize(image.cols(), image.rows() + bar_main.getHeight());
        ImageIcon icon = new ImageIcon(toBufferedImage(image));
        label_image.setIcon(icon);
    }
    public Image toBufferedImage(Mat m){
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if ( m.channels() > 1 ) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = m.channels()*m.cols()*m.rows();
        byte [] b = new byte[bufferSize];
        m.get(0,0,b);
        BufferedImage image = new BufferedImage(m.cols(),m.rows(), type);
        final byte[] targetPixels = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
        System.arraycopy(b, 0, targetPixels, 0, b.length);

        return image;

    }
    private void cameraCapture(){
        //--must to be runtime
        t_capture = new Thread(new Runnable() {
            @Override
            public void run() {
                for(;;){
                    Mat frame = new Mat();
                    cam.grab();
                    cam.retrieve(frame);
                    if(frame.rows()>0 && frame.cols()>0)
                        findFace(frame);
                    // System.out.print(frame.rows()+"||"+frame.cols());

                }
            }
        });
        t_capture.start();
    }
    public void stopCamera(){
        t_capture.stop();
        cam.release();
        toogleCameraCtrMenuItem(false);
    }
    public void startCamera(){
        try{
            cam = new VideoCapture();

            if(!cam.open(cameraNumber))
                JOptionPane.showMessageDialog(pointer,"please check your camera status!","open camera Error",JOptionPane.ERROR_MESSAGE);
            else
            {
                toogleCameraCtrMenuItem(true);
                cameraCapture();
            }

        }catch (Exception error){
            System.out.print(error.toString());
        }

    }

    private void toogleCameraCtrMenuItem(boolean cameraOpen){
        mu_item_start.setEnabled(!cameraOpen);
        mu_item_stop.setEnabled(cameraOpen);
        label_image.setVisible(cameraOpen);
    }
}
