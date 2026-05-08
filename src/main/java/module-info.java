module com.src.filmtracker {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.google.gson;
    
    opens com.src.filmtracker.controllers.auth to javafx.fxml;
    opens com.src.filmtracker.services.friends to javafx.fxml;
    opens com.src.filmtracker.controllers.shows to javafx.fxml;
    opens com.src.filmtracker.controllers.users to javafx.fxml;
    
    opens com.src.filmtracker.models.auth to com.google.gson;
    opens com.src.filmtracker.models.common to com.google.gson;
    opens com.src.filmtracker.models.friends to com.google.gson;
    opens com.src.filmtracker.models.library to com.google.gson;
    opens com.src.filmtracker.models.reviews to com.google.gson;
    opens com.src.filmtracker.models.shows to com.google.gson;
    opens com.src.filmtracker.models.users to com.google.gson;
    
    opens com.src.filmtracker to javafx.fxml;
    exports com.src.filmtracker;
}