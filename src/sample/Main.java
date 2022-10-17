package sample;

import java.io.*;
import java.net.URL;
import java.util.*;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Main {

    private static String SiteName = "https://yug-instrument.ru";
    private static String mainUrl = "https://simferopol.resantagroup.ru";
    private static int fromPage = 1;
    private static int toPage = 1;

    public static void main(String[] args) {
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
        File file = new File("/Users/prologistic/BufferedWriter.txt");
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

            Document doc = getDoc(URLPage.get(w));
            Excel excel = new Excel();
            excel.createExcel();
            int Row = 0;
            ArrayList<String> resList;

            //получили ссылки на страници товаров из меню товаров
            ArrayList<String> ListKartochkiInPage = new ArrayList<>();
            for(int KartochkiInPage = 0; KartochkiInPage < doc.select(".item-title").size(); KartochkiInPage++){
                ListKartochkiInPage.add(doc.select(".item-title").get(KartochkiInPage).select("a").attr("href"));
            }

            //проходим по товарам на странице
            for(int nomerTovara = 0; nomerTovara < doc.select(".item-title").size(); nomerTovara++){
                Document docTovara = getDoc(ListKartochkiInPage.get(nomerTovara));

                //регулярным выражением нужно убрать знак "/" из названия!
                //создает подпапки с названием товара
                String nameFolder = docTovara.getElementById("pagetitle").text().replace("/", "-").replace("*", "x").replace(":", " ").replace("\"", "");

                new File("parsing_" + SiteName + "/" + nameFolder).mkdirs();

                //загрузка картинок
                for(int e = 0; e < docTovara.select(".product-detail-gallery__picture").size(); e++){
                    String s = docTovara.select(".product-detail-gallery__picture").attr("src");
                    if(s.contains("/")){
                        try {
                            Download(mainUrl + s, s.substring(s.lastIndexOf("/")).replace("/", ""), "parsing_" + SiteName + "/" + nameFolder);
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                    }
                }
                System.out.println(doc.select(".product_gallery-previews").select("div").select("a").size());
                if(doc.select(".product_gallery-previews").select("div").select("a").size() == 0){
                    if(doc.getElementById("product-image") != null) {
                        String s = doc.getElementById("product-image").attr("src");
                        try {
                            Download(mainUrl + s, s.substring(s.lastIndexOf("/")).replace("/", ""), "parsing_" + SiteName + "/" + nameFolder);
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                    }
                }

                //Акция
                resList = getArrayStrOnFile("sale.txt");
                for(int e = 0; e < resList.size(); e++){
                    excel.setCell(Row + e, 0, resList.get(e));
                }
                Row = Row + resList.size() + 1;

                //описание
                select = doc.select(".tab-contents");
                excel.setCell(Row, 0, select.select(".product_description").select("p").text());  //не знаю максимальной длинны строки в ячейке excel. Может быть переполнение!
                Row = Row + 2;

                //характеристики
                select = doc.select(".product_features");
                for (int q = 0; q < select.select("tr").size(); q++) {
                    String [] res = corrector(select.select("tr").get(q).select("span").text(), select.select("tr").get(q).select(".product_features-value").text());
                    excel.setCell(q + Row, 0, res[0]);
                    excel.setCell(q + Row, 1, res[1]);
                }

                Row = Row + select.select("tr").size();
                Row++; //отступ

                //подвал
                resList = getArrayStrOnFile("bottom.txt");
                for(int e = 0; e < resList.size(); e++){
                    excel.setCell(Row + e, 0, resList.get(e));
                }
                Row = Row + resList.size();

                //цена, количество и ссылка на товар
                Row = Row + 2;
                select = doc.select(".product__price");
                excel.setCell(Row, 0, select.attr("data-price"));
                Row++;
                select = doc.select(".pr-stock_el");
                excel.setCell(Row, 0, select.get(0).text());
                Row++;
                excel.setCell(Row, 0, URLPage.get(w));

                excel.Build("parsing_" + SiteName + "/" + nameFolder + "/" + nameFolder + ".xlsx");

            }

        }

        System.out.println(URLPage.size());

    }

}
