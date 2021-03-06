package org.bladerunnerjs.spec.aliasing;

import java.io.IOException;

import org.bladerunnerjs.api.model.exception.request.ContentFileProcessingException;
import org.bladerunnerjs.api.spec.engine.BuilderChainer;
import org.bladerunnerjs.api.spec.engine.SpecTest;
import org.bladerunnerjs.api.spec.engine.SpecTestBuilder;
import org.bladerunnerjs.plugin.bundlers.aliasing.AliasDefinition;
import org.bladerunnerjs.plugin.bundlers.aliasing.AliasDefinitionsFile;
import org.bladerunnerjs.plugin.bundlers.aliasing.AliasOverride;

public class AliasDefinitionsFileBuilder implements SpecTestBuilder {
	private AliasDefinitionsFile aliasDefinitionsFile;
	private BuilderChainer builderChainer;
	private SpecTest specTest;
	
	public AliasDefinitionsFileBuilder(SpecTest specTest, AliasDefinitionsFile aliasDefinitionsFile) {
		this.aliasDefinitionsFile = aliasDefinitionsFile;
		builderChainer = new BuilderChainer(specTest);
		this.specTest = specTest;
	}
	
	public BuilderChainer exists() throws Exception {
		writeAliasDefinitionsFile();
		
		return builderChainer;
	}
	
	public BuilderChainer hasAlias(String aliasName, String classRef, String interfaceRef) throws Exception {
		aliasDefinitionsFile.addAlias(new AliasDefinition(aliasName, classRef, interfaceRef));
		writeAliasDefinitionsFile();
		
		return builderChainer;
	}
	
	public BuilderChainer hasAlias(String aliasName, String classRef) throws Exception {
		return hasAlias(aliasName, classRef, null);
	}
	
	public BuilderChainer hasIncompleteAlias(String aliasName, String interfaceRef) throws Exception {
		return hasAlias(aliasName, interfaceRef);
	}
	
	public BuilderChainer hasScenarioAlias(String scenarioName, String aliasName, String classRef) throws Exception {
		aliasDefinitionsFile.addScenarioAlias(scenarioName, new AliasOverride(aliasName, classRef));
		writeAliasDefinitionsFile();
		
		return builderChainer;
	}
	
	public BuilderChainer hasGroupAlias(String groupName, String aliasName, String classRef) throws Exception {
		aliasDefinitionsFile.addGroupAliasOverride(groupName, new AliasOverride(aliasName, classRef));
		writeAliasDefinitionsFile();
		
		return builderChainer;
	}
	
	public void writeAliasDefinitionsFile() throws ContentFileProcessingException, IOException {
		aliasDefinitionsFile.write();
		specTest.brjs.getFileModificationRegistry().incrementFileVersion(aliasDefinitionsFile.getUnderlyingFile());
	}
	
	public String getUnderlyingFilePath()
	{
		return aliasDefinitionsFile.getUnderlyingFile().getPath();
	}
	
}
