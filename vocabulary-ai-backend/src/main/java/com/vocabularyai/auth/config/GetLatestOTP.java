package com.vocabularyai.auth.config;

import java.sql.*;

public class GetLatestOTP {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/datn_2";
        String user = "root";
        String password = "180703";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, user, password);

            String query = "SELECT otp_code, email, otp_type FROM otp_verifications WHERE email LIKE 'testuser%' ORDER BY created_at DESC LIMIT 1";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            if (rs.next()) {
                String otpCode = rs.getString("otp_code");
                String email = rs.getString("email");
                String otpType = rs.getString("otp_type");

                System.out.println("Latest OTP:");
                System.out.println("Email: " + email);
                System.out.println("OTP Code: " + otpCode);
                System.out.println("Type: " + otpType);
            } else {
                System.out.println("No OTP found");
            }

            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
