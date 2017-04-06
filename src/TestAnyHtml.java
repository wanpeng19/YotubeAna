import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class TestAnyHtml {
	
	public static void main(String[] args) {
		try {
//			File input = new File("‪C:\1.html");
		      File s= FileUtils.getFile(new File("‪c:\\D\\11.html"));
			Document doc = Jsoup.parse(s, "UTF-8", "http://example.com/");
			Elements links = doc.select("a[href]"); //带有href属性的a元素
			System.out.println(links.text());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
