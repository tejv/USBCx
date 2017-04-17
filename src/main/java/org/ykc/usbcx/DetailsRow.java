package org.ykc.usbcx;

import javafx.scene.paint.Color;

public class DetailsRow {
	public enum BG {NORMAL, RED, GREEN, BLUE, YELLOW, PINK};
	private String name;
	private String value;
	private String decval;
	private String hexval;
	private String binaryval;
	private String len;
	private String offset;
	private BG bcolor;
	private Boolean bold;
	private int level;

	private DetailsRow(Builder builder) {
		this.name = builder.name;
		this.value = builder.value;
		this.decval = builder.decval;
		this.hexval = builder.hexval;
		this.binaryval = builder.binaryval;
		this.len = builder.len;
		this.offset = builder.offset;
		this.bcolor = builder.bcolor;
		this.bold = builder.bold;
		this.level = builder.level;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getDecval() {
		return decval;
	}
	public void setDecval(String decval) {
		this.decval = decval;
	}
	public String getHexval() {
		return hexval;
	}
	public void setHexval(String hexval) {
		this.hexval = hexval;
	}
	public String getBinaryval() {
		return binaryval;
	}
	public void setBinaryval(String binaryval) {
		this.binaryval = binaryval;
	}
	public String getLen() {
		return len;
	}
	public void setLen(String len) {
		this.len = len;
	}
	public String getOffset() {
		return offset;
	}
	public void setOffset(String offset) {
		this.offset = offset;
	}

	public BG getBcolor() {
		return bcolor;
	}

	public void setBcolor(BG bcolor) {
		this.bcolor = bcolor;
	}

	public Boolean getBold() {
		return bold;
	}
	public void setBold(Boolean bold) {
		this.bold = bold;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		if(level < 0) throw new RuntimeException("Level cannot be < 0");
		this.level = level;
	}

	public static class Builder{
		private String name = "Dummy";
		private String value = "0";
		private String decval = "0";
		private String hexval = "0x0";
		private String binaryval = "0b0";
		private String len = "0";
		private String offset = "0";
		private BG bcolor = BG.NORMAL;
		private Boolean bold = false;
		private int level = 0;

		public Builder name(final String name){
			this.name = name;
			return this;
		}

		public Builder value(final String value){
			this.value = value;
			return this;
		}

		public Builder decval(final String decval){
			this.decval = decval;
			return this;
		}

		public Builder hexval(final String hexval){
			this.hexval = hexval;
			return this;
		}

		public Builder binaryval(final String binaryval){
			this.binaryval = binaryval;
			return this;
		}

		public Builder len(final String len){
			this.len = len;
			return this;
		}

		public Builder offset(final String offset){
			this.offset = offset;
			return this;
		}

		public Builder bcolor(final BG bcolor){
			this.bcolor = bcolor;
			return this;
		}

		public Builder bold(final Boolean bold){
			this.bold = bold;
			return this;
		}

		public Builder level(final int level){
			if(level < 0) throw new RuntimeException("Level cannot be < 0");
			this.level = level;
			return this;
		}

		public DetailsRow build(){
			return new DetailsRow(this);
		}

	}

}
