/*
 * Maledict - An Interactive Tool for Learning Linear and Differential Cryptanalysis of SPNs
 * Copyright (C) 2016  Mike Carpenter
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.mjcarpenter.maledict.ui.util;

import java.util.HashMap;
import java.util.Map;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;

import net.mjcarpenter.maledict.crypto.spn.Key;
import net.mjcarpenter.maledict.crypto.spn.Permutation;
import net.mjcarpenter.maledict.crypto.spn.Round;
import net.mjcarpenter.maledict.crypto.spn.SBox;
import net.mjcarpenter.maledict.crypto.spn.SPNetwork;
import net.mjcarpenter.maledict.ui.dialog.component.SPNVisualizationFrame;
import net.mjcarpenter.maledict.util.HexByteConverter;

public class MasterPropertiesCache
{
	private static final MasterPropertiesCache instance;
	static
	{
		instance = new MasterPropertiesCache();
	}
	
	private SPNetwork spn;
	private SPNVisualizationFrame vis;
	
	private Map<String, SBox>        namedSboxes;
	private Map<String, Permutation> namedPermutations;
	private Map<String, Key>         namedKeys;
	
	private int blockSize, sboxSize, numRounds;
	
	private MasterPropertiesCache()
	{
		namedSboxes = new HashMap<String, SBox>();
		namedPermutations = new HashMap<String, Permutation>();
		namedKeys = new HashMap<String, Key>();
		
		spn = null;
		vis = null;
		blockSize = 0;
		sboxSize  = 0;
		numRounds = 0;
	}
	
	public static MasterPropertiesCache getInstance()
	{
		return instance;
	}
	
	public static XStream getReadyXStream()
	{
		XStream xs = new XStream();
		xs.processAnnotations(SPNetwork.class);
		xs.processAnnotations(Round.class);
		xs.processAnnotations(Key.class);
		xs.registerLocalConverter(Key.class, "key", (Converter)(new HexByteConverter()));
		xs.processAnnotations(Permutation.class);
		xs.processAnnotations(SBox.class);
		return xs;
	}
	
	/*
	 * BASIC PROPERTIES
	 */
	
	public int getBlockSize()
	{
		return blockSize;
	}
	
	public void setBlockSize(int bSize)
	{
		blockSize = bSize;
	}
	
	public int getSBoxSize()
	{
		return sboxSize;
	}
	
	public void setSBoxSize(int sSize)
	{
		sboxSize = sSize;
	}
	
	public int getNumRounds()
	{
		return numRounds;
	}
	
	public void setNumRounds(int nRounds)
	{
		numRounds = nRounds;
	}
	
	/*
	 * SPN COMPONENTS
	 */
	
	public SPNetwork getSPN()
	{
		return spn;
	}
	
	public void setSPN(SPNetwork inSpn)
	{
		spn = inSpn;
		getVisualizationFrame().setSPN(spn);
		setBlockSize(spn.getBlockSize());
		setNumRounds(spn.getRounds().length);
		setSBoxSize(spn.getRounds()[0].getSBoxes()[0].bitSize());
	}
	
	public SBox getNamedSBox(String name)
	{
		return (namedSboxes.containsKey(name)) ? namedSboxes.get(name) : null;
	}
	
	public void saveNamedSBox(String name, SBox sbox)
	{
		namedSboxes.put(name, sbox);
	}
	
	public Permutation getNamedPermutation(String name)
	{
		return (namedPermutations.containsKey(name)) ? namedPermutations.get(name) : null;
	}
	
	public void saveNamedPermutation(String name, Permutation perm)
	{
		namedPermutations.put(name, perm);
	}
	
	public Key getNamedKey(String name)
	{
		return (namedKeys.containsKey(name)) ? namedKeys.get(name) : null;
	}
	
	public void saveNamedKey(String name, Key key)
	{
		namedKeys.put(name, key);
	}

	public void setVisualizationFrame(SPNVisualizationFrame frm)
	{
		vis = frm;	
	}
	
	
	/*
	 * VISUALIZATION FRAME
	 */
	
	public SPNVisualizationFrame getVisualizationFrame()
	{
		if(vis == null) vis = new SPNVisualizationFrame(spn);
		return vis;
	}
	
	public void colorVisualization(long[] inMask, long[] outMask)
	{
		vis.colorVisualization(inMask, outMask);
	}
	
	public void clearVisualizationColoring()
	{
		vis.clearVisualizationColoring();
	}
}
