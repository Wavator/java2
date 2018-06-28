import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;
import java.util.TreeSet;

public class DataModel {
    private static ArrayList<Quake> quakes = new ArrayList<>();
    private static TreeSet<String> regions = new TreeSet<>();

    private static String maxDate = "";
    private static int maxID = 0;

    private PreparedStatement quakeStmt1 = null;
    private PreparedStatement quakeStmt2 = null;
    private PreparedStatement regionStmt = null;
    private PreparedStatement getMaxDateStmt = null;
    private PreparedStatement getMaxIDStmt = null;
    private PreparedStatement insertStmt = null;

    private Connection con = null;
    private Properties prop = new Properties();

    DataModel(){
        try {
            Class.forName("org.sqlite.JDBC");
            prop.load(new FileInputStream("res/cnf/main.cnf"));
            String name = prop.getProperty("location");
            con = DriverManager.getConnection("jdbc:sqlite:"+name);
            con.setAutoCommit(true);
            regionStmt = con.prepareStatement("SELECT region FROM quakes");
            quakeStmt1 = con.prepareStatement("SELECT * FROM quakes WHERE substr(UTC_date,1,10)>=?"
                +" AND substr(UTC_date,1,10)<=?"
                +" AND magnitude>=?"
                +" AND region=?");
            quakeStmt2 = con.prepareStatement("SELECT * FROM quakes WHERE substr(UTC_date,1,10)>=?"
                +" AND substr(UTC_date,1,10)<=?"
                +" AND magnitude>=?");
            getMaxDateStmt = con.prepareStatement("SELECT max(UTC_date) FROM quakes");
            getMaxIDStmt = con.prepareStatement("SELECT max(id) FROM quakes");
            insertStmt = con.prepareStatement("INSERT INTO quakes(id,UTC_date,latitude,longitude,depth,magnitude,region,area_id)"
                                                +" VALUES(?,?,?,?,?,?,?,?)");
        } catch (ClassNotFoundException e) {
            System.err.println("Can not find the driver");
            System.exit(1);
        } catch (FileNotFoundException e) {
            System.err.println("Can not find the property file");
        } catch (IOException e) {
            System.err.println("IO error happens");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void readQuakes(String fromDay, String toDay, float magnitude, String region){
        ResultSet resultSet = null;
        try {
            if(!region.equals(Quake.WORLDWIDE)) {
                quakeStmt1.setString(1, fromDay);
                quakeStmt1.setString(2, toDay);
                quakeStmt1.setString(3, String.valueOf(magnitude));
                quakeStmt1.setString(4, region);
                resultSet = quakeStmt1.executeQuery();
                while (resultSet.next()) {
                    quakes.add(new Quake(
                        resultSet.getInt("id"),
                        resultSet.getString("UTC_date"),
                        resultSet.getFloat("latitude"),
                        resultSet.getFloat("longitude"),
                        resultSet.getInt("depth"),
                        resultSet.getFloat("magnitude"),
                        resultSet.getString("region")
                    ));
                }
            }
            else{
                quakeStmt2.setString(1, fromDay);
                quakeStmt2.setString(2, toDay);
                quakeStmt2.setString(3, String.valueOf(magnitude));
                resultSet = quakeStmt2.executeQuery();
                while (resultSet.next()) {
                    quakes.add(new Quake(
                        resultSet.getInt("id"),
                        resultSet.getString("UTC_date"),
                        resultSet.getFloat("latitude"),
                        resultSet.getFloat("longitude"),
                        resultSet.getInt("depth"),
                        resultSet.getFloat("magnitude"),
                        resultSet.getString("region")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL ERROR1");
        }finally {
            try{
                resultSet.close();
            } catch (SQLException e) {
                System.err.println("close error");
            }
        }
    }

    private void loadRegions(){
        ResultSet resultSet = null;
        regions.add(Quake.WORLDWIDE);
        try{
            resultSet = regionStmt.executeQuery();
            while(resultSet.next()){
                regions.add(resultSet.getString(1));
            }

        } catch (SQLException e) {
            System.err.println("SQL ERROR2");
        }finally {
            try {
                resultSet.close();
            }catch (SQLException e){
                System.err.println("CLOSE ERROR");
            }
        }
    }

    private void loadMaxDate(){
        ResultSet resultSet = null;
        try{
            resultSet = getMaxDateStmt.executeQuery();
            maxDate = resultSet.getString(1).trim();
        } catch (SQLException e) {
            System.err.println("SQL ERROR");
        }
    }

    private void loadMaxID(){
        ResultSet resultSet = null;
        try{
            resultSet = getMaxIDStmt.executeQuery();
            maxID = Integer.parseInt(resultSet.getString(1));
        } catch (SQLException e) {
            System.err.println("SQL ERROR");
        }
    }

    /**
     *
     * @param fromDay the start day
     * @param toDay the end day
     * @param magnitude earthquake magnitude
     * @param region region
     * @return quake list     */
    public ArrayList<Quake> getQuakes(String fromDay, String toDay, float magnitude, String region){
        quakes.clear();
        readQuakes(fromDay,toDay,magnitude,region);
        return quakes;
    }

    /**
     *A method used to get region list
     * @return a list contains all regions
     */
    public TreeSet<String> getRegions(){
        if(regions.size()==0) loadRegions();
        return regions;
    }

    /**
     * This method can get the latest Date in the database
     * @return maxDate
     */
    public String getMaxDate(){
        loadMaxDate();
        return maxDate;
    }


    private boolean checkUnique(float longitude,float latitude, String date) throws SQLException {
        PreparedStatement checkStmt = con.prepareStatement("SELECT * FROM quakes WHERE longitude = ? AND latitude = ? AND UTC_date = ?");
        checkStmt.setString(1,String.valueOf(longitude));
        checkStmt.setString(2,String.valueOf(latitude));
        checkStmt.setString(3,date);
        ResultSet res = checkStmt.executeQuery();
        if(res.next()){
            //System.out.println("gg");
            return false;
        }else{
            return true;
        }

    }

    private boolean checkID(int id) throws SQLException{
        PreparedStatement checkStmt = con.prepareStatement("SELECT * FROM quakes WHERE id = ?");
        checkStmt.setString(1,String.valueOf(id));
        ResultSet res = checkStmt.executeQuery();
        if(res.next()){
            return false;
        }else{
            return true;
        }
    }

    /**
     * A method used to insert quake into database (robust check is realized)
     * @param id: An int ID of earthquake
     * @param date: An String contains the date it took place
     * @param latitude: A Latitude class about the latitude
     * @param longitude: A Longitude class about the longitude
     * @param depth: The depth of the earthquake
     * @param magnitude: The magnitude
     * @param region: A String of region
     * @param area_id: The ID of area in the database
     */
    public void insertQuake(int id, String date, float latitude, float longitude, int depth, float magnitude, String region,int area_id){
        try{
            if(checkUnique(longitude,latitude,date)&&checkID(id)) {
                insertStmt.setString(1, String.valueOf(id));
                insertStmt.setString(2, String.valueOf(date));
                insertStmt.setString(3, String.valueOf(latitude));
                insertStmt.setString(4, String.valueOf(longitude));
                insertStmt.setString(5, String.valueOf(depth));
                insertStmt.setString(6, String.valueOf(magnitude));
                insertStmt.setString(7, String.valueOf(region));
                insertStmt.setString(8, String.valueOf(area_id));
                insertStmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
