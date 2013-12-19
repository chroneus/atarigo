package com.chroneus.atarigo;

import java.util.concurrent.Callable;

public class RandomGameCallable implements Callable<Integer[]>{

	Board board;
	int move;
	Engine engine;
	public RandomGameCallable(Board b, int move,Engine e) {
		super();
		this.board=b;
		this.move=move;
		this.engine=e;
	}
	/**
	 * return pair move, score
	 */
	public Integer[] call() {
		return new Integer[]{move,engine.testBoardOnRandomPlay(board, move)};
	}

	
}
