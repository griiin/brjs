package org.bladerunnerjs.plugin.plugins.bundlers.namespacedjs;

import java.util.ArrayList;
import java.util.List;

import org.bladerunnerjs.model.Asset;
import org.bladerunnerjs.model.AssetFileInstantationException;
import org.bladerunnerjs.model.AssetLocation;
import org.bladerunnerjs.model.BRJS;
import org.bladerunnerjs.model.LinkedAsset;
import org.bladerunnerjs.model.SourceModule;
import org.bladerunnerjs.plugin.base.AbstractAssetPlugin;

public class NamespacedJsAssetPlugin extends AbstractAssetPlugin {
	@Override
	public void setBRJS(BRJS brjs) {
	}
	
	@Override
	public List<SourceModule> getSourceModules(AssetLocation assetLocation) {
		try {
			if (assetLocation.getJsStyle().equals(NamespacedJsContentPlugin.JS_STYLE)) {
				return assetLocation.getAssetContainer().root().createAssetFilesWithExtension(NamespacedJsSourceModule.class, assetLocation, "js");
			}
			else {
				return new ArrayList<>();
			}
		}
		catch (AssetFileInstantationException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public List<LinkedAsset> getLinkedAssets(AssetLocation assetLocation) {
		return new ArrayList<>();
	}
	
	@Override
	public List<Asset> getAssets(AssetLocation assetLocation) {
		return new ArrayList<>();
	}
}