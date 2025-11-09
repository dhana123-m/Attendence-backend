package com.attendance;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import java.io.File;

public class FaceRecognizer {
    static { System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME); }

    private static final String CASCADE = "models/haarcascade_frontalface_default.xml";

    public static String recognizeOnce() {
        CascadeClassifier detector = new CascadeClassifier(CASCADE);
        if (detector.empty()) {
            return "❌ Face detector not found (models/haarcascade_frontalface_default.xml)";
        }

        // Try to capture image (for local mode)
        Mat frame = new Mat();
        VideoCapture cap = new VideoCapture(0);
        boolean hasCamera = cap.isOpened();
        if (hasCamera) {
            cap.read(frame);
            cap.release();
        } else {
            // ✅ Render fallback: use test image instead of camera
            System.out.println("⚙️ No camera found — using fallback image: input.jpg");
            frame = Imgcodecs.imread("input.jpg");
        }

        if (frame.empty()) return "❌ No image captured or loaded.";

        Mat gray = new Mat();
        Imgproc.cvtColor(frame, gray, Imgproc.COLOR_BGR2GRAY);

        MatOfRect faces = new MatOfRect();
        detector.detectMultiScale(gray, faces);
        if (faces.empty()) return "❌ No face detected. Please try again.";

        Rect r = faces.toArray()[0];
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

            Mat diff = new Mat();
            Core.absdiff(face, k, diff);
            Scalar s = Core.sumElems(diff);
            double score = s.val[0];
            if (score < bestScore) {
                bestScore = score;
                bestName = f.getName();
            }
        }

        double threshold = 1500000.0;
        if (bestName != null && bestScore < threshold) {
            return bestName.replaceFirst("\\.jpg$", "").replaceFirst("\\.png$", "");
        } else {
            return "Unknown";
        }
    }
}
