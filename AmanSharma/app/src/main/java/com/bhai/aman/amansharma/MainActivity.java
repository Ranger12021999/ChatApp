package com.bhai.aman.amansharma;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.bhai.aman.amansharma.adapter.CustomAdapter;
import com.bhai.aman.amansharma.database.DatabaseHelper;
import com.bhai.aman.amansharma.model.ChatModel;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;
    private int count = 1; //Count for query with respect to row retreival
    private CustomAdapter adapter;
    private DatabaseHelper mDBHelper;
    private List<ChatModel> lstChat = new ArrayList<>();
    private List<ChatModel> newChat = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDBHelper = new DatabaseHelper(this);



        File database =  getApplicationContext().getDatabasePath(DatabaseHelper.DBNAME);
        if(!database.exists()){
            mDBHelper.getReadableDatabase();
            //Copy db
            if(copyDatabase(this)){
                Toast.makeText(this, "All ready. Press anywhere on screen to start!", Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(this, "Copy error", Toast.LENGTH_LONG).show();
            }
        }

        // Initialize shared preferences
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = mPreferences.edit();
        checkSharedPreferences();

        if(count != 1 ){
            int sPCount = count;
            for( int i= 1; i<sPCount; i++){
                count = i;
                getProgress(null);
            }
            count++;
        }
    }

    private boolean copyDatabase(Context context){
        try {
            InputStream inputStream = context.getAssets().open(DatabaseHelper.DBNAME);
            String outFileName = DatabaseHelper.DBLOCATION + DatabaseHelper.DBNAME;
            OutputStream outputStream = new FileOutputStream(outFileName);
            byte[]buff = new byte[1024];
            int length = 0;
            while ((length = inputStream.read(buff)) > 0){
                outputStream.write(buff, 0, length);
            }
            outputStream.flush();
            outputStream.close();
            Log.w("MainActivity", "DB copied successfully");
            return true;

        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private ChatModel setUpMessage(){
        Log.d(TAG, "setUpMessage: Exec");
        return mDBHelper.getListChat(count);

    }
    public void nextClicked(View view){


        if (AppStatus.getInstance(this).isOnline()) {

            Log.d(TAG, "nextClicked: Is Clicked");

            final int limit = 100;

            if(count == limit){
                Log.d(TAG, "nextClicked: Limit Reached");
            }

            loadList(null);


        } else {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Network Issue");
            builder.setMessage("Check Your Internet Connection");
            builder.setIcon(R.drawable.png);
            ;
            builder.setPositiveButton("Retry", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.dismiss();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    public void loadList(View view){
        ChatModel chat = setUpMessage();
        lstChat.add(chat);
        final ListView lstView = (ListView)findViewById(R.id.listView);
        final CustomAdapter adapter = new CustomAdapter(lstChat,this);
        lstView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        lstView.setAdapter(adapter);
        adapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                lstView.setSelection(adapter.getCount() - 1);
            }
        });

        adapter.notifyDataSetChanged();
        Log.i(TAG, "Counter is: "+count);
        count++;
    }

    public void getProgress(View view){
        Log.d(TAG, "getProgress: Executed");

        ChatModel chat = setUpMessage();
        lstChat.add(chat);

        final ListView lstView = (ListView)findViewById(R.id.listView);
        final CustomAdapter adapter = new CustomAdapter(lstChat,this);
        lstView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        lstView.setAdapter(adapter);

        adapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                lstView.setSelection(adapter.getCount() - 1);
            }
        });

        adapter.notifyDataSetChanged();

    }

    private void checkSharedPreferences(){
        //Check if user has read to a point and has been saved from previous
        int counter = mPreferences.getInt(getString(R.string.story_progress_count), 1);

        if(counter != 1){
            count = counter;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mEditor.putInt(getString(R.string.story_progress_count), count);
        mEditor.commit();
    }
}

