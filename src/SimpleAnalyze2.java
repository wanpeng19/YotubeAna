import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

public class SimpleAnalyze2 {

	private static String searchUrl = "http://www.youtube.com/results?q={key_words}&spf=navigate";
	private static String baseurl = "http://www.youtube.com/results?search_query={key_words}&spf=navigate";
	private static String aboutUrlU = "http://www.youtube.com/user/{user_id}/about";
	private static String aboutUrlC = "http://www.youtube.com/channel/{user_id}/about";
	
	private static Set<String> users = new HashSet<String>();
	private static Set<String> channels = new HashSet<String>();
	private static List<String> newKeys = new ArrayList<String>();
	private static List<String> hasEmali = new ArrayList<String>();
	
	private static int searchPage = 1;

	private int count = 1;

	public static void main(String[] args) {
		try {
			String keyWords = JOptionPane.showInputDialog("请输入搜索关键字");
			keyWords = keyWords.replace(" ", "++");
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
			new SimpleAnalyze2().analyze(baseurl.replace("{key_words}", keyWords), keyWords);
			System.out.println("所有用户为" + users);
			System.out.println("所有频道为" + channels);
			getAbount(users,true);
			getAbount(channels,false);
			
//			System.out.println(hasEmali);
			BufferedWriter br = new BufferedWriter(new FileWriter(new File(filePath + "/result.txt")));
			for(String tem : hasEmali){
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
	
	public static void getAbount(Collection<String> list,boolean isU)throws Exception{
		for(String id : list) {
			String url = (isU ? aboutUrlU : aboutUrlC).replace("{user_id}", id);
			HttpClient httpclient = HttpClients.createDefault();
			HttpGet httpget = new HttpGet(url);
			System.out.println("请求个人主页"+url);
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			String aboutStr = EntityUtils.toString(entity);
			if(aboutStr.indexOf("View email address") > 0 || aboutStr.indexOf("查看电子邮件地址") > 0 ) {
				hasEmali.add(url);
			}
		}
	}

	public void analyze(String url, String keyWords, String... nextKey) {
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

		Pattern userPattern = Pattern.compile("href=\"/user/(.*?)\"");
		Matcher matcherU = userPattern.matcher(json);
		while (matcherU.find()) {
			users.add(matcherU.group(1));
		}

		Pattern channelPattern = Pattern.compile("href=\"/channel/(.*?)\"");
		Matcher matcherC = channelPattern.matcher(json);
		while (matcherC.find()) {
			channels.add(matcherC.group(1));
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
		analyze(newsearchUrl, keyWords);
	}
}
