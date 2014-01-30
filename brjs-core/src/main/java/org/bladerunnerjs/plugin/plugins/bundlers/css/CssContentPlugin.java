package org.bladerunnerjs.plugin.plugins.bundlers.css;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.bladerunnerjs.model.Asset;
import org.bladerunnerjs.model.AssetLocation;
import org.bladerunnerjs.model.BRJS;
import org.bladerunnerjs.model.BundleSet;
import org.bladerunnerjs.model.ParsedContentPath;
import org.bladerunnerjs.model.ThemeAssetLocation;
import org.bladerunnerjs.model.exception.request.BundlerFileProcessingException;
import org.bladerunnerjs.model.exception.request.BundlerProcessingException;
import org.bladerunnerjs.model.exception.request.MalformedTokenException;
import org.bladerunnerjs.plugin.base.AbstractContentPlugin;
import org.bladerunnerjs.utility.ContentPathParser;
import org.bladerunnerjs.utility.ContentPathParserBuilder;

public class CssContentPlugin extends AbstractContentPlugin {
	private final ContentPathParser contentPathParser;
	
	{
		ContentPathParserBuilder contentPathParserBuilder = new ContentPathParserBuilder();
		contentPathParserBuilder.accepts("css/<theme>_css.bundle").as("simple-request").and("css/<theme>_<languageCode>_css.bundle")
			.as("language-request").and("css/<theme>_<languageCode>_<countryCode>_css.bundle").as("locale-request").where("theme")
			.hasForm(ContentPathParserBuilder.NAME_TOKEN).and("languageCode").hasForm("[a-z]{2}").and("countryCode").hasForm("[A-Z]{2}");
		
		contentPathParser = contentPathParserBuilder.build();
	}
	
	@Override
	public void setBRJS(BRJS brjs) {
		// do nothing
	}
	
	@Override
	public String getRequestPrefix() {
		return "css";
	}
	
	@Override
	public String getGroupName() {
		return "text/css";
	}
	
	@Override
	public ContentPathParser getContentPathParser() {
		return contentPathParser;
	}
	
	@Override
	public List<String> getValidDevContentPaths(BundleSet bundleSet, List<String> locales) throws BundlerProcessingException {
		return getValidContentPaths(bundleSet, locales);
	}
	
	@Override
	public List<String> getValidProdContentPaths(BundleSet bundleSet, List<String> locales) throws BundlerProcessingException {
		return getValidContentPaths(bundleSet, locales);
	}
	
	@Override
	public void writeContent(ParsedContentPath contentPath, BundleSet bundleSet, OutputStream os) throws BundlerProcessingException {
		String theme = contentPath.properties.get("theme");
		String languageCode = contentPath.properties.get("languageCode");
		String countryCode = contentPath.properties.get("countryCode");
		String locale = null;
		
		if (languageCode != null && countryCode != null) {
			locale = languageCode + "_" + countryCode;
		}
		else if (languageCode != null) {
			locale = languageCode;
		}
		
		String pattern = getFilePattern(locale, null);
		
		try(Writer writer = new OutputStreamWriter(os)) {
			List<Asset> cssAssets = bundleSet.getResourceFiles("css");
			for(Asset cssAsset : cssAssets) {
				String assetTheme = getTheme(cssAsset.getAssetLocation());
				File cssFile = cssAsset.getUnderlyingFile();
				
				if(assetTheme.equals(theme) && cssFile.getName().matches(pattern)) {
					writeAsset(cssAsset, writer);
				}
			}
		}
		catch (IOException e) {
			throw new BundlerProcessingException(e);
		}
	}
	
	private String getTheme(AssetLocation assetLocation) {
		File dir = assetLocation.dir();
		
		while(!dir.getParentFile().getName().equals("themes")) {
			dir = dir.getParentFile();
		}
		
		return dir.getName();
	}
	
	private void writeAsset(Asset cssAsset, Writer writer) throws BundlerFileProcessingException {
		try {
			CssRewriter processor = new CssRewriter(cssAsset);
			writer.append(processor.getFileContents());
		}
		catch (IOException e) {
			throw new BundlerFileProcessingException(cssAsset.getUnderlyingFile(), e, "Error while bundling file.");
		}
	}
	
	private List<String> getValidContentPaths(BundleSet bundleSet, List<String> locales) throws BundlerProcessingException {
		List<String> contentPaths = new ArrayList<>();
		
		try {
			ThemeAssetLocation themeAssetLocation = (ThemeAssetLocation) bundleSet.getBundlableNode().assetLocation("themes");
			contentPaths.add(contentPathParser.createRequest("simple-request", "common"));
			
			for (String theme : themeAssetLocation.themes()) {
				if (theme != "common") {
					contentPaths.add(contentPathParser.createRequest("simple-request", theme));
				}
				
				for (String locale : locales) {
					if (!locale.contains("_")) {
						String language = locale;
						
						contentPaths.add(contentPathParser.createRequest("language-request", theme, language));
					} else {
						String[] parts = locale.split("_");
						String language = parts[0];
						String country = parts[1];
						
						contentPaths.add(contentPathParser.createRequest("locale-request", theme, language, country));
					}
				}
			}
		}
		catch(MalformedTokenException e) {
			throw new BundlerProcessingException(e);
		}
		
		return contentPaths;
	}
	
	private String getFilePattern(String locale, String browser) {
		String pattern = "";
		if (locale != null) {
			// .*_en_GB.css
			pattern = ".*" + locale;
		} else if (browser != null) {
			// .*_ie7.css
			pattern = ".*" + browser;
		} else {
			// If we are looking for a CSS file without the locale or browser,
			// then
			// we can assume that it does not have underscores in it.
			pattern = "[^_]+";
		}
		
		return pattern + "\\.css";
	}
}
