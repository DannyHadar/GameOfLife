package com.example.danny.gameoflife;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.widget.Button;

/**
 * Created by Danny on 11-Mar-17.
 */

class GolMatrix {
    private static final int COLOR_ALIVE = Color.BLUE;
    private static final int COLOR_DEAD = Color.WHITE;

    private static final int LIVE_AGAIN = 3;
    private static final int LIVE_ONLY_IF_I_AM_ALIVE = 2;

    private boolean[][] mMatrix;
    private Button[][] mCells;

    GolMatrix(int size, Button[][] cells) {
        mMatrix = new boolean[size][size];
        mCells = cells;
    }

    void clearMatrix() {
        for (int i = 0; i < mMatrix.length; i++) {
            for (int j = 0; j < mMatrix[i].length; j++) {
                mMatrix[i][j] = false;
            }
        }
    }

    void promoteWorldOneGeneration() {
        for (int i = 0; i < mCells.length; i++) {
            for (int j = 0; j < mCells[i].length; j++) {
                int aliveNeighbours = getAliveNeighbours(i, j);

                ColorDrawable buttonColor = (ColorDrawable) mCells[i][j].getBackground();
                int color = buttonColor.getColor();

                mMatrix[i][j] = aliveNeighbours == LIVE_AGAIN || aliveNeighbours == LIVE_ONLY_IF_I_AM_ALIVE && color == (COLOR_ALIVE);
            }
        }

        equalizeCells();
    }

    private void equalizeCells() {
        for (int i = 0; i < mMatrix.length; i++) {
            for (int j = 0; j < mMatrix[i].length; j++) {
                mCells[i][j].setBackgroundColor(mMatrix[i][j] ? COLOR_ALIVE : COLOR_DEAD);
            }
        }
    }

    private int getAliveNeighbours(int row, int col) {
        int count = 0;
        int i = row == 0 ? 0 : row - 1;
        int z = col == 0 ? 0 : col - 1;

        for (; i <= row + 1 && i < mMatrix.length; i++) {
            for (int j = z; j <= col + 1 && j < mMatrix.length; j++) {
                ColorDrawable buttonColor = (ColorDrawable) mCells[i][j].getBackground();
                int color = buttonColor.getColor();
                if ((i != row || j != col) && color == COLOR_ALIVE) {
                    count++;
                }
            }
        }
        return count;
    }
}
