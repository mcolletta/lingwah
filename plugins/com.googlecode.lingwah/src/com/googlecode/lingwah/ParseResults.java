package com.googlecode.lingwah;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;


/**
 * Encapsulates (and potentially manipulates) the results of applying a parser
 * to a particular position of an input stream.
 * The ParseContext class caches instances of this class so that no parser is 
 * ever applied more than once to the same input position.
 * 
 * @See ParseContext
 * 
 * @author ted stockwell
 */
public class ParseResults {
	

	/**
	 * Listener interface for receiving notifications.
	 */
	static public interface Listener {
		
		public void onMatchFound(ParseResults results, Match node);

		public void onMatchError(ParseResults parseResults, ParseError error);
		
	}
	
	// forwards results to the given destination results.
	static public class DefaultListener implements Listener {
		
		final public ParseResults destinationResults;
		
		public DefaultListener(ParseResults results) {
			destinationResults= results;
		}
		
		public void onMatchFound(ParseResults results, Match node) {
			destinationResults.addMatch(node);
		}

		public void onMatchError(ParseResults parseResults, ParseError error) {
			destinationResults.setError(error);
		}
	}
	

	private final ParseContext _ctx;
	private final Parser _matcher;
	private final int _position; 
	private List<Match> _matches;
	protected ParseError _error;
	private HashSet<Listener> _listeners= new HashSet<Listener>();
	private HashSet<ParseError> _errors= new HashSet<ParseError>();

	public ParseResults(ParseContext ctx, Parser parser, int position) {
		_ctx= ctx;
		_matcher= parser;
		_position= position;
	}
	
	
	
	public ParseResults setError(String msg) {
		setError(new ParseError(_ctx, _matcher, msg, _position));
		return this;
	}
	public ParseResults setError(String msg, int position) {
		setError(new ParseError(_ctx, _matcher, msg, position));
		return this;
	}
	
	public ParseResults addMatch(int endPosition) {
		addMatch(Match.create(_ctx, _matcher, _position, endPosition));
		return this;
	}
	
	
	public boolean success() {
		return _matches != null && !_matches.isEmpty();
	}
	
	public List<Match> getMatches() {
		if (_matches == null)
			return Collections.<Match>emptyList();
		return Collections.unmodifiableList(_matches);
	}

	public int longestLength() {
		if (_matches == null)
			return 0;

		int l, m = 0;
		for (Match node : _matches) {
			if (m < (l = node.length()))
				m = l;
		}
		return m;
	}

	public String toString() {
		if (!success())
			return "Failed at position " + _error.position + ":"
					+ _error.errorMsg;
		String s = "Success, matched " + _matches.get(0).getText();
		for (int i = 1; i < _matches.size(); i++)
			s += "\nand " + _matches.get(i).getText();
		return s;
	}



	public Parser getMatcher() {
		return _matcher;
	}



	public int getPosition() {
		return _position;
	}

	synchronized public void addListener(Listener listener) {
		if (listener != null) {
			if (!_listeners.contains(listener)) {
				_listeners.add(listener);
				if (_matches != null) {
					for (Match node : new ArrayList<Match>(_matches)) {
						listener.onMatchFound(this, node);
					}
				}
			}
		}
	}
	synchronized public void addMatch(Match match) {
		if (match.getParser() != _matcher)
			match= Match.create(_ctx, _matcher, Arrays.asList(new Match[] { match }));
		if (_matches == null) 
			_matches= new ArrayList<Match>();
		for (Match m:_matches)
			if (m.length() == match.length())
				return;
		if (!_matches.contains(match)) {
			_matches.add(match);
			for (Listener listener:new HashSet<Listener>(_listeners)) {
				listener.onMatchFound(this, match);
			}
		}
	}
	public void setError(ParseError error) {
		// in order to avoid infinite recursion when a Parser is recursive, 
		// we have to check to see if we've seen this error before.
		// f we've seen it before then ignore it.
		if (_errors.contains(error))
			return;
		_errors.add(error);
		
		// just save the error.
		// if no matches are found then the error will be send to listeners
		saveError(error);
		for (Listener listener:new HashSet<Listener>(_listeners)) {
			listener.onMatchError(this, error);
		}
	}
	protected void saveError(ParseError error) {
		//if (_error == null || error.position < _error.position) 
		if (_error == null || _error.position < error.position) 
			_error= error;
	}
	
	public ParseError getError() {
		return _error;
	}
	
	public ParseContext getContext() { return _ctx; }
	
	public String getErrorMessage() {
		if (_error == null) {
			if (_matches == null || _matches.isEmpty())
				return "No matches found";
			return "";
		}
		return _error.errorMsg;
	}
	public String getErrorPosition() {
		if (_error == null) {
			return "unknown";
		}
		return ""+_error.position;
	}

	public Match getLongestMatch() {
		if (_matches == null)
			return null;

		Match longestMatch= null;
		for (Match node : _matches) {
			if (longestMatch == null || longestMatch.length() < node.length())
				longestMatch= node;
		}
		return longestMatch;
	}
}
