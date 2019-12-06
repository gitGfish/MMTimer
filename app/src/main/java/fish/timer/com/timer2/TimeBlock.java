package fish.timer.com.timer2;

import android.graphics.Color;

import org.w3c.dom.Text;

/**
 * Created by Pantelemon on 11/29/2019.
 */
public class TimeBlock {
    public int _minutes;
    public int _sec;
    public int _color;
    public String _name;
    public String _description;

    public TimeBlock( int m, int s, int c, String n, String d){
        _minutes = m;
        _sec = s;
        _color = c;
        _name = n;
        _description = d;
    }

    public void setAll(int m, int s, int c, String n, String d){
        _minutes = m;
        _sec = s;
        _color = c;
        _name = n;
        _description = d;
    }

}
