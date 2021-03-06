/**
 * 
 */
package com.googlecode.lingwah.parser;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.googlecode.lingwah.ParseContext;
import com.googlecode.lingwah.ParseError;
import com.googlecode.lingwah.ParseResults;

public final class RegularExpressionParser extends TerminalParser
{
	private final String _target;
	private final Pattern _pattern;

	public RegularExpressionParser(String target)
	{
		 _pattern= Pattern.compile(target);
		_target= target;
	}
	
	@Override
	public String getDefaultLabel() {
		return "regex('"+_target+"')";
	}

	public String getTarget() {
		return _target;
	}

	@Override
	public void startMatching(ParseContext ctx, int start, ParseResults parseResults) {
		Matcher m = _pattern.matcher(ctx.getDocument().substring(start));
		if (!m.find(0) || 0 < m.start()) {
			parseResults.setError(new ParseError(ctx, this, "Expected "+_target, start));
			return;
		}

		parseResults.addMatch(start + m.end());
	}

}