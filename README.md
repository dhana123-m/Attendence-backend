# Attendance Automation - Windows 11 (OpenCV demo)

This project demonstrates a conceptual + webcam-enabled attendance automation system using Java and OpenCV.
It is configured for **Windows 11**. The repo includes Java source code, a simple frontend, and helper files.

---
## What is included
- `frontend/` : index.html + style.css (Register & Mark buttons)
- `backend/src/com/attendance/` : Java source files
  - `MainServer.java` (starter, see instructions)
  - `AttendanceRecord.java`
  - `AttendanceService.java`
  - `FaceRegister.java`
  - `FaceRecognizer.java`
- `backend/lib/opencv-460.jar` : **placeholder JAR included** (you should replace with the official one if you prefer)
- `backend/models/haarcascade_frontalface_default.xml` : Haar cascade **(placeholder - please download real file if missing)**
- `backend/known_faces/` : folder where captured face images will be stored
- `backend/attendance.txt` : will contain attendance logs (created on first run)

---
## Important notes (Windows 11, OpenCV native libs)
- The project **needs the OpenCV native DLL** (e.g. `opencv_java460.dll`) to be present and accessible via `-Djava.library.path`.
- The `backend/lib/opencv-460.jar` included here is a placeholder. For a working setup:
  1. Download OpenCV for Windows (4.6.x or 4.x) from https://opencv.org/releases/ and extract.
  2. Copy the `opencv-460.jar` from the `build/java` folder into `backend/lib/` (or update classpath to point to it).
  3. Copy the matching native DLL (e.g. `opencv_java460.dll`) into `backend/lib/` or a folder you will reference.
  4. Make sure `models/haarcascade_frontalface_default.xml` exists (download from OpenCV repo) and is placed into `backend/models/`

---
## How to run (recommended - VS Code)
1. Open the project folder in VS Code.
2. Install the **Extension Pack for Java**.
3. Build the Java classes from the `backend/` folder. Example commands (PowerShell/CMD):

```powershell
cd backend
set CLASSPATH=lib\opencv-460.jar;out
javac -d out -cp lib\opencv-460.jar src\com\attendance\*.java
```

4. Run server supplying the native lib path (adjust paths to your extracted OpenCV):
```powershell
java -Djava.library.path=lib -cp out;lib\opencv-460.jar com.attendance.MainServer
```

5. Open `frontend/index.html` with Live Server or a browser. Press **Register Face** to capture and save an image named `{name}.jpg` in `backend/known_faces`.
   Press **Mark Attendance** to attempt recognition and auto-mark attendance in `attendance.txt`.

---
## Troubleshooting
- If camera fails to open, ensure no other app is using it and OpenCV native DLL matches the JAR version.
- If Haar cascade not found, download `haarcascade_frontalface_default.xml` and place into `backend/models/`.

---
## License & credits
This project skeleton is for educational purposes and demonstrates concepts only. The simplistic pixel-compare recognizer used here is **not** production-ready.
