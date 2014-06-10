package anzac.docs.maven.plugin;

import java.io.File;
import java.net.MalformedURLException;

import net.bican.wordpress.Wordpress;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;

public abstract class AbstractWordpressMojo extends AbstractMojo {

	@Parameter
	protected File docSource;
	@Parameter
	protected String serverId;
	@Parameter(readonly = true, defaultValue = "${settings}")
	protected Settings settings;
	@Parameter(required = true)
	protected String url;
	@Parameter(defaultValue = "false")
	protected boolean skip;

	protected Wordpress createWordpress() throws MojoFailureException {
		final Server server = settings.getServer(serverId);
		final String password = server.getPassword();
		final String username = server.getUsername();
		final Wordpress wordpress;
		try {
			wordpress = new Wordpress(username, password, url);
		} catch (final MalformedURLException e) {
			throw new MojoFailureException("Invalid url", e);
		}
		return wordpress;
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (skip) {
			getLog().info("Skipping");
			return;
		}
		if (!docSource.isDirectory()) {
			throw new MojoFailureException("docSource is not a valid directory.");
		}
	}
}