package com.cetc28.nettystudy.s02_mine;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @Auther: WSC
 * @Date: 2022/1/31 - 01 - 31 - 11:45
 * @Description: com.cetc28.nettystudy.s02
 * @version: 1.0
 */
public class ClientFrame extends Frame {
    TextArea ta = new TextArea();//多行文本
    TextField tf = new TextField();//单行文本
    Client cc = null;

    public ClientFrame(){
        this.setSize(600,400);
        this.setLocation(100,20);
        this.add(ta, BorderLayout.CENTER);
        this.add(tf, BorderLayout.SOUTH);
        tf.addActionListener(new ActionListener() {
            @Override//在tf中写完字符串回车就调用这个函数
            public void actionPerformed(ActionEvent e) {
                //给服务器发送消息
                cc.sendMsgTOServer(tf.getText());
                //主面板消息显示
                ta.setText(ta.getText() + tf.getText() + '\n');
                //输入面板重新赋空值
                tf.setText("");
            }
        });

        this.setVisible(true);
        this.addWindowListener(new WindowAdapter() {
            @Override//点击关闭窗口按钮就执行这个函数
            public void windowClosing(WindowEvent e) {
                System.exit(1);
            }
        });

        //窗口显示完毕之后 调用客户端连接
        cc = new Client(this);
    }

    public void showMsg(String str){
        ta.setText(ta.getText() + str + '\n');
    }

    // 这是程序的main函数:入口函数
    public static void main(String[] args) {
        new ClientFrame();
    }
}
