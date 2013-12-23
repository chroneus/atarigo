package com.chroneus.atarigo;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class BoardTest {
	Board board;
	Engine engine;

	@Before
	public void setup() {
		board = new Board();
		board.clear();
		engine = new Engine();
	}

	@Test
	public void convertMoves() {
		if (Board.SIZE == 9) {
			assertEquals(Board.convertFromGTPMove("A9"), 0);
			assertEquals(Board.convertFromGTPMove("J1"), 80);
			assertEquals(Board.convertFromGTPMove("J9"), 8);
		}

		for (int i = 0; i < Board.SIZE * Board.SIZE; i++) {
			String move = Board.convertToGTPMove(i);
			assertEquals(Board.convertFromGTPMove(move), i);
		}
	}


	// @Test
	public void heapSize() {
		List<Board> list = new ArrayList<Board>();
		try {
			while (true) {
				Board board = new Board();
				for (int i = 0; i < Board.SIZE * Board.SIZE / 2; i++) {
					board.play_move(i);
				}
				list.add(board);

				if (list.size()%100000==0) System.out.println(list.size());
			}
		} catch (OutOfMemoryError e) {

		} finally {
			System.out.println(list.size());
		}
	}


	@Test
	public void connectedGroups() {
		board.loadSGFLine(";B[cc];W[cd];B[dc];W[dd];B[ed];W[ec];B[fc];W[eb];B[fd];W[ef];B[fb];W[hf];B[ff];W[fg];B[gg];W[gf];B[fe];W[hg];B[fh];W[eg];B[gh]");
		assertEquals(board.connectedGroup(false).length, 3);
		assertEquals(board.connectedGroup(true).length, 4);
	}
  
	
	@Test
	public void connectedGroup() {
		board.play_move(false, "A3");
		board.play_move(false, "A2");
		board.play_move(false, "A4");
		board.play_move(false, "B4");
		board.play_move(false, "C4");
		board.play_move(false, "C2");
		board.play_move(false, "C3");
		board.play_move(false, "C6");
		board.play_move(false, "D6");
		board.play_move(false, "E7");
		board.play_move(true, "A1");
		BitBoard[] lists = board.connectedGroup(false);
		for (int i = 0; i < lists.length; i++) {
		//	System.out.println(lists[i]);
		}
	}

	@Test
	public void getPossibleMoves() {
		board.loadSGFLine(";B[cc];W[cd];B[dc];W[dd];B[ed];W[ec];B[fc];W[eb];B[fd];W[ef];B[fb];W[hf];B[ff];W[fg];B[gg];W[gf];B[fe];W[hg];B[fh];W[eg];B[gh];W[hh];B[be];W[eh];B[af];W[gi]))");
		BitBoard moves = engine.getAllConsidearableMoves(board);
		assertNull(moves);
	}


	//@Test
	public void getEye() {
		board.loadSGFLine(";B[bc];W[bb];B[cb];W[ab];B[ca];W[ba];B[ac]");
		BitBoard bitboard=new BitBoard();
		bitboard.set(1);
		bitboard=board.growGroupFromSeed(bitboard);
		BitBoard eyes = board.getEyes(bitboard);
		BitBoard test1=new BitBoard();
		test1.set(0);
		assertEquals(test1, eyes);
		board.clear();
		board.loadSGFLine(";B[fc];B[gd];B[ge];B[ff];B[ee];B[ed];B[df];B[eg];W[fb];W[gb];W[eb];W[dc];W[dd];W[de];W[cf];W[eh];W[fg];W[gf];W[he];W[hd];W[hc];W[dg]");
		
		assertEquals(board.getEyes(board.black).cardinality(),1);
	}

	@Test
	public void containsSubBitSet() throws Exception {
		board.play_move(false, "A3");
		board.play_move(false, "A2");
		board.play_move(false, "A4");
		board.play_move(false, "B4");
		board.play_move(false, "C4");
		board.play_move(false, "C2");
		board.play_move(false, "C3");
		board.play_move(false, "C6");
		board.play_move(false, "D6");	
		BitBoard connected=new BitBoard(2,2);
		connected.set(0);
		connected.set(1);
		assertEquals(Board.containsSubBitBoard(board.black, connected),board.black);
		board.play_move(false, "E7");
		assertNotSame(Board.containsSubBitBoard(board.black, connected),board.black);
	}
    
	


}
