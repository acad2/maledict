package net.mjcarpenter.maledict.crypto.spn;

import java.util.BitSet;

import org.apache.commons.lang3.ArrayUtils;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import net.mjcarpenter.maledict.util.BitUtils;
@SuppressWarnings("serial")
@XStreamAlias("permutation")
public final class Permutation implements SPNComponent
{	
	private static final String VALIDATION_INDICES = "All inputs must have corresponding output.";
	
	@XStreamAlias("mapping")
	private final int[] mapping;
	
	@XStreamAsAttribute
	@XStreamAlias("noop")
	private final boolean noop;
	
	// Derived transient fields.
	private transient int[] reverse;
	
	public Permutation(final int... mapping)
	{
		this(false, mapping);
	}
	
	private Permutation(boolean noop, final int... mapping)
	{
		this.noop = noop;
		this.mapping = mapping;
		this.reverse = constructReverse();
	}
	
	public static Permutation noop(final int length)
	{
		int[] map = new int[length];
		for(int i=0; i<map.length; i++)
			map[i] = i;
		return new Permutation(true, map);
	}
	
	public int length()
	{
		return mapping.length;
	}
	
	public int[] getMapping()
	{
		return this.mapping;
	}
	
	public long permuteFwd(long in)
	{
		return permute(in, mapping);
	}
	
	public long permuteRev(long in)
	{
		return permute(in, reverse);
	}
	
	private long permute(long in, final int[] map)
	{
		byte[] convertIn  = BitUtils.longToByte(in, map.length/Byte.SIZE);
		byte[] convertOut = permute(convertIn, map);
		
		ArrayUtils.reverse(convertOut);
		
		return BitUtils.byteToLong(convertOut);
	}
	
	public byte[] permuteFwd(final byte[] in)
	{
		byte[] out = permute(in, mapping);
		ArrayUtils.reverse(out);
		return out;
	}
	
	public byte[] permuteRev(final byte[] in)
	{
		byte[] out = permute(in, reverse);
		ArrayUtils.reverse(out);
		return out;
	}
	
	private byte[] permute(final byte[] in, final int[] map)
	{
		if(in.length*Byte.SIZE != map.length)
			throw new IllegalArgumentException("Input length must match permutation size! Found "+in.length*Byte.SIZE+" but expected "+map.length+"!");
		
		ArrayUtils.reverse(in);
		BitSet set = BitSet.valueOf(in);
		BitSet out = new BitSet(map.length);
		
		for(int i=0; i<map.length; i++)
		{
			if(set.get(i))
				out.set(map[i]);
		}
		
		return BitUtils.convertBitSetToByte(out, in.length);
	}
	
	public int outPosition(final int inPosition)
	{
		return mapping[inPosition];
	}
	
	public int inPosition(final int outPosition)
	{
		return reverse[outPosition];
	}
	
	private int[] constructReverse()
	{
		int[] revMap = new int[mapping.length];
		
		for(int i=0; i<mapping.length; i++)
		{
			boolean contains = false;
			
			for(int j=0; j<mapping.length; j++)
			{
				if(mapping[j] == i)
				{
					revMap[i] = j;
					contains = true;
					break;
				}
			}
			
			if(!contains)
				throw new IllegalArgumentException(VALIDATION_INDICES);
		}
		
		return revMap;
	}
	
	private Object readResolve()
	{
		// Reconstruct derivable transient fields during deserialization.
		this.reverse = constructReverse();
		return this;
	}

	@Override
	public boolean isNoop()
	{
		return noop;
	}
}