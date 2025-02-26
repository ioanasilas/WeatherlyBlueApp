# WeatherlyBlue 🌦️

Welcome to **WeatherlyBlue**. It's a weather app: it gets the past hour’s temperature of a city. You enter a city name, and it fetches the data for you.

## How It Works

1. **Run `AppLauncher.java`**  
   This is where the magic starts. Just run it, and the app will appear.  

2. **Enter a City Name**  
   On the main screen, type in the name of a city (default is Timișoara). But it should be an accurate name, not a shorthand.

3. **Get the Past Hour’s Temperature**  
   The app uses two APIs:
   - One converts the city name to coordinates.  
   - The other fetches the weather from those coordinates. 

## The Files

- `AppLauncher.java` – Starts the app.  
- `GradientPanel.java` – Makes the background look less boring.  
- `Main.java` – The logic that connects everything.  
- `WeatherApp.java` – Talks to the APIs and gets the temperature.  
- `WeatherAppGui.java` – The interface you interact with.

## Why Use This?

If you’re tired of opening weather websites every time you want to know if you need a jacket, this might help. It’s simple, it works, and it doesn’t ask for much.
