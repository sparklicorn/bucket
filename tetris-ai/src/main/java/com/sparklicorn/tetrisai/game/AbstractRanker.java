package com.sparklicorn.tetrisai.game;

import com.sparklicorn.bucket.tetris.TetrisGame;
import com.sparklicorn.bucket.tetris.util.structs.Shape;

public abstract class AbstractRanker implements ITetrisStateRanker {
	/**
	 * Ranks a game state according to how desirable the state is.
	 * A higher value indicates a higher desirability for the given state.
	 *
	 * @param state Represents the blocks currently placed on the Tetris board,
	 *     excluding the active piece, if it exists.
	 *     <br>The array indices correspond with the rows and columns of the
	 *     game board, where (0,0) is the top-left cell.
	 * @param rows Number of rows on the board.
	 * @param cols Number of columns on the board.
	 * @param next The next game piece's shape.
	 * @return A value representing the desirability of the givens game state.
	 */
	@Override public double rank(TetrisGame game) {
		int[] board = game.getBlocksOnBoard();

		//set all top row empty spaces to -1
		final int rows = game.getNumRows();
		final int cols = game.getNumCols();
		for (int c = 0; c < cols; c++) {
			for (int r = 0; r < rows; r++) {
				if (board[r * cols + c] == 0) {
					board[r * cols + c] = -1;
				} else {
					break;
				}
			}
		}

		int completeLines = 0;  //number of complete lines
		int sumBlockHeights = 0; //sum of block heights
		int sumBlockedSpaces = 0; //number of empty cells without LOS to ceiling
		int deepHoles = 0;   //number of gaps that are > 2 blocks deep AND NOT ON LEFT OR RIHT SIDES
		int deepHolesOnSides = 0;  //number of > 2 block gaps on LEFT or RIGHT sides only

		int[] topBlocks = new int[cols]; //height in each column

		for (int r = 0; r < rows; r++) {
			boolean line = true;
			for (int c = 0; c < cols; c++) {
				if (board[r * cols + c] > 0) {
					sumBlockHeights += rows - r;
					if (topBlocks[c] == 0) {
						topBlocks[c] = rows - r;
					}
				} else {
					line = false;
				}
				if (board[r * cols + c] == 0) {
					sumBlockedSpaces++;
				}
			}
			if (line) {
				completeLines++;
			}
		}

		if (topBlocks[1] - topBlocks[0] > 2) {
			deepHolesOnSides++;
		}
		for (int c = 1; c < cols - 1; c++) {
			if ((topBlocks[c - 1] - topBlocks[c] > 2) && (topBlocks[c + 1] - topBlocks[c] > 2)) {
				deepHoles++;
			}
		}
		if (topBlocks[cols - 2] - topBlocks[cols - 1] > 2) {
			deepHolesOnSides++;
		}

		// 1 deep hole is fine (for line block)
		// deepHoles = (deepHoles > 1) ? deepHoles - 1 : 0;
		// int excessDeepHolesInMiddle = (deepHoles > 0) ? deepHoles - 1 : 0;
		// int hasSingleLineBlockHoleInMiddle = (deepHoles == 1) ? 1 : 0;
		// int hasLineBlockHoleOnBothSides = (deepHolesOnSides > 1) ? 1 : 0;
		// int hasSingleLineBlockHoleOnSide = (deepHolesOnSides == 1) ? 1 : 0;

		return rankImpl(new int[] {
			completeLines, sumBlockHeights, sumBlockedSpaces, deepHoles, deepHolesOnSides
		});
	}

	/**
	 * Ranks a tetris state given an array of features.
	 * <br/>Features by index:
	 * <ol start="0">
	 * <li>Number of full rows.</li>
	 * <li>Sum of block heights.</li>
	 * <li>Number of empty cells without line of sight to the top.</li>
	 * <li>Number of gaps > 2 blocks deep.</li>
	 * <li>Shape index of the next piece. See {@link Shape}.</li>
	 * </ol>
	 *
	 * @param features - Array of quantifiable features gathered from a tetris state.
	 * @return A value representing the desirability of the givens game state.
	 */
	protected abstract double rankImpl(int[] features);
}