package com.attendance;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;

public class FaceRegister {
    static { System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME); }

    // Captures a single frame from the default camera and saves it as known_faces/{name}.jpg
    public static String registerFace(String name) {
        VideoCapture cap = new VideoCapture(0);
        if (!cap.isOpened()) {
            return "⚠️ Camera not available. Make sure it's connected and not used by another app.";
        }

        Mat frame = new Mat();
        boolean grabbed = cap.read(frame);
        cap.release();
        if (!grabbed || frame.empty()) {
            return "❌ Could not capture image from camera.";
        }

        String filename = "known_faces/" + name + ".jpg";
        boolean ok = Imgcodecs.imwrite(filename, frame);
        if (ok) return "✅ Face registered for " + name;
        else return "❌ Failed to write image to disk.";
    }
}
