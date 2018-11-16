package com.redgold.raktim.cs727;

import java.security.SecureRandom;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import com.fasterxml.jackson.databind.JsonNode;
import allbegray.slack.SlackClientFactory;
import allbegray.slack.exception.SlackResponseErrorException;
import allbegray.slack.rtm.Event;
import allbegray.slack.rtm.EventListener;
import allbegray.slack.rtm.SlackRealTimeMessagingClient;
import allbegray.slack.type.Authentication;
import allbegray.slack.type.Channel;
import allbegray.slack.type.User;
import allbegray.slack.webapi.SlackWebApiClient;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;
import java.util.UUID;


public class Game extends Activity  {

    public static final String FIRST_MOVE = "com.bamafolks.android.games.tictactoe.first_move";
    public static final int PLAYER_FIRST = 0;
    public static final int COMPUTER_FIRST = 1;
    public static final int RANDOM_FIRST = 2;
    public static final int CONTINUE = -1;
    private static final String PREF_STATE = "tictactoe";
    private static final String PREF_PLAYER_SYMBOL = "playerSymbol";
    private static final String PREF_COMPUTER_SYMBOL = "computerSymbol";

    private String[] cells;
    private Board board;

    public static final String SYMBOL_SPACE = " ";
    public static final String SYMBOL_X = "X";
    public static final String SYMBOL_O = "O";

    private String playerSymbol;
    private String computerSymbol;
    public boolean me = false;
    final String uuid = UUID.randomUUID().toString();

    SecureRandom random = new SecureRandom();
    MyModel model = new MyModel();
    SlackConnection connection;
    private class MyObserver implements Observer {
        @Override
        public void update(Observable o, Object Arg) {
            try {
                int opponent_move = Integer.valueOf(((MyModel) o).output);
                cells[opponent_move] = computerSymbol;
//            for (int i = 0; i < cells.length; i++) {
//                if (cells[i].equals(SYMBOL_SPACE)) {
//                    cells[i] = computerSymbol;
//                    break;
//                }
//            }

                board.invalidate();
                isGameOver();
                System.out.print("change noticed" + ((MyModel) o).output);
                me = true;
            }
            catch(Exception e){
                System.out.println(e);
            }
        }

    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(getClass().getSimpleName(), "onCreate");

        int first = getIntent().getIntExtra(FIRST_MOVE, RANDOM_FIRST);
        cells = getCells(first);

        board = new Board(this);
        setContentView(board);
        board.requestFocus();
        me = true;
        if (first != CONTINUE && computerSymbol.equals(SYMBOL_X)) {
            //doComputerMove();
            me = false;
        }
        connection = new SlackConnection(this);
        //connection.execute("start game ");
        connection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"Started listener");
        model.addObserver(new MyObserver());

    }
    private class SlackConnection extends AsyncTask<String, String, String> {

        String output = null;
        Game game;
        public SlackConnection(Game game) {
            this.game  = game;
        }

        @Override
        protected String doInBackground(String... input) {
            String slack_token = "xoxb-445228206210-445416796645-QOUSh3T34qBBlIUSRm2i8B9h";
            final SlackWebApiClient mWebApiClient = SlackClientFactory.createWebApiClient(slack_token);
            String webSocketUrl = mWebApiClient.startRealTimeMessagingApi().findPath("url").asText();
            //SlackRealTimeMessagingClient mRtmClient = new SlackRealTimeMessagingClient(webSocketUrl);
            SlackRealTimeMessagingClient client = SlackClientFactory.createSlackRealTimeMessagingClient(slack_token);
            client.addListener(Event.HELLO, new EventListener() {
                @Override
                public void onMessage(JsonNode message) {

                    Authentication authentication = mWebApiClient.auth();
                    String mBotId = authentication.getUser_id();
                    //System.out.println("User id: " + mBotId);
                    //System.out.println("Team name: " + authentication.getTeam());
                    //System.out.println("User name: " + authentication.getUser());
                }
            });

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
                    if (userId != null  && userId.equals(mBotId) && !tokens[tokens.length - 1].equals(uuid)) { //
                        Channel channel;
                        try {
                            channel = mWebApiClient.getChannelInfo(channelId);
                        } catch (SlackResponseErrorException e) {
                            channel = null;
                        }
                        User user = mWebApiClient.getUserInfo(userId);
                        String userName = user.getName();

                        //System.out.println("Channel id: " + channelId);
                        //System.out.println("Channel name: " + (channel != null ? "#" + channel.getName() : "DM"));
                        //System.out.println("User id: " + userId);
                        //System.out.println("User name: " + userName);
                        //System.out.println("Text: " + text);

                        // Copy cat
                        //mWebApiClient.meMessage(channelId, text + " " + uuid);
                        String[] split_text  = text.split("\\s+");
                        onProgressUpdate(split_text);
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

                //client.close();
                //postMessage();
                //System.out.println(output);

                return output;
            } catch (Exception e){
                System.out.print(e);
                return "failing to connect to slack";
            }

        }

        @Override
        protected void onProgressUpdate(String... split_text){
            game.model.output = String.join(" ",Arrays.copyOfRange(split_text,0,split_text.length - 1));
            game.runOnUiThread(new Runnable(){
                public void run() {
                    model.change("changed");
                }
            });

            //System.out.println(game.model.output);
            //System.out.println(String.join(" ",Arrays.copyOfRange(split_text,0,split_text.length - 1)));
        }

    }


    public boolean isGameOver() {
        int[] winner = findWinner();

        if (winner != null) {
            if (cells[winner[0]].equals(playerSymbol)) {
                showEndOfGame(" Congratulations!  You won this game! ");
                return true;
            } else {
                showEndOfGame(" Oops, the computer won this game. ");
                return true;
            }
        } else {

            boolean tie = true;
            for (int i = 0; i < cells.length; i++)
                if (cells[i].equals(SYMBOL_SPACE)) {
                    tie = false;
                    break;
                }

            if (tie) {
                connection.cancel(true);
                showEndOfGame(" Nobody won!  Better luck next time. ");
                return true;
            }
        }

        return false;
    }

    private int[][] winningCombos = new int[][] { { 0, 1, 2 }, { 0, 3, 6 },
            { 0, 4, 8 }, { 1, 4, 7 }, { 2, 4, 6 }, { 2, 5, 8 }, { 3, 4, 5 },
            { 6, 7, 8 } };

    private int[] findWinner() {
        int[] winner = null;

        for (int i = 0; i < winningCombos.length; i++) {
            int[] combo = winningCombos[i];
            String s1 = cells[combo[0]];
            String s2 = cells[combo[1]];
            String s3 = cells[combo[2]];

            Log.d(getClass().getSimpleName(), "[" + combo[0] + "-" + combo[1]
                    + "-" + combo[2] + "] -> cell[" + combo[0] + "] = '" + s1
                    + "', cell[" + combo[1] + "] = '" + s2 + "', cell["
                    + combo[2] + "] = '" + s3 + "'");

            if (!s1.equals(SYMBOL_SPACE) && !s2.equals(SYMBOL_SPACE)
                    && !s3.equals(SYMBOL_SPACE))
                if (s1.equals(s2) && s2.equals(s3)) {
                    winner = combo;
                    break;
                }
        }

        return winner;
    }

    private void showEndOfGame(String msg) {
        TextView message = new TextView(this);
        message.setText(msg);

        new AlertDialog.Builder(this)
                .setTitle("Game Over")
                .setView(message)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                connection.cancel(true);
                                finish();

                            }
                        }).create().show();
    }

    public String[] getCells(int first) {
        if (first == CONTINUE) {
            SharedPreferences prefs = getPreferences(MODE_PRIVATE);
            playerSymbol = prefs.getString(PREF_PLAYER_SYMBOL, SYMBOL_X);
            computerSymbol = prefs.getString(PREF_COMPUTER_SYMBOL, SYMBOL_O);
            return TextUtils.split(
                    prefs.getString(PREF_STATE, " , , , , , , , , "), ",");
        }

        String[] grid = new String[9];
        for (int i = 0; i < 9; i++)
            grid[i] = SYMBOL_SPACE;

        switch (first) {
            case PLAYER_FIRST:
                playerSymbol = SYMBOL_X;
                computerSymbol = SYMBOL_O;
                break;
            case COMPUTER_FIRST:
                playerSymbol = SYMBOL_O;
                computerSymbol = SYMBOL_X;
                break;
            case RANDOM_FIRST:
                if (random.nextBoolean()) {
                    playerSymbol = SYMBOL_X;
                    computerSymbol = SYMBOL_O;
                } else {
                    playerSymbol = SYMBOL_O;
                    computerSymbol = SYMBOL_X;
                }
        }

        return grid;
    }

    public boolean setCellIfValid(int x, int y, String symbol) {
        int index = (y * 3) + x;
        if (!cells[index].equals(SYMBOL_SPACE))
            return false;
        cells[index] = symbol;
        //connection.cancel(true);
        return true;
    }

    public String getPlayerSymbol() {
        return playerSymbol;
    }

    public String getComputerSymbol() {
        return computerSymbol;
    }

    public String getCellString(int i, int j) {
        int index = (j * 3) + i;
        return cells[index];
    }



}