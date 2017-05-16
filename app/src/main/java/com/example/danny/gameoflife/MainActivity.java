package com.example.danny.gameoflife;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final int STARTING_INTERVAL = 200;
    public static final int MIN_INTERVAL = 80;
    public static final int MAX_INTERVAL = 800;

    private GolMatrix mMatrix;

    private TextView mGenerationsCountTextView;

    private Button mFasterButton;
    private Button mSlowerButton;
    private Button mStartButton;
    private Button mNextButton;
    private Button mClearButton;

    private Button[][] mCells;

    CmdButtonsListener mCmdButtonsListener;
    CellsButtonListener mCellsButtonsListener;

    private GenerationsTask mTask;

    private volatile int mInterval;
    private int mGenerationsCount;

    private volatile boolean mPromoteTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findAllViewsById();
        initObjects();
        setCmdButtonsListener();
        startOpeningDialog();
    }

    private void findAllViewsById() {
        // Buttons
        mFasterButton = (Button) findViewById(R.id.bt_faster);
        mSlowerButton = (Button) findViewById(R.id.bt_slower);
        mStartButton = (Button) findViewById(R.id.bt_start);
        mNextButton = (Button) findViewById(R.id.bt_next);
        mClearButton = (Button) findViewById(R.id.bt_clear);
        // TextView
        mGenerationsCountTextView = (TextView) findViewById(R.id.tv_generation_count);
    }

    private void initObjects() {
        mInterval = STARTING_INTERVAL;
        mGenerationsCount = 0;

        mPromoteTask = false;

        mCmdButtonsListener = new CmdButtonsListener();
        mCellsButtonsListener = new CellsButtonListener();

        mGenerationsCountTextView.setText(getString(R.string.generations_count_format, mGenerationsCount));
    }

    private void setCmdButtonsListener() {
        mFasterButton.setOnClickListener(mCmdButtonsListener);
        mSlowerButton.setOnClickListener(mCmdButtonsListener);
        mStartButton.setOnClickListener(mCmdButtonsListener);
        mNextButton.setOnClickListener(mCmdButtonsListener);
        mClearButton.setOnClickListener(mCmdButtonsListener);
    }

    private void startOpeningDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.welcome_to_game_of_life)).setMessage(getString(R.string.please_enter_world_size));

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);

        builder.setView(input).setPositiveButton(getString(R.string.go), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int worldSize = Integer.parseInt(input.getText().toString());

                buildWorld(worldSize);
            }
        }).show();
    }

    private void buildWorld(int worldSize) {
        LinearLayout rootLinearLayout = (LinearLayout) findViewById(R.id.ll_root);

        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        );

        param.setMargins(1, 1, 1, 1);

        mCells = new Button[worldSize][worldSize];

        LinearLayout[] linearLayouts = new LinearLayout[worldSize];

        for (int i = 0; i < mCells.length; i++) {
            linearLayouts[i] = new LinearLayout(this);
            for (int j = 0; j < mCells[i].length; j++) {
                mCells[i][j] = new Button(this);
                mCells[i][j].setBackgroundColor(Color.WHITE);
                mCells[i][j].setOnClickListener(mCellsButtonsListener);
                linearLayouts[i].addView(mCells[i][j], param);
            }
            rootLinearLayout.addView(linearLayouts[i], param);
        }
        mMatrix = new GolMatrix(worldSize, mCells);
    }

    private void clearAllCells() {
        for (Button[] cellRow : mCells) {
            for (Button cell : cellRow) {
                cell.setBackgroundColor(Color.WHITE);
            }
        }

        mMatrix.clearMatrix();
        mGenerationsCountTextView.setText(getString(R.string.generations_count_format, mGenerationsCount = 0));
    }

    private void promoteOneGeneration() {
        mMatrix.promoteWorldOneGeneration();
        mGenerationsCountTextView.setText(getString(R.string.generations_count_format, ++mGenerationsCount));
    }

    private class GenerationsTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                while (mPromoteTask) {
                    Thread.sleep(mInterval);
                    publishProgress();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            promoteOneGeneration();
        }
    }

    private class CmdButtonsListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.bt_next:
                    promoteOneGeneration();
                    break;
                case R.id.bt_start:
                    if (!mPromoteTask) {
                        startTaskPromote();
                    } else {
                        stopTaskPromote();
                    }
                    break;
                case R.id.bt_clear:
                    clearAllCells();
                    break;
                case R.id.bt_faster:
                    mInterval = mInterval > MIN_INTERVAL ? mInterval - 20 : mInterval;
                    break;
                case R.id.bt_slower:
                    mInterval = mInterval < MAX_INTERVAL ? mInterval + 20 : mInterval;
                    break;
            }
        }
    }

    private void stopTaskPromote() {
        mPromoteTask = false;
        mTask.cancel(true);
        mStartButton.setText(R.string.Start);
        mNextButton.setEnabled(true);
        mClearButton.setEnabled(true);
        mSlowerButton.setEnabled(false);
        mFasterButton.setEnabled(false);
    }

    private void startTaskPromote() {
        mPromoteTask = true;
        mTask = new GenerationsTask();
        mTask.execute();
        mStartButton.setText(R.string.Stop);
        mNextButton.setEnabled(false);
        mClearButton.setEnabled(false);
        mSlowerButton.setEnabled(true);
        mFasterButton.setEnabled(true);
    }

    private class CellsButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            int color = ((ColorDrawable) v.getBackground()).getColor();
            v.setBackgroundColor(color == Color.WHITE ? Color.BLUE : Color.WHITE);
        }
    }
}
