package io.github.rowak.discovery;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.github.kevinsawicki.http.HttpRequest;

import io.github.rowak.Effect;

public class Discovery
{
	private static final String BASE_ENDPOINT = "https://my.nanoleaf.me/api/v1";
	
	public static EffectMetadata[] getTopEffects(int page)
	{
		return getEffectsList(getEffectsData("top", page));
	}
	
	public static EffectMetadata[] getRecentEffects(int page)
	{
		return getEffectsList(getEffectsData("recent", page));
	}
	
	private static JSONObject getEffectsData(String type, int page)
	{
		JSONObject json = null;
		while (json == null)
		{
			try
			{
				return new JSONObject(HttpRequest.get(String.format(
						"%s/effects/%s?page=%d", BASE_ENDPOINT, type, page)).body());
			}
			catch (JSONException je)
			{
				
			}
		}
		return null;
	}
	
	private static EffectMetadata[] getEffectsList(JSONObject data)
	{
		int numItems = data.getInt("items");
		JSONArray items = data.getJSONArray("data");
		EffectMetadata[] effects = new EffectMetadata[numItems];
		for (int i = 0; i < numItems; i++)
		{
			effects[i] = new EffectMetadata(items.getJSONObject(i));
		}
		return effects;
	}
	
	public static Effect downloadEffect(String key)
	{
		return Effect.fromJSON(HttpRequest.get(String.format("%s/effects/download/%s",
				BASE_ENDPOINT, key)).body());
	}
}