package fish.timer.com.timer2;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
        ArrayList<String[]> NAMES;
    DatabaseHelper myDb;
    static ListView list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        myDb = new DatabaseHelper(this);
        NAMES = SaveLoad.loadIdAndNames(myDb);
        setTitle("Micro Management Timer");

        list = (ListView)findViewById(R.id.main_list_view);

        final CustomAdapter customAdapter = new CustomAdapter();

        if (list != null) {
            list.setAdapter(customAdapter);
        }
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getBaseContext(), String.valueOf(position), Toast.LENGTH_SHORT).show();
                Intent i = new Intent(MainActivity.this, Main2Activity.class);
                i.putExtra("STRING_NAME", NAMES.get(position)[1]);
                i.putExtra("STRING_ID", NAMES.get(position)[0]);
                i.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(i);
            }
        });
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View v,
                                           int pos, long id) {

                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                final int position = pos;
                alert.setTitle(NAMES.get(position)[1]);
                alert.setMessage("Are you sure you want to delete?");

                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        myDb.deleteData(NAMES.get(position)[0]);
                        NAMES = SaveLoad.loadIdAndNames(myDb);
                        customAdapter.notifyDataSetChanged();
                        // Do something with value!
                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        // Canceled.
                    }
                });

                alert.show();
                return true;
            }
        });


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);

                alert.setTitle("Add Timer");
                alert.setMessage("Timer Name");

// Set an EditText view to get user input
                final EditText input = new EditText(MainActivity.this);
                alert.setView(input);

                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        SaveLoad.Save("0", input.getText().toString(), 1, null, myDb, true);
                        NAMES = SaveLoad.loadIdAndNames(myDb);
                        customAdapter.notifyDataSetChanged();
                        // Do something with value!
                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        // Canceled.
                    }
                });

                alert.show();

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    class CustomAdapter extends BaseAdapter {


        @Override
        public int getCount() {
            if(NAMES == null){
                return 0;
            }
            return NAMES.size();
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
            if(NAMES == null){
                return  null;
            }
            convertView = getLayoutInflater().inflate(R.layout.main_list_item_view,null);

            TextView name = (TextView) convertView.findViewById(R.id.menu_list_item_text);
            name.setText(NAMES.get(position)[1]);


            return convertView;
        }
    }
}
