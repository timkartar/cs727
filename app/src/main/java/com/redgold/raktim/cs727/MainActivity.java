package com.redgold.raktim.cs727;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.logging.Logger;
import allbegray.slack.SlackClientFactory;
import allbegray.slack.exception.SlackResponseErrorException;
import allbegray.slack.rtm.Event;
import allbegray.slack.rtm.EventListener;
import allbegray.slack.rtm.SlackRealTimeMessagingClient;
import allbegray.slack.type.Authentication;
import allbegray.slack.type.Channel;
import allbegray.slack.type.User;
import allbegray.slack.webapi.SlackWebApiClient;

public class MainActivity extends Activity implements OnClickListener {
    Logger log = Logger.getLogger("log");
    SlackListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //findViewById(R.id.continue_button).setOnClickListener(this);
        findViewById(R.id.new_button).setOnClickListener(this);
        //findViewById(R.id.about_button).setOnClickListener(this);
        findViewById(R.id.exit_button).setOnClickListener(this);
        listener = new SlackListener(this);
        listener.execute();
        //listener.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"listening for game start");
    }

    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.new_button:
                SlackSpeaker speaker = new SlackSpeaker();
                String inp = "new game ";
                try{
                    listener.cancel(true);
                }
                catch(Exception e){
                    System.out.println(e);
                }
                speaker.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"new game ");
                startGame(0);
                break;
            case R.id.exit_button: finish();
                break;
        }
    }


    private void openNewGameDialog() {
        try{
            //Toast toast = Toast.makeText(this, "I am here",Toast.LENGTH_LONG);
            //toast.show();

            new AlertDialog.Builder(this).setTitle(R.string.game_title).setItems(R.array.first_move, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                            int which) { listener.cancel(true);startGame(which + 1); }}).show();
        } catch (Exception e){
            System.out.println(e);
        }
    }


    protected void startGame(int which) {
        Log.d(this.getClass().getSimpleName(), "clicked on " + which);
        // Start game here...
        Intent intent = new Intent(this, Game.class);
        intent.putExtra(Game.FIRST_MOVE, which);
        startActivity(intent);
        //listener = new SlackListener(this);
        //listener.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"afaf");
    }

    private class SlackListener extends AsyncTask<String, String, String> {

        String output = null;
        MainActivity context;
        public SlackListener(MainActivity context){
            this.context = context;
        }

        @Override
        protected String doInBackground(String... input) {
            String slack_token = "xoxb-445228206210-445416796645-QOUSh3T34qBBlIUSRm2i8B9h";
            final SlackWebApiClient mWebApiClient = SlackClientFactory.createWebApiClient(slack_token);
//            String webSocketUrl = mWebApiClient.startRealTimeMessagingApi().findPath("url").asText();
            SlackRealTimeMessagingClient client = SlackClientFactory.createSlackRealTimeMessagingClient(slack_token);
//            client.addListener(Event.HELLO, new EventListener() {
//                @Override
//                public void onMessage(JsonNode message) {
//
//                    Authentication authentication = mWebApiClient.auth();
//                    String mBotId = authentication.getUser_id();
//                }
//            });

            client.addListener(Event.MESSAGE, new EventListener() {

                @Override
                public void onMessage(JsonNode message) {
                    String channelId = message.findPath("channel").asText();
                    String userId = message.findPath("user").asText();
                    String text = message.findPath("text").asText();
                    String[] tokens = text.split("\\s+");
                    //System.out.println(tokens[tokens.length - 1]);


                    Authentication authentication = mWebApiClient.auth();
                    String mBotId = authentication.getUser_id();
                    if (userId != null  && userId.equals(mBotId)) { //
                        Channel channel;
                        try {
                            channel = mWebApiClient.getChannelInfo(channelId);
                        } catch (SlackResponseErrorException e) {
                            channel = null;
                        }
                        User user = mWebApiClient.getUserInfo(userId);
                        String userName = user.getName();
                        String[] split_text  = text.split("\\s+");
                        if(split_text[split_text.length - 1].equals("start")) {
                            onProgressUpdate(split_text);
                        }
                    }
                }
            });



            try{
                //mWebApiClient.meMessage("#general",  input + " " + uuid);
                client.connect();

                while(true){
                    if (isCancelled()){
                        client.close();
                        break;
                    }
                }

                return output;
            } catch (Exception e){
                System.out.print(e);
                return "failing to connect to slack";
            }




        }

        @Override
        protected void onProgressUpdate(String... split_text){
            context.runOnUiThread(new Runnable(){
                public void run() {
                    openNewGameDialog();
                }
            });
        }
    }

    private class SlackSpeaker extends AsyncTask<String, String, String> {

        String output = null;

        @Override
        protected String doInBackground(final String... input) {
            String slack_token = "xoxb-445228206210-445416796645-QOUSh3T34qBBlIUSRm2i8B9h";
            final SlackWebApiClient mWebApiClient = SlackClientFactory.createWebApiClient(slack_token);
            List<Channel> channels = mWebApiClient.getChannelList();
            String channelId = "";
            for(Channel c: channels){
                if(c.getName().equals("general")){
                    channelId = c.getId();
                    System.out.println(c.getName());
                    break;
                }
            }
            mWebApiClient.meMessage(channelId,  input[0] + " " + "start");

            return output;
        }

    }
}



