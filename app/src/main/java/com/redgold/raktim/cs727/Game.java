package com.redgold.raktim.cs727;

import java.security.SecureRandom;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

public class Game extends Activity {

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

    SecureRandom random = new SecureRandom();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(getClass().getSimpleName(), "onCreate");

        int first = getIntent().getIntExtra(FIRST_MOVE, RANDOM_FIRST);
        cells = getCells(first);

        board = new Board(this);
        setContentView(board);
        board.requestFocus();

        if (first != CONTINUE && computerSymbol.equals(SYMBOL_X)) {
            doComputerMove();
        }
    }

    public void doComputerMove() {
        // Dumb IA. Just find the next available unused cell...
        for (int i = 0; i < cells.length; i++) {
            if (cells[i].equals(SYMBOL_SPACE)) {
                cells[i] = computerSymbol;
                break;
            }
        }

        board.invalidate();
        isGameOver();
    }

    public boolean isGameOver() {
        int[] winner = findWinner();

        if (winner != null) {
            if (cells[winner[0]].equals(playerSymbol)) {
                showEndOfGame("Congratulations!  You won this game!");
                return true;
            } else {
                showEndOfGame("Opps, the computer won this game.");
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
                showEndOfGame("Nobody won!  Better luck next time.");
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