package org.bladerunnerjs.spec.plugin.minifier;

import org.bladerunnerjs.model.App;
import org.bladerunnerjs.model.Aspect;
import org.bladerunnerjs.model.Blade;
import org.bladerunnerjs.plugin.bundler.js.NamespacedJsBundlerPlugin;
import org.bladerunnerjs.testing.specutility.engine.SpecTest;
import org.junit.Before;
import org.junit.Test;


public class ClosureMinifierPluginTest extends SpecTest
{
	private App app;
	private Aspect aspect;
	private Blade blade;
	
	private StringBuffer response = new StringBuffer();
	private String unminifiedContent;
	private String minifyWhitespaceContent;
	private String minifySimpleContent;
	private String minifyAdvancedContent;
	
	@Before
	public void initTestObjects() throws Exception
	{
		given(brjs).automaticallyFindsBundlers()
    		.and(brjs).automaticallyFindsMinifiers()
    		.and(brjs).hasBeenCreated();
    		app = brjs.app("app1");
    		aspect = app.aspect("default");
    		blade = app.bladeset("bs").blade("b1");
			
		/* only closure compiler service used to calculate responses - http://closure-compiler.appspot.com/home */
		unminifiedContent = "function hello(name) {\n"+
				"  alert('Hello, ' + name);\n"+
				"}\n"+
				"hello('New user');\n"+
				"\n";
		minifyWhitespaceContent = "function hello(name){alert(\"Hello, \"+name)}hello(\"New user\")";
		minifySimpleContent		= "function(){alert(\"Hello, New user\")";
		minifyAdvancedContent	= "alert(\"Hello, New user\")";
	}
	
	@Test
	public void closureMinifierRunsForRequestsWithClosureWhitespaceOption() throws Exception
	{
		given(aspect).hasClass("mypkg.Class1")
			.and(aspect).indexPageRefersTo("mypkg.Class1")
			.and(aspect).classFileHasContent("mypkg.Class1", unminifiedContent);
		when(app).requestReceived("/default-aspect/js/prod/en_GB/closure-whitespace/bundle.js", response);
		then(response).containsText(minifyWhitespaceContent);
	}
	
	@Test
	public void closureMinifierRunsForRequestsWithClosureSimpleOption() throws Exception
	{
		given(aspect).hasClass("mypkg.Class1")
			.and(aspect).indexPageRefersTo("mypkg.Class1")
			.and(aspect).classFileHasContent("mypkg.Class1", unminifiedContent);
		when(app).requestReceived("/default-aspect/js/prod/en_GB/closure-simple/bundle.js", response);
		then(response).containsText(minifySimpleContent);
	}
	
	@Test
	public void closureMinifierRunsForRequestsWithClosureAdvancedOption() throws Exception
	{
		given(aspect).hasClass("mypkg.Class1")
			.and(aspect).indexPageRefersTo("mypkg.Class1")
			.and(aspect).classFileHasContent("mypkg.Class1", unminifiedContent);
		when(app).requestReceived("/default-aspect/js/prod/en_GB/closure-advanced/bundle.js", response);
		then(response).containsText(minifyAdvancedContent);
	}
	
	@Test
	public void closureMinifierHandlesRequestsWithMultipleFiles() throws Exception
	{
		given(blade).hasPackageStyle("src/mypkg/bs/b1", NamespacedJsBundlerPlugin.JS_STYLE)
			.and(blade).hasClasses("mypkg.bs.b1.Class1", "mypkg.bs.b1.Class2")
			.and(aspect).indexPageRefersTo("mypkg.bs.b1.Class1")
			.and(blade).classRefersTo("mypkg.bs.b1.Class1", "mypkg.bs.b1.Class2");
		when(app).requestReceived("/default-aspect/js/prod/en_GB/closure-whitespace/bundle.js", response);
		then(response).containsMinifiedClasses("mypkg.bs.b1.Class1", "mypkg.bs.b1.Class2");
	}
	
	@Test
	public void closureMinifierHandlesAMixOfSourceFileTypes() throws Exception
	{
		given(blade).hasPackageStyle("src/mypkg.cjs", NamespacedJsBundlerPlugin.JS_STYLE)
			.and(blade).hasPackageStyle("mypkg.node", "node.js")
			.and(blade).hasClasses("mypkg.cjs.Class", "mypkg.node.Class")
			.and(aspect).indexPageRefersTo("mypkg.cjs.Class")
			.and(blade).classRefersTo("mypkg.cjs.Class",  "mypkg.node.Class");
		when(app).requestReceived("/default-aspect/js/prod/en_GB/closure-whitespace/bundle.js", response);
		then(response).containsMinifiedClasses("mypkg.cjs.Class", "mypkg.node.Class");
	}
	
	@Test
	public void closureMinifierStillAddsPackageDefinitionsBlock() throws Exception
	{
		given(blade).hasPackageStyle("src/mypkg.cjs", NamespacedJsBundlerPlugin.JS_STYLE)
    		.and(blade).hasClasses("mypkg.cjs.Class", "mypkg.node.Class")
    		.and(aspect).indexPageRefersTo("mypkg.cjs.Class");
		when(app).requestReceived("/default-aspect/js/prod/en_GB/closure-whitespace/bundle.js", response);
		then(response).containsMinifiedClasses("mypkg.cjs.Class")
			.and(response).containsText("window.mypkg={\"cjs\":{}};");
	}
	
}
