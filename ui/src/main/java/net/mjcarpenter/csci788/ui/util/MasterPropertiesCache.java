package net.mjcarpenter.csci788.ui.util;

import java.util.HashMap;
import java.util.Map;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;

import net.mjcarpenter.csci788.crypto.spn.Key;
import net.mjcarpenter.csci788.crypto.spn.Permutation;
import net.mjcarpenter.csci788.crypto.spn.Round;
import net.mjcarpenter.csci788.crypto.spn.SBox;
import net.mjcarpenter.csci788.crypto.spn.SPNetwork;
import net.mjcarpenter.csci788.ui.dialog.component.SPNVisualizationFrame;
import net.mjcarpenter.csci788.util.HexByteConverter;

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
	
	public SPNVisualizationFrame getVisualizationFrame()
	{
		if(vis == null) vis = new SPNVisualizationFrame(spn);
		return vis;
	}
	
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
}
