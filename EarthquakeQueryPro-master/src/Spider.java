import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Spider {
    private DataModel dataModel = new DataModel();
    private String maxDate = null;
    private Document doc = null;
    private String url = null;
    Properties prop = new Properties();
    private final String _url = "https://www.emsc-csem.org/Earthquake/?view=1";

    Spider() {
        try {
            prop.load(new FileInputStream("res/cnf/main.cnf"));
            url = prop.getProperty("web_url");
            maxDate = dataModel.getMaxDate();
        } catch (IOException e) {
            url = _url;
            e.printStackTrace();
        }

    }

    private boolean checkLongitude(float longi) {
        if (longi > -180 && longi < 180)
            return true;
        else
            return false;
    }

    private boolean checkLatitude(float lati) throws IOException {
        if (lati > -90 && lati < 90)
            return true;
        else
            return false;
    }

    /**
     * A method used to grab data from website
     * keep grabbing until encounter max date in the original database
     * the website url can be modified in the database.cnf
     * @return If success, return "success"; If fail, return "fail"
     */
    public String updateData() {
        int id;
        String date;
        float latitude;
        float longitude;
        int depth;
        float magnitude;
        String region;
        int area_id;
        for(int i=1;i<90;i++) {
            String real_url = url + String.valueOf(i);
            try {
                doc = Jsoup.connect(real_url).get();
                Elements tables = doc.select("div#content > table");
                Element table = tables.get(1);
                Elements rows = table.select("tbody tr");
                for (Element r : rows) {
                    Elements cells = r.select("td");
                    if (cells.size() > 3) {
                        id = Integer.parseInt(r.attr("id"));
                        date = cells.get(3).select("b > a").text().trim();
                        if (date.compareTo(maxDate)<=0) {
                            return ProjErr.updateSUCCESS;
                        }
                        latitude = Float.parseFloat(cells.get(4).text().trim());
                        if (cells.get(5).text().contains("S"))
                            latitude = -latitude;
                        longitude = Float.parseFloat(cells.get(6).text().trim());
                        if (cells.get(7).text().contains("W"))
                            longitude = -longitude;
                        depth = Integer.parseInt(cells.get(8).text().trim());
                        magnitude = Float.parseFloat(cells.get(10).text().trim());
                        region = cells.get(11).text().trim();
                        area_id = Integer.parseInt(cells.get(11).attr("id").replace("reg", "").trim());
                        dataModel.insertQuake(id, date, latitude, longitude, depth, magnitude, region, area_id);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                return ProjErr.updateFAIL;
            }
        }
        System.out.println(dataModel.getMaxDate());
        return ProjErr.updateSUCCESS;
    }

}
