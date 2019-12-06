package fish.timer.com.timer2;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
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
    public int currBlockTime;
    public int seconed;
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
    public int Ratio = 10;
    final int MS = 1000;
    final int Minute = 60;
    private int tick,curTick;
    private int pCount = 0;


    //Timer run
    Runnable updateTimerThread =new Runnable() {
        @Override
        public void run() {
            timeInMs = SystemClock.uptimeMillis() - startTime;
            updateTime = timeSwapBuff+timeInMs;
            int secs = (int)(updateTime/MS);
            int mins = secs/Minute;
            secs%=Minute;
            int millisec=(int)(updateTime%MS);
            Time_text.setText("" + mins + ":" + String.format("%2d", secs) + ":" + String.format("%3d", millisec));
            sv.scrollTo( ((int) (Block_size_px * updateTime / (Ratio * MS))),0);
            if( currBlockTime <= timeInMs ){
                    curBlock++;
                if( curBlock >= TimeBlocksArray.size()){
                    play_paus.setImageResource(android.R.drawable.ic_media_play);
                    restar();
                    return;
                }
                    DescriptionList.setSelection(curBlock);
                    DescriptionList.requestFocus();
                    currBlockTime += TimeBlocksArray.get(curBlock)._minutes * Ratio * MS;

            }
            customHandler.postDelayed(this,0);

        }
    };


    @Override
    protected void onResume() {
        super.onResume();
        Log.d("gggg",String.valueOf(parentLinearLayout.getHeight()));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        //Toolbar name handeling
        play_paus = (FloatingActionButton) findViewById(R.id.Play_pause);


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
        customAdapter = new CustomAdapter2();
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
                Block_size_px = (int)(0.05 * Block_Window_size_px);
                //set the arrow in place
                ImageView image = (ImageView) findViewById(R.id.arrow1);
                ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) image.getLayoutParams();
                marginParams.setMargins(0, (Block_Window_size_px / 2 - image.getHeight() / 2), -40, 0);
                //set 2 dummy blocks in scrollview
                View dummyBottom = findViewById(R.id.DummyBlockBottom);
                dummyBottom.setLayoutParams(new LinearLayout.LayoutParams( sv.getWidth() / 2,sv.getHeight()));
                View dummyTop = findViewById(R.id.DummyBlockTop);
                dummyTop.setLayoutParams(new LinearLayout.LayoutParams(sv.getWidth() / 2,sv.getHeight()));

                //check if first time enetring a timer if yes insert dummy time block
                if(TimeBlocksArray == null){
                    TimeBlocksArray = new ArrayList<>();
                    onAddField(null, "add", 10,Color.RED, "this is a description", true);
                    isTimerNew = true;
                }else{
                    //insert all time blocks from db
                    for (TimeBlock t: TimeBlocksArray
                            ) {
                        onAddField(null,t._name,t._minutes,t._color,t._description,true);
                    }
                }
                //setup first block time
                currBlockTime = TimeBlocksArray.get(0)._minutes*Ratio*MS;
                if (DescriptionList != null) {
                    DescriptionList.setAdapter(customAdapter);
                }
            }
        });




    }

    public void restar() {
        timeInMs=0L;
        timeSwapBuff=0L;
        updateTime=0L;
        startTime = SystemClock.uptimeMillis();
        Time_text.setText("0:00:000");
        curBlock=0;
        currBlockTime = TimeBlocksArray.get(0)._minutes*Ratio*MS;
        DescriptionList.setSelection(curBlock);
        DescriptionList.requestFocus();
        customHandler.removeCallbacks(updateTimerThread);
        sv.scrollTo( ((int) (Block_size_px * updateTime / (Ratio * MS))),0);
        SaveLoad.Save(ID,NAME,Ratio,TimeBlocksArray,myDb,false);
    }


    public void onAddField(View v,String Name, int Minutes,int color, String Description,boolean first) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.fancy_time_block, null);

        TextView block_text = (TextView) rowView.findViewById(R.id.block_time_text);
        LinearLayout l = (LinearLayout) rowView.findViewById(R.id.fancy_block_linear_layout);
        ViewGroup.LayoutParams params = l.getLayoutParams();


        params.width = (int)(Minutes * Block_size_px);
        l.setLayoutParams(params);
        block_text.setText(Name + "\n" + String.valueOf(Minutes));
        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEditDialog(v);


            }
        });
        // Add the new row before the add field button.
        l.setBackgroundColor(color);

        if(first){
            if(TimeBlocksArray.size()==0 ){
                TimeBlock time_block = new TimeBlock(Minutes,0,color,Name, Description);
                TimeBlocksArray.add(time_block);
            }
            l.setLayoutParams(params);
            parentLinearLayout.addView(rowView,parentLinearLayout.getChildCount()-1);
        }else{
            TimeBlock time_block = new TimeBlock(Minutes,0,color,Name, Description);
            TimeBlocksArray.add(parentLinearLayout.indexOfChild(v), time_block);
            parentLinearLayout.addView(rowView, parentLinearLayout.indexOfChild(v)+1);

        }

        DescriptionList.invalidateViews();
    }


    public void onDelete(View v) {
        TimeBlocksArray.remove(parentLinearLayout.indexOfChild(v)-1);
        parentLinearLayout.removeView(v);
        getWindow().getDecorView().findViewById(R.id.blocks_scrollview_linear_layout).invalidate();
        getWindow().getDecorView().findViewById(R.id.fancy_list_view).invalidate();
        DescriptionList.invalidateViews();

    }

    public void openEditDialog(View v) {
        BlockBeingEdited = v;
        BlockEdit dialog = new BlockEdit();
        dialog.show(getSupportFragmentManager(),"block edit");
    }

    @Override
    public void applyInfo(String Name, int Minutes, String Description,int action) {
        if(Description.length() == 0){
            Description = "-";
        }
        if(Name.length() == 0){
            Name = "-";
        }
        if(action == BlockEdit.EDIT_BLOCK) {
            TextView block_text = (TextView) BlockBeingEdited.findViewById(R.id.block_time_text);
            LinearLayout l = (LinearLayout) BlockBeingEdited.findViewById(R.id.fancy_block_linear_layout);
            ViewGroup.LayoutParams params = l.getLayoutParams();
            params.width = (int)(Minutes * Block_size_px);
            l.setLayoutParams(params);
            Random rnd = new Random();
            int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
            l.setBackgroundColor(color);
            block_text.setText(Name + "\n" + String.valueOf(Minutes));
            TimeBlock t = TimeBlocksArray.get(parentLinearLayout.indexOfChild(BlockBeingEdited));
            t.setAll(Minutes,0,color,Name,Description);
            DescriptionList.invalidateViews();
        }else{
            if(action == BlockEdit.ADD_BLOCK) {
                Random rnd = new Random();
                int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
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
        public View getView(int position, View convertView, ViewGroup parent) {

            convertView = getLayoutInflater().inflate(R.layout.description_box,null);
            ViewGroup.LayoutParams params = parent.getLayoutParams();
            params.height = DescriptionList.getHeight()/3;
            convertView.setLayoutParams(params);
            //dummy elements puting 1 element in the start and 1 in the end of list
            if(position == 0 || position > TimeBlocksArray.size()){
                return convertView;
            }
            TextView name = (TextView) convertView.findViewById(R.id.description_box_name);
            TextView time_text = (TextView) convertView.findViewById(R.id.description_box_description);
            name.setText(TimeBlocksArray.get(position-1)._name);
            time_text.setText(TimeBlocksArray.get(position-1)._description);
            convertView.setBackgroundColor(TimeBlocksArray.get(position-1)._color);
            Drawable background = convertView.getBackground();
            background.setAlpha(80);

            return convertView;
        }
    }

}
