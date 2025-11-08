import org.opencv.core.Core;
import org.opencv.videoio.VideoCapture;

public class CameraTest {
    static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    public static void main(String[] args) {
        VideoCapture cap = new VideoCapture(0);
        if (cap.isOpened()) {
            System.out.println("✅ Camera opened successfully");
        } else {
            System.out.println("❌ Camera not available");
        }
        cap.release();
    }
}
