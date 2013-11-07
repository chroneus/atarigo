package com.chroneus.atarigo;



public class BoardConstant {
	public static final byte SIZE = 9;
	public static final BitBoard ALL =new BitBoard(SIZE,SIZE);
	public static final BitBoard FirstLine = new BitBoard(SIZE,SIZE);
	public static final BitBoard SecondLine = new BitBoard(SIZE,SIZE);
	public static final BitBoard ThirdLine = new BitBoard(SIZE,SIZE);
	public static final BitBoard FourthLine = new BitBoard(SIZE,SIZE);
	public static final BitBoard Tengen = new BitBoard(SIZE,SIZE);
	public static final BitBoard LeftBorder = new BitBoard(SIZE,SIZE);
	public static final BitBoard RightBorder = new BitBoard(SIZE,SIZE);
	public static final BitBoard TopBorder = new BitBoard(SIZE,SIZE);
	public static final BitBoard BottomBorder = new BitBoard(SIZE,SIZE);

	public static final BitBoard Square = new BitBoard(2,2);
	public static final BitBoard EmptyTriangle = new BitBoard(2,2);
	public static final BitBoard Bamboo = new BitBoard(3,2);
	public static final BitBoard House = new BitBoard(3,3);
	public static final BitBoard TigerMouth = new BitBoard(3,2);
	public static final BitBoard Mouth = new BitBoard(3,3);
	public static final BitBoard Lines[] =new BitBoard[]{FirstLine,SecondLine,ThirdLine,FourthLine,Tengen};
static{
	init();
}
	
public static void init(){
	ALL.flipInRange(0, SIZE*SIZE-1);
	for (int i = 0; i < SIZE; i++) {
		FirstLine.set(i * SIZE);
		LeftBorder.set(i*SIZE);
		FirstLine.set(i);
		TopBorder.set(i);
		FirstLine.set(i * SIZE + SIZE - 1);
		RightBorder.set(i * SIZE + SIZE - 1);
		FirstLine.set(SIZE * SIZE - SIZE + i);
		BottomBorder.set(SIZE * SIZE - SIZE + i);
	}
	for (int i = 1; i < SIZE-1; i++) {
		SecondLine.set(i *SIZE+1);
		SecondLine.set(SIZE+i);
		SecondLine.set(i*SIZE+SIZE-2);
		SecondLine.set(SIZE*SIZE -2*SIZE+i);
	}
	for (int i = 2; i < SIZE-2; i++) {
		ThirdLine.set(i*SIZE+2);
		ThirdLine.set(2*SIZE+i);
		ThirdLine.set(i*SIZE+SIZE-3);
		ThirdLine.set(SIZE*SIZE-3*SIZE+i);
	}
	for (int i = 3; i < SIZE-3; i++) {
		FourthLine.set(i*SIZE+3);
		FourthLine.set(3*SIZE+i);
		FourthLine.set(i*SIZE+SIZE-4);
		FourthLine.set(SIZE*SIZE-4*SIZE+i);
	}
	Square.flipInRange(0, 3);
	EmptyTriangle.flipInRange(0,2);
	Tengen.set(SIZE*SIZE/2);
	Bamboo.set(0);Bamboo.set(1);Bamboo.set(4);Bamboo.set(5);
	House.set(0);House.set(2);House.set(6);House.set(7);
	TigerMouth.set(0);TigerMouth.set(3);TigerMouth.set(4);
	Mouth.set(0);Mouth.set(1);Mouth.set(5);Mouth.set(8);
}
	

	
}
