package com.redgold.raktim.cs727;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import android.os.AsyncTask;

import allbegray.slack.SlackClientFactory;
import allbegray.slack.rtm.SlackRealTimeMessagingClient;

public class MainActivity extends Activity implements OnClickListener {
    Logger log = Logger.getLogger("log");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.continue_button).setOnClickListener(this);
        findViewById(R.id.new_button).setOnClickListener(this);
        findViewById(R.id.about_button).setOnClickListener(this);
        findViewById(R.id.exit_button).setOnClickListener(this);

    }

    private class SlackConnection extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... input) {
            String slack_token = "xoxb-445228206210-445416796645-QOUSh3T34qBBlIUSRm2i8B9h";
            SlackRealTimeMessagingClient client = SlackClientFactory.createSlackRealTimeMessagingClient(slack_token);

            try{
                client.connect();
                return "connecting to slack";
            } catch (Exception e){
                System.out.print(e);
                return "failing to connect to slack";
            }
        }


    }
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//// Inflate the menu; this adds items to the action bar if it is present.
//                getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.about_button:
                Intent i = new Intent(this, About.class);
                startActivity(i);
                break;
            case R.id.new_button: openNewGameDialog();
                break;
            case R.id.continue_button:
                try {
                    openSlackGame();
                } catch (Exception e) {
                    Log.d(this.getClass().getSimpleName(), "error 1");
                }
            case R.id.exit_button: finish();
                break;
// More code later...
        }
    }


    private void openNewGameDialog() {
        try{
            //Toast toast = Toast.makeText(this, "I am here",Toast.LENGTH_LONG);
            //toast.show();
            SlackConnection connection = new SlackConnection();
            connection.execute("1");
            String result = null;
            try {
                result = connection.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
            new AlertDialog.Builder(this).setTitle(R.string.game_title).setItems(R.array.first_move, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                            int which) { startGame(which); }}).show();
        } catch (Exception e){
            Toast toast = Toast.makeText(this, e.toString(),Toast.LENGTH_LONG);
            toast.show();
        }
    }

//    public void openSlackGame()  {
////        Intent intent = new Intent(this, Game.class);
////        intent.putExtra(Game.FIRST_MOVE, 1);
//
////        Toast toast = Toast.makeText(this, "I am here after " + result,Toast.LENGTH_LONG);
////        toast.show();
//
//    }

    protected void startGame(int which) {
        Log.d(this.getClass().getSimpleName(), "clicked on " + which);
        // Start game here...
        Intent intent = new Intent(this, Game.class);
        intent.putExtra(Game.FIRST_MOVE, which);
        startActivity(intent);
    }
}


//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) { case R.id.action_settings: startActivity(new Intent(this, Prefs.class));
//            return true;
//// Add more as needed
//        }
//        return false;
//    }

