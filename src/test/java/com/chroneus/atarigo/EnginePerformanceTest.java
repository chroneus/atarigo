package com.chroneus.atarigo;


import org.junit.Before;
import org.junit.Test;

public class EnginePerformanceTest {
	Board board;
	Engine engine;

	@Before
	public void setup() {
		board = new Board();
		board.clear();
		engine = new Engine();
	}

	 @Test
	public void testPlayRandom() {
		for (int i = 0; i < Engine.SIZE; i++) {
			for (int j = 0; j < Engine.SIZE; j++) {
				double testfrombegin = engine.testBoardOnRandomPlay(board, i
						* Engine.SIZE + j, 1000);
				System.out.printf("%.2f ", testfrombegin);
			}
			System.out.println();
		}
	}

//	@Test
	public void testPVS() {
		board.play_move(40);
		for (int i = 0; i < Engine.SIZE; i++) {
			for (int j = 0; j < Engine.SIZE; j++) {
				board.play_move(i * Engine.SIZE + j);
				System.out.printf("%3d", engine.alphabeta(board, 
						engine.getAllPossibleMoves(board),4,
						Integer.MIN_VALUE, Integer.MAX_VALUE,true));
				board.undo_move(i * Engine.SIZE + j);
			}
			System.out.println();
		}

	}

	@Test
	public void testCount() {
		board.play_move(40);
        for (int i = 0; i < Engine.SIZE; i++) {
			for (int j = 0; j < Engine.SIZE; j++) {
				int move = i * Engine.SIZE + j;
				if (move != 40) {
					board.play_move(move);
					System.out.printf("%4d", engine.countBoard(board));					
					board.undo_move(move);
				} else {
					System.out.print(" * ");
				}
			}
			System.out.println();
		}
	}

	@Test
	public void testGenMove() {
		//board.play_move(40);
		System.out.println(engine.doMove(board));

	}
}
