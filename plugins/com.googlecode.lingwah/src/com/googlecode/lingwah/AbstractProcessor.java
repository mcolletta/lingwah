package com.googlecode.lingwah;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;

import com.googlecode.lingwah.util.ProcessorUtils;

/**
 * Base class for Lingwah AST processors.
 * @author Ted Stockwell
 *
 */
abstract public class AbstractProcessor implements MatchProcessor {

	private HashMap<Match, Object> _results= new HashMap<Match, Object>();
	private HashSet<Match> _visited= new HashSet<Match>();
	private HashSet<Match> _completed= new HashSet<Match>();
	private Match _currentMatch;
	
	
	public boolean process(Match node) {

		if (_visited.contains(node))
			return true;
		_visited.add(node);
		Match prevMatch= _currentMatch;
		_currentMatch= node;
		Method visitMethod = ProcessorUtils.findVisitMethod(this, node);
		try {
			Boolean visitChildren= (Boolean) visitMethod.invoke(this, new Object[] { node });
			return visitChildren;
			
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Error invoking method '"+visitMethod.getName()+"'", e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Error invoking method '"+visitMethod.getName()+"'", e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Error invoking method '"+visitMethod.getName()+"'", e);
		}
		finally {
			_currentMatch= prevMatch;
		}
	}
	
	/**
	 * Invoked after invoking the visit method and visiting all children nodes   
	 */
	public void complete(Match node) {
		if (_completed.contains(node))
			return;
		_completed.add(node);

		Match prevMatch= _currentMatch;
		_currentMatch= node;
		Method leaveMethod = ProcessorUtils.findLeaveMethod(this, node);
		try {
			leaveMethod.invoke(this, new Object[] { node });
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Error invoking method '"+leaveMethod.getName()+"'", e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Error invoking method '"+leaveMethod.getName()+"'", e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Error invoking method '"+leaveMethod.getName()+"'", e);
		}
		finally {
			_currentMatch= prevMatch;
		}
	}
	
	public Match getCurrentMatch() {
		return _currentMatch;
	}

	/**
	 * Save result for the Match that is currently being processed 
	 */
	public void putResult(Object result) {
		putResult(getCurrentMatch(), result);
		
	}

	public void putResult(Match match, Object result) {
		_results.put(match, result);
		
	}

	/**
	 * Get the result for the given Match.
	 * If there is no result and the Match has not yet been processed 
	 * then the Match will be processed before returning. 
	 */
	@SuppressWarnings("unchecked")
	public <T> T getResult(Match match) {
		if (match == null)
			return null;
		Object result= _results.get(match);
		if (result == null && !_visited.contains(match)) {
			match.accept(this);
			result= _results.get(match);
		}
		return (T) result;
	}


}
