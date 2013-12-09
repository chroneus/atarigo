package com.chroneus.atarigo;

public class EngineThread implements Runnable{

	Board board;
	int move;
	Engine engine;
	String method;
	public EngineThread(Board b, int move,Engine e, String method) {
		super();
		this.board=b;
		this.move=move;
		this.engine=e;
		this.method=method;
	}
	@Override
	public void run() {
		if(method.equals("testBoardOnRandomPlay")) //will use reflection if many methods
		engine.testBoardOnRandomPlay(board, move);
	}

	
}
