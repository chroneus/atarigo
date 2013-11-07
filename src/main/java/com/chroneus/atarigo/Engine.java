package com.chroneus.atarigo;

import java.util.*;

public class Engine {
	Random random = new Random();
	int depth_minimax_ply = 4;
	int move_to_consider_after_random = 10;
	int max_ply = 1000;
	Integer result = null;
	public static final byte SIZE = Board.SIZE;
	private static final boolean DEBUG = false;
	boolean am_i_white = false;
	static long node_processed = 0;

	public Engine() {
	}

	/**
	 * play random from set
	 */
	public Integer playRandomMove(BitBoard set) {
		if (set.isEmpty())
			return null;
		int length = set.size();
		int probe = random.nextInt(length);
		int low_test = probe, high_test = probe;
		while (!set.get(probe)) {
			low_test--;
			if (low_test >= 0 && set.get(low_test))
				return low_test;
			high_test++;
			if (high_test < length && set.get(high_test))
				return high_test;
		}
		return probe;
	}

	/**
	 * split to divided groups check if each group can be cut from others check
	 * if can kill any group check if my group alive maximize territory consider
	 * moves as good shape, good line, max territory
	 */

	public String doMove(Board board) {
		if (this.result != null) {
			if (this.result == 1)
				return "pass";
			else
				return "resign";
		}
		am_i_white = board.is_white_next;
		BitBoard possible_moves = getAllPossibleMoves(board);
		int cardinality = possible_moves.cardinality();
		if (possible_moves == null || possible_moves.isEmpty()) {
			this.result = -1;
			return "resign";
		}

		if (cardinality == 1) {
			board.play_move(possible_moves.nextSetBit(0));
			if (board.is_terminal())
				this.result = 1;
			board.undo_move(possible_moves.nextSetBit(0));
			return Board.convertToGTPMove(possible_moves.nextSetBit(0));
		}
		int[] filtered_moves = filterBoardWithRandom(board, possible_moves);
		int best_move = filtered_moves[0], best_value = -100;
		for (int i = 0; i < filtered_moves.length; i++) {
			board.play_move(filtered_moves[i]);
			int test_value = pvs(board, depth_minimax_ply, Integer.MIN_VALUE,
					Integer.MAX_VALUE);
			if (test_value > best_value) {
				best_move = filtered_moves[i];
				best_value = test_value;
			}
			board.undo_move(filtered_moves[i]);
		}
		if (DEBUG)
			System.out.println("best value " + best_value);
		if (best_value < -30)
			return "resign";
		return Board.convertToGTPMove(best_move);
	}

	int[] filterBoardWithRandom(Board board, BitBoard possible_moves) {
		int[] filtered_moves = new int[move_to_consider_after_random];
		int[] filtered_moves_values = new int[move_to_consider_after_random];
		Arrays.fill(filtered_moves_values, Integer.MIN_VALUE);
		int random_to_test = max_ply / (possible_moves.cardinality() + 10);
		for (int i = possible_moves.nextSetBit(0); i >= 0; i = possible_moves
				.nextSetBit(i + 1)) {
			int current_weight = testBoardOnRandomPlay(board, i, random_to_test);
			for (int j = 0; j < filtered_moves_values.length; j++) {
				if (current_weight > filtered_moves_values[j]) {
					for (int shift = move_to_consider_after_random - 1; shift > j; shift--) {
						filtered_moves[shift] = filtered_moves[shift - 1];
						filtered_moves_values[shift] = filtered_moves_values[shift - 1];
					}
					filtered_moves_values[j] = current_weight;
					filtered_moves[j] = i;
					break;
				}
			}
			// board.undo_move(i);
		}
		return filtered_moves;
	}

	/**
	 * percentage white win
	 */
	int testBoardOnRandomPlay(Board board, int move, int number_of_attempt) {
		Board newboard = (Board) board.clone();
		newboard.play_move(move);
		int white_wins = 0, black_wins = 0;
		for (int i = 0; i < number_of_attempt; i++) {
			if (playRandomGame(newboard))
				white_wins++;
			else
				black_wins++;
		}
		return am_i_white ? (white_wins - black_wins) * 100 / number_of_attempt
				: (black_wins - white_wins) * 100 / number_of_attempt;
	}

	int countBoard(Board board) {
		int myliberties = 0;
		BitBoard[] connected = board.connectedGroup(!board.is_white_next);
		for (BitBoard eachGroup : connected) {
			int liberties = board.getLiberties(eachGroup).cardinality();
			myliberties += liberties;
			if (liberties < 2)
				return Integer.MIN_VALUE;
			if (liberties == 2 && checkLadder(board, eachGroup) != null)
				return Integer.MIN_VALUE;
		}
		int opponentliberties = 0;
		BitBoard[] opponentconnected = board
				.connectedGroup(board.is_white_next);
		for (BitBoard eachGroup : opponentconnected) {
			int liberties = board.getLiberties(eachGroup).cardinality();
			opponentliberties += liberties;
			if (liberties < 2)
				return Integer.MAX_VALUE;
			if (liberties == 2 && checkLadder(board, eachGroup) != null)
				return Integer.MAX_VALUE;
		}

		return myliberties - opponentliberties + countTerritory(board);
	}

	public int countTerritory(Board testboard) {
		Board board = (Board) testboard.clone();
		while (board.white.cardinality() + board.black.cardinality() < SIZE
				* SIZE) {
			if (board.is_white_next) {
				BitBoard whiteaddon = board.white.nearest_stones();
				whiteaddon.andNot(board.black);
				board.white.or(whiteaddon);
				BitBoard blackaddon = board.black.nearest_stones();
				blackaddon.andNot(board.white);
				board.black.or(blackaddon);
			} else {
				BitBoard blackaddon = board.black.nearest_stones();
				blackaddon.andNot(board.white);
				board.black.or(blackaddon);
				BitBoard whiteaddon = board.white.nearest_stones();
				whiteaddon.andNot(board.black);
				board.white.or(whiteaddon);
			}
		}
		int local_result = board.black.cardinality()
				- board.white.cardinality();
		if (testboard.is_white_next)
			local_result = -local_result;
		return local_result;
	}

	/**
	 * 
	 * @param newboard
	 * @return if white wins
	 */
	public Boolean playRandomGame(Board board) {
		Board newboard = (Board) board.clone();
		while (!newboard.is_terminal()) {
			BitBoard possible_moves = getClearCells(newboard);
			// getAllPossibleMoves(newboard); slow down a lot

			if (possible_moves == null || possible_moves.isEmpty())
				return !newboard.is_white_next;
			else
				newboard.play_move(playRandomMove(possible_moves));
		}

		if (newboard.minLiberties(true) == 0)
			return false;
		if (newboard.minLiberties(false) == 0)
			return true;
		return null;
	}

	public BitBoard getClearCells(Board board) {
		BitBoard moves = new BitBoard();
		moves.not();
		moves.andNot(board.black);
		moves.andNot(board.white);

		return moves;
	}

	public BitBoard getAllPossibleMoves(Board board) {
		BitBoard moves = getClearCells(board);
		
		if (moves.cardinality() < 2 * (SIZE-1))
			moves.andNot(BoardConstant.FirstLine);
        
		
		// fast win check
		BitBoard[] connected_opponent = board
				.connectedGroup(!board.is_white_next);
		for (int i = 0; i < connected_opponent.length; i++) {
			BitBoard liberty_move = board.getLiberties(connected_opponent[i]);
			if (liberty_move.cardinality() == 1) { // will win in atari go
				return liberty_move;
			}
			if (liberty_move.cardinality() == 2) { // check the ladder

				if (checkLadder(board, connected_opponent[i]) != null) {
					return checkLadder(board, connected_opponent[i]);
				}
			}

		}
		// single move check
		BitBoard[] connected = board.connectedGroup(board.is_white_next);
		for (int i = 0; i < connected.length; i++) {
			BitBoard liberty_move = board.getLiberties(connected[i]);
			if (liberty_move.cardinality() == 1) {
				Board newboard = (Board) board.clone();
				newboard.play_move(liberty_move.nextSetBit(0));
				if (newboard.minLiberties(board.is_white_next) < 2) {
					return null;
				} else {
					return liberty_move;
				}
			}
		}
		// suicide check
		for (int i = moves.nextSetBit(0); i >= 0; i = moves.nextSetBit(i + 1)) {
			board.play_move(i);
			int minLiberties = board.minLiberties(!board.is_white_next);
			if (minLiberties < 2)
				moves.clear(i);
			if (minLiberties == 2) {
				BitBoard[] only2liberties_boards = board
						.connectedGroup(!board.is_white_next);
				for (BitBoard check4liberties : only2liberties_boards) {
					if (checkLadder(board, check4liberties) != null)
						moves.clear(i);
				}
			}
			board.undo_move(i);
		}

		return moves;
	}

	// check ladder or net
	BitBoard checkLadder(Board board, BitBoard groupToKill) {

		BitBoard liberty_move = board.getLiberties(groupToKill);
		if (liberty_move.cardinality() > 2)
			return null;
		if (liberty_move.cardinality() < 2)
			return liberty_move;

		int miai1 = liberty_move.nextSetBit(0);
		int miai2 = liberty_move.nextSetBit(miai1 + 1);
		Board first_variant = (Board) board.clone();
		first_variant.play_move(miai1);
		if (first_variant.minLiberties(!first_variant.is_white_next) < 2)
			return null;
		first_variant.play_move(miai2);
		BitBoard group_to_kill_check1 = first_variant
				.growGroupFromSeed(groupToKill);
		if (checkLadder(first_variant, group_to_kill_check1) != null) {
			liberty_move.clear(miai2);
			return liberty_move;
		}
		Board second_variant = (Board) board.clone();
		second_variant.play_move(miai2);
		if (second_variant.minLiberties(!second_variant.is_white_next) < 2)
			return null;
		second_variant.play_move(miai1);
		BitBoard group_to_kill_check2 = second_variant
				.growGroupFromSeed(groupToKill);
		if (checkLadder(second_variant, group_to_kill_check2) != null) {
			liberty_move.clear(miai1);
			return liberty_move;
		}
		return null;

	}

	/**
	 * @see http://en.wikipedia.org/wiki/Negascout
	 */
	int pvs(Board board, int depth, int α, int β) {
		node_processed++;
		if (DEBUG && node_processed % 1000 == 0)
			System.out.println(node_processed);
		BitBoard moves = getAllPossibleMoves(board);
		if (depth == 0 || board.is_terminal() || moves.isEmpty())
			return (am_i_white==board.is_white_next) ? countBoard(board) : -countBoard(board);

		int[] filtered = filterBoardWithRandom(board, moves);
		for (int i : filtered) {
			int score;
			board.play_move(i);
			if (i != filtered[0]) {
				score = -pvs(board, depth - 1, -α - 1, -α);// search
																		// with
																		// a
																		// null
																		// window
				if (α < score && score < β) // if it failed high,
					score = -pvs(board, depth - 1, -β, -α); // do a
																		// full
																		// re-search
			} else
				score = -pvs(board, depth - 1, -β, -α);
			α = Math.max(α, score);
			board.undo_move(i);
			if (α >= β)
				break;

		}
		return α;
	}
}
