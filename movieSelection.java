import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.sql.*;
import java.util.*;

public class movieSelection extends JFrame {
    private JPanel contentPanel;

    public movieSelection() {
        setTitle(" Welcome to CineZone ");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        contentPanel = new JPanel(new CardLayout());
        add(contentPanel);

        showMovieSelection();
    }

    class BackgroundPanel extends JPanel {
        private Image backgroundImage;

        public BackgroundPanel(String imagePath) {
            try {
                backgroundImage = new ImageIcon(imagePath).getImage();
            } catch (Exception e) {
                System.out.println("Background image not found.");
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            } else {
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    }

    private void showMovieSelection() {
        BackgroundPanel mainPanel = new BackgroundPanel("images/movielist_bg.jpg");
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(Color.BLACK);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel heading = new JLabel("🎞 What's Your Vibe? Pick a Movie & Let's Pop 🍿");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 38));
        heading.setForeground(new Color(200, 200, 200));
        heading.setHorizontalAlignment(SwingConstants.CENTER);
        heading.setBorder(BorderFactory.createEmptyBorder(40, 20, 30, 20));
        topPanel.add(heading, BorderLayout.CENTER);

        JPanel moviePanel = new JPanel(new GridLayout(0, 3, 30, 30));
        moviePanel.setOpaque(false);
        moviePanel.setBackground(Color.BLACK);
        moviePanel.setBorder(BorderFactory.createEmptyBorder(30, 80, 30, 80));

        for (Movie movie : getSampleMovies()) {
            JPanel card = createMovieCard(movie);
            moviePanel.add(card);
        }

        JScrollPane scrollPane = new JScrollPane(moviePanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setBackground(Color.BLACK);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        contentPanel.add(mainPanel, "Movies");
        ((CardLayout) contentPanel.getLayout()).show(contentPanel, "Movies");
    }

    private JPanel createMovieCard(Movie movie) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(30, 30, 30, 180));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 100), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        card.setPreferredSize(new Dimension(330, 520));
        card.setOpaque(true);
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel imgLabel = new JLabel();
        imgLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        imgLabel.setPreferredSize(new Dimension(300, 280));

        File imgFile = new File(movie.imagePath);
        if (imgFile.exists()) {
            ImageIcon icon = new ImageIcon(movie.imagePath);
            Image scaledImg = icon.getImage().getScaledInstance(300, 280, Image.SCALE_SMOOTH);
            imgLabel.setIcon(new ImageIcon(scaledImg));
        } else {
            imgLabel.setText("Image not found");
            imgLabel.setForeground(Color.RED);
        }

        JLabel name = new JLabel("🎬 " + movie.name);
        JLabel duration = new JLabel("⏱ " + movie.duration);
        JLabel price = new JLabel("💸 ₹" + movie.price);

        for (JLabel label : new JLabel[]{name, duration, price}) {
            label.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            label.setForeground(new Color(220, 220, 220));
            label.setAlignmentX(Component.CENTER_ALIGNMENT);
            label.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
        }
        name.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JButton bookButton = new JButton("🎟 Book Now");
        bookButton.setBackground(new Color(0, 255, 180));
        bookButton.setForeground(Color.BLACK);
        bookButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        bookButton.setFocusPainted(false);
        bookButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        bookButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        bookButton.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));

        bookButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                bookButton.setBackground(new Color(0, 210, 150));
            }

            public void mouseExited(MouseEvent e) {
                bookButton.setBackground(new Color(0, 255, 180));
            }
        });

        bookButton.addActionListener(e -> {
            java.util.List<String> posterList = Arrays.asList(movie.imagePath);
            new SeatBookingApp(movie.name, "Default Time", movie.price, movie.duration, posterList).setVisible(true);
        });

        card.add(imgLabel);
        card.add(Box.createVerticalStrut(12));
        card.add(name);
        card.add(duration);
        card.add(price);
        card.add(Box.createVerticalStrut(12));
        card.add(bookButton);

        return card;
    }

    private java.util.List<Movie> getSampleMovies() {
        java.util.List<Movie> movies = new ArrayList<>();
        String url = "jdbc:mysql://localhost:3306/test";
        String user = "root"; // Replace with your MySQL username
        String password = "aamir1011"; // Replace with your MySQL password

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, user, password);
            String query = "SELECT name, duration, price, image_path FROM movies";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String name = rs.getString("name");
                String duration = rs.getString("duration");
                double price = rs.getDouble("price");
                String imagePath = rs.getString("image_path");
                movies.add(new Movie(name, duration, price, imagePath));
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load movies from database.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }

        return movies;
    }

    class Movie {
        String name, duration, imagePath;
        double price;

        public Movie(String name, String duration, double price, String imagePath) {
            this.name = name;
            this.duration = duration;
            this.price = price;
            this.imagePath = imagePath;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new movieSelection().setVisible(true));
    }
}
