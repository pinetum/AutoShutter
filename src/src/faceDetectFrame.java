package src;


import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.highgui.VideoCapture;
import org.opencv.objdetect.CascadeClassifier;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
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

    final int define_TimeCounter = 100;
    final boolean bool_debug = false;
    //****************************//

    private AudioClip               aalip;
    public static faceDetectFrame   pointer;
    private VideoCapture            cam;
    private Thread                  t_capture;
    private JLabel                  label_image;
    private JLabel                  label_explain;
    private JPanel                  panel_ctrl;
    private CascadeClassifier       faceDetector;
    private int                     n_targetFace=3;
    private JMenuBar                bar_main;
    private JMenu                   menu_Camera;
    private JMenu                   menu_Help;
    private JMenu                   menu_numOfPeople;
    private JMenuItem               mu_item_about;
    private JMenuItem               mu_item_start;
    private JMenuItem               mu_item_stop;
    private JRadioButtonMenuItem    rad_btn_num_3;
    private JRadioButtonMenuItem    rad_btn_num_4;
    private JRadioButtonMenuItem    rad_btn_num_5;
    private JRadioButtonMenuItem    rad_btn_num_6;
    private ButtonGroup             rad_group;
    private boolean                 m_b_finFace         = false;
    private int                     m_nFrameTimeCounter = define_TimeCounter;
    public faceDetectFrame(){
        super("face");
        pointer = this;
        // intial member variable
        inital();
        // inital many input listener

        addListener();
    }


    public static void main (String[] argc){
        faceDetectFrame app = new faceDetectFrame();
        app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        app.setVisible(true);


    }




    private void inital(){

        try{
            setSize(600, 400);
            setLocationRelativeTo(null);

            //get the system jdk bit to load library
            String jdkBit = System.getProperty("sun.arch.data.model");
            if(jdkBit.equals("64"))
                System.loadLibrary("opencv_java249x64");
            else
                System.loadLibrary("opencv_java249x86");



            faceDetector =      new CascadeClassifier("lbpcascade_frontalface.xml");//getClass().getResource("/lbpcascade_frontalface.xml").getPath());
            label_image =       new JLabel();
            label_explain =     new JLabel();
            panel_ctrl =        new JPanel();
            bar_main =          new JMenuBar();
            menu_Camera =       new JMenu("Camera...");
            menu_Help =         new JMenu("Help");
            menu_numOfPeople =  new JMenu("peoples");
            mu_item_about =     new JMenuItem("About");
            mu_item_start =     new JMenuItem("Start Capture");
            mu_item_stop =      new JMenuItem("Stop Capture");
            rad_btn_num_3 =     new JRadioButtonMenuItem("3");
            rad_btn_num_4 =     new JRadioButtonMenuItem("4");
            rad_btn_num_5 =     new JRadioButtonMenuItem("5");
            rad_btn_num_6 =     new JRadioButtonMenuItem("6");
            rad_group =         new ButtonGroup();

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


            menu_Camera.add(mu_item_start);
            menu_Camera.add(mu_item_stop);
            menu_Camera.add(menu_numOfPeople);
            bar_main.add(menu_Camera);

            // menu_Help.add(mu_item_about);

            // bar_main.add(menu_Help);


            bar_main.add(mu_item_about);



            label_explain.setText(str_explan);
            add(bar_main,BorderLayout.NORTH);
            add(panel_ctrl, BorderLayout.WEST);
            add(label_explain,BorderLayout.CENTER);
            add(label_image, BorderLayout.SOUTH);

        }catch (Exception e){

            JOptionPane.showMessageDialog(pointer,e.toString(),"Error",JOptionPane.CLOSED_OPTION);


        }


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

    }
    public void findFace(Mat image){
        Scalar color_rect = new Scalar(0, 255, 0);
        MatOfRect faceDetections = new MatOfRect();
        if(!m_b_finFace){
            faceDetector.detectMultiScale(image, faceDetections);
            int faces = faceDetections.toArray().length;

            if( faces == n_targetFace || bool_debug )
                m_b_finFace =true;

            color_rect = new Scalar(0,0,255);
            if(faceDetections.toArray().length > 0){
                for (Rect rect : faceDetections.toArray()) {
                    Core.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), color_rect,2);

                }
            }

        }
        else{
            m_nFrameTimeCounter--;

            if(m_nFrameTimeCounter==0){
                m_nFrameTimeCounter = define_TimeCounter;
                m_b_finFace = false;

                Date now = new Date();
                try{
                    //Highgui.imwrite("c:\\IMG_"+now.toString(),image);
                    try{
                        File mscFile = new File("CAMERALENS.wav");
                        aalip = Applet.newAudioClip(mscFile.toURI().toURL());
                        aalip.play();
                    }
                    catch (Exception e){
                        System.out.println(e.toString());
                    }
                    Image aa = toBufferedImage(image);
                    BufferedImage bimage = new BufferedImage(aa.getWidth(null), aa.getHeight(null), BufferedImage.TYPE_3BYTE_BGR);
                    Graphics bg = bimage.getGraphics();
                    bg.drawImage(aa, 0, 0, null);
                    bg.dispose();
                    //ImageIO.write(bimage, "jpg", new File("IMG_" + now.toString() + ".jpg"));
                    ImageIO.write(bimage, "JPEG", new File("IMG_"+now.getYear()+now.getMonth()+now.getDate()+now.getHours()+now.getMinutes()+now.getSeconds()+".jpg"));
                    toogleCameraCtrMenuItem(false);
                    label_image.setVisible(true);
                    cam.release();


                }catch (Exception e){
                    JOptionPane.showMessageDialog(pointer,e.toString(),"Write File Error",JOptionPane.CLOSED_OPTION);

                }

            }
            else{


                try{
                    if(m_nFrameTimeCounter % 20 ==0){
                        File mscFile = new File("radarping.wav");
                        aalip = Applet.newAudioClip(mscFile.toURI().toURL());
                        aalip.play();

                    }

                }
                catch (Exception e){
                    JOptionPane.showMessageDialog(pointer,e.toString(),"play audio File Error",JOptionPane.CLOSED_OPTION);

                }


                //System.out.println(textn);









            }

        }

        setSize(image.cols() + 200, image.rows() + bar_main.getHeight());
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
