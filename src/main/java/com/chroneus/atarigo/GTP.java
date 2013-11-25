package com.chroneus.atarigo;

import java.io.*;
import java.util.*;

public class GTP {
	Board board = new Board();
	Engine engine = new Engine();
	Properties properties = new Properties();

	public static void main(String[] args) throws Exception {
		GTP gtp = new GTP();
		InputStream in = gtp.getClass().getClassLoader()
				.getResourceAsStream("atarigo.properties");
		gtp.properties.load(in);
		in.close();
		BufferedReader input = new BufferedReader(new InputStreamReader(
				System.in));

		while (true) {
			StringBuffer out = new StringBuffer();
			String line = input.readLine().trim();
			out.append("= ");
			if (line.matches("^\\d+\\s+\\w+")) {
				String[] numbered_line = line.split("\\s");
				out.append(numbered_line[0] + " ");
				line = numbered_line[1];
			}
			gtp.processCommand(line, out);
			System.out.println(out + "\n");
		}
	}

	public void processCommand(String line, StringBuffer out) throws Exception {
		if (properties.containsKey(line)) {
			out.append(properties.get(line));
			return;
		}
		if (line.startsWith("known_command")) {
			if (properties.get("list_commands").toString()
					.contains(line.split("\\s")[1])) {
				out.append("true");
			} else
				out.append("false");
			return;
		}
		if (line.startsWith("boardsize")) {
			board = new Board();
			board.clear();
			Board.SIZE = Byte.valueOf(line.split("\\s")[1]);
			engine = new Engine();
			return;
		}
		if (line.startsWith("komi")) {
			// don't care in Atari go
			// TODO change it if you want to implement go engine
			return;
		}
		if (line.startsWith("play ")) {
			boolean is_white = false;
			if (line.split("\\s")[1].toLowerCase().startsWith("w"))
				is_white = true;
			String move = line.split("\\s")[2];
			if (move.equalsIgnoreCase("pass")) {
				board.is_white_next = !board.is_white_next;
				return;
			}
			board.play_move(is_white, move);
			return;
		}
		if (line.startsWith("genmove ")) {
			board.is_white_next = (line.split("\\s")[1].toLowerCase()
					.startsWith("w"));
			String move = engine.doMove(board);
			out.append(move);
			board.play_move(move);
			return;
		}
		if (line.equals("clear_board")) {
			board = new Board();
			board.clear();
			engine = new Engine();
			return;
		}
		if (line.startsWith("showboard")) {
			out.append("\n");
			out.append(board);
			if (board.is_white_next) out.append("White to play\n");
			else out.append("Black to play\n");
			return;
		}
		if (line.equals("quit"))
			System.exit(0);
	}

}
