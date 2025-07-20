import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class payment extends JFrame {
    public payment(String movieName, String showtime, double pricePerTicket, String duration, List<String> selectedSeats, String posterPath) {
        setTitle("💳 Payment - CineZone");
        setSize(900, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        double subtotal = pricePerTicket * selectedSeats.size();
        double gst = subtotal * 0.18;
        double total = subtotal + gst;

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.BLACK);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        JLabel title = new JLabel("🧾 Booking Summary");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel summaryPanel = new JPanel(new GridLayout(0, 2, 20, 10));
        summaryPanel.setOpaque(false);
        summaryPanel.setMaximumSize(new Dimension(700, 250));
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        summaryPanel.add(createSummaryLabel("🎬 Movie:"));
        summaryPanel.add(createValueLabel(movieName));

        summaryPanel.add(createSummaryLabel("⏱ Duration:"));
        summaryPanel.add(createValueLabel(duration));

        summaryPanel.add(createSummaryLabel("🕒 Showtime:"));
        summaryPanel.add(createValueLabel(showtime));

        summaryPanel.add(createSummaryLabel("🎫 Selected Seats:"));
        summaryPanel.add(createValueLabel(String.join(", ", selectedSeats)));

        summaryPanel.add(createSummaryLabel("💵 Price per Ticket:"));
        summaryPanel.add(createValueLabel("₹" + pricePerTicket));

        summaryPanel.add(createSummaryLabel("📊 Subtotal:"));
        summaryPanel.add(createValueLabel("₹" + String.format("%.2f", subtotal)));

        summaryPanel.add(createSummaryLabel("🧾 GST (18%):"));
        summaryPanel.add(createValueLabel("₹" + String.format("%.2f", gst)));

        summaryPanel.add(createSummaryLabel("💰 Total Amount:"));
        summaryPanel.add(createValueLabel("₹" + String.format("%.2f", total)));

        // Poster
        JLabel poster = new JLabel();
        ImageIcon icon = new ImageIcon(posterPath);
        Image scaled = icon.getImage().getScaledInstance(180, 260, Image.SCALE_SMOOTH);
        poster.setIcon(new ImageIcon(scaled));
        poster.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        poster.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Card details section
        JLabel cardDetailsTitle = new JLabel("💳 Enter Card Details");
        cardDetailsTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        cardDetailsTitle.setForeground(new Color(0, 255, 180));
        cardDetailsTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardDetailsTitle.setBorder(BorderFactory.createEmptyBorder(30, 0, 20, 0));

        JPanel cardForm = new JPanel(new GridLayout(4, 2, 20, 15));
        cardForm.setMaximumSize(new Dimension(700, 180));
        cardForm.setOpaque(false);

        JTextField nameField = createTextField("Cardholder Name");
        JTextField cardNumberField = createTextField("Card Number");
        JTextField expiryField = createTextField("MM/YY");
        JTextField cvvField = createTextField("CVV");

        cardForm.add(createFormLabel("👤 Name on Card:"));
        cardForm.add(nameField);

        cardForm.add(createFormLabel("💳 Card Number:"));
        cardForm.add(cardNumberField);

        cardForm.add(createFormLabel("📅 Expiry Date:"));
        cardForm.add(expiryField);

        cardForm.add(createFormLabel("🔒 CVV:"));
        cardForm.add(cvvField);

        // 💸 Pay Button
        JButton payButton = new JButton("💸 Pay ₹" + String.format("%.2f", total));
        payButton.setBackground(new Color(0, 255, 180));
        payButton.setForeground(Color.BLACK);
        payButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        payButton.setFocusPainted(false);
        payButton.setBorder(BorderFactory.createEmptyBorder(12, 30, 12, 30));
        payButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        payButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (nameField.getText().isEmpty() || cardNumberField.getText().isEmpty() ||
                        expiryField.getText().isEmpty() || cvvField.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(payment.this,
                            "⚠️ Please fill in all the card details.", "Incomplete Info", JOptionPane.WARNING_MESSAGE);
                } else {
                    storePaymentDetails(
                            nameField.getText(),
                            cardNumberField.getText(),
                            expiryField.getText(),
                            cvvField.getText(),
                            movieName,
                            showtime,
                            selectedSeats,
                            total
                    );

                    JOptionPane.showMessageDialog(payment.this,
                            "✅ Payment Successful! Enjoy your movie 🎉", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dispose(); // Close payment screen
                }
            }
        });

        // Add to main panel
        mainPanel.add(poster);
        mainPanel.add(title);
        mainPanel.add(summaryPanel);
        mainPanel.add(cardDetailsTitle);
        mainPanel.add(cardForm);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(payButton);
        mainPanel.add(Box.createVerticalStrut(30));

        // Wrap main panel in scroll pane
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.setBackground(Color.BLACK);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(Color.BLACK);

        add(scrollPane, BorderLayout.CENTER);
    }

    private void storePaymentDetails(String name, String cardNumber, String expiry, String cvv,
                                     String movieName, String showtime, List<String> seats, double total) {
        String url = "jdbc:mysql://localhost:3306/test"; // Update this to your DB name
        String username = "root"; // Update to your DB username
        String password = "aamir1011"; // Update to your DB password

        String sql = "INSERT INTO payment (name, card_number, expiry, cvv, movie_name, showtime, seats, total_amount) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(url, username, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setString(2, cardNumber);
            stmt.setString(3, expiry);
            stmt.setString(4, cvv);
            stmt.setString(5, movieName);
            stmt.setString(6, showtime);
            stmt.setString(7, String.join(", ", seats));
            stmt.setDouble(8, total);

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "❌ Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JLabel createSummaryLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(new Color(0, 255, 180));
        return label;
    }

    private JLabel createValueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        label.setForeground(Color.WHITE);
        return label;
    }

    private JLabel createFormLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        label.setForeground(Color.LIGHT_GRAY);
        return label;
    }

    private JTextField createTextField(String placeholder) {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        field.setForeground(Color.BLACK);
        field.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        return field;
    }

    public static void main(String[] args) {
        java.util.List<String> seats = java.util.Arrays.asList("B1", "B2", "B3");
        new payment("Avatar", "2:00 PM", 250, "2h 45m", seats, "images/avatar.jpg").setVisible(true);
    }
}
