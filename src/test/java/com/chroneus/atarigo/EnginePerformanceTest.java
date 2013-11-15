package com.chroneus.atarigo;

	import static org.junit.Assert.*;

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
			for(int i=0;i<Engine.SIZE;i++){
				for(int j=0;j<Engine.SIZE;j++){
					double testfrombegin=engine.testBoardOnRandomPlay(board, i*Engine.SIZE+j, 1000);
					System.out.printf("%.2f ",testfrombegin);
				}System.out.println();
			}
		}
		
		@Test
		public void testPVS(){
			for(int i=0;i<Engine.SIZE;i++){
				for(int j=0;j<Engine.SIZE;j++){
					board.play_move( i*Engine.SIZE+j);
					System.out.printf("%d ",engine.pvs(board, 4,
							Integer.MIN_VALUE, Integer.MAX_VALUE));
					board.undo_move( i*Engine.SIZE+j);
				}System.out.println();
			}
		
		}
		
		@Test
		public void testGenMove(){
			System.out.println(engine.doMove(board));
		
		}
}
