module com.src.filmtracker {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.google.gson;
    
    opens com.src.filmtracker.models to com.google.gson;
    opens com.src.filmtracker.controllers to javafx.fxml;
    opens com.src.filmtracker to javafx.fxml;
    exports com.src.filmtracker;
}
