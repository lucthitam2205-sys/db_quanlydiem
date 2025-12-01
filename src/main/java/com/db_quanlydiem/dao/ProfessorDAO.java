package com.db_quanlydiem.dao;

import com.db_quanlydiem.DatabaseConnection;
import com.db_quanlydiem.model.Professor;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProfessorDAO {

    public List<Professor> getAllProfessors() {
        List<Professor> list = new ArrayList<>();
        String sql = "SELECT * FROM Professor";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(new Professor(
                        rs.getString("ProfessorID"),
                        rs.getString("ProfessorName"),
                        rs.getString("ProfessorEmail"),
                        rs.getString("ProfessorPhone"),
                        rs.getString("ProfessorHometown"),
                        rs.getString("ProfessorTitle")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean addProfessor(Professor p) {
        String sql = "INSERT INTO Professor (ProfessorID, ProfessorName, ProfessorEmail, ProfessorPhone, ProfessorHometown, ProfessorTitle) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, p.getProfessorID());
            pstmt.setString(2, p.getProfessorName());
            pstmt.setString(3, p.getProfessorEmail());
            pstmt.setString(4, p.getProfessorPhone());
            pstmt.setString(5, p.getProfessorHometown());
            pstmt.setString(6, p.getProfessorTitle());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean updateProfessor(Professor p) {
        String sql = "UPDATE Professor SET ProfessorName=?, ProfessorEmail=?, ProfessorPhone=?, ProfessorHometown=?, ProfessorTitle=? WHERE ProfessorID=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, p.getProfessorName());
            pstmt.setString(2, p.getProfessorEmail());
            pstmt.setString(3, p.getProfessorPhone());
            pstmt.setString(4, p.getProfessorHometown());
            pstmt.setString(5, p.getProfessorTitle());
            pstmt.setString(6, p.getProfessorID());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean deleteProfessor(String id) {
        String sql = "DELETE FROM Professor WHERE ProfessorID=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public List<Professor> searchProfessor(String keyword) {
        List<Professor> list = new ArrayList<>();
        String sql = "SELECT * FROM Professor WHERE ProfessorID LIKE ? OR ProfessorName LIKE ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String k = "%" + keyword + "%";
            pstmt.setString(1, k);
            pstmt.setString(2, k);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new Professor(
                        rs.getString("ProfessorID"),
                        rs.getString("ProfessorName"),
                        rs.getString("ProfessorEmail"),
                        rs.getString("ProfessorPhone"),
                        rs.getString("ProfessorHometown"),
                        rs.getString("ProfessorTitle")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}