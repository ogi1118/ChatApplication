package ChatPackage;

import java.io.*;
import java.util.*;

import ChatPackage.TestWeather.*;
import ChatPackage.ResponseCandidate;
import ChatPackage.ReactionPattern;
import ChatPackage.TestWikipedia.*;

// 雑談対話システムの単純なサンプルプログラム
public class ChatSystem {
	// インスタンス変数
	public List<ReactionPattern> patternList; // 反応パターンリスト

	// メインメソッド
	public static void main(String[] args) {
		ChatSystem instance = new ChatSystem();
	}

	// コンストラクタ：ここでは雑談対話システムのメインフローを記述する
	public ChatSystem() {
		String output = "よオ！元気か？？"; // システムの応答文

		setupReactionPattern(); // 反応パターンリストの初期化

		// 最初の挨拶
		System.out.println("ヤンキー：" + output);

		try {
			// 標準入力の準備
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			System.out.print("あなた　：");
			String input;
			while ((input = br.readLine()) != null) { // ユーザによる文の入力を受け取る
				// システムの応答を生成し出力する
				// System.out.println(input);
				output = generateResponse(input, output);
				System.out.println("ヤンキー：" + output);
				System.out.print("あなた　：");
			}
			br.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	// 反応パターンリストの初期化を行う
	public void setupReactionPattern() {
		// データはファイルpattern.txtに1行1パターンの形式で格納されているとする
		// それらのデータを順次読み込み，リストに格納する
		patternList = new ArrayList<ReactionPattern>(); // リストの初期化
		try {
			BufferedReader br = new BufferedReader(new FileReader("pattern.txt"));
			String line;
			while ((line = br.readLine()) != null) {
				String[] split = line.split("\t"); // タブ文字を区切りとして分割する
				ReactionPattern ptn = new ReactionPattern(); // 新しいインスタンスを作る
				ptn.keyword = split[0];
				ptn.response = split[1];
				// System.out.println(ptn.keyword + " : " + ptn.response);
				patternList.add(ptn); // パターンリストに追加する
			}
			br.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	// 応答文を生成する
	public String generateResponse(String input, String previousOutput) {
		// 応答候補リスト
		List<ResponseCandidate> candidateList = new ArrayList<ResponseCandidate>();

		// 複数の方法での応答候補生成を行う
		// それぞれの方法で生成した応答候補はcandidateListに順次追加していく
		generateResponseByPattern(input, candidateList);
		generateResponseByExpansion(previousOutput, candidateList);
		generateOtherResponse(candidateList);
		generateMeCabQuestion(input, candidateList);

		// スコア最大の応答候補を選択する
		String ret = "";
		double maxScore = -1.0;
		for (ResponseCandidate cdd : candidateList) {
			if (cdd.score > maxScore) {
				ret = cdd.response;
				maxScore = cdd.score;
			}
		}

		return ret;
	}

	// 反応パターンを利用した応答候補の生成
	void generateResponseByPattern(String input, List<ResponseCandidate> candidateList) {
		double score = 4.0;
		for (ReactionPattern ptn : patternList) {
			if (input.contains(ptn.keyword)) { // ユーザ入力文がキーワードを含んでいたら
				ResponseCandidate cdd = new ResponseCandidate(); // 応答候補を生成する

				cdd.response = ptn.response;
				cdd.score = score;
				if (input.contains("都の天気") || input.contains("府の天気") || input.contains("道の天気")
						|| input.contains("県の天気")) {
					System.out.println("ヤンキー：" + cdd.response);
					String[] tmp = input.split("の天気");
					WeatherOfPrefecture weather = TestWeather.getForecast(tmp[0]);
					String textToShow = "";
					boolean isRain = false;
					for (int i = 0; i < weather.areas.size(); i++) {
						textToShow += weather.areas.get(i) + "の天気は" + weather.weatherOfArea.get(i);
						if (weather.weatherOfArea.get(i).contains("雨")) {
							isRain = true;
						}
						if (i != weather.areas.size() - 1) {
							textToShow += "\n          ";
						}
					}
					textToShow += "\n          だぜ！";
					if (isRain) {
						textToShow += "\n          傘持っていくの忘れるなよ！風邪ひいちまわないようにナ！";
					}
					cdd.response = textToShow;
					cdd.score = 10.0;
					candidateList.add(cdd);
					break;
				}
				if (input.contains("の意味")) {
					System.out.println("ヤンキー：" + cdd.response);
					String[] tmp = input.split("の意味");
					String word = tmp[0];
					String meaning = TestWikipedia.getWiki(word);
					if (meaning.equals("。")) {
						cdd.response = "わりぃんだけどよ、" + word + "ってのはわかんねぇや。";
						cdd.score = 10.0;
						candidateList.add(cdd);
						break;
					}
					cdd.response = "オレくわしいことはわかんねェけどよぉ、" + word + "ってのは\n          " + meaning
							+ "\n          ってことなんじゃねーか？";
					cdd.score = 10.0;
					candidateList.add(cdd);
					break;
				}
				if (input.contains("雑学")) {
					try {
						BufferedReader br = new BufferedReader(new FileReader("zatugaku.txt"));
						String line = "";
						for (int k = 0; k < (int) (Math.random() * 10); k++) {
							line = br.readLine();
						}
						cdd.response += ("\n          " + line);
						br.close();
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
				candidateList.add(cdd);
			}
		}
	}

	// 1つ前の自分の発言からの話題展開による応答候補の生成
	void generateResponseByExpansion(String previousOutput, List<ResponseCandidate> candidateList) {
		double score = 2.0;

		// この例では単に繰り返すだけ
		ResponseCandidate cdd = new ResponseCandidate();
		cdd.response = "おう、" + previousOutput;
		cdd.score = score;
		candidateList.add(cdd);
	}

	// その他の応答候補の生成
	void generateOtherResponse(List<ResponseCandidate> candidateList) {
		double score = 1.0;

		// この例では単にうなるだけ
		ResponseCandidate cdd = new ResponseCandidate();
		cdd.response = "うーん。";
		cdd.score = score;
		candidateList.add(cdd);
	}

	// 入力を形態素解析したのち単語について聞き返す応答候補の生成
	void generateMeCabQuestion(String input, List<ResponseCandidate> candidateList) {

		double score = 3.0;

		try {
			Process process = Runtime.getRuntime().exec("mecab");
			BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
			PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(process.getOutputStream())));

			pw.println(input);
			pw.flush();

			String ret;
			ArrayList<String> replyList = new ArrayList<String>();// String配列の初期化をする。
			int index = 0;
			while ((ret = br.readLine()) != null) {
				if (ret.equals("EOS")) {
					break;
				}
				String[] split = ret.split("[\t,]");
				String word = split[7];
				String category = split[1];
				if (category.equals("動詞") || category.equals("名詞") || category.equals("形容詞")) {
					replyList.add(word);
				}
			}
			br.close();
			pw.close();
			ResponseCandidate cdd = new ResponseCandidate();
			cdd.score = score;
			if (replyList.size() == 0) {
				cdd.score = 1.0;
				cdd.response = "わりぃんだけどよ、なんて言ってんのかわかンねぇや。";
				candidateList.add(cdd);
				return;
			}
			Random rand = new Random();
			// System.out.println(replyList.size());
			String responseWord = replyList.get(rand.nextInt(replyList.size()));
			cdd.response = "俺あンまりアタマよくねえからよぉ、" + responseWord + "って何だ？？";
			candidateList.add(cdd);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
