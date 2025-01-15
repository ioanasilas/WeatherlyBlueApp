import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * WeatherAppGui class provides a graphical user interface for retrieving
 * and displaying current, past, and future weather data.
 */
public class WeatherAppGui extends JFrame {

    // weather data retrieved from the WeatherApp class
    private JSONObject hourlyWeatherData;
    private String userInput;
    // for comparison
    // why does it work if I initialize them here instead of in method?
    // tabbedPane with every tab
    private final JTabbedPane tabbedPane;

    /**
     * Constructor initializes the WeatherApp GUI and its components.
     */
    public WeatherAppGui() {
        super("Weather App");

        // configure GUI
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(580, 650);
        getContentPane().setBackground(Color.decode("#f4f9ff"));
        setLocationRelativeTo(null);
        setResizable(false);

        // tabs for current, past, and future weather
        tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(Color.decode("#f4f9ff"));

        // add 3 tabs to the GUI
        tabbedPane.addTab("Current Weather", createCurrentWeatherTab());
        tabbedPane.addTab("Past Weather", createPastWeatherTabbedPane());
        tabbedPane.addTab("Future Weather", createFutureWeatherTabbedPane());
        // implement
        tabbedPane.addTab ("Compare Weather", createWeatherComparisonSplitPane());

//        // change listener for animations
//        tabbedPane.addChangeListener(e -> {
//            int targetIndex = tabbedPane.getSelectedIndex();
//        });

        add(tabbedPane);
    }

    /**
     * Creates the tab for displaying current weather data.
     *
     * @return JPanel containing components for the current weather tab.
     */
    private JPanel createCurrentWeatherTab() {
        // currentweather is a Panel on its tab
        JPanel panel = new GradientPanel(Color.decode("#abd8ff"), Color.decode("#ebf6ff"));
        panel.setLayout(null);
        panel.setBackground(Color.decode("#f4f9ff"));

        // search field
        JTextField searchTextField = new JTextField();
        searchTextField.setBounds(85, 13, 351, 45);
        searchTextField.setFont(new Font("Dialog", Font.PLAIN, 24));
        panel.add(searchTextField);

        // search button
        JButton searchButton = new JButton(loadImage("src/assets/search.png"));
        searchButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        searchButton.setBounds(437, 13, 47, 45);
        panel.add(searchButton);

        // retrieve initial data for Timisoara
        hourlyWeatherData = WeatherApp.getHourlyWeatherData("Timisoara");

        String weatherCondition = (String) hourlyWeatherData.get("weather_condition");
        double temperature = (double) hourlyWeatherData.get("temperature");
        long humidity = (long) hourlyWeatherData.get("humidity");
        double windspeed = (double) hourlyWeatherData.get("windspeed");
        double uvIndex = (double) hourlyWeatherData.get("uvIndex");
        long precChance = (long) hourlyWeatherData.get("precProb");

        // City name
        JLabel cityName = new JLabel("Timisoara");
        cityName.setBounds(55, 85, 450, 90);
        cityName.setFont(new Font("Serif", Font.BOLD, 48));
        cityName.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(cityName);

        // Weather image
        JLabel weatherConditionImage = new JLabel();
        weatherConditionImage.setBounds(55, 175, 450, 177);
        weatherConditionImage.setIcon(getWeatherIcon(weatherCondition));
        weatherConditionImage.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(weatherConditionImage);

        // Temperature text
        JLabel temperatureText = new JLabel(temperature + "°C");
        temperatureText.setBounds(55, 345, 450, 60);
        temperatureText.setFont(new Font("Serif", Font.BOLD, 44));
        temperatureText.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(temperatureText);

        // Weather condition description
        JLabel weatherConditionDesc = new JLabel(weatherCondition);
        weatherConditionDesc.setBounds(55, 400, 450, 36);
        weatherConditionDesc.setFont(new Font("Dialog", Font.PLAIN, 32));
        weatherConditionDesc.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(weatherConditionDesc);

        // UV image and text
        JLabel uvImage = new JLabel(loadImage("src/assets/uv.png"));
        uvImage.setBounds(165, 418, 74, 81);
        panel.add(uvImage);

        JLabel uvIndexText = new JLabel("<html><b>UV Index:</b> " + uvIndex + "</html>");
        uvIndexText.setBounds(65, 430, 450, 60);
        uvIndexText.setFont(new Font("Dialog", Font.BOLD, 16));
        uvIndexText.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(uvIndexText);

        // Humidity image and text
        JLabel humidityImage = new JLabel(loadImage("src/assets/humidity.png"));
        humidityImage.setBounds(35, 500, 74, 66);
        panel.add(humidityImage);

        JLabel humidityText = new JLabel("<html><b>Humidity</b> " + humidity + "%</html>");
        humidityText.setBounds(115, 500, 85, 55);
        humidityText.setFont(new Font("Dialog", Font.PLAIN, 16));
        panel.add(humidityText);

        // Prec image and text
        JLabel precImage = new JLabel(loadImage("src/assets/prec.png"));
        precImage.setBounds(196, 495, 85, 66);
        panel.add(precImage);

        JLabel precText = new JLabel("<html><b>Rain chance</b> " + precChance + "%</html>");
        precText.setBounds(275, 500, 100, 55);
        precText.setFont(new Font("Dialog", Font.PLAIN, 16));
        panel.add(precText);

        // Windspeed image and text
        JLabel windspeedImage = new JLabel(loadImage("src/assets/windspeed.png"));
        windspeedImage.setBounds(375, 500, 74, 66);
        panel.add(windspeedImage);

        JLabel windspeedText = new JLabel("<html><b>Wind</b> " + windspeed + "km/h</html>");
        windspeedText.setBounds(455, 500, 100, 55);
        windspeedText.setFont(new Font("Dialog", Font.PLAIN, 16));
        panel.add(windspeedText);

        // search button action
        searchButton.addActionListener(e -> {
            userInput = searchTextField.getText().trim();

            if (userInput.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a location.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // call method from WeatherApp for hourly data
            hourlyWeatherData = WeatherApp.getHourlyWeatherData(userInput);

            if (hourlyWeatherData != null) {
                // if we have user input, we have what to work with
                // we extract from JSON object
                String newWeatherCondition = (String) hourlyWeatherData.get("weather_condition");
                double newTemperature = (double) hourlyWeatherData.get("temperature");
                long newHumidity = (long) hourlyWeatherData.get("humidity");
                double newWindspeed = (double) hourlyWeatherData.get("windspeed");
                double newUvIndex = (double) hourlyWeatherData.get("uvIndex");

                // Update UI
                weatherConditionImage.setIcon(getWeatherIcon(newWeatherCondition));
                cityName.setText(userInput);
                uvIndexText.setText("<html><b>UV Index:</b> " + newUvIndex + "</html>");
                temperatureText.setText(newTemperature + "°C");
                weatherConditionDesc.setText(newWeatherCondition);
                humidityText.setText("<html><b>Humidity</b> " + newHumidity + "%</html>");
                windspeedText.setText("<html><b>Wind</b> " + newWindspeed + "km/h</html>");
            } else {
                JOptionPane.showMessageDialog(this, "Weather data not found.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            // whenever new user input, we always update the future and past weather tabbed pane
            tabbedPane.setComponentAt(2, createFutureWeatherTabbedPane());
            tabbedPane.setComponentAt(1, createFutureWeatherTabbedPane());
        });

        return panel;
    }

    /**
     * Creates the tab for displaying past weather data.
     *
     * @return JTabbedPane containing components for the future weather tab.
     */
    private JTabbedPane createPastWeatherTabbedPane() {
        JTabbedPane pastTabbedPane = new JTabbedPane();
        pastTabbedPane.setBackground(Color.decode("#f4f9ff"));

        JSONArray dailyWeatherData = userInput == null || userInput.isEmpty()
                ? WeatherApp.getPastWeatherData("Timisoara")
                : WeatherApp.getPastWeatherData(userInput);

        if (dailyWeatherData != null) {
            for (int i = 0; i < 7; i++) {
                // get daily data for each of the days
                JSONObject dayData = (JSONObject) dailyWeatherData.get(i);
                // to this main tab, we add 7 other tabs, for each day
                pastTabbedPane.addTab("Day " + (i + 1), createPastWeatherDayPanel(dayData));
            }
        }

        return pastTabbedPane;
    }

    private JPanel fillPanelWithData(JSONObject dayData) {
        JPanel panel = new GradientPanel(Color.decode("#abd8ff"), Color.decode("#ebf6ff"));;
        panel.setLayout(null);
        panel.setBackground(Color.decode("#f4f9ff"));

        // Day title
        JLabel dayTitle = new JLabel(userInput == null || userInput.isEmpty() ? "Timisoara" : userInput);
        dayTitle.setBounds(60, 30, 450, 40);
        dayTitle.setFont(new Font("Serif", Font.BOLD, 36));
        dayTitle.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(dayTitle);

        // Weather image
        JLabel weatherConditionImage = new JLabel();
        weatherConditionImage.setBounds(60, 75, 450, 177);
        weatherConditionImage.setHorizontalAlignment(SwingConstants.CENTER);
        weatherConditionImage.setIcon(getWeatherIcon((String) dayData.get("weather_condition")));
        panel.add(weatherConditionImage);

        // Temperature text
        JLabel temperatureText = new JLabel(dayData.get("temperature_max") + "°C / " + dayData.get("temperature_min") + "°C");
        temperatureText.setBounds(60, 250, 450, 60);
        temperatureText.setFont(new Font("Serif", Font.BOLD, 44));
        temperatureText.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(temperatureText);

        // Weather condition description
        JLabel weatherConditionDesc = new JLabel("Condition: " + dayData.get("weather_condition"));
        weatherConditionDesc.setBounds(60, 320, 450, 36);
        weatherConditionDesc.setFont(new Font("Dialog", Font.PLAIN, 32));
        weatherConditionDesc.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(weatherConditionDesc);

        // UV index
        JLabel uvIndexText = new JLabel("<html><b>Max UV Index:</b> " + dayData.get("uv_index_max") + " </html>");
        uvIndexText.setBounds(62, 387, 450, 36);
        uvIndexText.setFont(new Font("Dialog", Font.PLAIN, 19));
        uvIndexText.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(uvIndexText);

        JLabel uvImage = new JLabel(loadImage("src/assets/uv.png"));
        uvImage.setBounds(127, 365, 74, 81);
        uvImage.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(uvImage);

        // Precipitation hours
        JLabel precipLabel = new JLabel("<html><b>Total Precipitation:</b> " + dayData.get("precipitation_hours") + " hours</html>");
        precipLabel.setBounds(60, 430, 450, 36);
        precipLabel.setFont(new Font("Dialog", Font.PLAIN, 19));
        precipLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(precipLabel);

        JLabel precImage = new JLabel(loadImage("src/assets/prec.png"));
        precImage.setBounds(60, 400, 74, 81);
        precImage.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(precImage);

        return panel;
    }

    private JPanel createPastWeatherDayPanel(JSONObject dayData) {
        return fillPanelWithData(dayData);
    }

    /**
     * Creates the tab for displaying future weather data.
     *
     * @return JTabbedPane containing components for the future weather tab.
     */
    private JTabbedPane createFutureWeatherTabbedPane() {
        JTabbedPane futureTabbedPane = new JTabbedPane();
        futureTabbedPane.setBackground(Color.decode("#f4f9ff"));

        JSONArray dailyWeatherData = userInput == null || userInput.isEmpty()
                ? WeatherApp.getDailyWeatherData("Timisoara")
                : WeatherApp.getDailyWeatherData(userInput);

        if (dailyWeatherData != null) {
            for (int i = 0; i < 7; i++) {
                // get daily data for each of the days
                JSONObject dayData = (JSONObject) dailyWeatherData.get(i);
                // to this main tab, we add 7 other tabs, for each day
                futureTabbedPane.addTab("Day " + (i + 1), createFutureWeatherDayPanel(dayData));
            }
        }

        return futureTabbedPane;
    }

    private JPanel createFutureWeatherDayPanel(JSONObject dayData) {
        return fillPanelWithData(dayData);
    }

    private JPanel createWeatherComparisonSplitPane() {
        JPanel mainPanel = new GradientPanel(Color.decode("#abd8ff"), Color.decode("#ebf6ff")); // Gradient background
        mainPanel.setLayout(new BorderLayout());

        // Create left and right panels
        JPanel leftPanel = new GradientPanel(Color.decode("#abd8ff"), Color.decode("#ebf6ff"));
        JPanel rightPanel = new GradientPanel(Color.decode("#abd8ff"), Color.decode("#ebf6ff"));

        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Add a decorative split line
        JSeparator splitLine = new JSeparator(SwingConstants.VERTICAL);
        splitLine.setForeground(Color.decode("#333333")); // Darker color for visibility
        splitLine.setBackground(Color.decode("#ffffff"));
        splitLine.setPreferredSize(new Dimension(2, 0));

        JSplitPane compareTabbedPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        compareTabbedPane.setDividerSize(2); // Match the separator size
        compareTabbedPane.setDividerLocation(290); // Fixed width for the left panel
        compareTabbedPane.setEnabled(false); // Disable resizing

        // Initialize default cities
        setupCityPanel(leftPanel, "Timisoara");
        setupCityPanel(rightPanel, "Arad");

        // Add everything to the main panel
        mainPanel.add(compareTabbedPane, BorderLayout.CENTER);

        return mainPanel;
    }

    // Helper method to set up a panel for a city
    private void setupCityPanel(JPanel panel, String defaultCity) {
        // Retrieve initial weather data
        JSONObject cityWeatherData = WeatherApp.getHourlyWeatherData(defaultCity);

        String weatherCondition = (String) cityWeatherData.get("weather_condition");
        double temperature = (double) cityWeatherData.get("temperature");

        // Search field
        JTextField searchTextField = new JTextField();
        searchTextField.setMaximumSize(new Dimension(300, 30));
        searchTextField.setFont(new Font("Dialog", Font.PLAIN, 16));
        panel.add(searchTextField);

        // Search button
        JButton searchButton = new JButton(loadImage("src/assets/search.png"));
        searchButton.setFont(new Font("Dialog", Font.BOLD, 12));
        searchButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        searchButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(searchButton);

        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        // City name
        JLabel cityNameLabel = new JLabel(defaultCity);
        cityNameLabel.setFont(new Font("Serif", Font.BOLD, 42));
        cityNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        cityNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(cityNameLabel);

        panel.add(Box.createRigidArea(new Dimension(0, 35)));

        // Weather image
        JLabel weatherConditionImage = new JLabel();
        weatherConditionImage.setIcon(getWeatherIcon(weatherCondition));
        weatherConditionImage.setHorizontalAlignment(SwingConstants.CENTER);
        weatherConditionImage.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(weatherConditionImage);

        // temp go lower
        panel.add(Box.createRigidArea(new Dimension(0, 56)));

        // Temperature text
        JLabel temperatureLabel = new JLabel(temperature + "°C");
        temperatureLabel.setFont(new Font("Serif", Font.BOLD, 44));
        temperatureLabel.setHorizontalAlignment(SwingConstants.CENTER);
        temperatureLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(temperatureLabel);

        // Weather condition description
        JLabel weatherConditionDesc = new JLabel(weatherCondition);
        weatherConditionDesc.setFont(new Font("Dialog", Font.PLAIN, 32));
        weatherConditionDesc.setHorizontalAlignment(SwingConstants.CENTER);
        weatherConditionDesc.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(weatherConditionDesc);

        // Action listener for search button
        searchButton.addActionListener(e -> {
            String userInput = searchTextField.getText().trim();

            if (userInput.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please enter a city name.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Fetch weather data for the entered city
            JSONObject newCityWeatherData = WeatherApp.getHourlyWeatherData(userInput);

            if (newCityWeatherData != null) {
                String newWeatherCondition = (String) newCityWeatherData.get("weather_condition");
                double newTemperature = (double) newCityWeatherData.get("temperature");

                // Update UI with the new data
                cityNameLabel.setText(userInput);
                weatherConditionImage.setIcon(getWeatherIcon(newWeatherCondition));
                temperatureLabel.setText(newTemperature + "°C");
                weatherConditionDesc.setText(newWeatherCondition);

            } else {
                JOptionPane.showMessageDialog(null, "Weather data not found.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }



    /**
     * Returns the appropriate weather icon based on the weather condition.
     *
     * @param weatherCondition The weather condition.
     * @return ImageIcon for the specified weather condition.
     */
    private ImageIcon getWeatherIcon(String weatherCondition) {
        return switch (weatherCondition) {
            case "Clear" -> loadImage("src/assets/clear.png");
            case "Cloudy" -> loadImage("src/assets/cloudy.png");
            case "Rain" -> loadImage("src/assets/rain.png");
            case "Snow" -> loadImage("src/assets/snow.png");
            case "Foggy" -> loadImage("src/assets/foggy.png");
            default -> null;
        };
    }

    /**
     * Loads an image from the specified file path.
     *
     * @param path Path to the image file.
     * @return ImageIcon containing the loaded image.
     */
    private ImageIcon loadImage(String path) {
        try {
            BufferedImage image = ImageIO.read(new File(path));
            return new ImageIcon(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            WeatherAppGui app = new WeatherAppGui();
            app.setVisible(true);
        });
    }
}
