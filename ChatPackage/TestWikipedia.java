package ChatPackage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

// Wikipedia API（HTTPS）のテスト

public class TestWikipedia {

    // コンストラクタ
    public TestWikipedia() {
    }

    // Wikipedia APIを利用して単語の説明文を得るメソッド
    public static String getWiki(String word) {
        String response = null; // Webサーバからの応答

        try {
            // Web APIのリクエストURLを構築する
            String url = "https://ja.wikipedia.org/w/api.php?action=query&titles=" +
                    URLEncoder.encode(word, "UTF-8") +
                    "&prop=extracts&exintro&explaintext&redirects=1&format=xml";

            // HTTP接続を確立し，処理要求を送る
            HttpURLConnection conn = (HttpURLConnection) (new URL(url)).openConnection();
            conn.setRequestMethod("GET"); // GETメソッド

            // Webサーバからの応答を受け取る
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            response = "";
            String line;
            while ((line = br.readLine()) != null) {
                response += line;
            }
            br.close();
            conn.disconnect();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // System.out.println(response); // サーバからの応答全体を出力する（確認用）

        // extractタグで囲まれた文字列を取得して返す
        String extract = ""; // 返り値
        if (response != null && response.length() > 0) {
            int offset = 0;
            while ((offset = response.indexOf("<", offset)) != -1) {
                if (response.startsWith("<extract", offset)) {
                    offset = response.indexOf(">", offset) + 1; // extract開始タグの直後の位置
                    int end = response.indexOf("</extract>", offset); // extract終了タグの位置
                    extract += response.substring(offset, end);
                }
                offset++;
            }
        }
        String res = extract.split(("。"))[0] + "。";
        return res;
    }

    // 動作テスト用のmainメソッド
    public static void main(String[] args) {
        String word = "test";
        TestWikipedia instance = new TestWikipedia();

        String extract = instance.getWiki(word);
        System.out.println(word + "の説明文：");
        System.out.println(extract);
    }
}
