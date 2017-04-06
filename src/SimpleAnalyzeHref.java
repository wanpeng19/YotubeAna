import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SimpleAnalyzeHref {

	private static String searchUrl = "http://www.youtube.com/results?q={key_words}&spf=navigate";
	public static Map<String ,String> GOOGLE_URLS = new HashMap<String,String>();
	static{
		GOOGLE_URLS.put("google.uk", "https://www.google.co.uk/search?q={key_words}");//英国     
		GOOGLE_URLS.put("google.fr", "https://www.google.fr/search?q={key_words}"); //法国  
		GOOGLE_URLS.put("google.de", "https://www.google.de/search?q={key_words}");//德国   
		GOOGLE_URLS.put("google.es", "https://www.google.es/search?q={key_words}");   //西班牙  
		GOOGLE_URLS.put("google.it", "https://www.google.it/search?q={key_words}");//意大利      
		GOOGLE_URLS.put("google.hk", "https://www.google.com.hk/search?q={key_words}");//中国香港    
		GOOGLE_URLS.put("google.jp", "https://www.google.co.jp/search?q={key_words}");//日本 
		GOOGLE_URLS.put("google.au", "https://www.google.com.au/search?q={key_words}");//澳大利亚  
		GOOGLE_URLS.put("google.ca", "https://www.google.ca/search?q={key_words}"); //加拿大  
		GOOGLE_URLS.put("google.us", "https://www.google.com/search?q={key_words}");//美国     
		
	}
	
	private static String youtubeurl = "http://www.youtube.com/results?search_query={key_words}&spf=navigate";
	
	
	private static List<String> hrefs = new ArrayList<String>();
	private static List<String> newKeys = new ArrayList<String>();
	
	private static int searchPage = 1;

	private int count = 1;

	public static void main(String[] args) {
		try {
			
		    Object[] options ={ "GOOGLE", "油管" };  
		    int option = JOptionPane.showOptionDialog(null, "请选择网站？", "不知道用什么标题",JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]); 
		    
		    String country = null;
		    if(option == 0){
			    Object[] countrys ={ "UK","FR ","FR","ES","IT","HK","JP","AU","CA","US"};  
			    country = (String) JOptionPane.showInputDialog(null,"请选择google国家:\n", "国家", JOptionPane.PLAIN_MESSAGE, null, countrys, "英国");
			    System.out.println("国家："+country);
		    }

		    
			String keyWords = JOptionPane.showInputDialog("请输入搜索关键字");

		    if(option == 0){//google
				keyWords = keyWords.replace(" ", "+");
		    } else if(option == 1){
				keyWords = keyWords.replace(" ", "++");
			}
			
			
			searchPage = Integer.valueOf(JOptionPane.showInputDialog("请输入搜索页数"));
			JFileChooser fileChooser = new JFileChooser("D:\\");
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			
			JOptionPane.showMessageDialog(null, "请选择搜索结果导出路径，导出文本为result.txt", "请选择导出路径", JOptionPane.ERROR_MESSAGE); 
			
			int returnVal = fileChooser.showOpenDialog(fileChooser);
			String filePath = "";
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				 filePath = fileChooser.getSelectedFile().getAbsolutePath();// 这个就是你选择的文件夹的路径
			}
			
			// String keyWords = "巴雷特";
			if(option==0){// google
				String googleUrl = GOOGLE_URLS.get("google."+ country.toLowerCase());
				new SimpleAnalyzeHref().analyzeGoogle(googleUrl.replace("{key_words}", keyWords), keyWords);
			} else if(option == 1){
				new SimpleAnalyzeHref().analyzeYoutube(youtubeurl.replace("{key_words}", keyWords), keyWords);
			}

			System.out.println("hrefs="+hrefs);
			BufferedWriter br = new BufferedWriter(new FileWriter(new File(filePath + "/result.txt")));
			for(String tem : hrefs){
				br.append(tem).append("\n");
			}
			br.flush();
			br.close();
			System.out.println("############################搜索分析结束，请查看输出结果文档###################################");
			System.out.println("结果文档路径为：" + filePath + "/result.txt");
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
		}
	}

	private void analyzeGoogle(String baseurl, String keyWords) {
	    Pattern linkPattern = Pattern.compile("href=\"/url\\?q=(.*?)\"");
		while (count <= searchPage){
			String currentUrl = baseurl + "&start="+ (count-1)*10;
			try {
				HttpClient httpclient = HttpClients.createDefault();
				System.out.println("请求：" + currentUrl + "次数为" + count);
				HttpGet httpget = new HttpGet(currentUrl);
				HttpResponse response = httpclient.execute(httpget);
				HttpEntity entity = response.getEntity();
				String html = EntityUtils.toString(entity);
				System.out.println(html);
				Document doc = Jsoup.parse(html);
				Elements links = doc.select("h3 a"); // a with href
                Iterator<Element> iterator = links.iterator();
                while(iterator.hasNext()){
                	String link = iterator.next().toString();
                	if(link.toString().indexOf("/url?q=http") > 0){
                        Matcher matcherU = linkPattern.matcher(link);
                        if (matcherU.find()) {
                        	String result = matcherU.group(1);
                        	System.out.println(result);
                        	hrefs.add(result);
                        }
                	}
      
                }
				count++;
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}
		
	}

	public void analyzeYoutube(String url, String keyWords, String... nextKey) {
		System.out.println(url);
		if (count > searchPage)
			return;
		try {
			HttpClient httpclient = HttpClients.createDefault();
			System.out.println("请求：" + url + "次数为" + count);
			HttpGet httpget = new HttpGet(url);
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			fromJson(EntityUtils.toString(entity), keyWords);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void fromJson(String json, String keyWords) throws Exception {
		// ObjectMapper om = new ObjectMapper();
		// List list = om.readValue(json, List.class);
		// Map map = (Map) list.get(1);
		// Map body = (Map) map.get("body");
		//
		// String bodyStr = body.get("content").toString();
		json = json.replace("\\", "");
//		System.out.println(json);
		System.out.println("######################################################################");
		// href=\"\/user\/billywah2\"

	    Pattern userPattern = Pattern.compile("href=\"/watch(.*?)\"");
		Matcher matcherU = userPattern.matcher(json);
		while (matcherU.find()) {
			hrefs.add("https://www.youtube.com/watch?" + matcherU.group(1));
		}

		String nextKey = null;
		if (count == 1 || count % 7 == 0) {
			// href="/results?sp=EgIIAw%253D%253Du0026amp;q=%E5%B7%B4%E9%9B%B7%E7%89%B9"
			Pattern patternNext = Pattern.compile("href=\"/results\\?sp=(.*?)amp;q=");
			Matcher matcherN = patternNext.matcher(json);
			while (matcherN.find()) {
				String ms = matcherN.group(1);
				// System.out.println(ms + " " + ms.length());
				if (ms.startsWith("S")) {
					newKeys.add(ms);
				}
			}
			if (newKeys.size() == 0) {
				// href="/results?q=%E5%B7%B4%E9%9B%B7%E7%89%B9u0026amp;sp=EgIIAg%253D%253D"
				patternNext = Pattern.compile("href=\"/results\\?q=(.*?)amp;sp=(.*?)\"");
				matcherN = patternNext.matcher(json);
				while (matcherN.find()) {
					String ms = matcherN.group(2);
					if (ms.startsWith("S")) {
						newKeys.add(ms);
					}
				}
			}
			if (newKeys.size() == 0)
				System.out.println("youtube返回异常,请稍后重试");
		}
		// System.out.println(newKeys);
		nextKey = newKeys.get((count - 1) % 7);
		count++;
		String newsearchUrl = searchUrl.replace("{key_words}", keyWords) + "&sp=" + nextKey;
		analyzeYoutube(newsearchUrl, keyWords);
	}
}
