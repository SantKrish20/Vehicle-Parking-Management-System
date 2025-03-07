import java.sql.*;
import java.util.Scanner;

public class VehicleParkingManagementSystem {

    static final String DB_URL = "jdbc:mysql://localhost:3306/parking_management";
    static final String USER = "root";
    static final String PASS = "PassWord@123";

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            System.out.println("Got Connected to the database");
            while (true) {
                System.out.println("\n1.Enter to Record Vehicle Entry\n2.Enter to Record Vehicle Exit\n3.Enter to View Parking Records\n4. Exit");
                int choice = sc.nextInt();
                sc.nextLine();
                switch (choice) {
                    case 1:
                        recordVehicleEntry(conn, sc);
                        break;
                    case 2:


                        recordVehicleExit(conn, sc);
                        break;
                    case 3:
                        viewParkingRecords(conn);
                        break;
                    case 4:
                        System.out.println("Exiting");
                        return;
                    default:
                        System.out.println("Invalid choice");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void recordVehicleEntry(Connection conn, Scanner sc) throws SQLException {
        System.out.println("Enter vehicle number plate:");
        String numberPlate = sc.nextLine();

        String sql = "INSERT INTO vehicles (number_plate, entry_time) VALUES (?, NOW())";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, numberPlate);
            pstmt.executeUpdate();
            System.out.println("Vehicle entry recorded successfully");
        }
    }

    private static void recordVehicleExit(Connection conn, Scanner sc) throws SQLException {
        System.out.println("Enter vehicle number plate:");
        String numberPlate = sc.nextLine();

        // Check if the vehicle exists and has not exited
        String checkSql = "SELECT vehicle_id, entry_time FROM vehicles WHERE number_plate = ? AND exit_time IS NULL";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, numberPlate);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    int vehicleId = rs.getInt("vehicle_id");
                    Timestamp entryTime = rs.getTimestamp("entry_time");

                    // Calculate parking fee
                    long milliseconds = System.currentTimeMillis() - entryTime.getTime();
                    double hours = milliseconds / (1000.0 * 60 * 60);
                    double parkingFee = Math.ceil(hours) * 10.0;

                    // Update exit time and parking fee
                    String updateSql = "UPDATE vehicles SET exit_time = NOW(), parking_fee = ? WHERE vehicle_id = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setDouble(1, parkingFee);
                        updateStmt.setInt(2, vehicleId);
                        updateStmt.executeUpdate();
                        System.out.println("Vehicle exit recorded successfully Parking fee: " + parkingFee);
                    }
                } else {
                    System.out.println("No matching vehicle found or already exited");
                }
            }
        }
    }

    private static void viewParkingRecords(Connection conn) throws SQLException {
        String sql = "SELECT * FROM vehicles";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            System.out.printf("%-10s %-20s %-20s %-20s %-10s%n", "ID", "Number Plate", "Entry Time", "Exit Time", "Fee");
            while (rs.next()) {
                System.out.printf("%-10d %-20s %-20s %-20s $%-10f%n",
                        rs.getInt("vehicle_id"),
                        rs.getString("number_plate"),
                        rs.getTimestamp("entry_time"),
                        rs.getTimestamp("exit_time"),
                        rs.getDouble("parking_fee"));
            }
        }
    }
}