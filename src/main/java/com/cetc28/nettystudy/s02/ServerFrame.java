package com.cetc28.nettystudy.s02;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @Auther: WSC
 * @Date: 2022/2/1 - 02 - 01 - 17:39
 * @Description: com.cetc28.nettystudy.s02
 * @version: 1.0
 */
public class ServerFrame extends Frame {
    public static final ServerFrame INSTANCE = new ServerFrame();

    Button btnStart = new Button("start");
    TextArea taLeft = new TextArea();
    TextArea taRight = new TextArea();
    Server server = new Server();

    public ServerFrame(){
        this.setSize(1600,600);
        this.setLocation(0,30);
        this.add(btnStart,BorderLayout.NORTH);
        Panel p = new Panel(new GridLayout(1,2));
        p.add(taLeft);
        p.add(taRight);
        this.add(p);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        //这样做会导致UI线程发生堵塞, 之后ui没有反应
//        this.btnStart.addActionListener((e)->{
//            server.serverStart();
//        });
    }

    public void updateServerMsg(String str) {
        this.taLeft.setText(taLeft.getText() + str + System.getProperty("line.separator") );
    }

    public void updateClientMsg(String str){
        this.taRight.setText(taRight.getText() + str + System.getProperty("line.separator"));
    }

    // 这是程序的main函数:入口函数
    public static void main(String[] args) {
        ServerFrame.INSTANCE.setVisible(true);
        //将服务器启动放在主线程, 这样ui线程不受影响
        ServerFrame.INSTANCE.server.serverStart();
    }
}
