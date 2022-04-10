package me.minefreak19.tryp.lex;

import java.io.File;

public class FileLocation {
	private String name;
	private int line;
	private int col;

	public static FileLocation from(File file) {
		return new FileLocation(file.getName(), 1, 1);
	}

	public FileLocation(FileLocation loc) {
		this(loc.name, loc.line, loc.col);
	}

	public FileLocation(String name, int line, int col) {
		this.name = name;
		this.line = line;
		this.col = col;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}

	public int getCol() {
		return col;
	}

	public void setCol(int col) {
		this.col = col;
	}

	public void advanceLine(int lines) {
		this.col = 1;
		this.line += lines;
	}

	public void advanceLine() {
		this.advanceLine(1);
	}

	public void advanceCol(int cols) {
		this.col += cols;
	}

	public void advanceCol() {
		this.advanceCol(1);
	}

	@Override
	public String toString() {
		return this.name + ':' + this.line + ':' + this.col;
	}
}
