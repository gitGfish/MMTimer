package fish.timer.com.timer2;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by Pantelemon on 11/29/2019.
 */
public class TimeBlock {
    public long _milisec;
    public long _curent_milisec;
    public int _color;
    public String _name;
    public String _description;
    public TimeBlock( long milisec, int c, String n, String d){
        _milisec = milisec;
        _curent_milisec = milisec;
        _color = c;
        _name = n;
        _description = d;

    }
    public void inc(long m){
        _curent_milisec = m;
        if ( _curent_milisec <= 0){
            _curent_milisec = 0;
        }
    }
    public void reset(){
        _curent_milisec = _milisec;
    }
    public void setAll(long m, int c, String n, String d,Resources res){
        _milisec = m;
        _color = c;
        _name = n;
        _description = d;
    }
    public int getProgPrec(){
        return (int) (100 - (_curent_milisec*100 / _milisec));
    }

}
