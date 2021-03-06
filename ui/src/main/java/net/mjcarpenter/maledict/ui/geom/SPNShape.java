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
package net.mjcarpenter.maledict.ui.geom;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import net.mjcarpenter.maledict.crypto.spn.SPNetwork;

@SuppressWarnings("serial")
public final class SPNShape extends JPanel
{
	private static final double LENGTH_SEGMENT     = 0.5;
	private static final double HEIGHT_KEY         = 2.0;
	private static final double HEIGHT_PERM        = 5.0;
	private static final double HEIGHT_SBOX        = 3.0;
	private static final double WIDTH_PER_BIT      = 2.0;
	private static final float  LINE_WEIGHT_NORMAL = 2.0f;
	private static final float  LINE_WEIGHT_HIGHLT = 3.0f;
	
	private long[] roundInMasks;
	private long[] roundOutMasks;
	
	private Map<Shape, Color> colorMap;
	private Collection<Line2D>      lines;
	private Collection<Rectangle2D> rects;
	
	private SPNetwork spn;
	private double scale;
	private int    fullUsrHeight;
	
	public SPNShape(SPNetwork spn)
	{
		this.spn = spn;
		this.roundInMasks = new long[this.spn.getRounds().length];
		this.roundOutMasks = new long[this.spn.getRounds().length];
		scaleTo(getSize());
		build();
		setVisible(true);
	}
	
	public void clearRoundMasks()
	{
		this.roundInMasks  = new long[spn.getRounds().length];
		this.roundOutMasks = new long[spn.getRounds().length];
		
		build();
	}
	
	public void applyRoundMasks(long[] roundInMasks, long[] roundOutMasks)
	{
		this.roundInMasks  = roundInMasks;
		this.roundOutMasks = roundOutMasks;
		
		build();
	}
	
	public void build()
	{
		this.colorMap = new HashMap<Shape, Color>();
		this.lines    = new ArrayList<Line2D>();
		this.rects    = new ArrayList<Rectangle2D>();
		
		int bitWidth  = spn.getBlockSize();
		int curHeight = 0;
		
		for(int i=0; i<spn.getRounds().length; i++)
		{
			// HANDLE KEY
			if(!spn.getRounds()[i].getSubKey().isNoop())
			{
				for(int j=0; j<bitWidth; j++)
				{
					double x = j*WIDTH_PER_BIT+1;
					int maskShift = bitWidth-j-1;
					Color bitColor = ((roundInMasks[i]>>>maskShift)&0x1) == 0 ? Color.BLACK : Color.BLUE;
					
					Line2D lineA = new Line2D.Double(x, curHeight, x, curHeight+LENGTH_SEGMENT);
					Line2D lineB = new Line2D.Double(x, curHeight+HEIGHT_KEY, x, curHeight+(HEIGHT_KEY-LENGTH_SEGMENT));
					lines.add(lineA);
					lines.add(lineB);
					colorMap.put(lineA, bitColor);
					colorMap.put(lineB, bitColor);
				}
				
				Rectangle2D rect = new Rectangle2D.Double(0.5, curHeight+LENGTH_SEGMENT, bitWidth*WIDTH_PER_BIT-1, HEIGHT_KEY-2*LENGTH_SEGMENT);
				rects.add(rect);
				colorMap.put(rect, Color.BLACK);
				
				
				curHeight += HEIGHT_KEY;
			}
			
			// HANDLE S-BOXES
			boolean includeRow = false;
			for(int j=0; j<spn.getRounds()[i].getSBoxes().length; j++)
			{
				// If at least one S-box in the row is non-noop then include it.
				includeRow |= !spn.getRounds()[i].getSBoxes()[j].isNoop();
			}
			if(includeRow)
			{
				int sbx = 0;
				
				for(int j=0; j<spn.getRounds()[i].getSBoxes().length; j++)
				{
					int boxBitSize = spn.getRounds()[i].getSBoxes()[j].bitSize();
					
					boolean isRectColor = false;
					
					for(int k=0; k<boxBitSize; k++)
					{
						int maskShift = bitWidth-(sbx + k)-1;
						
						boolean isInColor  = ((roundInMasks[i]>>maskShift)&0x1)  != 0;
						boolean isOutColor = ((roundOutMasks[i]>>maskShift)&0x1) != 0;
						
						Color bitInColor  = isInColor  ? Color.BLUE : Color.BLACK;
						Color bitOutColor = isOutColor ? Color.BLUE : Color.BLACK;
						
						isRectColor |= (isInColor || isOutColor);
						
						double x = (sbx+k)*WIDTH_PER_BIT+1;
						
						Line2D lineA = new Line2D.Double(x, curHeight, x, curHeight+LENGTH_SEGMENT);
						Line2D lineB = new Line2D.Double(x, curHeight+HEIGHT_SBOX, x, curHeight+(HEIGHT_SBOX-LENGTH_SEGMENT));
						lines.add(lineA);
						lines.add(lineB);
						colorMap.put(lineA, bitInColor);
						colorMap.put(lineB, bitOutColor);
					}
					
					Rectangle2D rect = new Rectangle2D.Double((sbx*2)+0.5, curHeight+0.5, boxBitSize*WIDTH_PER_BIT-1, HEIGHT_SBOX-2*LENGTH_SEGMENT);
					rects.add(rect);
					colorMap.put(rect, isRectColor ? Color.BLUE : Color.BLACK);
					sbx += boxBitSize;
				}
				
				curHeight += HEIGHT_SBOX;
			}
			
			// HANDLE PERMUTATIONS
			if(!spn.getRounds()[i].getPermutation().isNoop())
			{
				for(int j=0; j<bitWidth; j++)
				{
					int maskShift = bitWidth-j-1;
					Color bitColor = ((roundOutMasks[i]>>maskShift)&0x1) == 0 ? Color.BLACK : Color.BLUE;
					
					double x1 = j*WIDTH_PER_BIT+1;
					double x2 = spn.getRounds()[i].getPermutation().outPosition(j)*WIDTH_PER_BIT+1;
					
					Line2D line = new Line2D.Double(x1, curHeight, x2, curHeight+HEIGHT_PERM);
					lines.add(line);
					colorMap.put(line, bitColor);
				}
				
				curHeight += HEIGHT_PERM;
			}
		}
		
		this.fullUsrHeight = curHeight;
		scaleTo(getSize());
		
		revalidate();
		repaint();
	}
	
	public void scaleTo(Dimension d)
	{
		scale = (double)d.getWidth()/((double)spn.getBlockSize()*2.0);
	}
	
	@Override
	public Dimension getMinimumSize()
	{
		scaleTo(getSize());
		int prefHeight = Math.max(getHeight(), (int)Math.ceil(fullUsrHeight/scale));
		
		return new Dimension(getWidth(), prefHeight);
	}
	
	@Override
	public void paintComponent(Graphics g)
	{
		Graphics2D g2 = (Graphics2D)g;
		
		super.paintComponent(g2);
		
		scaleTo(getSize());
		g2.scale(scale, scale);
		
		for(Line2D each: lines)
		{
			Color thisColor = colorMap.get(each);
			double lineWeight = (thisColor.equals(Color.BLACK)) ? LINE_WEIGHT_NORMAL : LINE_WEIGHT_HIGHLT;
			g2.setStroke(new BasicStroke((float)(lineWeight/scale)));
			g2.setColor(thisColor);
			
			g2.draw(each);
		}
		
		for(Rectangle2D each: rects)
		{
			Color thisColor = colorMap.get(each);
			double lineWeight = (thisColor.equals(Color.BLACK)) ? LINE_WEIGHT_NORMAL : LINE_WEIGHT_HIGHLT;
			g2.setStroke(new BasicStroke((float)(lineWeight/scale)));
			g2.setColor(thisColor);
			
			g2.draw(each);
		}
	}
	
	public BufferedImage captureAsImage()
	{
		BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
		paint(img.getGraphics());
		return img;
	}
}
