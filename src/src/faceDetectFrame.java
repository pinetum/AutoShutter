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
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.Date;

/**
 * Created by Pinetum on 2014/12/2.
 */
public class faceDetectFrame extends JFrame{
    //****************************//

    final int cameraNumber = 0 ;

    //****************************//
    public static void main (String[] argc){

        faceDetectFrame app = new faceDetectFrame();
        app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        app.setVisible(true);



    }

    private VideoCapture cam;
    private boolean b_camIsOpen = false;
    private Thread t_capture;
    private JComboBox s_numOfPeople;
    private JLabel label_image;
    private JButton btn_openFile;
    private JButton btn_openCam;
    private JButton btn_reconiz;
    private JFileChooser dlg_openFile;
    private CascadeClassifier faceDetector;
    private JPanel panel_ctrl;
    private int n_targetFace=99;
    public void findFace(Mat image){

        MatOfRect faceDetections = new MatOfRect();
        faceDetector.detectMultiScale(image, faceDetections);
        int faces = faceDetections.toArray().length;

        if(n_targetFace == faces){
            Date now = new Date();
            System.out.println(String.format("Detected %s faces", faces));


            try{
                //Highgui.imwrite("IMG_"+now.toString(),image);

                //ImageIO.write(toBufferedImage(image), "jpg",new File("IMG_"+now.toString()+".jpg"));
            }catch (Exception e){System.out.print("write image file error:" + e.toString());}
            System.out.println(String.format("write photo!"));
            //stopCamera();
        }

        if(faceDetections.toArray().length > 0){
            for (Rect rect : faceDetections.toArray()) {
                Core.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
            }
        }
        setSize(image.cols(), image.rows() + btn_openFile.getHeight());
        ImageIcon icon = new ImageIcon(toBufferedImage(image));
        label_image.setIcon(icon);
    }
    public faceDetectFrame(){
        super("face");
        setSize(400, 400);
        System.loadLibrary("opencv_java249x64");
        String[] str= {"1","2","3"};
        JLabel label_num_text = new JLabel("自拍人數");

        faceDetector = new CascadeClassifier("D:/JAVA/out/production/JAVA/lbpcascade_frontalface.xml");//getClass().getResource("/lbpcascade_frontalface.xml").getPath());
        label_image = new JLabel();
        btn_openFile = new JButton("開啟檔案");
        btn_openCam = new JButton("open Cam");
        btn_reconiz = new JButton("辨識");
        dlg_openFile = new JFileChooser();
        panel_ctrl = new JPanel();
        s_numOfPeople = new JComboBox(str);
        btn_openFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               int option = dlg_openFile.showDialog(null,null);
                if(option == JFileChooser.APPROVE_OPTION){
                    //System.out.print(dlg_openFile.getSelectedFile());
                    Mat image= Highgui.imread(dlg_openFile.getSelectedFile().toString());
                    findFace(image);
                }
            }
        });
        btn_openCam.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(b_camIsOpen){

                    stopCamera();

                }
                else{
                    startCamera();
                }

            }
        });
        s_numOfPeople.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                n_targetFace = s_numOfPeople.getSelectedIndex() +1 ;
            }
        });
        panel_ctrl.add(btn_openCam);
        //panel_ctrl.add(btn_openFile);
        panel_ctrl.add(label_num_text);
        panel_ctrl.add(s_numOfPeople);

        add(panel_ctrl, BorderLayout.NORTH);
        add(label_image, BorderLayout.SOUTH);
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
        btn_openCam.setText("開啟攝影機");
        b_camIsOpen=!b_camIsOpen;

    }
    public void startCamera(){
        try{
            cam = new VideoCapture();
            cam.open(cameraNumber);
            b_camIsOpen = !b_camIsOpen;
            btn_openCam.setText("關閉攝影機");
            cameraCapture();
        }catch (Exception error){
            System.out.print(error.toString());
        }

    }
}
