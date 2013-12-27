package com.chroneus.atarigo;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class EngineTest {
	Board board;
	Engine engine;

	@Before
	public void setup() {
		board = new Board();
		board.clear();
		engine = new Engine();
	}
	@Test
	public void testCountTerritory(){
		board=new Board(
" · · · · · · · · ·\r\n" + 
" · · · · · · · · ·\r\n" + 
" · · W · · · B · ·\r\n" + 
" · · · · · · · · ·\r\n" + 
" · · · · · · · · ·\r\n" + 
" · · · · · · · · ·\r\n" + 
" · · W · · · B · ·\r\n" + 
" · · · · · · · · ·\r\n" + 
" · · · · · · · · ·\r\n" + 
"");
		System.out.println(engine.countTerritory(board));
	}
	
	@Test
	public void testRandomGame(){
		board=new Board(
" · · · B · · · · ·\r\n" + 
" · · W · B · · · ·\r\n" + 
" · · W · · · B · ·\r\n" + 
" · · W · B · · · ·\r\n" + 
" · W · B · · · · ·\r\n" + 
" · · W · B · · · ·\r\n" + 
" · · W · B · B · ·\r\n" + 
" · · W · · · · · ·\r\n" + 
" · · W · · · · · ·\r\n" + 
"");
		System.out.println(engine.filterBoardWithRandom(board, engine.getAllMoves(board),10));
		

	}
	@Test
	public void testWeakGroup(){
		board=new Board(
				" · · · · · · · · ·\r\n" + 
				" · · W W · · · · ·\r\n" + 
				" · · W · B W W W ·\r\n" + 
				" · · W · B · · B B\r\n" + 
				" · · W B B · · B ·\r\n" + 
				" · · W W W · · B ·\r\n" + 
				" · · W · · · · B ·\r\n" + 
				" · B B · · · · B ·\r\n" + 
				" · · · · · · · · ·");
		BitBoard seed=new BitBoard();
		seed.set(40);
		assertTrue(engine.moyoGroupCardinality(board, seed)<2);
		board.play_move(41);
		assertTrue(engine.moyoGroupCardinality(board, seed)>3);
	}
	@Test
	public void testGetAllPossibleMoves() {
		board=new Board(
" · · · · · · · · ·\r\n" + 
" · · · · · · · · ·\r\n" + 
" · · · B · · · · ·\r\n" + 
" · W · · B · · · W\r\n" + 
" · · · · B · · · ·\r\n" + 
" · W · W · · · · ·\r\n" + 
" B W · · · · · · ·\r\n" + 
" B · · · · · · · ·\r\n" + 
" · · · · · · · · ·\r\n" + 
"");

	assertNotNull(engine.getAllMoves(board));
	}
	

	@Test
	public void testLadder() {
		board=new Board(
				" · · · · · · · · ·\r\n" + 
				" · · · · · · · · ·\r\n" + 
				" · · · B · · · · ·\r\n" + 
				" · W · W B · · · W\r\n" + 
				" · · · · B · · · ·\r\n" + 
				" · W · W · · · · ·\r\n" + 
				" B W · · · · · · ·\r\n" + 
				" B · · · · · · · ·\r\n" + 
				" · · · · · · · · ·\r\n" + 
				"");
		BitBoard groupToKill=new BitBoard();
		groupToKill.set(30);
		assertNull(engine.checkLadder(board, groupToKill));
		assertFalse(engine.getAllMoves(board).get(47));
		board=new Board(
				" · · · · · · · · ·\r\n" + 
				" · · · · · · · · ·\r\n" + 
				" · W · B · · · · ·\r\n" + 
				" · · · W B · · · W\r\n" + 
				" · · · · B · · · ·\r\n" + 
				" · · · · · · · · ·\r\n" + 
				" · · · · · · · · ·\r\n" + 
				" · · · · · · · · ·\r\n" + 
				" · · · · · · · · ·\r\n" + 
				"");
		assertNotNull(engine.checkLadder(board, groupToKill));
	}
}
