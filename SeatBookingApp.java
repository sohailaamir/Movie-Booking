import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;

public class SeatBookingApp extends JFrame {
    private static final int ROWS = 5;
    private static final int COLS = 8;

    private Set<String> bookedSeats = new HashSet<>();
    private java.util.List<String> selectedSeats = new ArrayList<>();
    private Map<String, JButton> seatButtons = new HashMap<>();

    private JPanel seatPanel;
    private JComboBox<String> showtimeDropdown;
    private String selectedShowtime = "2:00 PM"; // Default

    // Database credentials
    private static final String DB_URL = "jdbc:mysql://localhost:3306/test";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "aamir1011";

    public SeatBookingApp(String movieName, String showtimePlaceholder, double moviePrice, String movieDuration, java.util.List<String> posters) {
        setTitle("🎟️ Book Your Spot - " + movieName);
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(12, 12));
        getContentPane().setBackground(Color.BLACK);

        Font titleFont = new Font("Segoe UI", Font.BOLD, 22);
        Font regularFont = new Font("Segoe UI", Font.PLAIN, 15);

        // 🔝 Top Bar
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        topPanel.setBackground(Color.BLACK);

        JButton backBtn = new JButton("←");
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 20));
        backBtn.setBackground(Color.DARK_GRAY);
        backBtn.setForeground(Color.WHITE);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backBtn.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        backBtn.setToolTipText("Go Back");
        backBtn.addActionListener(e -> dispose());
        topPanel.add(backBtn);

        JLabel showtimeLabel = new JLabel("🕐 Showtime:");
        showtimeLabel.setFont(titleFont);
        showtimeLabel.setForeground(Color.WHITE);
        topPanel.add(showtimeLabel);

        showtimeDropdown = new JComboBox<>(new String[]{"11:00 AM", "2:00 PM", "5:30 PM", "9:00 PM"});
        showtimeDropdown.setFont(regularFont);
        showtimeDropdown.setSelectedItem("2:00 PM");
        showtimeDropdown.setBackground(Color.DARK_GRAY);
        showtimeDropdown.setForeground(Color.WHITE);
        showtimeDropdown.setFocusable(false);
        showtimeDropdown.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        topPanel.add(showtimeDropdown);

        // 🎞️ Poster Panel
        JLabel posterLabel = new JLabel();
        posterLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        ImageIcon icon = new ImageIcon(getPosterPath(movieName, posters));
        Image scaled = icon.getImage().getScaledInstance(280, 400, Image.SCALE_SMOOTH);
        posterLabel.setIcon(new ImageIcon(scaled));
        JPanel posterPanel = new JPanel();
        posterPanel.setBackground(Color.BLACK);
        posterPanel.add(posterLabel);

        // 💺 Seats Grid
        seatPanel = new JPanel(new GridLayout(ROWS, COLS, 12, 12));
        seatPanel.setBackground(Color.BLACK);
        seatPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        generateSeats(regularFont);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.BLACK);
        centerPanel.add(seatPanel, BorderLayout.CENTER);
        centerPanel.add(posterPanel, BorderLayout.EAST);

        // ✅ Confirm Button
        JButton confirm = createStyledButton("✅ Confirm Booking", titleFont, new Color(0, 255, 180), Color.BLACK);
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(Color.BLACK);
        bottomPanel.add(confirm);

        confirm.addActionListener(e -> {
            selectedShowtime = (String) showtimeDropdown.getSelectedItem();
            if (selectedSeats.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select at least one seat.");
                return;
            }

            int confirmation = JOptionPane.showConfirmDialog(
                    this,
                    "🎬 Movie: " + movieName +
                            "\n🕒 Showtime: " + selectedShowtime +
                            "\n💺 Seats: " + selectedSeats +
                            "\n💸 Total: ₹" + (selectedSeats.size() * moviePrice),
                    "Confirm Booking",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirmation == JOptionPane.YES_OPTION) {
                // 🔗 Insert into database
                insertBooking(movieName, selectedShowtime, selectedSeats, selectedSeats.size() * moviePrice);

                dispose();

                new payment(
                        movieName,
                        selectedShowtime,
                        moviePrice,
                        movieDuration,
                        new ArrayList<>(selectedSeats),
                        getPosterPath(movieName, posters)
                ).setVisible(true);
            }
        });

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void generateSeats(Font font) {
        seatPanel.removeAll();
        selectedSeats.clear();
        seatButtons.clear();

        for (int row = 0; row < ROWS; row++) {
            for (int col = 1; col <= COLS; col++) {
                String seatID = (char) ('A' + row) + String.valueOf(col);
                JButton seat = new JButton(seatID);
                seat.setFont(font);
                seat.setBackground(new Color(50, 50, 50));
                seat.setForeground(Color.WHITE);
                seat.setFocusPainted(false);
                seat.setBorder(BorderFactory.createLineBorder(new Color(150, 150, 150)));
                seat.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                seat.addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) {
                        if (!bookedSeats.contains(seatID) && !selectedSeats.contains(seatID)) {
                            seat.setBackground(new Color(80, 80, 80));
                        }
                    }

                    public void mouseExited(MouseEvent e) {
                        if (!bookedSeats.contains(seatID)) {
                            seat.setBackground(selectedSeats.contains(seatID) ? new Color(0, 255, 180) : new Color(50, 50, 50));
                        }
                    }
                });

                seat.addActionListener(e -> {
                    if (bookedSeats.contains(seatID)) return;

                    if (selectedSeats.contains(seatID)) {
                        selectedSeats.remove(seatID);
                        seat.setBackground(new Color(50, 50, 50));
                    } else {
                        selectedSeats.add(seatID);
                        seat.setBackground(new Color(0, 255, 180));
                    }
                });

                seatButtons.put(seatID, seat);
                seatPanel.add(seat);
            }
        }

        seatPanel.revalidate();
        seatPanel.repaint();
    }

    private JButton createStyledButton(String text, Font font, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(font);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return btn;
    }

    private String getPosterPath(String movieName, java.util.List<String> posters) {
        for (String path : posters) {
            if (path.toLowerCase().contains(movieName.toLowerCase().replaceAll(" ", ""))) {
                return path;
            }
        }
        return "images/default.jpg";
    }

    private void insertBooking(String movieName, String showtime, java.util.List<String> seats, double totalPrice) {
        String seatStr = String.join(",", seats);
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "INSERT INTO bookings (movie_name, showtime, seats, total_price) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, movieName);
            stmt.setString(2, showtime);
            stmt.setString(3, seatStr);
            stmt.setDouble(4, totalPrice);
            stmt.executeUpdate();
            System.out.println("Booking saved to database!");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        java.util.List<String> posters = Arrays.asList(
                "images/avatar.jpg",
                "images/stree2.jpg",
                "images/avengers.jpg",
                "images/titanic.jpg",
                "images/default.jpg"
        );

        SwingUtilities.invokeLater(() -> {
            new SeatBookingApp(
                    "Avatar",           // movieName
                    "2:00 PM",          // showtime placeholder
                    250,                // price per ticket
                    "2h 42m",           // movie duration
                    posters             // posters list
            ).setVisible(true);
        });
    }
}
