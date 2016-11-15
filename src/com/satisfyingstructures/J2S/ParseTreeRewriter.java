/*
The MIT License (MIT)

Copyright (c) 2016 Torsten Louland

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

/**
 * Created by Torsten Louland on 21/10/2016.
 */
package com.satisfyingstructures.J2S;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.misc.Interval;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ParseTreeRewriter extends TokenStreamRewriter {

    /// ChangedIntervalsList records an ordered normalised list of intervals in the rewriter's token stream that will be
    /// rewritten. The list can be added to, tested, reset and regenerated.
	class ChangedIntervalsList
	{
		final ArrayList<Interval> intervals = new ArrayList<>(256); // Normalised (overlapping/abutting entries merged), sorted

		void rebuildFromRewriteOps(List<TokenStreamRewriter.RewriteOperation> opList)
		{
            intervals.clear();
            /*  Don't have sufficient access to RewriteOperation subclasses to do this:
            TokenStreamRewriter.RewriteOperation op;
            TokenStreamRewriter.ReplaceOp rop;
            TokenStreamRewriter.InsertOp iop;
            int i, len, start, stop;
            for (i = 0, len = opList.size(); i < len; i++)
            {
                if (null == (op = opList.get(i)))
                    continue;
                start = stop = op.index;
                if (op instanceof ReplaceOp)
                {
                    rop = (ReplaceOp)op;
                    stop = rop.lastIndex;
                }
                changed(start, stop);
            }
            */
		}

		void changed(int start, int stop)
		{
            // Rewriting in response to a parse tree walk is generally done linearly going forward, with occasional
            // larger rewrites reaching further back as the tree walk steps out to wider scopes. Therefore, rather than
            // use a binary search, we search linearly backwards from the end, implying from most recent.

            // We scan backwards for to identify all existing changed intervals that we intersect plus those that abutt
            // each end if present, then replace them with a combined interval. We also check if the changed interval is
            // completely contained in a gap, and if so insert it. Note that Interval represents a closed interval -
            // both ends included.

            if (0 == intervals.size())
            {
                intervals.add(Interval.of(start, stop));
                return;
            }

            // Make a test interval that is one larger on each end, so that we can test for intersection with intervals
            // that abutt in before and after
            Interval intervalChanged = Interval.of(start, stop);
            Interval intervalToCatch = Interval.of(start > 0 ? start-1 : 0, stop + 1);
            Interval interval;
            int gapStart, gapStop;
            int combinedStart = start;
            int combinedStop = stop;
            final int NO_INDEX = Integer.MAX_VALUE;
            int intersectingIndexLast = NO_INDEX;
            int intersectingIndexFirst = NO_INDEX;
            int insertionIndex = NO_INDEX;
            int i = intervals.size();

            // Scan backwards for the first intersection with an interval or complete containment by a gap
            gapStop = Integer.MAX_VALUE;
            while (0 < i--)
            {
                interval = intervals.get(i);
                // Look for an intersection
                if (!intervalToCatch.disjoint(interval))
                {
                    if (interval.properlyContains(intervalChanged))
                        return; // already included in changed intervals, so nothing more to do
                    intersectingIndexFirst = intersectingIndexLast = i;
                    if (combinedStop < interval.b)
                        combinedStop = interval.b;
                    if (combinedStart > interval.a)
                        combinedStart = interval.a;
                    break;
                }
                // Create trailing gap and test
                gapStart = interval.b + 1;
                if (start >= gapStart && stop <= gapStop)
                {
                    insertionIndex = i + 1; // insert before following entry
                    break;
                }
                gapStop = interval.a - 1;
            }
            if (0 > i && insertionIndex == NO_INDEX)
            {
                gapStart = 0;
                if (start >= gapStart && stop <= gapStop)
                    insertionIndex = i + 1; // insert before following entry
            }

            // Continue scanning backwards until last of intersecting intervals
            if (insertionIndex == NO_INDEX)
            while (0 < i--)
            {
                interval = intervals.get(i);
                // Accumulate if we have another intersection
                if (!intervalToCatch.disjoint(interval))
                {
                    intersectingIndexFirst = i;
                    if (combinedStart > interval.a)
                        combinedStart = interval.a;
                }
                else
                    break; // done
            }

            for (i = intersectingIndexLast; i > intersectingIndexFirst; i--)
                intervals.remove(i);
            if (intersectingIndexFirst != NO_INDEX && intersectingIndexFirst < intervals.size())
                intervals.set(intersectingIndexFirst, Interval.of(combinedStart, combinedStop));
            else
            if (insertionIndex != NO_INDEX)
                intervals.add(insertionIndex, Interval.of(combinedStart, combinedStop));
		}

		void changed(Interval interval)
		{
            changed(interval.a, interval.b);
        }

        boolean isChanged(Interval interval)
        {
            return isChanged(interval.a, interval.b);
        }

        boolean isChanged(int from, int to)
        {
            Interval interval;
            int i = intervals.size();
            while (0 < i--)
            {
                interval = intervals.get(i);
                if (interval.b < from)
                    return false;
                if (interval.a <= to)
                    return true;
            }
			return false;
		}

        Interval getChangedIntervalContaining(int from, int to)
        {
            Interval interval;
            int i = intervals.size();
            while (0 < i--)
            {
                interval = intervals.get(i);
                if (interval.b < from)
                    return null;
                if (interval.a <= to)
                    return interval;
            }
            return null;
        }

	}

    final ParseTree tree;
    final Map<String, ChangedIntervalsList> changedIntervalsByProgram;

    public ParseTreeRewriter(ParseTree tree, TokenStream tokens) {
        super(tokens);
		this.changedIntervalsByProgram = new HashMap();
		this.changedIntervalsByProgram.put("default", new ChangedIntervalsList());
        this.tree = tree;
    }

    // changed intervals

	@Override public void rollback(String programName, int instructionIndex)
	{
		super.rollback(programName, instructionIndex);
		ChangedIntervalsList changedIntervals = changedIntervalsByProgram.get(programName);
		if (null != changedIntervals)
			changedIntervals.rebuildFromRewriteOps(programs.get(programName));
	}

	@Override public void insertBefore(String programName, int index, Object text)
	{
		super.insertBefore(programName, index, text);
		ChangedIntervalsList changedIntervals = changedIntervalsByProgram.get(programName);
        if (null == changedIntervals)
            changedIntervalsByProgram.put(programName, (changedIntervals = new ChangedIntervalsList()));
		changedIntervals.changed(index, index);
	}

	@Override public void replace(String programName, int from, int to, Object text)
	{
		super.replace(programName, from, to, text);
		ChangedIntervalsList changedIntervals = changedIntervalsByProgram.get(programName);
        if (null == changedIntervals)
            changedIntervalsByProgram.put(programName, (changedIntervals = new ChangedIntervalsList()));
		changedIntervals.changed(from, to);
	}

	@Override public String getText(String programName, Interval interval)
	{
        // super.getText is very expensive - avoid it if we can.
		ChangedIntervalsList changedIntervals = changedIntervalsByProgram.get(programName);
		if (null != changedIntervals && !changedIntervals.isChanged(interval))
			return this.tokens.getText(interval);
		return super.getText(programName, interval);
	}

	public boolean tokenRangeIsChanged(String programName, int from, int to)
    {
        ChangedIntervalsList changedIntervals = changedIntervalsByProgram.get(programName);
        if (null != changedIntervals && !changedIntervals.isChanged(from, to))
            return false;
        return true;
    }

    public boolean tokenRangeIsChanged(int from, int to)
    {
        return tokenRangeIsChanged(DEFAULT_PROGRAM_NAME, from, to);
    }

    public Interval getChangedIntervalContaining(String programName, int from, int to)
    {
        ChangedIntervalsList changedIntervals = changedIntervalsByProgram.get(programName);
        if (null != changedIntervals)
            return changedIntervals.getChangedIntervalContaining(from, to);
        return null;
    }

    public Interval getChangedIntervalContaining(int from, int to)
    {
        return getChangedIntervalContaining(DEFAULT_PROGRAM_NAME, from, to);
    }

	// operations on an Interval

	public void insertAfter(Interval i, Object text) {
		insertAfter(DEFAULT_PROGRAM_NAME, i.b, text);
	}

	public void insertAfter(String programName, Interval i, Object text) {
		insertAfter(programName, i.b, text);
	}

	public void insertBefore(Interval i, Object text) {
		insertBefore(DEFAULT_PROGRAM_NAME, i.a, text);
	}

	public void insertBefore(String programName, Interval i, Object text) {
		insertBefore(programName, i.a, text);
	}

	public void replace(Interval i, Object text) {
		replace(DEFAULT_PROGRAM_NAME, i.a, i.b, text);
	}

	public void replace(String programName, Interval i, Object text) {
		replace(programName, i.a, i.b, text);
	}

	public void delete(Interval i) {
		delete(DEFAULT_PROGRAM_NAME, i);
	}

	public void delete(String programName, Interval i) {
		replace(programName, i, null);
	}

    // operations on a ParseTree

	public void insertAfter(ParseTree pt, Object text) {
		insertAfter(DEFAULT_PROGRAM_NAME, pt, text);
	}

	public void insertAfter(String programName, ParseTree pt, Object text) {
		insertAfter(programName, pt.getSourceInterval(), text);
	}

	public void insertBefore(ParseTree pt, Object text) {
		insertBefore(DEFAULT_PROGRAM_NAME, pt, text);
	}

	public void insertBefore(String programName, ParseTree pt, Object text) {
		insertBefore(programName, pt.getSourceInterval(), text);
	}

	public void replace(ParseTree pt, Object text) {
		replace(DEFAULT_PROGRAM_NAME, pt, text);
	}

	public void replace(String programName, ParseTree pt, Object text) {
		replace(programName, pt.getSourceInterval(), text);
	}

	public void delete(ParseTree pt) {
		delete(DEFAULT_PROGRAM_NAME, pt);
	}

	public void delete(String programName, ParseTree pt) {
		replace(programName, pt.getSourceInterval(), null);
	}

	// additional getText overloads

    public String getText(int from, int to) {
        return getText(DEFAULT_PROGRAM_NAME, from, to);
    }

    public String getText(String programName, int from, int to) {
        return getText(programName, Interval.of(from, to));
    }

    public String getText(Token t) {
		return getText(DEFAULT_PROGRAM_NAME, t);
	}

	public String getText(String programName, Token t) {
        return getText(programName, Interval.of(t.getTokenIndex(), t.getTokenIndex()));
    }

	public String getText(ParseTree pt) {
		return getText(DEFAULT_PROGRAM_NAME, pt);
	}

	public String getText(String programName, ParseTree pt) {
        return getText(programName, pt.getSourceInterval());
    }
}
