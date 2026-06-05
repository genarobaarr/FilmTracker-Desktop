package com.src.filmtracker.utils;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public final class ImageHelper {
    
    private static Image fallbackErrorImage;

    private ImageHelper() {
        // Clase utilitaria estática
    }

    public static void cargarImagenConRespaldo(String url, ImageView imageView) {
        try {
            Image img = new Image(url, true);
            img.errorProperty().addListener((obs, oldVal, isError) -> {
                if (Boolean.TRUE.equals(isError)) {
                    Platform.runLater(() -> ponerImagenError(imageView));
                }
            });
            imageView.setImage(img);
        } catch (Exception e) {
            ponerImagenError(imageView);
        }
    }

    public static void ponerImagenError(ImageView imageView) {
        if (fallbackErrorImage == null) {
            try {
                String errorPath = ImageHelper.class.getResource("/com/src/filmtracker/images/error.png").toExternalForm();
                fallbackErrorImage = new Image(errorPath, true);
            } catch (Exception ex) {
                // Falla silenciosa intencional permitida para imágenes estáticas
            }
        }
        
        if (fallbackErrorImage != null) {
            imageView.setImage(fallbackErrorImage);
        }
    }
}