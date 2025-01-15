import javax.swing.*;

public class AppLauncher {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run ()
            {
// //                dispay weather app gui
                new WeatherAppGui().setVisible(true);

//                System.out.println(WeatherApp.getLocationData("Cluj"))
                System.out.println(WeatherApp.getCurrentTime());
            }
        });
    }
}