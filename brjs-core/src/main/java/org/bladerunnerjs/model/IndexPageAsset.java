package org.bladerunnerjs.model;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bladerunnerjs.api.Asset;
import org.bladerunnerjs.api.memoization.MemoizedFile;
import org.bladerunnerjs.api.model.exception.ModelOperationException;
import org.bladerunnerjs.api.model.exception.RequirePathException;
import org.bladerunnerjs.utility.RequirePathUtility;
import org.bladerunnerjs.utility.reader.JsCommentStrippingReader;

public class IndexPageAsset extends LinkedFileAsset {

	public IndexPageAsset(MemoizedFile assetFile, AssetContainer assetContainer, String requirePrefix, List<Asset> implicitDependencies) {
		super(assetFile, assetContainer, requirePrefix, implicitDependencies);
	}
	
	@Override
	public List<Asset> getDependentAssets(BundlableNode bundlableNode) throws ModelOperationException {		
		List<Asset> assetList = super.getDependentAssets(bundlableNode);
		
		Set<String> dependencies = new HashSet<String>();
		List<String> aliases = new ArrayList<>();
		try {
			RequirePathUtility.addRequirePathsFromReader(getReader(), dependencies, aliases);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		List<String> dependenciesList = new ArrayList<String>(dependencies);
		try {
			assetList.addAll(bundlableNode.assets(assetContainer(), dependenciesList));
		} catch (RequirePathException e) {
			throw new ModelOperationException(e);
		}
		return assetList;
	}
	
	@Override
	public Reader getReader() throws IOException {
		return new JsCommentStrippingReader(super.getReader(), false);
	}
}
