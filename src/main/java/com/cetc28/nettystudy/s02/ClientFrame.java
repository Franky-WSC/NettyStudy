package com.cetc28.nettystudy.s02;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @Auther: WSC
 * @Date: 2022/1/31 - 01 - 31 - 21:09
 * @Description: com.cetc28.nettystudy.s02
 * @version: 1.0
 */
public class ClientFrame extends Frame {
    public static final ClientFrame INSTANCE = new ClientFrame();

    TextArea ta = new TextArea();//多行文本
    TextField tf = new TextField();//单行文本
    Client c = null;

    public ClientFrame() {
        this.setSize(600,400);
        this.setLocation(100,20);
        this.add(ta, BorderLayout.CENTER);
        this.add(tf, BorderLayout.SOUTH);
        tf.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                c.send(tf.getText());//将tf中的消息发送到服务器
                //面板消息显示
//                ta.setText(ta.getText() + tf.getText() + System.getProperty("line.separator"));
                tf.setText("");
            }
        });

//        this.setVisible(true);
        this.addWindowListener(new WindowAdapter() {
            @Override//点击关闭窗口按钮就执行这个函数
            public void windowClosing(WindowEvent e) {
                System.exit(1);
            }
        });

//        //窗口显示完毕之后 调用客户端连接
//        connectToServer();
    }

    public void connectToServer(){
        c = new Client();
        c.connect();
    }

    public void updateContext(String str){
        ta.setText(ta.getText() + System.getProperty("line.separator") + str);
    }

    // 这是程序的main函数:入口函数
    public static void main(String[] args) {
        ClientFrame frame = ClientFrame.INSTANCE;
        frame.setVisible(true);
        frame.connectToServer();
    }
}
