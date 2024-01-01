package dev.jvfs;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class EscapeCrystalPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(EscapeCrystalPlugin.class);
		RuneLite.main(args);
	}
}
