package anzac.docs.maven.plugin;

import static java.text.MessageFormat.format;
import static org.apache.commons.io.FileUtils.readFileToString;

import java.io.File;
import java.io.IOException;
import java.util.List;

import net.bican.wordpress.Page;
import net.bican.wordpress.Wordpress;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import redstone.xmlrpc.XmlRpcArray;
import redstone.xmlrpc.XmlRpcFault;

@Mojo(name = "AnnounceToWordpress", defaultPhase = LifecyclePhase.DEPLOY)
public class AnnounceToWordpressMojo extends AbstractWordpressMojo {

	@Parameter(required = false)
	private List<String> categories;

	@Parameter(required = false)
	private List<String> tags;

	@Parameter(required = false, defaultValue = "true")
	private boolean allowComments;

	@Parameter(required = false, defaultValue = "true")
	private boolean allowPings;

	@Parameter(required = true)
	private int authorId;

	@Parameter(required = false, defaultValue = "releasenotes.xml")
	private String releaseNotes;

	@Parameter(required = false, defaultValue = "true")
	private boolean publish;

	@Component
	private MavenProject project;

	@SuppressWarnings("unchecked")
	@Override
	public void execute(final Wordpress wordpress) throws MojoExecutionException, MojoFailureException {
		final String name = project.getName();
		final String version = project.getVersion();

		final File releaseNotesFile = new File(docSource, releaseNotes);
		if (!releaseNotesFile.isFile()) {
			throw new MojoFailureException(releaseNotes + " is not a valid file.");
		}

		try {
			final String description = readFileToString(releaseNotesFile);
			final Page page = new Page();
			page.setDescription(description);
			page.setTitle(format("{0} {1} released", name, version));
			final XmlRpcArray catList = new XmlRpcArray();
			catList.addAll(categories);
			page.setCategories(catList);
			page.setMt_allow_comments(allowComments ? 1 : 0);
			page.setMt_allow_pings(allowPings ? 1 : 0);
			final String keywords = StringUtils.join(tags, ", ");
			page.setMt_keywords(keywords);
			page.setWp_author_id(Integer.toString(authorId));
			final String newId = wordpress.newPost(page, publish);
			getLog().info(newId);
		} catch (final XmlRpcFault e) {
			throw new MojoExecutionException("Unable to upload doc", e);
		} catch (final IOException e) {
			throw new MojoExecutionException("Unable to read changelog", e);
		}
	}
}
