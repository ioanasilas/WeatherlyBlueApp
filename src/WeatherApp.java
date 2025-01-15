import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLOutput;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

/**
 The user inputs a location name (e.g. "Arad").
 The app sends a request to the geolocation API to retrieve the latitude and longitude of location.
 With these coordinates, the app builds a request URL for the weather API.
 The app fetches the weather data for the given location and processes it (current temp, uv index etc).
 The processed data is returned as a JSON object for display in the GUI.
 */

public class WeatherApp {
    // try daily weather thing, see if it works
    public static void main(String[] args) {
        String locationName = "Arad";

        // fetch daily weather data for the given location
        // JSONArray is between [ ]
        JSONArray dailyWeatherData = getDailyWeatherData(locationName);

        // Check if data is fetched successfully
        if (dailyWeatherData != null) {
            System.out.println(dailyWeatherData.toJSONString());
        } else {
            System.out.println("Error: Daily weather data could not be fetched.");
        }

        // JSONObject is between { } with :
        JSONObject hourlyWeatherData = getHourlyWeatherData(locationName);
        if (dailyWeatherData != null) {
            System.out.println(hourlyWeatherData);
        }
        else
        {
            System.out.println("Error: Hourly weather data could not be fetched.");
        }
    }
    public static JSONObject getHourlyWeatherData(String locationName)
    {
        // get location coordinates using geolocation API
        JSONArray locationData = getLocationData(locationName);

        // extract latitude and longitude data
        // when introducing location, we might get multiple results, but we take the first one
        JSONObject location = (JSONObject) locationData.get(0);
        double latitude = (Double) location.get("latitude");
        double longitude = (Double) location.get("longitude");

        // build API req url with location coordinates for weather api
        // we get hourly and daily stuff
        String urlString = "https://api.open-meteo.com/v1/forecast?" +
        "latitude=" + latitude + "&longitude=" + longitude +
                "&hourly=temperature_2m,relative_humidity_2m,apparent_temperature,precipitation_probability,precipitation,weather_code,cloud_cover,visibility,wind_speed_10m,uv_index";

        try
        {
            // call api ang get response
            HttpURLConnection conn = fetchApiResponse(urlString);

            // check for response status
            if (conn.getResponseCode() != 200)
            {
                System.out.println("Error: Could not connect to API");
                return null;
            }
            // store resulting json
            StringBuilder resultJson = new StringBuilder();
            Scanner scanner = new Scanner(conn.getInputStream());
            while(scanner.hasNext())
            {
                // read and store into string builder
                resultJson.append(scanner.nextLine());
            }
            scanner.close();
            conn.disconnect();
            // parse data
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(String.valueOf(resultJson));

            // get hourly data
            JSONObject hourly = (JSONObject) jsonObject.get("hourly");

            // get current hour data
            // so we need to get the index of our current hour
            JSONArray time = (JSONArray) hourly.get("time");
            int index = findIndexOfCurrentTime(time);
            // name of the thing we are getting
            JSONArray temperatureData = (JSONArray) hourly.get("temperature_2m");

            // to get temp data of current hour we just pass index of current hour
            double temperature = (double) temperatureData.get(index);

            // get weather code
            JSONArray weathercode = (JSONArray) hourly.get("weather_code");
            // api we use has own decoder that helps us convert weather code to more readable smth
            String weatherCondition = convertWeatherCode((long) weathercode.get(index));

            // get humidity
            JSONArray relativeHumidity = (JSONArray) hourly.get("relative_humidity_2m");
            long humidity = (long) relativeHumidity.get(index);

            // windspeed
            JSONArray windspeedData = (JSONArray) hourly.get("wind_speed_10m");
            double windspeed = (double) windspeedData.get(index);

            // uv
            JSONArray uvIndexData = (JSONArray) hourly.get("uv_index");
            double uvIndex = (double) uvIndexData.get(index);

            // precipitation probability
            JSONArray precProb = (JSONArray) hourly.get("precipitation_probability");
            long precipProb = (long) precProb.get(index);

            // build json data object that we are going to access in frontend
            JSONObject hourlyWeatherData = new JSONObject();
            hourlyWeatherData.put("temperature", temperature);
            hourlyWeatherData.put("weather_condition", weatherCondition);
            hourlyWeatherData.put("humidity", humidity);
            hourlyWeatherData.put("windspeed", windspeed);
            // new
            hourlyWeatherData.put("uvIndex", uvIndex);
            hourlyWeatherData.put("precProb", precipProb);

            return hourlyWeatherData;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONArray getDailyWeatherData(String locationName) {
        JSONArray locationData = getLocationData(locationName);

        // ong lat
        JSONObject location = (JSONObject) locationData.get(0);
        double latitude = (Double) location.get("latitude");
        double longitude = (Double) location.get("longitude");

        // build second api request for daily stuff
        String urlString = "https://api.open-meteo.com/v1/forecast?" +
                "latitude=" + latitude + "&longitude=" + longitude + "&daily=weather_code,temperature_2m_max,temperature_2m_min,sunrise,sunset,uv_index_max,precipitation_hours,precipitation_probability_max&timezone=auto";

        try {
            // call api and get response
            HttpURLConnection conn = fetchApiResponse(urlString);

            // check for response status
            if (conn.getResponseCode() != 200) {
                System.out.println("Error: Could not connect to API");
                return null;
            }
            // store resulting JSON
            StringBuilder resultJson = new StringBuilder();
            Scanner scanner = new Scanner(conn.getInputStream());
            while (scanner.hasNext()) {
                resultJson.append(scanner.nextLine());
            }
            scanner.close();
            conn.disconnect();

            // Parse data
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(resultJson.toString());

            // get daily data
            JSONObject daily = (JSONObject) jsonObject.get("daily");

            // Extract arrays for daily data
            JSONArray weatherCodes = (JSONArray) daily.get("weather_code");
            JSONArray maxTemperatures = (JSONArray) daily.get("temperature_2m_max");
            JSONArray minTemperatures = (JSONArray) daily.get("temperature_2m_min");
            JSONArray sunrises = (JSONArray) daily.get("sunrise");
            JSONArray sunsets = (JSONArray) daily.get("sunset");
            JSONArray uvIndexMaxes = (JSONArray) daily.get("uv_index_max");
            JSONArray precipitationHours = (JSONArray) daily.get("precipitation_hours");
            JSONArray precipitationProbabilities = (JSONArray) daily.get("precipitation_probability_max");

            // create array to store the first three days of data
            JSONArray dailyWeatherData = new JSONArray();

            for (int i = 0; i < 7; i++) { // first three days
                JSONObject dayData = new JSONObject();
                dayData.put("weather_condition", convertWeatherCode((long) weatherCodes.get(i)));
                dayData.put("temperature_max", maxTemperatures.get(i));
                dayData.put("temperature_min", minTemperatures.get(i));
                dayData.put("sunrise", sunrises.get(i));
                dayData.put("sunset", sunsets.get(i));
                dayData.put("uv_index_max", uvIndexMaxes.get(i));
                dayData.put("precipitation_hours", precipitationHours.get(i));
                dayData.put("precipitation_probability_max", precipitationProbabilities.get(i));

                dailyWeatherData.add(dayData);
            }

            return dailyWeatherData;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONArray getPastWeatherData(String locationName) {
        JSONArray locationData = getLocationData(locationName);

        // long lat
        JSONObject location = (JSONObject) locationData.get(0);
        double latitude = (Double) location.get("latitude");
        double longitude = (Double) location.get("longitude");

        // build second api request for daily stuff
        String urlString = "https://api.open-meteo.com/v1/forecast?" +
                "latitude=" + latitude + "&longitude=" + longitude + "&daily=weather_code,temperature_2m_max,temperature_2m_min,sunrise,sunset,uv_index_max,precipitation_hours,precipitation_probability_max&timezone=auto&past_days=7";

        try {
            // call api and get response
            HttpURLConnection conn = fetchApiResponse(urlString);

            // check for response status
            if (conn.getResponseCode() != 200) {
                System.out.println("Error: Could not connect to API");
                return null;
            }
            // store resulting JSON
            StringBuilder resultJson = new StringBuilder();
            Scanner scanner = new Scanner(conn.getInputStream());
            while (scanner.hasNext()) {
                resultJson.append(scanner.nextLine());
            }
            scanner.close();
            conn.disconnect();

            // Parse data
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(resultJson.toString());

            // get daily data
            JSONObject daily = (JSONObject) jsonObject.get("daily");

            // Extract arrays for daily data
            JSONArray weatherCodes = (JSONArray) daily.get("weather_code");
            JSONArray maxTemperatures = (JSONArray) daily.get("temperature_2m_max");
            JSONArray minTemperatures = (JSONArray) daily.get("temperature_2m_min");
            JSONArray sunrises = (JSONArray) daily.get("sunrise");
            JSONArray sunsets = (JSONArray) daily.get("sunset");
            JSONArray uvIndexMaxes = (JSONArray) daily.get("uv_index_max");
            JSONArray precipitationHours = (JSONArray) daily.get("precipitation_hours");
            JSONArray precipitationProbabilities = (JSONArray) daily.get("precipitation_probability_max");

            // create array to store the first three days of data
            JSONArray dailyWeatherData = new JSONArray();

            for (int i = 0; i < 7; i++) { // first three days
                JSONObject dayData = new JSONObject();
                dayData.put("weather_condition", convertWeatherCode((long) weatherCodes.get(i)));
                dayData.put("temperature_max", maxTemperatures.get(i));
                dayData.put("temperature_min", minTemperatures.get(i));
                dayData.put("sunrise", sunrises.get(i));
                dayData.put("sunset", sunsets.get(i));
                dayData.put("uv_index_max", uvIndexMaxes.get(i));
                dayData.put("precipitation_hours", precipitationHours.get(i));
                dayData.put("precipitation_probability_max", precipitationProbabilities.get(i));

                dailyWeatherData.add(dayData);
            }

            return dailyWeatherData;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    // retrieves geographic coordinates for given location name
    public static JSONArray getLocationData(String locationName) {
        // replace any whitespace in location name to + to adhere to API's request format
        locationName = locationName.replaceAll(" ", "+");

        // build API url with location parameter
        String urlString = "https://geocoding-api.open-meteo.com/v1/search?name=" + locationName + "&count=10&language=en&format=json";

        HttpURLConnection conn = null;
        try {
            // attempt to fetch api response
            conn = fetchApiResponse(urlString);

            // check if connection is valid and response code is 200 (success)
            if (conn == null || conn.getResponseCode() != 200) {
                System.out.println("error: could not connect to api");
                return null;
            }

            // store API results
            StringBuilder resultJson = new StringBuilder();
            // use to read JSON data from API call
            try (Scanner scanner = new Scanner(conn.getInputStream())) {
                // read and store resulting json data into our string builder
                while (scanner.hasNext()) {
                    resultJson.append(scanner.nextLine());
                }
            }

            // parse json string into json object to access data more properly
            // to be able to get("json_property")
            JSONParser parser = new JSONParser();
            JSONObject resultsJsonObj = (JSONObject) parser.parse(resultJson.toString());

            // get list of location data the API generated from location name
            JSONArray locationData = (JSONArray) resultsJsonObj.get("results");
            if (locationData == null) {
                System.out.println("error: no results found for the given location");
            }
            return locationData;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // close url connection
            if (conn != null) {
                conn.disconnect();
            }
        }
        // could not find location
        return null;
    }


    private static HttpURLConnection fetchApiResponse(String urlString)
    {
        try
        {
            // attempt to create conn
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // set method to get
            conn.setRequestMethod("GET");

            // connect to API
            conn.connect();
            return conn;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        // if we could not make connection
        return null;
    }

    private static int findIndexOfCurrentTime(JSONArray timeList)
    {
        String currentTime = getCurrentTime();

        // iterate through time list and find matching one
        for(int i = 0; i < timeList.size(); i++)
        {
            String time = (String) timeList.get(i);
            if (time.equalsIgnoreCase(currentTime))
            {
                // return index
                return i;
            }
        }
        return 0;

    }

    protected static String getCurrentTime()
    {
        // get curr date time
        LocalDateTime currentDateTime = LocalDateTime.now();

        // format data as in API eg 2023-09-02T00:00
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH':00'");

        // format and print curr date and time
        String formattedDateTime = currentDateTime.format(formatter);
        return formattedDateTime;
    }

    // fix this is not acc anymore
    // convert weather code to something more readable
    public static String convertWeatherCode(long weathercode)
    {
        String weatherCondition = "";
        if (weathercode == 0)
        {
            weatherCondition = "Clear";
        }
        else if (weathercode <= 3 && weathercode > 0)
        {
            weatherCondition = "Cloudy";
        }
        else if (weathercode >= 41 && weathercode < 51)
        {
            weatherCondition = "Foggy";
        }
        else if ((weathercode >= 51 && weathercode <= 67) || (weathercode >= 80 && weathercode <= 99))
        {
            weatherCondition = "Rain";
        }
        else if (weathercode >= 71 && weathercode <= 77)
        {
            weatherCondition = "Snow";
        }
        return weatherCondition;

    }
}

