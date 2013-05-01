package com.googlecode.lingwah;

public class ParseError extends Error {
	private static final long serialVersionUID = 1L;

	/**
	 * A message that describes the parsing failure.
	 */
	public final String errorMsg;
	
	/**
	 * The position within the parsed document where matching failed.
	 */
	public final int position;
	
	/**
	 * The parser that created this error
	 */
	public final Parser parser;
	
	/**
	 * The parse context in which the error occurred 
	 */
	public final ParseContext context;
	
	public ParseError(ParseContext context, Parser parser, String msg, int position) {
		super(msg);
		this.context= context;
		this.parser= parser;
		this.errorMsg= msg;
		this.position= position;
	}
	
	@Override
	public String toString() {
		return "{parser:"+parser+"position:"+position+",message:"+errorMsg+"}";
	}

}
