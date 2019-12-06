package fish.timer.com.timer2;

import android.database.Cursor;
import android.graphics.Color;
import android.util.Pair;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Pantelemon on 11/30/2019.
 */
public class SaveLoad {

    public static boolean Save(String id,String Name,int ratio,ArrayList<TimeBlock> TimeBlocksArray,DatabaseHelper myDb,boolean isNew){
        if(id == "0"){
            boolean isInserted = myDb.insertData(Name, "Name","10", String.valueOf(Color.RED), "This is a Description", 1);
            return isInserted;
        }
        boolean isInserted;
        String b_names = "";
        String b_times = "";
        String b_colors = "";
        String b_description = "";
        for (TimeBlock block:TimeBlocksArray) {
            b_names += block._name + "^";
            b_times += block._minutes + "^";
            b_colors += block._color + "^" ;
            b_description += block._description + "^" ;
        }
        if(isNew) {
            isInserted = myDb.insertData(Name, b_names, b_times, b_colors, b_description, ratio);
        }else{
            isInserted = myDb.updateData(id,Name, b_names, b_times, b_colors, b_description, ratio);
        }
        if(isInserted == true)
            return true;
        else
            return true;
    }
    // get id of timers and their names
    public static ArrayList<String[]> loadIdAndNames(DatabaseHelper myDb){
        Cursor res = myDb.getIdAndNames();
        if(res.getCount() == 0) {
            return null;
        }
        ArrayList<String[]> arr = new ArrayList<>();
        while(res.moveToNext()){
            String[] s = new String[2];
            //ID
            s[0] = res.getString(0);
            //Name
            s[1] = res.getString(1);
            arr.add(s);
        }
        return arr;
    }
    public static ArrayList<TimeBlock> Load(DatabaseHelper myDb,String id){
        Cursor res = myDb.getData(String.valueOf(id));
        if(res.getCount() == 0) {
            // show message
            return null;
        }
        ArrayList<TimeBlock> arr = new ArrayList<>();
        res.moveToNext();
        String[] Names = res.getString(2).split("\\^");
        String[] Times = res.getString(3).split("\\^");
        String[] Colors = res.getString(4).split("\\^");
        String[] Descriptions = res.getString(5).split("\\^");
        if(Names[0] == "" && Names.length == 1){

            return null;
        }
        for (int index = 0;(index <(Names.length) );index++) {
            TimeBlock t = new TimeBlock(Integer.parseInt(Times[index]),0,Integer.parseInt(Colors[index]),Names[index],Descriptions[index]);
            arr.add(t);
        }
        return arr;
    }
}
