package io.github.rowak.nanoleafdesktop.spotify.effect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.github.rowak.Aurora;
import io.github.rowak.Color;
import io.github.rowak.Frame;
import io.github.rowak.Panel;
import io.github.rowak.StatusCodeException;
import io.github.rowak.effectbuilder.CustomEffectBuilder;
import io.github.rowak.nanoleafdesktop.spotify.SpecificAudioAnalysis;
import io.github.rowak.nanoleafdesktop.spotify.SpotifyEffectType;

public class SpotifyPulseBeatsEffect extends SpotifyEffect
{
	private float loudness;
	private Random random;
	
	public SpotifyPulseBeatsEffect(Color[] palette, Aurora aurora)
	{
		super(SpotifyEffectType.PULSE_BEATS, palette, aurora);
		random = new Random();
	}
	
	@Override
	public void init(){}
	
	@Override
	public void run(SpecificAudioAnalysis analysis)
					throws StatusCodeException, IOException
	{
		updateLoudness(analysis);
		
		if (analysis.getBeat() != null)
		{
			int panelIndex = random.nextInt(panels.length);
			int panelId = panels[panelIndex].getId();
			int r = palette[paletteIndex].getRed();
			int g = palette[paletteIndex].getGreen();
			int b = palette[paletteIndex].getBlue();
			java.awt.Color original = new java.awt.Color(r, g, b);
			original = applyLoudnessToColor(original);
			java.awt.Color darker = original.darker().darker().darker();
			CustomEffectBuilder ceb = new CustomEffectBuilder(aurora);
			ceb.addFrame(panelId, new Frame(original.getRed(),
					original.getGreen(), original.getBlue(), 0, 1));
			ceb.addFrame(panelId, new Frame(0, 0, 0, 0, 5));
			List<Integer> marked = new ArrayList<Integer>();
			marked.add(panelId);
			final int INITIAL_TIME = 1;
			setNeighbors(panels[panelIndex], marked, 
					panels, ceb, darker, INITIAL_TIME);
			
			new Thread(() ->
			{
				try
				{
					aurora.effects().displayEffect(ceb.build("", false));
				}
				catch (StatusCodeException sce)
				{
					sce.printStackTrace();
				}
			}).start();
			setNextPaletteColor();
		}
	}
	
	public void setNeighbors(Panel panel, final List<Integer> marked,
			Panel[] panels, CustomEffectBuilder ceb, java.awt.Color color,
			int time) throws StatusCodeException, IOException
	{
		time += 1;
		for (Panel p : panel.getNeighbors(panels))
		{
			if (!marked.contains(p.getId()))
			{
				ceb.addFrame(p, new Frame(color.getRed(),
						color.getGreen(), color.getBlue(), 0, time));
				ceb.addFrame(p, new Frame(0, 0, 0, 0, 5));
				marked.add(p.getId());
				setNeighbors(p, marked, panels, ceb, color, time);
			}
		}
	}
	
	private void updateLoudness(SpecificAudioAnalysis analysis)
	{
		if (analysis.getSegment() != null)
		{
			loudness = loudnessToPercent(analysis.getSegment().getLoudnessStart());
		}
	}
	
	private java.awt.Color applyLoudnessToColor(java.awt.Color color)
	{
		float[] hsb = new float[3];
		hsb = java.awt.Color.RGBtoHSB(color.getRed(),
				color.getGreen(), color.getBlue(), hsb);
		hsb[2] = ((hsb[2]*100f)*loudness)/100f;
		color = java.awt.Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
		return color;
	}
	
	private float loudnessToPercent(float loudness)
	{
		final float MAX = 0f;
		final float MIN = -40.0f;
		if (loudness < MIN)
		{
			return 0f;
		}
		else if (loudness > MAX)
		{
			return 1f;
		}
		return (1 - loudness/MIN);
	}
}