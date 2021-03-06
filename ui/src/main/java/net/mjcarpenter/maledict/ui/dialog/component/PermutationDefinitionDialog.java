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
package net.mjcarpenter.maledict.ui.dialog.component;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import net.mjcarpenter.maledict.crypto.spn.Permutation;
import net.mjcarpenter.maledict.ui.geom.PermutationWeb;
import net.mjcarpenter.maledict.ui.message.help.HelpMessageConstants;

@SuppressWarnings("serial")
public class PermutationDefinitionDialog extends ComponentDefinitionDialog<Permutation> implements ActionListener, FocusListener
{
	private PermutationWeb visualWeb;
	private JButton jbOK;
	private JButton jbCancel;
	private JButton jbHelp;
	
	private List<JTextField> textFields;
	private List<JLabel>     textLabels;
	
	private String cachedEntry;
	private int[]  cachedMappings;
	
	public PermutationDefinitionDialog(int length)
	{
		this(Permutation.noop(length));
	}
	
	public PermutationDefinitionDialog(Permutation component)
	{
		super("Edit Permutation", component);
		setLayout(new BorderLayout());
		
		cachedMappings = this.component.getMapping();
		
		jbOK = new JButton("OK");
		jbOK.setMnemonic('O');
		jbCancel = new JButton("Cancel");
		jbCancel.setMnemonic('C');
		jbHelp = new JButton("Help");
		jbHelp.setMnemonic('H');
		
		jbOK.addActionListener(this);
		jbCancel.addActionListener(this);
		jbHelp.addActionListener(this);
		
		JPanel buttonPanel   = new JPanel();
		JPanel subPanelLeft  = new JPanel();
		JPanel subPanelRight = new JPanel();
		
		buttonPanel.setLayout(new BorderLayout());
		subPanelLeft.add(jbHelp);
		subPanelRight.add(jbOK);
		subPanelRight.add(jbCancel);
		
		buttonPanel.add(subPanelLeft, BorderLayout.WEST);
		buttonPanel.add(subPanelRight, BorderLayout.EAST);
		
		visualWeb = new PermutationWeb(this.component.length(), true);		
		
		JPanel fieldPanel = new JPanel();
		fieldPanel.setLayout(new GridLayout(1, this.component.length()));
		fieldPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
		textFields = new ArrayList<JTextField>(this.component.length());
		textLabels = new ArrayList<JLabel>(this.component.length());
		
		for(int i=0; i<this.component.length(); i++)
		{
			JPanel colPanel = new JPanel();
			colPanel.setLayout(new GridLayout(2, 1, 5, 5));
			
			JLabel     iLabel = new JLabel(Integer.toHexString(i).toUpperCase());
			iLabel.setHorizontalAlignment(JLabel.CENTER);
			JTextField iField = new JTextField();
			iField.setText(Integer.toHexString(this.component.outPosition(i)).toUpperCase());
			iField.setHorizontalAlignment(JTextField.CENTER);
			
			iField.addFocusListener(this);
						
			textLabels.add(i, iLabel);
			textFields.add(i, iField);
			
			colPanel.add(iLabel);
			colPanel.add(iField);
			
			fieldPanel.add(colPanel);
		}
		
		JPanel footPanel = new JPanel();
		footPanel.setLayout(new BorderLayout());
		footPanel.add(fieldPanel, BorderLayout.CENTER);
		footPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		add(visualWeb,   BorderLayout.CENTER);
		add(footPanel, BorderLayout.SOUTH);
		
		setSize(800, 500);
		setMinimumSize(new Dimension(500,450));
		visualWeb.updateMappings(cachedMappings);
		setVisible(true);
		visualWeb.repaint();
		visualWeb.revalidate();
		
		cachedEntry = textFields.get(0).getText();
		textFields.get(0).requestFocusInWindow();
	}

	@Override
	public boolean validateComponent()
	{
		for(int i=0; i<cachedMappings.length; i++)
		{
			boolean contains = false;
			
			for(int j=0; j<cachedMappings.length; j++)
			{
				if(cachedMappings[j] == i)
				{
					cachedMappings[i] = j;
					contains = true;
					break;
				}
			}
			
			if(!contains)
				return false;
		}
		
		return true;
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource().equals(jbOK))
		{
			if(validateComponent())
			{
				component = new Permutation(cachedMappings);
				this.dispose();
			}
			else
			{
				JOptionPane.showMessageDialog(this,
						"This is not a valid permutation.",
						"Validation Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
		else if(e.getSource().equals(jbCancel))
		{
			component = originalComponent;
			this.dispose();
		}
		else if(e.getSource().equals(jbHelp))
		{
			openHelpMessage(HelpMessageConstants.HELP_DLG_PERMUTATION);
		}
	}

	@Override
	public void focusGained(FocusEvent e)
	{
		if(e.getSource() instanceof JTextField)
		{
			JTextField field = (JTextField)e.getSource();
			cachedEntry = field.getText();
		}
	}

	@Override
	public void focusLost(FocusEvent e)
	{
		if(e.getSource() instanceof JTextField)
		{
			JTextField field = (JTextField)e.getSource();
			int index = textFields.indexOf(field);
			int input;
			
			try
			{
				input = Integer.valueOf(field.getText(), 16);
			}
			catch(NumberFormatException nfe)
			{
				input = -1;
			}
			
			if(input >= 0 && input < cachedMappings.length)
			{
				cachedMappings[index] = input;
				visualWeb.updateMappings(cachedMappings);
				
				if(validateComponent())
				{
					component = new Permutation(cachedMappings);
				}
			}
			else
			{
				field.setText(cachedEntry);
			}
			
			cachedEntry = null;
		}
	}

}
