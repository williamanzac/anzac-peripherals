package anzac.docs.maven.plugin;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import net.bican.wordpress.Page;
import net.bican.wordpress.Wordpress;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;

import redstone.xmlrpc.XmlRpcFault;

@Mojo(name = "PublishToWordpress", defaultPhase = LifecyclePhase.DEPLOY)
public class PublishToWordpressMojo extends AbstractMojo {

	@Parameter
	private File docSource;

	@Parameter(required = false)
	private Map<String, String> idToFile;

	@Parameter
	private String serverId;

	@Parameter(readonly = true, defaultValue = "${settings}")
	private Settings settings;

	@Parameter(required = true)
	private String url;

	@Parameter(defaultValue = "false")
	private boolean skip;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (skip) {
			getLog().info("Skipping");
			return;
		}
		final Server server = settings.getServer(serverId);
		final String password = server.getPassword();
		final String username = server.getUsername();
		if (!docSource.isDirectory()) {
			throw new MojoFailureException("docSource is not a valid directory.");
		}
		try {
			final Wordpress wordpress = new Wordpress(username, password, url);
			if (idToFile != null) {
				for (final Entry<String, String> entry : idToFile.entrySet()) {
					if (entry == null || entry.getKey() == null || entry.getValue() == null) {
						getLog().debug("entry null");
						continue;
					}
					try {
						final Page page = wordpress.getPage(Integer.parseInt(entry.getValue()));
						final File file = new File(docSource, entry.getKey());
						if (file.isFile()) {
							final String description = FileUtils.readFileToString(file);
							page.setDescription(description);
							page.setDateCreated(new Date());
							final Boolean success = wordpress.editPage(page.getPage_id(), page, page.getPage_status());
							if (!success) {
								throw new MojoExecutionException("Unable to upload new content.");
							}
							getLog().info("Published changes for " + file);
						} else {
							getLog().warn("file " + file + " does not exist");
						}
					} catch (final XmlRpcFault e) {
						throw new MojoExecutionException("Unable to upload doc", e);
					} catch (IOException e) {
						throw new MojoExecutionException("Unable to read source doc", e);
					}
				}
			} else {
				getLog().warn("No id to File mapping provided");
			}
		} catch (final MalformedURLException e) {
			throw new MojoFailureException("Invalid url", e);
		}
	}
}
