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
			String keyWords = JOptionPane.showInputDialog("�����������ؼ���");
			keyWords = keyWords.replace(" ", "++");
			searchPage = Integer.valueOf(JOptionPane.showInputDialog("����������ҳ��"));
			JFileChooser fileChooser = new JFileChooser("D:\\");
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			
			JOptionPane.showMessageDialog(null, "��ѡ�������������·���������ı�Ϊresult.txt", "��ѡ�񵼳�·��", JOptionPane.ERROR_MESSAGE); 
			
			int returnVal = fileChooser.showOpenDialog(fileChooser);
			String filePath = "";
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				 filePath = fileChooser.getSelectedFile().getAbsolutePath();// ���������ѡ����ļ��е�·��
			}
			
			// String keyWords = "������";
			new SimpleAnalyze2().analyze(baseurl.replace("{key_words}", keyWords), keyWords);
			System.out.println("�����û�Ϊ" + users);
			System.out.println("����Ƶ��Ϊ" + channels);
			getAbount(users,true);
			getAbount(channels,false);
			
//			System.out.println(hasEmali);
			BufferedWriter br = new BufferedWriter(new FileWriter(new File(filePath + "/result.txt")));
			for(String tem : hasEmali){
				br.append(tem).append("\n");
			}
			br.flush();
			br.close();
			System.out.println("############################����������������鿴�������ĵ�###################################");
			System.out.println("����ĵ�·��Ϊ��" + filePath + "/result.txt");
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
			System.out.println("���������ҳ"+url);
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			String aboutStr = EntityUtils.toString(entity);
			if(aboutStr.indexOf("View email address") > 0 || aboutStr.indexOf("�鿴�����ʼ���ַ") > 0 ) {
				hasEmali.add(url);
			}
		}
	}

	public void analyze(String url, String keyWords, String... nextKey) {
		if (count > searchPage)
			return;
		try {
			HttpClient httpclient = HttpClients.createDefault();
			System.out.println("����" + url + "����Ϊ" + count);
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
				System.out.println("youtube�����쳣,���Ժ�����");
		}
		// System.out.println(newKeys);
		nextKey = newKeys.get((count - 1) % 7);
		count++;
		String newsearchUrl = searchUrl.replace("{key_words}", keyWords) + "&sp=" + nextKey;
		analyze(newsearchUrl, keyWords);
	}
}
