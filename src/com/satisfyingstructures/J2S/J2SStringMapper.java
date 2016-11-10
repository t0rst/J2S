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

/*
 * Created by Torsten Louland on 25/10/2016.
 */
package com.satisfyingstructures.J2S;

import java.util.Map;
import java.util.HashMap;

public class J2SStringMapper
{
    private MapperLevel top = null;

    // public

    public String map(String inType)
    {
        return null != top ? top.getMapping(inType) : inType;
    }

    public void push()
    {
        if (null != top)
            top.nests++;
    }

    public void pop()
    {
        if (null == top)
            return;
        if (--top.nests < 0)
            top = top.parent;
    }

    public void addOne(String from, String to)
    {
        MapperLevel mapper = getMutableTop();
        mapper.addOneMapping(from, to);
    }

    public void addMap(Map<String, String> map)
    {
        MapperLevel mapper = getMutableTop();
        mapper.addManyMappings(map);
    }

    public void addPairs(String... strings)
    {
        MapperLevel mapper = getMutableTop();
        for (int i = 0, n = strings.length & ~1; i < n; i += 2)
            mapper.addOneMapping(strings[i], strings[i+1]);
    }


    // protected

    protected MapperLevel getMutableTop()
    {
        if (null == top || 0 < top.nests)
            push(make());
        return top;
    }

    protected MapperLevel getTop()
    {
        return top;
    }

    protected MapperLevel make()
    {
        return new MapperLevel();
    }

    private void push(MapperLevel mapper)
    {
        if (null == mapper)
            return;
        mapper.parent = top;
        top = mapper;
    }

    // helper

    protected class MapperLevel
    {
        private MapperLevel parent;
        private int nests;
        private Map<String, String> map;

        public MapperLevel()
        {
            nests = 0;
            this.map = new HashMap<>();
        }

        public void addOneMapping(String from, String to)
        {
            map.put(from, to);
        }

        public void addManyMappings(Map<String, String> map)
        {
            this.map.putAll(map);
        }

        public String getMapping(String inType)
        {
            if (null == inType)
                return null;
            String outType = map.get(inType);
            if (null == outType)
                outType = null != parent ? parent.getMapping(inType) : inType;
            return outType;
        }
    }
}
