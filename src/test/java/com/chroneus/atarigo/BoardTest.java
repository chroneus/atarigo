package com.chroneus.atarigo;

import static org.junit.Assert.*;

import java.util.*;

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
board=new Board(" · · · · · · · · ·\r\n" + 
				" · · · · W B · · ·\r\n" + 
				" · · B B W B · · ·\r\n" + 
				" · · W W B B · · ·\r\n" + 
				" · · · · · B · · ·\r\n" + 
				" · · · · W B W W ·\r\n" + 
				" · · · · W W B W ·\r\n" + 
				" · · · · · B B · ·\r\n" + 
				" · · · · · · · · ·"
				);
		assertEquals(board.connectedGroup(false).length, 3);
		assertEquals(board.connectedGroup(true).length, 4);
	}
  
	@Test
	public void weakConnectedGroups() {
board=new Board(" · · B B · · · · ·\r\n" + 
				" · · · · W B · · ·\r\n" + 
				" · · B B · B W · ·\r\n" + 
				" · · W W B · · · ·\r\n" + 
				" · · · · · · · · ·\r\n" + 
				" · · · · W W W W ·\r\n" + 
				" · · · · W · B W ·\r\n" + 
				" · · · · · B B · ·\r\n" + 
				" · · · · B · · · ·"
				);
Set<WeakConnection> set=board.getWeakConnections(board.connectedGroup(board.black), board.white);
		assertEquals(set.size(), 3);
	}
  
	
	

	@Test
	public void getPossibleMoves() {
		board.loadSGFLine(";B[cc];W[cd];B[dc];W[dd];B[ed];W[ec];B[fc];W[eb];B[fd];W[ef];B[fb];W[hf];B[ff];W[fg];B[gg];W[gf];B[fe];W[hg];B[fh];W[eg];B[gh];W[hh];B[be];W[eh];B[af];W[gi]))");
		BitBoard moves = engine.getAllMoves(board);
		assertNull(moves);
	}


	@Test
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
		
	//	assertEquals(board.getEyes(board.black).cardinality(),1);
	}

    
	


}
