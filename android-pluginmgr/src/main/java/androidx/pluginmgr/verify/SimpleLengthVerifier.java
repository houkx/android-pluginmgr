package androidx.pluginmgr.verify;
import java.io.File;

/**
  * @author Lody
  * @version 1.0
  */
public class SimpleLengthVerifier implements PluginOverdueVerifier
{

	@Override
	public boolean isOverdue(File originPluginFile, File targetExistFile)
	{
		
		return originPluginFile.length() != targetExistFile.length();
	}
	
}
