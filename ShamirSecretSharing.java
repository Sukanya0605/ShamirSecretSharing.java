import org.json.JSONObject;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class ShamirSecretSharing {

    public static void main(String[] args) {
        try {
            // Read the JSON files for the test cases
            JSONObject json1 = readJsonFile("testcase1.json");
            JSONObject json2 = readJsonFile("testcase2.json");

            // Find the secrets for both test cases
            System.out.println("Test Case 1:");
            findSecret(json1, false);

            System.out.println("\nTest Case 2:");
            findSecret(json2, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Reads the JSON file and returns a JSONObject
    public static JSONObject readJsonFile(String fileName) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(fileName)));
            return new JSONObject(content);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Finds the secret value using the JSON input
    public static void findSecret(JSONObject jsonObject, boolean detectWrongPoints) {
        JSONObject keys = jsonObject.getJSONObject("keys");
        int n = keys.getInt("n"); // Number of points provided
        int k = keys.getInt("k"); // Minimum number of points needed (m+1)

        // Store the points (x, y) in a map
        Map<Integer, Long> points = new HashMap<>();

        for (int i = 1; i <= n; i++) {
            if (jsonObject.has(String.valueOf(i))) {
                JSONObject point = jsonObject.getJSONObject(String.valueOf(i));
                int base = point.getInt("base");
                String value = point.getString("value");
                
                // Convert the given value to decimal based on the base
                long decodedValue = Long.parseLong(value, base);
                points.put(i, decodedValue);
            }
        }

        if (detectWrongPoints) {
            // Detect and print wrong points for the second test case
            detectWrongPoints(points, k);
        } else {
            // Calculate the secret for the first test case
            long secret = calculateConstantTerm(points, k);
            System.out.println("Secret: " + secret);
        }
    }

    // Method to calculate the constant term using Lagrange interpolation
    public static long calculateConstantTerm(Map<Integer, Long> points, int k) {
        double constantTerm = 0.0;

        // Apply Lagrange Interpolation Formula
        for (Map.Entry<Integer, Long> entry1 : points.entrySet()) {
            int xi = entry1.getKey();
            long yi = entry1.getValue();

            double li = 1.0; // Lagrange basis polynomial

            for (Map.Entry<Integer, Long> entry2 : points.entrySet()) {
                int xj = entry2.getKey();
                if (xi != xj) {
                    li *= (0 - xj) / (double) (xi - xj);
                }
            }

            // Sum up the value of the constant term
            constantTerm += yi * li;
        }

        // Round the result to get the closest integer
        return Math.round(constantTerm);
    }

    // Method to detect and print wrong points that do not fit the curve
    public static void detectWrongPoints(Map<Integer, Long> points, int k) {
        for (Map.Entry<Integer, Long> entry : points.entrySet()) {
            int testKey = entry.getKey();
            long testValue = entry.getValue();

            // Remove the test point and calculate the constant term with the remaining points
            Map<Integer, Long> subsetPoints = new HashMap<>(points);
            subsetPoints.remove(testKey);

            long calculatedConstant = calculateConstantTerm(subsetPoints, k);

            // Compare with the actual constant term
            long actualConstant = calculateConstantTerm(points, k);

            if (calculatedConstant != actualConstant) {
                System.out.println("Wrong Point Detected: (" + testKey + ", " + testValue + ")");
            }
        }
        System.out.println("Wrong Point Detection Complete.");
    }
}
