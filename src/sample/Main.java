package sample;

import java.io.*;
import java.net.URL;
import java.util.*;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;

public class Main {

    private static String SiteName = "yug-instrument";
    private static String mainUrl = "https://yug-instrument.ru";
    private static int fromPage = 1;
    private static int toPage = 1;

    public static void main(String[] args) {

       /* System.setProperty("webdriver.chrome.driver", "selenium\\chromedriver.exe");
        WebDriver webDriver = new ChromeDriver();
        webDriver.get("https://yug-instrument.ru/catalog/elektroinstrumenty/pily/pily_montazhnye_otreznye/8869528/");
        webDriver.getPageSource();*/

        try {
            parser(mainUrl, fromPage, toPage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Document getDoc(String url) {

        Excel excel = new Excel();
        excel.createExcel();

        System.out.println("Connect to page...");
        Connection connect = Jsoup.connect(url)
                .userAgent("Mozilla");
        boolean connected=false;
        Document doc=null;
        while(!connected){
            try{
                doc = connect.get();
                connected=true;
            }catch(Exception ex){

            }finally{
                System.out.println("connected: "+connected);
                if(!connected){
                    try{
                        Thread.sleep(1000);
                    }catch(Exception ex){

                    }
                }
            }
        }
        System.out.println("Ok!");

        return doc;

    }

    public static ArrayList<String> getArrayStrOnFile(String pathname) {
        ArrayList<String> Data = new ArrayList<>();
        try {
            File file = new File(pathname);
            FileReader fr = new FileReader(file);
            BufferedReader reader = new BufferedReader(fr);

            String line = reader.readLine();
            while (line != null) {
                Data.add(line);
                line = reader.readLine();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Data;
    }

    public static String [] corrector(String title, String attribute) {
        String metric;
        String [] res = new String[2];

        if(title.contains(",")) {
            metric = title.substring(title.indexOf(",") + 2);
            title = title.substring(0, title.indexOf(","));
            attribute = attribute + " " + metric;
        }
        title = title + ":";

        res[0] = title;
        res[1] = attribute;

        return res;
    }

    private static void Download(String URL, String Name, String URLSave) throws Exception {

        try{
            String fileName = Name;
            String website = URL;

            System.out.println("Downloading File From: " + website);

            java.net.URL url = new URL(website);
            InputStream inputStream = url.openStream();
            OutputStream outputStream = new FileOutputStream(URLSave + "/" + fileName);
            byte[] buffer = new byte[2048];

            int length = 0;

            while ((length = inputStream.read(buffer)) != -1) {
                System.out.println("Buffer Read of length: " + length);
                outputStream.write(buffer, 0, length);
            }

            inputStream.close();
            outputStream.close();

        } catch(Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }

    }

    //запись в txt файл
    private static void writeOnTxt(String data, int noOfLines) {
        File file = new File("BufferedWriter.txt");
        FileWriter fr = null;
        BufferedWriter br = null;
        String dataWithNewLine = data + System.getProperty("line.separator");
        try{
            fr = new FileWriter(file);
            br = new BufferedWriter(fr);
            for(int i = noOfLines; i>0; i--){
                br.write(dataWithNewLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            try {
                br.close();
                fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void parser(String mainUrl, int fromPage, int toPage) throws IOException, NullPointerException {

        Elements select;

        //Date date = new Date();
        new File("parsing_" + SiteName).mkdir();
        ArrayList<String> URLPage = getArrayStrOnFile("pageForParsing.txt"); //тут указываем ссылки на страници для парсинга

        for(int w = 0; w < URLPage.size(); w++) {

            System.setProperty("webdriver.chrome.driver", "selenium\\chromedriver.exe");
            WebDriver webDriver = new ChromeDriver();
            webDriver.get(URLPage.get(w));

            //Document doc = getDoc(URLPage.get(w));
            Document doc = Jsoup.parse(webDriver.getPageSource());
            webDriver.close();
            ArrayList<String> resList;

            //получили ссылки на страници товаров из меню товаров
            ArrayList<String> ListKartochkiInPage = new ArrayList<>();
            for(int KartochkiInPage = 0; KartochkiInPage < doc.select(".item-title").size(); KartochkiInPage++){
                System.out.println(mainUrl + doc.select(".item-title").get(KartochkiInPage).select("a").attr("href"));
                ListKartochkiInPage.add(mainUrl + doc.select(".item-title").get(KartochkiInPage).select("a").attr("href"));
            }

            //проходим по товарам на странице
            for(int nomerTovara = 0; nomerTovara < doc.select(".item-title").size(); nomerTovara++){

                WebDriver webDriverTovar = new ChromeDriver();
                webDriverTovar.get(ListKartochkiInPage.get(nomerTovara));

                //Document docTovara = getDoc(ListKartochkiInPage.get(nomerTovara));
                Document docTovara = getDoc(webDriverTovar.getPageSource());
                webDriverTovar.close();
                Excel excel = new Excel();
                excel.createExcel();
                int Row = 0;

                //регулярным выражением нужно убрать знак "/" из названия!
                //создает подпапки с названием товара
                String nameFolder = docTovara.getElementById("pagetitle").text().replace("/", "-").replace("*", "x").replace(":", " ").replace("\"", "");

                new File("parsing_" + SiteName + "/" + nameFolder).mkdirs();

                //загрузка картинок
                for(int e = 0; e < docTovara.select(".product-detail-gallery__link").select(".popup_link").select(".fancy").size(); e++){
                    String s = docTovara.select(".product-detail-gallery__link").select(".popup_link").select(".fancy").attr("href");
                    System.out.println(s);
                    if(s.contains("/")){
                        try {
                            Download(mainUrl + s, s.substring(s.lastIndexOf("/")).replace("/", ""), "parsing_" + SiteName + "/" + nameFolder);
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                    }
                }
                //System.out.println(doc.select(".product_gallery-previews").select("div").select("a").size());
                /*if(doc.select(".product_gallery-previews").select("div").select("a").size() == 0){
                    if(doc.getElementById("product-image") != null) {
                        String s = doc.getElementById("product-image").attr("src");
                        try {
                            Download(mainUrl + s, s.substring(s.lastIndexOf("/")).replace("/", ""), "parsing_" + SiteName + "/" + nameFolder);
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                    }
                }*/

                //Акция
                resList = getArrayStrOnFile("sale.txt");
                for(int e = 0; e < resList.size(); e++){
                    excel.setCell(Row + e, 0, resList.get(e));
                }
                Row = Row + resList.size() + 1;

                //описание
                System.out.println(doc.select(".tab-pane").select("div").text());
                //writeOnTxt(doc.html(), 1);
                select = doc.select(".tab-content").select(".tab-pane");
                excel.setCell(Row, 0, doc.select(".tab-pane").select("div").text());  //не знаю максимальной длинны строки в ячейке excel. Может быть переполнение!
                Row = Row + 2;

                //характеристики
                /*select = doc.select(".product_features");
                for (int q = 0; q < select.select("tr").size(); q++) {
                    String [] res = corrector(select.select("tr").get(q).select("span").text(), select.select("tr").get(q).select(".product_features-value").text());
                    excel.setCell(q + Row, 0, res[0]);
                    excel.setCell(q + Row, 1, res[1]);
                }*/

                /*Row = Row + select.select("tr").size();
                Row++; //отступ*/

                //подвал
                resList = getArrayStrOnFile("bottom.txt");
                for(int e = 0; e < resList.size(); e++){
                    excel.setCell(Row + e, 0, resList.get(e));
                }
                Row = Row + resList.size();

                //цена, количество и ссылка на товар
                Row = Row + 2;
                select = doc.select(".item-stock");
                excel.setCell(Row, 0, select.get(1).text());
                Row++;
                //select = doc.select(".pr-stock_el");
                excel.setCell(Row, 0, ListKartochkiInPage.get(nomerTovara));
                Row++;
                excel.setCell(Row, 0, URLPage.get(w));

                System.out.println("parsing_" + SiteName + "/" + nameFolder + "/" + nameFolder + ".xlsx");
                excel.Build("parsing_" + SiteName + "/" + nameFolder + "/" + "Описание" + ".xlsx");

            }

        }

        System.out.println(URLPage.size());

    }

}
