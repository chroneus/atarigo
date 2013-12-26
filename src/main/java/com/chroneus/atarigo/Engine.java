package com.chroneus.atarigo;

import java.util.*;
import java.util.concurrent.*;


public class Engine {
	ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	Random random = new Random();
	int depth_minimax_ply = 4;
	int move_to_consider_after_random = 20;
	static int random_game_to_check = 100;
	Integer result = null;
	public Board best_board, current_board;
	public static final byte SIZE = Board.SIZE;
	private static final boolean DEBUG = false;
	boolean am_i_white = false;
	public BitBoard[] white_connected, black_connected;

	public Engine() {
		white_connected = new BitBoard[0];
		black_connected = new BitBoard[0];
	}

	/**
	 * play random from possible variants
	 */
	public Integer playRandomMove(BitBoard set) {
		if (set.isEmpty()) return null;
		int length = set.size();
		int probe = random.nextInt(length);
		int low_test = probe, high_test = probe;
		while (!set.get(probe)) {
			low_test--;
			if (low_test >= 0 && set.get(low_test)) return low_test;
			high_test++;
			if (high_test < length && set.get(high_test)) return high_test;
		}
		return probe;
	}

	/**
	 * split to divided groups check if each group can be cut from others check
	 * if can kill any group check if my group alive maximize territory consider
	 * moves as good shape, good line, max territory
	 */
	public String doMove(Board board) {
		cacheConnected(board);
		this.am_i_white = board.is_white_next;
		if (this.result != null) {
			if (this.result == 1)
				return "pass";
			else
				return "resign";
		}
		if (board.black.cardinality() == 0) return Board.convertToGTPMove(SIZE * SIZE / 2);
		if (board.white.cardinality() == 0)
			return Board.convertToGTPMove(firstMoveDictionary(board.black.nextSetBit(0)));
		BitBoard possible_moves = getAllMoves(board);
		if (possible_moves == null || possible_moves.isEmpty()) {
			this.result = -1;
			return "resign";
		}
		int cardinality = possible_moves.cardinality();
		if (cardinality == 1) {
			board.play_move(possible_moves.nextSetBit(0));
			if (board.is_terminal()) this.result = 1;
			board.undo_move(possible_moves.nextSetBit(0));
			return Board.convertToGTPMove(possible_moves.nextSetBit(0));
		}

		possible_moves = weakGroupsCheck(board, possible_moves);
		if(cardinality>40)
		 possible_moves = filterBoardWithRandom(board,
		 possible_moves,10);
		int best_value = alphabeta(board, possible_moves, depth_minimax_ply, Integer.MIN_VALUE, Integer.MAX_VALUE, true);
		if (DEBUG) {
			System.out.println(best_board);
			System.out.println(best_value + "=" + countBoard(best_board));
		}
		BitBoard move;
		if (am_i_white) {
			move = best_board.white;
			move.andNot(board.white);
		}
		else {
			move = best_board.black;
			move.andNot(board.black);
		}
		if (DEBUG) System.out.println("best value " + best_value);
		if (best_value < -30) return "resign";
		return Board.convertToGTPMove(move.nextSetBit(0));
	}

	private BitBoard weakGroupsCheck(Board board, BitBoard possible_moves) {
		BitBoard[] my_groups = getMyCached(board);
		BitBoard[] opponent_groups = getOpponentCached(board);
		BitBoard moves = new BitBoard();
		boolean weakness_flag = false;
		for (BitBoard my_group : my_groups) {
			if (isWeakGroup(board, my_group)) {
				weakness_flag = true;
				moves.or(tryToRescueWeakGroup(board, my_group));
			}
		}
		if (weakness_flag) return moves;
		for (BitBoard opponent_group : opponent_groups) {
			if (isWeakGroup(board, opponent_group)) {
				int capture_move = tryToCaptureWeakGroup(board, opponent_group);
				if (capture_move != -1) {
					moves.set(capture_move);
					return moves;

				}
			}
		}
		return possible_moves;
	}

	private int firstMoveDictionary(int nextSetBit) {
		if (nextSetBit == SIZE * SIZE / 2) return SIZE * SIZE / 4;
		BitBoard position = new BitBoard();
		position.set(nextSetBit);
		position.and(BoardConstant.FirstLine);
		if (!position.isEmpty()) {
			BitBoard move = position.nearest_stones();
			move.and(BoardConstant.SecondLine);
			return move.nextSetBit(0);
		}
		position = new BitBoard();
		position.set(nextSetBit);
		position.and(BoardConstant.SecondLine);
		if (!position.isEmpty()) {
			position = position.nearest_stones();
			position.and(BoardConstant.ThirdLine);
			return position.nextSetBit(0);
		}
		position = new BitBoard();
		position.set(nextSetBit);
		position = position.rotate90().rotate90();
		position.and(BoardConstant.ThirdLine);
		return position.nextSetBit(0);
	}

	/**
	 * subset of @param possible_moves on @param board which contains several
	 * (move_to_consider_after_random)best moves from a point of series of
	 * random game
	 */
	BitBoard filterBoardWithRandom(Board board, BitBoard possible_moves, int moves_to_consider) {
		try {
			List<Callable<Integer[]>> tasks = new ArrayList<Callable<Integer[]>>();
			for (int i = possible_moves.nextSetBit(0); i >= 0; i = possible_moves.nextSetBit(i + 1)) {
				tasks.add(new RandomGameCallable(board, i, this));
			}
			int[] best_moves = new int[moves_to_consider];
			int[] best_moves_positions = new int[moves_to_consider];
			Arrays.fill(best_moves, Integer.MIN_VALUE);
			List<Future<Integer[]>> futures = executor.invokeAll(tasks);
			for (Iterator<Future<Integer[]>> iterator = futures.iterator(); iterator.hasNext();) {
				Future<Integer[]> future = iterator.next();
				for (int j = 0; j < best_moves.length; j++) { // bubble sort :))
					if (future.isDone()) {
						int move = future.get()[0];
						int score = future.get()[1];
						if (score >= best_moves[j]) {
							if (j > 0) {
								best_moves_positions[j - 1] = best_moves_positions[j];
								best_moves_positions[j] = move;
								best_moves[j - 1] = best_moves[j];
								best_moves[j] = score;
							}
							else
								best_moves[0] = score;
							best_moves_positions[0] = move;
						}
					}
				}
			}
			BitBoard result = new BitBoard();
			for (int i = 0; i < best_moves_positions.length; i++) {
				result.set(best_moves_positions[i]);
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return possible_moves;
		}
	}

	/**
	 * returns normalized white score (-100..+100)
	 */
	Integer testBoardOnRandomPlay(Board board, int move) {

		Board newboard = (Board) board.clone();
		newboard.play_move(move);
		int white_wins = 0, black_wins = 0;
		for (int i = 0; i < random_game_to_check; i++) {
			if (playRandomGame(newboard))
				white_wins++;
			else
				black_wins++;
		}
		return am_i_white ? (white_wins - black_wins) * 100 / random_game_to_check : (black_wins - white_wins) * 100
				/ random_game_to_check;

	}

	private void cacheConnected(Board board) {
		this.current_board = (Board) board.clone();
		this.white_connected = board.connectedGroup(Board.WHITE);
		this.black_connected = board.connectedGroup(Board.BLACK);
	}

	/**
	 * counting function
	 */
	private int countBoard(Board board) {
		int myliberties = 0;
		BitBoard[] connected = getMyCached(board), opponentconnected = getOpponentCached(board);

		for (BitBoard eachGroup : connected) {
			int liberties = board.getLiberties(eachGroup).cardinality();
			myliberties += liberties;
			if (liberties < 2) return Integer.MIN_VALUE;
			if (liberties == 2 && checkLadder(board, eachGroup) != null) return Integer.MIN_VALUE;
			if (liberties == 3) myliberties -= 10;
		}
		int opponentliberties = 0;

		for (BitBoard eachGroup : opponentconnected) {
			int liberties = board.getLiberties(eachGroup).cardinality();
			opponentliberties += liberties;
			if (liberties < 2) return Integer.MAX_VALUE;
			if (liberties == 2 && checkLadder(board, eachGroup) != null) return Integer.MAX_VALUE;
		}

		return myliberties - opponentliberties + countTerritory(board);
	}

	private BitBoard[] getOpponentCached(Board board) {
		if (am_i_white) {
			return board.connectedGroupAddElement(this.current_board.black, this.black_connected, board.black);
		}
		else {
			return board.connectedGroupAddElement(this.current_board.white, this.white_connected, board.white);

		}
	}

	private BitBoard[] getMyCached(Board board) {
		if (am_i_white) {
			return board.connectedGroupAddElement(this.current_board.white, this.white_connected, board.white);
		}
		else {
			return board.connectedGroupAddElement(this.current_board.black, this.black_connected, board.black);
		}
	}

	/**
	 * score territory
	 */
	public int countTerritory(Board testboard) {
		Board board = testboard.fillBoardWithNearestStones(-1);
		int local_result = board.black.cardinality() - board.white.cardinality();
		if (am_i_white) local_result = -local_result;
		return local_result;
	}

	/**
	 * 
	 * @param board
	 * @return if white wins
	 */
	public Boolean playRandomGame(Board board) {
		Board newboard = (Board) board.clone();
		while (!newboard.is_terminal()) {
			BitBoard possible_moves = getClearCells(newboard);
			// getAllConsidearableMoves(newboard); //slow down a lot

			if (possible_moves == null || possible_moves.isEmpty())
				return !newboard.is_white_next;
			else
				newboard.play_move(playRandomMove(possible_moves));
		}

		if (newboard.minLiberties(true) == 0) return false;
		if (newboard.minLiberties(false) == 0) return true;
		return null;
	}

	/**
	 * get all points which is not settled
	 */
	public BitBoard getClearCells(Board board) {
		BitBoard moves = new BitBoard();
		moves.not();
		moves.andNot(board.black);
		moves.andNot(board.white);

		return moves;
	}

	/**
	 * consider self-atari and ladders
	 */
	public BitBoard getAllMoves(Board board) {
		BitBoard moves = getClearCells(board);

		// fast win check
		BitBoard[] connected_opponent = board.connectedGroup(!board.is_white_next);
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
				}
				else {
					return liberty_move;
				}
			}
		}

		if (board.white.cardinality() < SIZE - 1) moves.andNot(BoardConstant.FirstLine);
		// suicide check
		for (int i = moves.nextSetBit(0); i >= 0; i = moves.nextSetBit(i + 1)) {
			board.play_move(i);
			int minLiberties = board.minLiberties(!board.is_white_next);
			if (minLiberties < 2) moves.clear(i);
			if (minLiberties == 2) {
				BitBoard[] only2liberties_boards = board.connectedGroup(!board.is_white_next);
				for (BitBoard check4liberties : only2liberties_boards) {
					if (checkLadder(board, check4liberties) != null) moves.clear(i);
				}
			}
			// BitBoard seed = new BitBoard();
			// seed.set(i);
			// if (is_weak_group(board, seed)) moves.clear(i);
			board.undo_move(i);
		}

		return moves;
	}

	// check ladder or net (liberty count =2)
	BitBoard checkLadder(Board board, BitBoard groupToKill) {

		BitBoard liberty_move = board.getLiberties(groupToKill);
		if (liberty_move.cardinality() > 2) return null;
		if (liberty_move.cardinality() < 2) return liberty_move;

		int miai1 = liberty_move.nextSetBit(0);
		int miai2 = liberty_move.nextSetBit(miai1 + 1);
		Board first_variant = (Board) board.clone();
		first_variant.play_move(miai1);
		if (first_variant.minLiberties(!first_variant.is_white_next) < 2) return null;
		first_variant.play_move(miai2);
		BitBoard group_to_kill_check1 = first_variant.growGroupFromSeed(groupToKill);
		if (checkLadder(first_variant, group_to_kill_check1) != null) {
			liberty_move.clear(miai2);
			return liberty_move;
		}
		Board second_variant = (Board) board.clone();
		second_variant.play_move(miai2);
		if (second_variant.minLiberties(!second_variant.is_white_next) < 2) return null;
		second_variant.play_move(miai1);
		BitBoard group_to_kill_check2 = second_variant.growGroupFromSeed(groupToKill);
		if (checkLadder(second_variant, group_to_kill_check2) != null) {
			liberty_move.clear(miai1);
			return liberty_move;
		}
		return null;

	}

	boolean isWeakGroup(Board board, BitBoard seed) {
		BitBoard test_group = board.growGroupFromSeed(seed);
		Board moyo = board.getMoyo(test_group.nearest_stones());
		BitBoard moyo_test_group = moyo.growGroupFromSeed(seed);
		moyo_test_group.andNot(test_group);
		return moyo_test_group.cardinality() < 3;
	}

	/**
	 */
	int tryToCaptureWeakGroup(Board board, BitBoard stone_group) {
		if (board.getEyes(stone_group).cardinality() > 1) return -1;
		BitBoard near_moves = stone_group.nearest_stones();
		BitBoard all_moves = getAllMoves(board);
		if (all_moves.isEmpty()) return 100;
		near_moves.and(all_moves);
		for (int i = near_moves.nextSetBit(0); i >= 0; i = near_moves.nextSetBit(i + 1)) {
			board.play_move(i);
			if (tryToRescueWeakGroup(board, stone_group).isEmpty()) return i;
			board.undo_move(i);
		}
		return -1;
	}

	BitBoard tryToRescueWeakGroup(Board board, BitBoard stone_group) {
		BitBoard result = new BitBoard();
		if (board.getEyes(stone_group).cardinality() > 1) return result;
		BitBoard near_moves = stone_group.nearest_stones();
		BitBoard all_moves = getAllMoves(board);
		if (all_moves.isEmpty()) return result;
		near_moves.and(all_moves);
		for (int i = near_moves.nextSetBit(0); i >= 0; i = near_moves.nextSetBit(i + 1)) {
			board.play_move(i);
			if (tryToCaptureWeakGroup(board, stone_group) == -1) result.set(i);
			board.undo_move(i);
		}
		return result;
	}

	/**
	 * @see http://en.wikipedia.org/wiki/Alpha–beta_pruning
	 */
	int alphabeta(Board board, BitBoard moves, int depth, int α, int β, boolean maximizingPlayer) {
		if (depth == 0 || board.is_terminal() || moves == null || moves.isEmpty()) {
			return countBoard(board);
		}
		if (maximizingPlayer) {
			for (int i = moves.nextSetBit(0); i >= 0; i = moves.nextSetBit(i + 1)) {
				board.play_move(i);
				int score = alphabeta(board, getAllMoves(board), depth - 1, α, β, false);
				if (score > α) {
					α = score;
					if (depth == depth_minimax_ply) {
						best_board = (Board) board.clone();
					}
				}
				board.undo_move(i);
				if (β <= α) break;// (* β cut-off *)
			}
			return α;
		}
		else {
			for (int i = moves.nextSetBit(0); i >= 0; i = moves.nextSetBit(i + 1)) {
				board.play_move(i);
				β = Math.min(β, alphabeta(board, getAllMoves(board), depth - 1, α, β, true));
				board.undo_move(i);
				if (β <= α) break; // (* α cut-off *)
			}
			return β;
		}
	}

}
