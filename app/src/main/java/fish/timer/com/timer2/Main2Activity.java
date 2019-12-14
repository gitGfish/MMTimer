package fish.timer.com.timer2;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;


public class Main2Activity extends AppCompatActivity implements BlockEdit.BlockEditListener {
    public Main2Activity(){}
    public LinearLayout parentLinearLayout;
    Button addBlockEnd,pause_button,restart_button;
    TextView Time_text;
    Handler customHandler = new Handler();
    LinearLayout container;
    HorizontalScrollView sv;
    ArrayList<TimeBlock> TimeBlocksArray;
    public View BlockBeingEdited;
    ListView DescriptionList;
    public int curBlock;
    public long currBlockTime;
    long startTime=0L,timeInMs=0L,timeSwapBuff=0L,updateTime=0L;
    public String NAME;
    public String ID;
    boolean isTimerNew= false;
    DatabaseHelper myDb;
    CustomAdapter2 customAdapter;
    FloatingActionButton play_paus;
    boolean playOrPause;
    //ratio handeling
    int Block_Window_size_px;
    float Block_size_px;
    public int Ratio = 1;
    final int MS = 1000;
    final int Minute = 60;
    private int tick,curTick;
    private int pCount = 0;
    ProgressBar mProgress;
    Resources res;
    Drawable drawable;
    TextView tv;
    int pStatus = 0;
    private Handler handlerProgressBar = new Handler();
    long precentCalcTemp = 0;

    static int remeber_color = 0;




    //Timer run
    Runnable updateTimerThread =new Runnable() {
        @Override
        public void run() {
            timeInMs = SystemClock.uptimeMillis() - startTime;
            updateTime = timeSwapBuff+timeInMs;
            Time_text.setText(MiliToStr(updateTime));
            sv.scrollTo(((int) (Block_size_px * updateTime / (Ratio * MS))), 0);

            //circle progress bar inc
            pStatus = (int) (((double)(updateTime - precentCalcTemp) / (currBlockTime - precentCalcTemp) ) *100);
            customAdapter.incProgress(curBlock,pStatus,(int) (currBlockTime - updateTime));

            if( currBlockTime <= updateTime ){
                curBlock++;
                if( curBlock >= TimeBlocksArray.size()){
                    play_paus.setImageResource(android.R.drawable.ic_media_play);
                    playOrPause=true;
                    restar();
                    return;
                }
                    DescriptionList.smoothScrollToPosition(curBlock+2);
                    colorHorizontalList();
                    parentLinearLayout = (LinearLayout) findViewById(R.id.blocks_scrollview_linear_layout);
                    parentLinearLayout.invalidate();
                    precentCalcTemp = currBlockTime;
                    currBlockTime += TimeBlocksArray.get(curBlock)._milisec;

            }
            customHandler.postDelayed(this, 0);

        }
    };





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);




        res = getResources();
        drawable = res.getDrawable(R.drawable.circular);

        //Toolbar name handeling
        play_paus = (FloatingActionButton) findViewById(R.id.Play_pause);

        //get info from caller activity
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                NAME = null;
            } else {
                ID = extras.getString("STRING_ID");
                NAME= extras.getString("STRING_NAME");
            }
        } else {
            NAME = (String) savedInstanceState.getSerializable("STRING_INAME");
            ID = (String) savedInstanceState.getSerializable("STRING_INAME");
        }
        setTitle(NAME);
        //init database
        myDb = new DatabaseHelper(this);

        curBlock = 0;

        /////////////////////////Timer/////////////////////////////////////
//        pause_button = (Button) findViewById(R.id.pause_button);
        addBlockEnd = (Button) findViewById(R.id.addBlockEnd);
        restart_button = (Button) findViewById(R.id.restart_button);
        Time_text = (TextView) findViewById(R.id.timer_text);
        container = (LinearLayout) findViewById(R.id.timer_clock_linear_layout);
        playOrPause = true;
        play_paus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(playOrPause){
                    //play Button
                    startTime = SystemClock.uptimeMillis();
                    customHandler.postDelayed(updateTimerThread, 0);
                    playOrPause=false;
                    play_paus.setImageResource(android.R.drawable.ic_media_pause);
                    DescriptionList.smoothScrollToPosition(curBlock);
                    colorHorizontalList();
                    parentLinearLayout = (LinearLayout) findViewById(R.id.blocks_scrollview_linear_layout);
                    parentLinearLayout.invalidate();
                }else{
                    timeSwapBuff += timeInMs;
                    customHandler.removeCallbacks(updateTimerThread);
                    playOrPause=true;
                    play_paus.setImageResource(android.R.drawable.ic_media_play);
                }

            }
        });

        addBlockEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEditDialog(parentLinearLayout.getChildAt(parentLinearLayout.getChildCount()-2));
            }
        });
        restart_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                playOrPause=true;
                play_paus.setImageResource(android.R.drawable.ic_media_play);
                restar();
            }
        });

        /////////////////////////////////////////////////////////////////
        // lists

        sv = (HorizontalScrollView) findViewById(R.id.fancy_list_view);
        DescriptionList = (ListView)findViewById(R.id.DescriptionList);
        DescriptionList.setItemsCanFocus(true);
        parentLinearLayout = (LinearLayout) findViewById(R.id.blocks_scrollview_linear_layout);
        TimeBlocksArray = SaveLoad.Load(myDb,ID);

        //layou size unknown in on create so i build a listener
        //ther the layout can be extracted after on create is done
        //all elements depended on the layout size must be in here for initialization
        sv.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {


                sv.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                Block_Window_size_px = sv.getWidth(); //height is ready
                Block_size_px = (int) (0.05 * Block_Window_size_px);
                //set the arrow in place
                ImageView image = (ImageView) findViewById(R.id.arrow1);
                ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) image.getLayoutParams();
                marginParams.setMargins(0, (Block_Window_size_px / 2 - image.getHeight() / 2), -40, 0);
                //set 2 dummy blocks in scrollview
                View dummyBottom = findViewById(R.id.DummyBlockBottom);
                dummyBottom.setLayoutParams(new LinearLayout.LayoutParams(sv.getWidth() / 2, sv.getHeight()));
                View dummyTop = findViewById(R.id.DummyBlockTop);
                dummyTop.setLayoutParams(new LinearLayout.LayoutParams(sv.getWidth() / 2, sv.getHeight()));
                //insert all time blocks from db
                for (TimeBlock t : TimeBlocksArray) {
                    onAddField(null, t._name, t._milisec, t._color, t._description, true);
                }
                //setup first block time
                currBlockTime = TimeBlocksArray.get(0)._milisec;
                customAdapter = new CustomAdapter2();
                if (DescriptionList != null) {
                    DescriptionList.setAdapter(customAdapter);
                }

            }
        });
        DescriptionList.setAdapter(customAdapter);
        final Handler handlerList = new Handler();
        handlerList.postDelayed(new Runnable() {

            @Override
            public void run() {
                customAdapter.notifyDataSetChanged();

                handlerList.postDelayed(this,  200);
            }
        },  200);



    }

    public void restar() {
        precentCalcTemp = 0;
        timeInMs=0L;
        timeSwapBuff=0L;
        updateTime=0L;
        startTime = SystemClock.uptimeMillis();
        Time_text.setText("0:00");
        curBlock=0;
        for (TimeBlock t: TimeBlocksArray) {
            t.reset();
        }
        currBlockTime = TimeBlocksArray.get(0)._milisec;
        DescriptionList.smoothScrollToPosition(curBlock);
        colorHorizontalList();
        parentLinearLayout = (LinearLayout) findViewById(R.id.blocks_scrollview_linear_layout);
        parentLinearLayout.invalidate();
        customHandler.removeCallbacks(updateTimerThread);
        sv.scrollTo(((int) (Block_size_px * updateTime / (Ratio * MS))), 0);
        SaveLoad.Save(ID, NAME, Ratio, TimeBlocksArray, myDb, false);
    }


    public void onAddField(View v,String Name, long Mili,int color, String Description,boolean first) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.fancy_time_block, null);

        TextView block_text = (TextView) rowView.findViewById(R.id.block_time_text);
        LinearLayout l = (LinearLayout) rowView.findViewById(R.id.fancy_block_linear_layout);
        ViewGroup.LayoutParams params = l.getLayoutParams();

        params.width = (int)((Mili/MS) * Block_size_px);
        l.setLayoutParams(params);
        block_text.setText("\n");

        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEditDialog(v);
            }
        });
        // Add the new row before the add field button.
        l.setBackgroundColor(color);
        int pos = -2;
        if(first){
            if(TimeBlocksArray.size()==0 ){
                pos = -1;
            }
            l.setLayoutParams(params);
            parentLinearLayout.addView(rowView,parentLinearLayout.getChildCount()-1);
        }else{
            pos = parentLinearLayout.indexOfChild(v);
            parentLinearLayout.addView(rowView, parentLinearLayout.indexOfChild(v)+1);
        }
        addElement(new TimeBlock(Mili,color, Name, Description), pos);
        SaveLoad.Save(ID, NAME, Ratio, TimeBlocksArray, myDb, false);
    }

    public void addElement(TimeBlock t, int pos){
        if(pos == -2){
            return;
        }
        if(pos <= 0){
            TimeBlocksArray.add(t);
        }else{
            TimeBlocksArray.add(pos, t);
        }
    }
    public void onDelete(View v) {
        if(TimeBlocksArray.size() > 1) {
            TimeBlocksArray.remove(parentLinearLayout.indexOfChild(v) - 1);
            parentLinearLayout.removeView(v);
            SaveLoad.Save(ID, NAME, Ratio, TimeBlocksArray, myDb, false);
        }else{
            Toast.makeText(Main2Activity.this, "Can't delete last time box", Toast.LENGTH_SHORT).show();
        }


    }

    public void openEditDialog(View v) {
        BlockBeingEdited = v;
        BlockEdit dialog = new BlockEdit();
        dialog.show(getSupportFragmentManager(), "block edit");
    }

    @Override
    public void applyInfo(String Name, int Minutes, String Description,int action) {
        if(Description.length() == 0){
            Description = "-";
        }
        if(Name.length() == 0){
            Name = "-";
        }
        Minutes *= MS;
        if(action == BlockEdit.EDIT_BLOCK) {
            int color = generate_color(parentLinearLayout.indexOfChild(BlockBeingEdited) - 1);
            onAddField(BlockBeingEdited, Name, Minutes, color, Description, false);
            onDelete(BlockBeingEdited);

        }else{
            if(action == BlockEdit.ADD_BLOCK) {
                int color = generate_color(parentLinearLayout.indexOfChild(BlockBeingEdited) - 1);
                onAddField(BlockBeingEdited, Name, Minutes, color, Description, false);
            }else{
                if(action == BlockEdit.DELETE_BLOCK){
                    onDelete(BlockBeingEdited);
                }

            }
        }

        SaveLoad.Save(ID, NAME, Ratio, TimeBlocksArray, myDb, isTimerNew);


    }




    class CustomAdapter2 extends BaseAdapter {
        public void incProgress(int pos,int amount,long time){
            View v = getView(pos,null,parentLinearLayout);
            TimeBlock tb = TimeBlocksArray.get(pos);
            tb.inc(time);
            ProgressBar b = (ProgressBar) v.findViewById(R.id.circularProgressbar);
            b.setProgress(amount);
            TextView t = (TextView) v.findViewById(R.id.tv);
            t.setText(MiliToStr(tb._curent_milisec));
        }
        @Override
        public int getCount() {
            return TimeBlocksArray.size()+2;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            convertView = getLayoutInflater().inflate(R.layout.description_box, null);

            ViewGroup.LayoutParams params = parent.getLayoutParams();
            params.height = (DescriptionList.getHeight()/3 ) - 30;
            convertView.setLayoutParams(params);

            ProgressBar prog;
            prog = (ProgressBar) convertView.findViewById(R.id.circularProgressbar);
            TextView t;
            t = (TextView) convertView.findViewById(R.id.tv);
            //dummy elements puting 1 element in the start and 1 in the end of list
            if(position == 0 || position > TimeBlocksArray.size()){
                prog.setVisibility(View.GONE);
                t.setVisibility(View.GONE);
                return convertView;
            }



            TimeBlock time_block = TimeBlocksArray.get(position - 1);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openEditDialog(parentLinearLayout.getChildAt(position));
                }
            });

            TextView name = (TextView) convertView.findViewById(R.id.description_box_name);
            TextView time_text = (TextView) convertView.findViewById(R.id.description_box_description);
            name.setText(time_block._name);
            time_text.setText(time_block._description);



            TextView timerText = (TextView) convertView.findViewById(R.id.tv);
            ProgressBar progBar = (ProgressBar) convertView.findViewById(R.id.circularProgressbar);
            timerText.setText(MiliToStr(time_block._curent_milisec));
            drawable = res.getDrawable(R.drawable.circular);
            progBar.setProgress(time_block.getProgPrec());   // Main Progress
            progBar.setSecondaryProgress(100); // Secondary Progress
            progBar.setMax(100); // Maximum Progress
            progBar.setProgressDrawable(drawable);

            //set progress bar befor curentJob to full and all jobs after curent to 0
            if(position-1 < curBlock){
                progBar.setProgress(100);
                timerText.setText(MiliToStr(0));
                convertView.getBackground().setTint(generate_color(2));
                convertView.setBackgroundResource(R.drawable.round_corners);
            }else {
                if(position-1 == curBlock){
                    convertView.getBackground().setTint(generate_color(1));
                }else{
                    convertView.getBackground().setTint(generate_color(0));
                }
                timerText.setText(MiliToStr(time_block._curent_milisec));
                progBar.setProgress(time_block.getProgPrec());

            }



            return convertView;
        }
    }
    public String MiliToStr(long l){
        int secs = (int)(l/MS);
        int mins = secs/Minute;
        secs%=Minute;
        return "" + mins + ":" + String.format("%1$02d", secs);
    }

    public int generate_color(int pos){
        int r = pos % 3;
        switch (r){
            case 0:
                r = ResourcesCompat.getColor(getResources(), R.color.app2, null);
                break;
            case 1:
                r = ResourcesCompat.getColor(getResources(), R.color.app3, null);
                break;
            case 2:
                r = ResourcesCompat.getColor(getResources(), R.color.app4, null);
                break;


        }

        return r;
    }
    public void colorHorizontalList(){
        for(int i = 0; i <  TimeBlocksArray.size() ; i++){
            if(i+1 == curBlock) {
                parentLinearLayout.getChildAt(i + 1).setBackgroundColor(generate_color(0));
            }else{
                parentLinearLayout.getChildAt(i + 1).setBackgroundColor(generate_color(2));
            }
        }
    }

}
