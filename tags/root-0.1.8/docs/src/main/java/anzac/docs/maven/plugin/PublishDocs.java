package anzac.docs.maven.plugin;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.bican.wordpress.Page;
import net.bican.wordpress.Wordpress;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import redstone.xmlrpc.XmlRpcFault;

public class PublishDocs {
	private static final Map<Integer, String> idToClass = new HashMap<Integer, String>();

	static {
		idToClass.put(113, "Workbench");
		idToClass.put(115, "RecipeStorage");
		idToClass.put(143, "ItemRouter");
		idToClass.put(150, "FluidRouter");
		idToClass.put(162, "ItemStorage");
		idToClass.put(164, "FluidStorage");
	}

	private static File docDir = new File(".");

	private static final Set<String> editedPages = new HashSet<String>();

	public static void main(final String[] args) throws XmlRpcFault, IOException {
		final Wordpress wordpress = new Wordpress("admin", "%$D%&&@#LV", "http://anzacgaming.co.uk/xmlrpc.php");
		// final List<Page> pages = wordpress.getPages();
		// for (final Page page : pages) {
		// System.out.println(page.getPage_id());
		// System.out.println(page.getLink());
		// System.out.println(page.getTitle());
		// // page.setDescription("");
		// System.out.println(page.getPage_status());
		// final String fileName = StringUtils.deleteWhitespace(page.getTitle());
		// System.out.println(fileName);
		// }
		// final List<PageDefinition> pageList = wordpress.getPageList();
		// for (final PageDefinition pageDefinition : pageList) {
		// System.out.println(pageDefinition.getPage_title());
		// System.out.println(pageDefinition.toOneLinerString());
		// }
		// final List<PostAndPageStatus> pageStatusList = wordpress.getPageStatusList();
		// for (final PostAndPageStatus postAndPageStatus : pageStatusList) {
		// System.out.println(postAndPageStatus.toOneLinerString());
		// }
		for (final Entry<Integer, String> entry : idToClass.entrySet()) {
			final Page page = wordpress.getPage(entry.getKey());
			System.out.println(page.getPage_id());
			System.out.println(page.getLink());
			System.out.println(page.getTitle());
			System.out.println(page.getPage_status());
			final String fileName = StringUtils.deleteWhitespace(page.getTitle());
			System.out.println(fileName);
			final File file = new File(docDir, fileName);
			if (file.isFile()) {
				final String description = FileUtils.readFileToString(file);
				page.setDescription(description);
				page.setDateCreated(new Date());
				final Boolean success = wordpress.editPage(page.getPage_id(), page, page.getPage_status());
				System.out.println(success);
				if (success) {
					editedPages.add(fileName);
				}
			}
		}
		for (final String page : editedPages) {
			System.out.println(page);
		}
	}
}
