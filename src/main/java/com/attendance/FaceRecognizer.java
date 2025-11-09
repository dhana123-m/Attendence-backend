package com.attendance;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

import java.io.File;

/*
  Simple recognizer strategy for the demo:
  - Detect a face using Haar Cascade
  - Capture an image from camera and compare it to files in known_faces
  - For simplicity this demo uses pixel-based comparison of resized grayscale images
    (This is NOT production-grade; for better results use face descriptors + LBPH/Fisher/Deep models)
*/
public class FaceRecognizer {
    static { System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME); }

    private static final String CASCADE = "models/haarcascade_frontalface_default.xml";

    public static String recognizeOnce() {
        CascadeClassifier detector = new CascadeClassifier(CASCADE);
        if (detector.empty()) {
            return "❌ Face detector not found (models/haarcascade_frontalface_default.xml)";
        }

        VideoCapture cap = new VideoCapture(0);
        if (!cap.isOpened()) {
            return "⚠️ Camera not available.";
        }

        Mat frame = new Mat();
        boolean grabbed = cap.read(frame);
        cap.release();
        if (!grabbed || frame.empty()) return "❌ No image captured.";

        Mat gray = new Mat();
        Imgproc.cvtColor(frame, gray, Imgproc.COLOR_BGR2GRAY);

        MatOfRect faces = new MatOfRect();
        detector.detectMultiScale(gray, faces);
        if (faces.empty()) return "❌ No face detected. Please try again.";

        // For simplicity: use the first detected face ROI; resize and compare to known images
        org.opencv.core.Rect r = faces.toArray()[0];
        Mat face = new Mat(gray, r);
        Imgproc.resize(face, face, new Size(100, 100));

        File folder = new File("known_faces");
        File[] imgs = folder.listFiles((d, n) -> n.toLowerCase().endsWith(".jpg") || n.toLowerCase().endsWith(".png"));
        if (imgs == null || imgs.length == 0) return "❌ No registered faces. Please register first.";

        double bestScore = Double.MAX_VALUE;
        String bestName = null;

        for (File f : imgs) {
            Mat k = Imgcodecs.imread(f.getAbsolutePath(), Imgcodecs.IMREAD_GRAYSCALE);
            if (k.empty()) continue;
            Imgproc.resize(k, k, new Size(100, 100));

            // simple pixel difference
            Mat diff = new Mat();
            Core.absdiff(face, k, diff);
            Scalar s = Core.sumElems(diff);
            double score = s.val[0];
            if (score < bestScore) {
                bestScore = score;
                bestName = f.getName();
            }
        }

        // threshold is heuristic - adjust as needed
        double threshold = 1500000.0;
        if (bestName != null && bestScore < threshold) {
            return bestName.replaceFirst("\\.jpg$", "").replaceFirst("\\.png$", ""); // matched name
        } else {
            return "Unknown";
        }
    }
}
