package com.cetc28.nettystudy.s02;

/**
 * @Auther: WSC
 * @Date: 2022/2/1 - 02 - 01 - 18:32
 * @Description: com.cetc28.nettystudy.s02
 * @version: 1.0
 */
public class TankMsg {
    public int x,y;

    public TankMsg(int x, int y){
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "TankMsg: " + x + " , " + y;
    }
}
